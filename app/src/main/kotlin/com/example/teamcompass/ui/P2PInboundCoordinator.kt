package com.example.teamcompass.ui

import android.util.Log
import com.example.teamcompass.p2p.P2PInboundMessage
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * Coordinates inbound P2P stream collection and telemetry updates.
 *
 * Scope ownership:
 * [scope] is provided by TeamCompassViewModel (viewModelScope) and cancelled in onCleared().
 */
internal class P2PInboundCoordinator(
    private val scope: CoroutineScope,
    private val coroutineExceptionHandler: CoroutineExceptionHandler,
    private val observeInbound: (teamCode: String) -> Flow<P2PInboundMessage>,
    private val updateState: ((UiState) -> UiState) -> Unit,
    private val onStreamFailure: (err: Throwable, teamCode: String, uid: String) -> Unit,
    private val logTag: String = DEFAULT_LOG_TAG,
) {
    private var observerJob: Job? = null

    fun start(teamCode: String, localUid: String) {
        stop()
        observerJob = scope.launch(coroutineExceptionHandler) {
            try {
                observeInbound(teamCode).collectLatest { inbound ->
                    handleInbound(inbound = inbound, localUid = localUid)
                }
            } catch (cancel: CancellationException) {
                throw cancel
            } catch (err: Throwable) {
                updateState { state ->
                    state.copy(
                        tracking = state.tracking.copy(
                            telemetry = state.telemetry.copy(
                                p2pInboundErrors = state.telemetry.p2pInboundErrors + 1,
                            ),
                        ),
                    )
                }
                onStreamFailure(err, teamCode, localUid)
            }
        }
    }

    fun stop() {
        observerJob?.cancel()
        observerJob = null
    }

    private fun handleInbound(inbound: P2PInboundMessage, localUid: String) {
        val senderId = inbound.message.metadata.senderId
        if (senderId == localUid) return

        updateState { state ->
            state.copy(
                tracking = state.tracking.copy(
                    telemetry = state.telemetry.copy(
                        p2pInboundMessages = state.telemetry.p2pInboundMessages + 1,
                    ),
                ),
            )
        }
        Log.i(
            logTag,
            "P2P inbound via ${inbound.transportName}: type=${inbound.message.metadata.type} sender=$senderId",
        )
    }

    private companion object {
        private const val DEFAULT_LOG_TAG = "P2PInboundCoord"
    }
}
