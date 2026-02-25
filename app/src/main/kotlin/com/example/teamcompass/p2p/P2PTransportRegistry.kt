package com.example.teamcompass.p2p

import com.example.teamcompass.core.p2p.P2PTransport
import java.util.concurrent.ConcurrentHashMap

class P2PTransportRegistry(
    initialTransports: List<P2PTransport> = emptyList(),
) {
    private val transportsByName = ConcurrentHashMap<String, P2PTransport>()

    init {
        initialTransports.forEach(::register)
    }

    fun register(transport: P2PTransport) {
        require(transport.name.isNotBlank()) { "transport.name must not be blank" }
        transportsByName[transport.name] = transport
    }

    fun unregister(name: String) {
        transportsByName.remove(name)
    }

    fun get(name: String): P2PTransport? {
        return transportsByName[name]
    }

    fun snapshot(): List<P2PTransport> {
        return transportsByName.values
            .sortedBy { it.name }
            .toList()
    }

    fun size(): Int = transportsByName.size
}
