package com.sstpinger.sstp_connect.sstp_connect

import android.content.Context
import android.content.SharedPreferences
import androidx.preference.PreferenceManager
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.getBooleanPrefValue
import io.flutter.plugin.common.EventChannel

/*
 streams only boolean true false
 true - connected
 false - disconnected
 */
internal class SstpConnectionHandler : EventChannel.StreamHandler {
    private var eventSink: EventChannel.EventSink? = null

    constructor(context: Context) {

        val prefs = PreferenceManager.getDefaultSharedPreferences(context)

        val listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            if (key == OscPrefKey.ROOT_STATE.name) {
                stateChanged(getBooleanPrefValue(OscPrefKey.ROOT_STATE, prefs))
            }
        }

        prefs.registerOnSharedPreferenceChangeListener(listener)

    }

    private fun stateChanged(state: Boolean) {
        eventSink?.success(state)
    }

    override fun onListen(p0: Any?, sink: EventChannel.EventSink?) {
        eventSink = sink
    }

    override fun onCancel(p0: Any?) {
        eventSink = null
    }

}