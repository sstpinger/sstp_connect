package com.sstpinger.sstp_connect.sstp_connect.extension

import android.net.Uri


internal fun sum(vararg words: String): String {
    var result = ""

    words.forEach {
        result += it
    }

    return result
}

internal fun String.toUri(): Uri? {
    return if (this.isEmpty()) {
        null
    } else {
        Uri.parse(this)
    }
}
