package com.sstpinger.sstp_connect.sstp_connect.preference.accessor

import android.content.SharedPreferences
import com.sstpinger.sstp_connect.sstp_connect.preference.DEFAULT_BOOLEAN_MAP
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey


internal fun getBooleanPrefValue(key: OscPrefKey, prefs: SharedPreferences): Boolean {
    return prefs.getBoolean(key.name, DEFAULT_BOOLEAN_MAP[key]!!)
}

internal fun setBooleanPrefValue(value: Boolean, key: OscPrefKey, prefs: SharedPreferences) {
    prefs.edit().also {
        it.putBoolean(key.name, value)
        it.apply()
    }
}
