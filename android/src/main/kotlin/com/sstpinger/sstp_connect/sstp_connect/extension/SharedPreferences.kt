package com.sstpinger.sstp_connect.sstp_connect.extension

import android.content.SharedPreferences
import com.sstpinger.sstp_connect.sstp_connect.preference.TEMP_KEY_HEADER


internal fun SharedPreferences.removeTemporaryPreferences() {
    val editor = edit()

    all.keys.filter { it.startsWith(TEMP_KEY_HEADER) }.forEach { editor.remove(it) }

    editor.apply()
}
