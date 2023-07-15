package com.sstpinger.sstp_connect.sstp_connect.preference.custom

import androidx.preference.Preference
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey


internal interface OscPreference {
    val oscPrefKey: OscPrefKey
    val parentKey: OscPrefKey?
    val preferenceTitle: String
    fun updateView()
}

internal fun <T> T.initialize() where T : Preference, T : OscPreference {
    title = preferenceTitle
    isSingleLineTitle = false

    parentKey?.also { dependency = it.name }

    updateView()
}
