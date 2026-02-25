package com.example.teamcompass.ui.components

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.TouchApp
import androidx.compose.material.icons.filled.ZoomInMap
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.annotation.StringRes
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Dimens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing
import kotlinx.coroutines.launch

data class OnboardingPage(
    val icon: androidx.compose.ui.graphics.vector.ImageVector,
    @StringRes val titleRes: Int,
    @StringRes val descriptionRes: Int,
)

val onboardingPages = listOf(
    OnboardingPage(
        icon = Icons.Default.ZoomInMap,
        titleRes = R.string.onboarding_zoom_title,
        descriptionRes = R.string.onboarding_zoom_description,
    ),
    OnboardingPage(
        icon = Icons.Default.TouchApp,
        titleRes = R.string.onboarding_mark_title,
        descriptionRes = R.string.onboarding_mark_description,
    ),
    OnboardingPage(
        icon = Icons.Default.GpsFixed,
        titleRes = R.string.onboarding_modes_title,
        descriptionRes = R.string.onboarding_modes_description,
    )
)

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun OnboardingDialog(
    onDismiss: () -> Unit
) {
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val pagerState = rememberPagerState(pageCount = { onboardingPages.size })
    val scope = rememberCoroutineScope()

    fun closeDialog() {
        scope.launch {
            sheetState.hide()
            onDismiss()
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = AlphaTokens.overlayStrong),
        shape = RoundedCornerShape(topStart = Radius.lg, topEnd = Radius.lg)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(Spacing.md),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedButton(onClick = { closeDialog() }) {
                    Text(stringResource(R.string.onboarding_skip))
                }
                IconButton(onClick = { closeDialog() }) {
                    Icon(
                        Icons.Default.Close,
                        contentDescription = stringResource(R.string.dialog_close),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Text(
                stringResource(R.string.help_title_radar),
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface
            )

            Spacer(Modifier.height(Spacing.md))

            HorizontalPager(
                state = pagerState,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(Dimens.onboardingPagerHeight)
            ) { page ->
                OnboardingPageContent(onboardingPages[page])
            }

            Row(
                horizontalArrangement = Arrangement.spacedBy(Spacing.xs),
                modifier = Modifier.padding(vertical = Spacing.md)
            ) {
                repeat(onboardingPages.size) { index ->
                    Box(
                        modifier = Modifier
                            .size(
                                if (pagerState.currentPage == index) {
                                    Dimens.onboardingIndicatorActive
                                } else {
                                    Dimens.onboardingIndicatorIdle
                                }
                            )
                            .background(
                                color = if (pagerState.currentPage == index) {
                                    MaterialTheme.colorScheme.primary
                                } else {
                                    MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.4f)
                                },
                                shape = CircleShape
                            )
                    )
                }
            }

            Spacer(Modifier.height(Spacing.md))

            Button(
                onClick = {
                    if (pagerState.currentPage < onboardingPages.lastIndex) {
                        scope.launch {
                            pagerState.animateScrollToPage(pagerState.currentPage + 1)
                        }
                    } else {
                        closeDialog()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (pagerState.currentPage < onboardingPages.lastIndex) {
                        stringResource(R.string.onboarding_next)
                    } else {
                        stringResource(R.string.dialog_understood)
                    }
                )
            }

            Spacer(Modifier.height(Spacing.sm))
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(Spacing.sm),
        shape = RoundedCornerShape(Radius.md),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = AlphaTokens.cardSubtle)
        )
    ) {
        Column(
            modifier = Modifier.padding(Dimens.cardPaddingLg),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(Spacing.md)
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(Dimens.onboardingIconSize),
                tint = MaterialTheme.colorScheme.primary
            )

            Text(
                stringResource(page.titleRes),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                color = MaterialTheme.colorScheme.onSurface,
                textAlign = TextAlign.Center
            )

            Text(
                stringResource(page.descriptionRes),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}
