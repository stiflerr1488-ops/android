package com.example.teamcompass.core.p2p

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class P2PQosPolicyTest {

    @Test
    fun qosForSos_isCriticalReliable() {
        val qos = P2PQosPolicy.forType(P2PMessageType.SOS_SIGNAL)
        assertEquals(P2PPriority.CRITICAL, qos.priority)
        assertEquals(P2PDeliveryMode.RELIABLE, qos.deliveryMode)
        assertTrue(qos.maxRetries > 0)
        assertTrue(qos.relayEnabled)
    }

    @Test
    fun qosForPositionUpdate_isBestEffort() {
        val qos = P2PQosPolicy.forType(P2PMessageType.POSITION_UPDATE)
        assertEquals(P2PPriority.LOW, qos.priority)
        assertEquals(P2PDeliveryMode.BEST_EFFORT, qos.deliveryMode)
        assertEquals(0, qos.maxRetries)
    }
}
