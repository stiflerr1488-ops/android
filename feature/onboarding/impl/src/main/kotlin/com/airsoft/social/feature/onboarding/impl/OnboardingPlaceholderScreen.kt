package com.airsoft.social.feature.onboarding.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.airsoft.social.feature.onboarding.api.OnboardingFeatureApi

const val ONBOARDING_CONTINUE_BUTTON_TAG = "onboarding_continue_button"

@Composable
fun OnboardingPlaceholderScreen(
    onPrimaryAction: () -> Unit = {},
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                brush = Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF080B10),
                        Color(0xFF111A2A),
                        Color(0xFF05070A),
                    ),
                ),
            )
            .padding(20.dp),
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(
                text = "AIRSOFT SOCIAL",
                style = MaterialTheme.typography.headlineSmall,
                color = Color(0xFFE9EEF8),
                fontWeight = FontWeight.Black,
            )
            Text(
                text = "App skeleton: key sections, navigation, and radar-mode entry.",
                style = MaterialTheme.typography.bodyMedium,
                color = Color(0xFFB6C3D6),
            )
            Spacer(modifier = Modifier.height(6.dp))
            FeatureTile(title = "Players", subtitle = "Search, cards, ratings")
            FeatureTile(title = "Teams", subtitle = "Roster, roles, participation")
            FeatureTile(title = "Tournaments", subtitle = "Brackets, schedule, statuses")
            FeatureTile(title = "Radar", subtitle = "The V BOI! button opens tactical mode")
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag(ONBOARDING_CONTINUE_BUTTON_TAG),
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFFFF7A00),
                    contentColor = Color(0xFF111111),
                ),
                shape = RoundedCornerShape(10.dp),
            ) {
                Text(
                    text = "Continue",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                )
            }
            Text(
                text = OnboardingFeatureApi.contract.title,
                style = MaterialTheme.typography.labelMedium,
                color = Color(0xFF7E8A9A),
                modifier = Modifier.padding(top = 4.dp),
            )
        }
    }
}

@Composable
private fun FeatureTile(
    title: String,
    subtitle: String,
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        color = Color(0x441A2433),
        shape = RoundedCornerShape(12.dp),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
        ) {
            Box(
                modifier = Modifier
                    .width(4.dp)
                    .height(36.dp)
                    .background(Color(0xFFFF7A00), RoundedCornerShape(12.dp)),
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = title,
                    color = Color(0xFFF2F6FD),
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                )
                Text(
                    text = subtitle,
                    color = Color(0xFF9FB0C6),
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }
    }
}
