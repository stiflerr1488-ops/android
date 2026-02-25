package com.example.teamcompass.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.teamcompass.R
import com.example.teamcompass.ui.theme.AlphaTokens
import com.example.teamcompass.ui.theme.Radius
import com.example.teamcompass.ui.theme.Spacing

@Composable
internal fun JoinScreen(
    callsign: String,
    onCallsignChange: (String) -> Unit,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit,
    isBusy: Boolean,
    savedCodeHint: String?,
) {
    var code by rememberSaveable(savedCodeHint) { mutableStateOf(savedCodeHint ?: "") }
    val normalizedCallsign = callsign.trim()
    val callsignValid = normalizedCallsign.length in 3..16 &&
        normalizedCallsign.all { it.isLetterOrDigit() || it == '_' || it == '-' }
    val callsignError = normalizedCallsign.isNotEmpty() && !callsignValid
    val codeNormalized = code.filter(Char::isDigit).take(6)
    val codeError = codeNormalized.isNotEmpty() && codeNormalized.length != 6

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .testTag("join_screen")
            .padding(Spacing.md),
    ) {
        Column(verticalArrangement = Arrangement.Center) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    Modifier
                        .size(Spacing.xl + Spacing.xs)
                        .clip(RoundedCornerShape(Spacing.md))
                        .background(MaterialTheme.colorScheme.surfaceVariant),
                    contentAlignment = Alignment.Center,
                ) {
                    Image(
                        painter = painterResource(R.drawable.ic_compass),
                        contentDescription = null,
                        modifier = Modifier.size(Spacing.lg + Spacing.xs),
                    )
                }
                Spacer(Modifier.width(Spacing.sm))
                Column {
                    Text(
                        "TeamCompass",
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.join_tagline),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            }

            Spacer(Modifier.height(Spacing.lg - Spacing.xs))

            Card(
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(Radius.button),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            ) {
                Column(Modifier.padding(Spacing.md)) {
                    Text(
                        stringResource(R.string.join_title),
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.SemiBold,
                    )
                    Text(
                        stringResource(R.string.join_step_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Spacer(Modifier.height(Spacing.sm))

                    OutlinedTextField(
                        value = callsign,
                        onValueChange = onCallsignChange,
                        label = { Text(stringResource(R.string.join_callsign_label)) },
                        singleLine = true,
                        enabled = !isBusy,
                        isError = callsignError,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
                        supportingText = {
                            if (callsignError) {
                                Text(stringResource(R.string.join_callsign_error))
                            } else {
                                Text(stringResource(R.string.join_callsign_help))
                            }
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("callsign_input"),
                    )

                    Spacer(Modifier.height(Spacing.sm))

                    BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
                        val stacked = maxWidth < 360.dp

                        val cardsContent: @Composable (Modifier) -> Unit = { cardModifier ->
                            Card(
                                modifier = cardModifier,
                                shape = RoundedCornerShape(Spacing.md),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = AlphaTokens.cardStrong,
                                    ),
                                ),
                            ) {
                                Column(
                                    Modifier.padding(Spacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                ) {
                                    Text(
                                        stringResource(R.string.join_new_team_title),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    Text(
                                        stringResource(R.string.join_new_team_description),
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    )
                                    Button(
                                        onClick = onCreate,
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("create_team_button"),
                                        shape = RoundedCornerShape(Radius.button),
                                        enabled = !isBusy && callsignValid,
                                    ) {
                                        Icon(
                                            Icons.Default.Groups,
                                            contentDescription = stringResource(
                                                R.string.join_create_team_cd,
                                            ),
                                        )
                                        Spacer(Modifier.width(Spacing.xs))
                                        Text(stringResource(R.string.join_create_team))
                                    }
                                }
                            }

                            Card(
                                modifier = cardModifier,
                                shape = RoundedCornerShape(Spacing.md),
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(
                                        alpha = AlphaTokens.cardSubtle,
                                    ),
                                ),
                            ) {
                                Column(
                                    Modifier.padding(Spacing.sm),
                                    verticalArrangement = Arrangement.spacedBy(Spacing.sm),
                                ) {
                                    Text(
                                        stringResource(R.string.join_by_code_title),
                                        fontWeight = FontWeight.SemiBold,
                                    )
                                    OutlinedTextField(
                                        value = code,
                                        onValueChange = { code = it.filter(Char::isDigit).take(6) },
                                        label = { Text(stringResource(R.string.join_code_label)) },
                                        singleLine = true,
                                        keyboardOptions = KeyboardOptions(
                                            keyboardType = KeyboardType.Number,
                                            imeAction = ImeAction.Done,
                                        ),
                                        keyboardActions = KeyboardActions(
                                            onDone = {
                                                if (!isBusy && callsignValid && codeNormalized.length == 6) {
                                                    onJoin(codeNormalized)
                                                }
                                            },
                                        ),
                                        enabled = !isBusy,
                                        isError = codeError,
                                        supportingText = {
                                            if (codeError) {
                                                Text(stringResource(R.string.join_code_error))
                                            } else {
                                                Text(stringResource(R.string.join_code_help))
                                            }
                                        },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("team_code_input"),
                                    )
                                    FilledTonalButton(
                                        onClick = { onJoin(codeNormalized) },
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .testTag("join_team_button"),
                                        shape = RoundedCornerShape(Radius.button),
                                        enabled = !isBusy && callsignValid && codeNormalized.length == 6,
                                    ) {
                                        Icon(
                                            Icons.Default.GpsFixed,
                                            contentDescription = stringResource(
                                                R.string.join_enter_team_cd,
                                            ),
                                        )
                                        Spacer(Modifier.width(Spacing.xs))
                                        Text(stringResource(R.string.join_enter_team))
                                    }
                                }
                            }
                        }

                        if (stacked) {
                            Column(verticalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                cardsContent(Modifier.fillMaxWidth())
                            }
                        } else {
                            Row(horizontalArrangement = Arrangement.spacedBy(Spacing.sm)) {
                                cardsContent(Modifier.weight(1f))
                            }
                        }
                    }

                    Spacer(Modifier.height(Spacing.xs))
                    Text(
                        stringResource(R.string.join_footer_hint),
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall,
                    )
                }
            }
        }

        AnimatedVisibility(
            visible = isBusy,
            modifier = Modifier.fillMaxSize(),
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120)),
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = AlphaTokens.scrim)),
            )
        }

        AnimatedVisibility(
            visible = isBusy,
            modifier = Modifier.align(Alignment.Center),
            enter = fadeIn(tween(120)),
            exit = fadeOut(tween(120)),
        ) {
            Card(
                shape = RoundedCornerShape(Radius.button),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            ) {
                Row(
                    modifier = Modifier.padding(
                        horizontal = Spacing.lg - Spacing.xs,
                        vertical = Spacing.sm,
                    ),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(Spacing.sm),
                ) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(Spacing.md + Spacing.xs),
                        strokeWidth = 2.dp,
                    )
                    Text(
                        stringResource(R.string.join_connecting_team),
                        style = MaterialTheme.typography.bodyMedium,
                    )
                }
            }
        }
    }
}
