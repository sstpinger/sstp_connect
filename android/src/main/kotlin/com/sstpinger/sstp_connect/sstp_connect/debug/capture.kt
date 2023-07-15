package com.sstpinger.sstp_connect.sstp_connect.debug

import android.util.Log
import com.sstpinger.sstp_connect.sstp_connect.DEFAULT_MRU
import com.sstpinger.sstp_connect.sstp_connect.extension.toHexString
import com.sstpinger.sstp_connect.sstp_connect.unit.DataUnit
import kotlinx.coroutines.sync.Mutex
import java.nio.ByteBuffer
import java.text.SimpleDateFormat
import java.util.*


internal enum class Direction {
    RECEIVED,
    SENT,
}

internal class DataUnitCapture {
    private val mutex = Mutex()
    private val buffer = ByteBuffer.allocate(DEFAULT_MRU)
    private val currentTime: String
        get() = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault()).format(Date())

    internal suspend fun logDataUnit(unit: DataUnit, direction: Direction) {
        mutex.lock()

        val message = mutableListOf(direction.name)

        message.add("[INFO]")
        message.add("time = $currentTime")
        message.add("size = ${unit.length}")
        message.add("class = ${unit::class.java.simpleName}")
        message.add("")

        message.add("[HEX]")
        buffer.clear()
        unit.write(buffer)
        buffer.flip()
        message.add(buffer.array().sliceArray(0 until unit.length).toHexString(true))
        message.add("")

        message.reduce { acc, s -> acc + "\n" + s }.also {
            Log.d("CAPTURE", it)
        }

        mutex.unlock()
    }
}
