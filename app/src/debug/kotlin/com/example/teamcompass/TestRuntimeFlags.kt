package com.example.teamcompass

import android.os.Bundle

internal data class TestRuntimeFlags(
    val hermetic: Boolean = false,
    val disableTelemetry: Boolean = false,
)

internal object TestRuntimeFlagsReader {
    private const val ARG_HERMETIC = "teamcompass.test.hermetic"
    private const val ARG_DISABLE_TELEMETRY = "teamcompass.test.disable_telemetry"

    fun current(): TestRuntimeFlags {
        val args = readInstrumentationArgsOrNull() ?: return TestRuntimeFlags()
        val hermetic = args[ARG_HERMETIC].toBooleanFlag()
        val disableTelemetry = args[ARG_DISABLE_TELEMETRY].toBooleanFlag() || hermetic
        return TestRuntimeFlags(
            hermetic = hermetic,
            disableTelemetry = disableTelemetry,
        )
    }

    private fun readInstrumentationArgsOrNull(): Map<String, String>? {
        return runCatching {
            val registryClass = Class.forName("androidx.test.platform.app.InstrumentationRegistry")
            val getArgumentsMethod = registryClass.getMethod("getArguments")
            val bundle = getArgumentsMethod.invoke(null) as? Bundle ?: return null
            bundle.keySet().associateWith { key ->
                bundle.getString(key).orEmpty()
            }
        }.getOrNull()
    }

    private fun String?.toBooleanFlag(): Boolean {
        return this?.trim()?.equals("true", ignoreCase = true) == true
    }
}
