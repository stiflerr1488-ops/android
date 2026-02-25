package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PMessage
import com.example.teamcompass.core.p2p.P2PTransport
import com.example.teamcompass.core.p2p.P2PTransportCapability
import com.example.teamcompass.core.p2p.P2PTransportLimits
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Test

class P2PTransportRegistryTest {

    @Test
    fun registerAndUnregister_updates_registry_state() {
        val registry = P2PTransportRegistry()
        val transport = StaticTransport(name = "ble")

        registry.register(transport)
        assertEquals(1, registry.size())
        assertNotNull(registry.get("ble"))

        registry.unregister("ble")
        assertEquals(0, registry.size())
        assertNull(registry.get("ble"))
    }

    @Test
    fun register_sameName_replaces_existing_transport() {
        val registry = P2PTransportRegistry()
        val first = StaticTransport(name = "ble")
        val second = StaticTransport(name = "ble")

        registry.register(first)
        registry.register(second)

        assertEquals(1, registry.size())
        assertEquals(second, registry.get("ble"))
    }
}

private class StaticTransport(
    override val name: String,
) : P2PTransport {
    override val limits: P2PTransportLimits = P2PTransportLimits(maxPayloadBytes = 128)
    override val capabilities: Set<P2PTransportCapability> = emptySet()

    override suspend fun send(peerId: String, message: P2PMessage): Result<Unit> = Result.success(Unit)

    override suspend fun broadcast(message: P2PMessage): Result<Unit> = Result.success(Unit)

    override fun receive(): Flow<P2PMessage> = emptyFlow()

    override fun connectedPeers(): Set<String> = emptySet()
}
