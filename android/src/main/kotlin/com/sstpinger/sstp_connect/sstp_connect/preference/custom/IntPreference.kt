package com.sstpinger.sstp_connect.sstp_connect.preference.custom

import android.content.Context
import android.text.InputType
import android.util.AttributeSet
import androidx.preference.EditTextPreference
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.getIntPrefValue


internal abstract class IntPreference(context: Context, attrs: AttributeSet) : EditTextPreference(context, attrs), OscPreference {
    override fun updateView() {
        text = getIntPrefValue(oscPrefKey, sharedPreferences!!).toString()
    }

    override fun onAttached() {
        summaryProvider = SimpleSummaryProvider.getInstance()

        setOnBindEditTextListener { editText ->
            editText.inputType = InputType.TYPE_CLASS_NUMBER
        }

        initialize()
    }
}

internal class SSLPortPreference(context: Context, attrs: AttributeSet) : IntPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.SSL_PORT
    override val parentKey: OscPrefKey? = null
    override val preferenceTitle = "Port Number"
}

internal class ProxyPortPreference(context: Context, attrs: AttributeSet) : IntPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.PROXY_PORT
    override val parentKey = OscPrefKey.PROXY_DO_USE_PROXY
    override val preferenceTitle = "Proxy Server Port Number"
}

internal class PPPMruPreference(context: Context, attrs: AttributeSet) : IntPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.PPP_MRU
    override val parentKey: OscPrefKey? = null
    override val preferenceTitle = "MRU"
}

internal class PPPMtuPreference(context: Context, attrs: AttributeSet) : IntPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.PPP_MTU
    override val parentKey: OscPrefKey? = null
    override val preferenceTitle = "MTU"
}

internal class PPPAuthTimeoutPreference(context: Context, attrs: AttributeSet) : IntPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.PPP_AUTH_TIMEOUT
    override val parentKey: OscPrefKey? = null
    override val preferenceTitle = "Timeout Period (second)"
}

internal class ReconnectionCountPreference(context: Context, attrs: AttributeSet) : IntPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.RECONNECTION_COUNT
    override val parentKey = OscPrefKey.RECONNECTION_ENABLED
    override val preferenceTitle = "Retry Count"
}

internal class ReconnectionIntervalPreference(context: Context, attrs: AttributeSet) : IntPreference(context, attrs) {
    override val oscPrefKey = OscPrefKey.RECONNECTION_INTERVAL
    override val parentKey = OscPrefKey.RECONNECTION_ENABLED
    override val preferenceTitle = "Retry Interval (second)"
}
