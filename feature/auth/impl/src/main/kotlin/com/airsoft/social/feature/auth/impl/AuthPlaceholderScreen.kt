package com.airsoft.social.feature.auth.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airsoft.social.feature.auth.api.AuthFeatureApi

const val AUTH_GUEST_BUTTON_TAG = "auth_guest_button"

@Composable
fun AuthPlaceholderScreen(
    onPrimaryAction: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF06080C),
                        Color(0xFF121821),
                        Color(0xFF0A0C10),
                    ),
                ),
            )
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = "AIRSOFT",
                style = MaterialTheme.typography.headlineLarge,
                color = Color(0xFFECEFF5),
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "SOCIAL ASSISTANT",
                style = MaterialTheme.typography.titleMedium,
                color = Color(0xFFFF7A00),
                fontWeight = FontWeight.Bold,
            )
            Spacer(modifier = Modifier.height(28.dp))
            Text(
                text = AuthFeatureApi.contract.title,
                style = MaterialTheme.typography.labelLarge,
                color = Color(0xFF9BA8BC),
            )
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7A00),
                    contentColor = Color(0xFF111111),
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Sign in (provider later)")
            }
            Spacer(modifier = Modifier.height(10.dp))
            OutlinedButton(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .testTag(AUTH_GUEST_BUTTON_TAG),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text("Continue as guest")
            }
            Spacer(modifier = Modifier.height(18.dp))
            Text(
                text = "Auth skeleton: AuthGateway and provider adapters will be connected later.",
                style = MaterialTheme.typography.bodySmall,
                color = Color(0xFF93A2B8),
            )
        }
    }
}
