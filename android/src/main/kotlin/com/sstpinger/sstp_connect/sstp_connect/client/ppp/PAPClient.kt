package com.sstpinger.sstp_connect.sstp_connect.client.ppp

import com.sstpinger.sstp_connect.sstp_connect.client.ClientBridge
import com.sstpinger.sstp_connect.sstp_connect.client.ControlMessage
import com.sstpinger.sstp_connect.sstp_connect.client.Result
import com.sstpinger.sstp_connect.sstp_connect.client.Where
import com.sstpinger.sstp_connect.sstp_connect.unit.ppp.PAPAuthenticateRequest
import com.sstpinger.sstp_connect.sstp_connect.unit.ppp.PAPFrame
import com.sstpinger.sstp_connect.sstp_connect.unit.ppp.PAP_CODE_AUTHENTICATE_ACK
import com.sstpinger.sstp_connect.sstp_connect.unit.ppp.PAP_CODE_AUTHENTICATE_NAK
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch


internal class PAPClient(private val bridge: ClientBridge) {
    internal val mailbox = Channel<PAPFrame>(Channel.BUFFERED)
    private var jobAuth: Job? = null

    internal fun launchJobAuth() {
        jobAuth = bridge.service.scope.launch(bridge.handler) {
            val currentID = bridge.allocateNewFrameID()

            sendPAPRequest(currentID)

            while (isActive) {
                val received = mailbox.receive()

                if (received.id != currentID) continue

                when (received.code) {
                    PAP_CODE_AUTHENTICATE_ACK -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.PAP, Result.PROCEEDED)
                        )
                    }

                    PAP_CODE_AUTHENTICATE_NAK -> {
                        bridge.controlMailbox.send(
                            ControlMessage(Where.PAP, Result.ERR_AUTHENTICATION_FAILED)
                        )
                    }
                }
            }
        }
    }

    private suspend fun sendPAPRequest(id: Byte) {
        PAPAuthenticateRequest().also {
            it.id = id
            it.idFiled = bridge.HOME_USERNAME.toByteArray(Charsets.US_ASCII)
            it.passwordFiled = bridge.HOME_PASSWORD.toByteArray(Charsets.US_ASCII)

            bridge.sslTerminal!!.sendDataUnit(it)
        }
    }

    internal fun cancel() {
        jobAuth?.cancel()
        mailbox.close()
    }
}
