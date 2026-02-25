package com.example.teamcompass.bluetooth

import android.app.Application
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class BluetoothScannerTest {

    @Test
    fun startScan_returns_empty_when_bluetooth_adapter_unavailable() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val scanner = BluetoothScanner(
            context = app,
            adapterProvider = { null },
        )
        var callbackCalled = false
        var resultSize = -1

        scanner.startScan(duration = 10L) { devices ->
            callbackCalled = true
            resultSize = devices.size
        }

        assertTrue(callbackCalled)
        assertEqualsZero(resultSize)
        assertFalse(scanner.isScanning())
    }

    @Test
    fun stopScan_when_not_running_does_not_throw() {
        val app = ApplicationProvider.getApplicationContext<Application>()
        val scanner = BluetoothScanner(
            context = app,
            adapterProvider = { null },
        )

        scanner.stopScan()
        assertFalse(scanner.isScanning())
    }

    private fun assertEqualsZero(value: Int) {
        assertTrue(value == 0)
    }
}
