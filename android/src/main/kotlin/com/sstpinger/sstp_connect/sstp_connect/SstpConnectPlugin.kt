package com.sstpinger.sstp_connect.sstp_connect

import android.app.Activity.RESULT_OK
import android.content.Context
import android.content.Intent
import android.net.VpnService
import android.os.Build
import android.util.Log
import androidx.annotation.NonNull
import androidx.preference.PreferenceManager
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.setBooleanPrefValue
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.setIntPrefValue
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.setSetPrefValue
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.setStringPrefValue
import com.sstpinger.sstp_connect.sstp_connect.preference.custom.OscPreference
import com.sstpinger.sstp_connect.sstp_connect.service.ACTION_VPN_CONNECT
import com.sstpinger.sstp_connect.sstp_connect.service.ACTION_VPN_DISCONNECT
import com.sstpinger.sstp_connect.sstp_connect.service.SstpVpnService

import io.flutter.embedding.engine.plugins.FlutterPlugin
import io.flutter.embedding.engine.plugins.activity.ActivityAware
import io.flutter.embedding.engine.plugins.activity.ActivityPluginBinding
import io.flutter.plugin.common.EventChannel
import io.flutter.plugin.common.MethodCall
import io.flutter.plugin.common.MethodChannel
import io.flutter.plugin.common.MethodChannel.MethodCallHandler
import io.flutter.plugin.common.MethodChannel.Result
import io.flutter.plugin.common.PluginRegistry

/** SstpConnectPlugin */
class SstpConnectPlugin: FlutterPlugin, MethodCallHandler, ActivityAware {
  private lateinit var activityBinding: ActivityPluginBinding
  /// The MethodChannel that will the communication between Flutter and native Android
  ///
  /// This local reference serves to register the plugin with the Flutter Engine and unregister it
  /// when the Flutter Engine is detached from the Activity
  private lateinit var channel : MethodChannel
  private lateinit var eventChannel: EventChannel

  private lateinit var context: Context

  override fun onAttachedToEngine(@NonNull flutterPluginBinding: FlutterPlugin.FlutterPluginBinding) {
    // Get context
    context = flutterPluginBinding.applicationContext

    // Register method channel.
    channel = MethodChannel(flutterPluginBinding.binaryMessenger, "sstp_connect")
    channel.setMethodCallHandler(this)

    // Register event channel
    eventChannel = EventChannel(flutterPluginBinding.binaryMessenger, "sstp_connection_status")
    eventChannel.setStreamHandler(SstpConnectionHandler(context))
  }

  private fun startVpnService(action: String) {
    val intent = Intent(context, SstpVpnService::class.java).setAction(action)

    if (action == ACTION_VPN_CONNECT && Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
      context.startForegroundService(intent)
    } else {
      context.startService(intent)
    }
  }

  override fun onMethodCall(@NonNull call: MethodCall, @NonNull result: Result) {
    when(call.method) {
      "getPlatformVersion" -> {
        result.success("Android ${android.os.Build.VERSION.RELEASE}")
      }

      "disconnect" -> {
        startVpnService(ACTION_VPN_DISCONNECT)
        result.success("Success")
      }

      "connect" -> {
        val intent = VpnService.prepare(activityBinding.activity.applicationContext)
        if (intent != null) {
          var listener: PluginRegistry.ActivityResultListener? = null
          listener = PluginRegistry.ActivityResultListener { req, res, _ ->
            result.success(req == 0 && res == RESULT_OK)
            listener?.let { activityBinding.removeActivityResultListener(it) }
            true
          }
          activityBinding.addActivityResultListener(listener)
          activityBinding.activity.startActivityForResult(intent, 0)
        } else {
          // Already prepared if intent is null.
          // result.success(true)

          val args = call.arguments as Map<*, *>

          val hostname = args["Hostname"] as String // "88.81.42.84"
          val port = args["Port"] as Int // 1195
          val username = args["Username"] as String // "vpn"
          val password = args["Password"] as String // "vpn"

          val prefs = PreferenceManager.getDefaultSharedPreferences(context)

          setBooleanPrefValue(true, OscPrefKey.SSL_DO_SELECT_SUITES, prefs)
          setBooleanPrefValue(false, OscPrefKey.SSL_DO_VERIFY, prefs)
          setSetPrefValue(setOf("TLS_AES_256_GCM_SHA384"), OscPrefKey.SSL_SUITES, prefs)
          setStringPrefValue(hostname, OscPrefKey.HOME_HOSTNAME, prefs)
          setIntPrefValue(port, OscPrefKey.SSL_PORT, prefs)
          setStringPrefValue(username, OscPrefKey.HOME_USERNAME, prefs)
          setStringPrefValue(password, OscPrefKey.HOME_PASSWORD, prefs)

          startVpnService(ACTION_VPN_CONNECT)
          result.success("Success")
        }

      }

      else -> result.notImplemented()
    }

  }

  override fun onDetachedFromEngine(@NonNull binding: FlutterPlugin.FlutterPluginBinding) {
    channel.setMethodCallHandler(null)
    eventChannel.setStreamHandler(null)
  }

  override fun onAttachedToActivity(binding: ActivityPluginBinding) {
    activityBinding = binding
  }

  override fun onDetachedFromActivityForConfigChanges() {
    TODO("Not yet implemented")
  }

  override fun onReattachedToActivityForConfigChanges(binding: ActivityPluginBinding) {
    activityBinding = binding
  }

  override fun onDetachedFromActivity() {
    TODO("Not yet implemented")
  }
}
