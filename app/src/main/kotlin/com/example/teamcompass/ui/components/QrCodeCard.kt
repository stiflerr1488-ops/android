package com.example.teamcompass.ui.components

import android.graphics.Bitmap
import android.graphics.Color
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.core.graphics.createBitmap
import androidx.core.graphics.scale
import androidx.core.graphics.set
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.Spacing
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter

/**
 * QR-код для кода команды
 */
@Composable
fun QrCodeCard(
    teamCode: String,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier,
        shape = RoundedCornerShape(Spacing.lg),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface
        )
    ) {
        Box(
            modifier = Modifier
                .padding(Spacing.lg)
                .align(Alignment.CenterHorizontally),
            contentAlignment = Alignment.Center
        ) {
            val qrBitmap = generateQrCode(teamCode, 512)

            if (qrBitmap != null) {
                Image(
                    bitmap = qrBitmap.asImageBitmap(),
                    contentDescription = stringResource(R.string.qr_content_description_format, teamCode),
                    modifier = Modifier
                        .size(256.dp)
                        .clip(RoundedCornerShape(Spacing.md))
                )
            }

            // Логотип в центре (опционально)
            Box(
                modifier = Modifier
                    .size(64.dp)
                    .background(
                        MaterialTheme.colorScheme.surface,
                        RoundedCornerShape(Spacing.sm)
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = teamCode,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
            }
        }

        Text(
            text = stringResource(R.string.qr_scan_hint),
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .padding(horizontal = Spacing.md)
                .padding(top = Spacing.sm, bottom = Spacing.md)
                .align(Alignment.CenterHorizontally)
        )
    }
}

/**
 * Генерация QR-кода из строки
 */
fun generateQrCode(content: String, size: Int): Bitmap? {
    return try {
        val qrWriter = QRCodeWriter()
        val hints = mutableMapOf<EncodeHintType, Any>()
        hints[EncodeHintType.MARGIN] = 1
        hints[EncodeHintType.ERROR_CORRECTION] = com.google.zxing.qrcode.decoder.ErrorCorrectionLevel.M

        val bitMatrix = qrWriter.encode(content, BarcodeFormat.QR_CODE, size, size, hints)

        val bitmap = createBitmap(size, size, Bitmap.Config.RGB_565)
        for (x in 0 until size) {
            for (y in 0 until size) {
                bitmap[x, y] = if (bitMatrix[x, y]) Color.BLACK else Color.WHITE
            }
        }

        // Масштабирование для лучшей читаемости
        bitmap.scale(size * 2, size * 2)
    } catch (e: Exception) {
        null
    }
}
