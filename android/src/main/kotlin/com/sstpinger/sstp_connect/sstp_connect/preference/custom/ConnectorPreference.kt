package com.sstpinger.sstp_connect.sstp_connect.preference.custom

import android.content.Context
import android.content.SharedPreferences
import android.util.AttributeSet
import androidx.preference.SwitchPreferenceCompat
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.getBooleanPrefValue


internal class HomeConnectorPreference(context: Context, attrs: AttributeSet) : SwitchPreferenceCompat(context, attrs), OscPreference {
    override val oscPrefKey = OscPrefKey.HOME_CONNECTOR
    override val parentKey: OscPrefKey? = null
    override val preferenceTitle = ""
    override fun updateView() {
        isChecked = getBooleanPrefValue(oscPrefKey, sharedPreferences!!)
    }

    private var listener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
        if (key == oscPrefKey.name) {
            updateView()
        }
    }

    override fun onAttached() {
        sharedPreferences!!.registerOnSharedPreferenceChangeListener(listener)
    }

    override fun onDetached() {
        sharedPreferences!!.unregisterOnSharedPreferenceChangeListener(listener)
    }
}
