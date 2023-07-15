package com.sstpinger.sstp_connect.sstp_connect.debug


internal class ParsingDataUnitException : Exception("Failed to parse data unit")

internal fun assertAlways(value: Boolean) {
    if (!value) {
        throw AssertionError()
    }
}
