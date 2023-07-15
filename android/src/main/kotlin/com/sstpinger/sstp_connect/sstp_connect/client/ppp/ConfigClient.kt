package com.sstpinger.sstp_connect.sstp_connect.client.ppp

import com.sstpinger.sstp_connect.sstp_connect.client.ClientBridge
import com.sstpinger.sstp_connect.sstp_connect.client.ControlMessage
import com.sstpinger.sstp_connect.sstp_connect.client.Result
import com.sstpinger.sstp_connect.sstp_connect.client.Where
import com.sstpinger.sstp_connect.sstp_connect.unit.ppp.*
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.withTimeoutOrNull


private const val PPP_REQUEST_INTERVAL = 3000L
private const val PPP_REQUEST_COUNT = 10
internal const val PPP_NEGOTIATION_TIMEOUT = PPP_REQUEST_INTERVAL * PPP_REQUEST_COUNT


internal abstract class ConfigClient<T: Frame>(private val where: Where, protected val bridge: ClientBridge) {
    internal val mailbox = Channel<T>(Channel.BUFFERED)

    private var jobNegotiation: Job? = null

    private var requestID: Byte = 0 // for only client
    private var requestCount = PPP_REQUEST_COUNT

    private var isClientReady = false
    private var isServerReady = false

    private val isOpen: Boolean
        get() = isClientReady && isServerReady

    private suspend fun consumeRequestCounter() { // consumed in any circumstance for converging
        requestCount--

        if (requestCount < 0) {
            bridge.controlMailbox.send(ControlMessage(where, Result.ERR_COUNT_EXHAUSTED))
        }
    }

    protected abstract fun tryCreateServerReject(request: T): T?

    protected abstract fun tryCreateServerNak(request: T): T?

    protected abstract fun createServerAck(request: T): T

    protected abstract fun createClientRequest(): T

    protected abstract suspend fun tryAcceptClientReject(reject: T)

    protected abstract suspend fun tryAcceptClientNak(nak: T)

    private suspend fun sendClientRequest() {
        consumeRequestCounter()
        requestID = bridge.allocateNewFrameID()

        createClientRequest().also {
            it.id = requestID
            bridge.sslTerminal!!.sendDataUnit(it)
        }
    }

    internal fun launchJobNegotiation() {
        jobNegotiation = bridge.service.scope.launch(bridge.handler) {
            sendClientRequest()

            while (isActive) {
                val tried  = withTimeoutOrNull(PPP_REQUEST_INTERVAL) { mailbox.receive() }


                val received: T // accept only CONFIGURE_[REQUEST|ACK|NAK|REJECT]
                if (tried == null) {
                    isClientReady = false

                    sendClientRequest()

                    continue
                } else {
                    received = tried
                }


                if (received.code == LCP_CODE_CONFIGURE_REQUEST) {
                    isServerReady = false

                    val reject = tryCreateServerReject(received)
                    if (reject != null) {
                        bridge.sslTerminal!!.sendDataUnit(reject)
                        continue
                    }

                    val nak = tryCreateServerNak(received)
                    if (nak != null) {
                        bridge.sslTerminal!!.sendDataUnit(nak)
                        continue
                    }

                    createServerAck(received).also {
                        bridge.sslTerminal!!.sendDataUnit(it)
                        isServerReady = true
                    }
                } else {
                    if (isClientReady) {
                        isClientReady = false

                        sendClientRequest()

                        continue
                    }

                    if (received.id != requestID) {
                        continue
                    }

                    when(received.code) {
                        LCP_CODE_CONFIGURE_ACK -> {
                            isClientReady = true
                        }

                        LCP_CODE_CONFIGURE_NAK -> {
                            tryAcceptClientNak(received)
                            sendClientRequest()
                        }

                        LCP_CODE_CONFIGURE_REJECT -> {
                            tryAcceptClientReject(received)
                            sendClientRequest()
                        }
                    }
                }

                if (isOpen) {
                    requestCount = PPP_REQUEST_COUNT
                    bridge.controlMailbox.send(ControlMessage(where, Result.PROCEEDED))

                    break
                }
            }
        }
    }

    internal fun cancel() {
        jobNegotiation?.cancel()
        mailbox.close()
    }
}
