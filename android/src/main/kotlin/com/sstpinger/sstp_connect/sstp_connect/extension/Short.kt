package com.sstpinger.sstp_connect.sstp_connect.extension


internal fun Short.toIntAsUShort(): Int {
    return this.toInt() and 0x0000FFFF
}
