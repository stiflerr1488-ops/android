package com.example.teamcompass.ui

import androidx.activity.compose.ManagedActivityResultLauncher
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.compose.runtime.Composable
import androidx.core.net.toUri
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanIntentResult
import com.journeyapps.barcodescanner.ScanOptions

/**
 * Лаунчер для сканирования QR-кода
 */
class QrScannerLauncher {
    /**
     * Опции сканирования: только QR-коды, портретная ориентация
     */
    private val scanOptions = ScanOptions().apply {
        setDesiredBarcodeFormats(ScanOptions.QR_CODE)
        setPrompt("Point camera at the team QR code")
        setCameraId(0)
        setBeepEnabled(true)
        setBarcodeImageEnabled(false)
        setOrientationLocked(true)
        setCaptureActivity(com.journeyapps.barcodescanner.CaptureActivity::class.java)
    }

    /**
     * Создать launcher для сканирования
     */
    @Composable
    fun createLauncher(
        onResult: (String?) -> Unit
    ): ManagedActivityResultLauncher<ScanOptions, ScanIntentResult> {
        return rememberLauncherForActivityResult(
            contract = ScanContract()
        ) { result ->
            onResult(result.contents)
        }
    }

    /**
     * Запустить сканирование
     */
    fun scan(launcher: ManagedActivityResultLauncher<ScanOptions, ScanIntentResult>) {
        launcher.launch(scanOptions)
    }
}

/**
 * Получить данные для QR-кода команды (ссылка + код)
 */
fun getQrDataForTeam(teamCode: String, matchId: String? = null): String {
    // Формат: teamcompass://join?code=123456&matchId=xxx
    // Или просто код, если matchId нет
    return if (matchId != null) {
        "teamcompass://join?code=$teamCode&matchId=$matchId"
    } else {
        teamCode
    }
}

/**
 * Распарсить данные из QR-кода
     * Возвращает код команды или null
 */
fun parseQrData(qrData: String): Pair<String?, String?> {
    return try {
        if (qrData.startsWith("teamcompass://")) {
            // Парсим URL
            val uri = qrData.toUri()
            val code = uri.getQueryParameter("code")
            val matchId = uri.getQueryParameter("matchId")
            code to matchId
        } else if (qrData.length == 6 && qrData.all { it.isDigit() }) {
            // Просто 6-значный код
            qrData to null
        } else {
            null to null
        }
    } catch (e: Exception) {
        null to null
    }
}
