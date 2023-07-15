package com.sstpinger.sstp_connect.sstp_connect.client.control

import com.sstpinger.sstp_connect.sstp_connect.client.*
import com.sstpinger.sstp_connect.sstp_connect.client.incoming.IncomingClient
import com.sstpinger.sstp_connect.sstp_connect.client.ppp.*
import com.sstpinger.sstp_connect.sstp_connect.debug.assertAlways
import com.sstpinger.sstp_connect.sstp_connect.preference.OscPrefKey
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.getBooleanPrefValue
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.getIntPrefValue
import com.sstpinger.sstp_connect.sstp_connect.preference.accessor.resetReconnectionLife
import com.sstpinger.sstp_connect.sstp_connect.service.NOTIFICATION_ERROR_ID
import com.sstpinger.sstp_connect.sstp_connect.terminal.SSL_REQUEST_INTERVAL
import com.sstpinger.sstp_connect.sstp_connect.unit.ppp.option.AuthOptionMSChapv2
import com.sstpinger.sstp_connect.sstp_connect.unit.ppp.option.AuthOptionPAP
import com.sstpinger.sstp_connect.sstp_connect.unit.sstp.SSTP_MESSAGE_TYPE_CALL_ABORT
import com.sstpinger.sstp_connect.sstp_connect.unit.sstp.SSTP_MESSAGE_TYPE_CALL_DISCONNECT
import com.sstpinger.sstp_connect.sstp_connect.unit.sstp.SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.withTimeoutOrNull


internal class ControlClient(internal val bridge: ClientBridge) {
    private var observer: NetworkObserver? = null

    private var sstpClient: SstpClient? = null
    private var pppClient: PPPClient? = null
    private var incomingClient: IncomingClient? = null
    private var outgoingClient: OutgoingClient? = null

    private var lcpClient: LCPClient? = null
    private var papClient: PAPClient? = null
    private var chapClient: ChapClient? = null
    private var ipcpClient: IpcpClient? = null
    private var ipv6cpClient: Ipv6cpClient? = null

    private var jobMain: Job? = null

    private val mutex = Mutex()

    private val isReconnectionEnabled = getBooleanPrefValue(OscPrefKey.RECONNECTION_ENABLED, bridge.prefs)
    private val isReconnectionAvailable: Boolean
        get() = getIntPrefValue(OscPrefKey.RECONNECTION_LIFE, bridge.prefs) > 0

    private fun attachHandler() {
        bridge.handler = CoroutineExceptionHandler { _, throwable ->
            kill(isReconnectionEnabled) {
                val header = "OSC: ERR_UNEXPECTED"
                bridge.service.logWriter?.report(header + "\n" + throwable.stackTraceToString())
                bridge.service.makeNotification(NOTIFICATION_ERROR_ID, header)
            }
        }
    }

    internal fun launchJobMain() {
        attachHandler()

        jobMain = bridge.service.scope.launch(bridge.handler) {
            bridge.attachSSLTerminal()
            bridge.attachIPTerminal()


            bridge.sslTerminal!!.initialize()
            if (!expectProceeded(Where.SSL, SSL_REQUEST_INTERVAL)) {
                return@launch
            }


            IncomingClient(bridge).also {
                it.launchJobMain()
                incomingClient = it
            }


            SstpClient(bridge).also {
                sstpClient = it
                incomingClient!!.registerMailbox(it)
                it.launchJobRequest()

                if (!expectProceeded(Where.SSTP_REQUEST, SSTP_REQUEST_TIMEOUT)) {
                    return@launch
                }

                sstpClient!!.launchJobControl()
            }


            PPPClient(bridge).also {
                pppClient = it
                incomingClient!!.registerMailbox(it)
                it.launchJobControl()
            }


            LCPClient(bridge).also {
                incomingClient!!.registerMailbox(it)
                it.launchJobNegotiation()

                if (!expectProceeded(Where.LCP, PPP_NEGOTIATION_TIMEOUT)) {
                    return@launch
                }

                incomingClient!!.unregisterMailbox(it)
            }


            val authTimeout = getIntPrefValue(OscPrefKey.PPP_AUTH_TIMEOUT, bridge.prefs) * 1000L
            when (bridge.currentAuth) {
                is AuthOptionPAP -> PAPClient(bridge).also {
                    incomingClient!!.registerMailbox(it)
                    it.launchJobAuth()

                    if (!expectProceeded(Where.PAP, authTimeout)) {
                        return@launch
                    }

                    incomingClient!!.unregisterMailbox(it)
                }

                is AuthOptionMSChapv2 -> ChapClient(bridge).also {
                    chapClient = it
                    incomingClient!!.registerMailbox(it)
                    it.launchJobAuth()

                    if (!expectProceeded(Where.CHAP, authTimeout)) {
                        return@launch
                    }
                }

                else -> throw NotImplementedError(bridge.currentAuth.protocol.toString())
            }


            sstpClient!!.sendCallConnected()


            if (bridge.PPP_IPv4_ENABLED) {
                IpcpClient(bridge).also {
                    incomingClient!!.registerMailbox(it)
                    it.launchJobNegotiation()

                    if (!expectProceeded(Where.IPCP, PPP_NEGOTIATION_TIMEOUT)) {
                        return@launch
                    }

                    incomingClient!!.unregisterMailbox(it)
                }
            }


            if (bridge.PPP_IPv6_ENABLED) {
                Ipv6cpClient(bridge).also {
                    incomingClient!!.registerMailbox(it)
                    it.launchJobNegotiation()

                    if (!expectProceeded(Where.IPV6CP, PPP_NEGOTIATION_TIMEOUT)) {
                        return@launch
                    }

                    incomingClient!!.unregisterMailbox(it)
                }
            }


            bridge.ipTerminal!!.initialize()
            if (!expectProceeded(Where.IP, null)) {
                return@launch
            }


            OutgoingClient(bridge).also {
                it.launchJobMain()
                outgoingClient = it
            }


            observer = NetworkObserver(bridge)

            if (isReconnectionEnabled) {
                resetReconnectionLife(bridge.prefs)
            }


            expectProceeded(Where.SSTP_CONTROL, null) // wait ERR_ message until disconnection
        }
    }

    private suspend fun expectProceeded(where: Where, timeout: Long?): Boolean {
        val received = if (timeout != null) {
            withTimeoutOrNull(timeout) {
                bridge.controlMailbox.receive()
            } ?: ControlMessage(where, Result.ERR_TIMEOUT)
        } else {
            bridge.controlMailbox.receive()
        }

        if (received.result == Result.PROCEEDED) {
            assertAlways(received.from == where)

            return true
        }

        val lastPacketType = if (received.result == Result.ERR_DISCONNECT_REQUESTED) {
            SSTP_MESSAGE_TYPE_CALL_DISCONNECT_ACK
        } else {
            SSTP_MESSAGE_TYPE_CALL_ABORT
        }

        kill(isReconnectionEnabled) {
            sstpClient?.sendLastPacket(lastPacketType)

            val message = "${received.from.name}: ${received.result.name}"
            bridge.service.logWriter?.report(message)
            bridge.service.makeNotification(NOTIFICATION_ERROR_ID, message)
        }

        return false
    }

    internal fun disconnect() { // use if the user want to normally disconnect
        kill(false) {
            sstpClient?.sendLastPacket(SSTP_MESSAGE_TYPE_CALL_DISCONNECT)
        }
    }

    internal fun kill(isReconnectionRequested: Boolean, cleanup: (suspend () -> Unit)?) {
        if (!mutex.tryLock()) return

        bridge.service.scope.launch {
            observer?.close()

            jobMain?.cancel()
            cancelClients()

            cleanup?.invoke()

            closeTerminals()

            if (isReconnectionRequested && isReconnectionAvailable) {
                bridge.service.launchJobReconnect()
            } else {
                bridge.service.close()
            }
        }
    }

    private fun cancelClients() {
        lcpClient?.cancel()
        papClient?.cancel()
        chapClient?.cancel()
        ipcpClient?.cancel()
        ipv6cpClient?.cancel()
        sstpClient?.cancel()
        pppClient?.cancel()
        incomingClient?.cancel()
        outgoingClient?.cancel()
    }

    private fun closeTerminals() {
        bridge.sslTerminal?.close()
        bridge.ipTerminal?.close()
    }
}
