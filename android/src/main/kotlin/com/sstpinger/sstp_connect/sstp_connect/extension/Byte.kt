package com.sstpinger.sstp_connect.sstp_connect.extension


internal fun Byte.toIntAsUByte(): Int {
    return this.toInt() and 0x000000FF
}
