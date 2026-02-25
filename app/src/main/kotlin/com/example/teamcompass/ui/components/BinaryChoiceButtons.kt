package com.example.teamcompass.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Button
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.example.teamcompass.ui.theme.Spacing

@Composable
fun BinaryChoiceButtons(
    modifier: Modifier = Modifier,
    leftText: String,
    rightText: String,
    leftSelected: Boolean,
    onLeftClick: () -> Unit,
    onRightClick: () -> Unit,
) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
        val weightModifier = Modifier.weight(1f)
        if (leftSelected) {
            Button(onClick = onLeftClick, modifier = weightModifier) { Text(leftText) }
            OutlinedButton(onClick = onRightClick, modifier = weightModifier) { Text(rightText) }
        } else {
            OutlinedButton(onClick = onLeftClick, modifier = weightModifier) { Text(leftText) }
            Button(onClick = onRightClick, modifier = weightModifier) { Text(rightText) }
        }
    }
}
