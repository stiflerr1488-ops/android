package com.airsoft.social.core.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.airsoft.social.core.designsystem.AirsoftSpacing

@Composable
fun LoadingScreen(
    title: String = "Loading...",
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AirsoftSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        CircularProgressIndicator()
        Text(
            text = title,
            style = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.padding(top = AirsoftSpacing.md),
        )
    }
}

@Composable
fun ErrorScreen(
    title: String,
    message: String,
    primaryActionLabel: String? = null,
    onPrimaryAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AirsoftSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = message,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = AirsoftSpacing.sm),
        )
        if (primaryActionLabel != null) {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.padding(top = AirsoftSpacing.md),
            ) {
                Text(primaryActionLabel)
            }
        }
    }
}

@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AirsoftSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = AirsoftSpacing.sm),
        )
    }
}

@Composable
fun ComingSoonScreen(
    title: String,
    body: String,
    primaryActionLabel: String? = null,
    onPrimaryAction: () -> Unit = {},
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AirsoftSpacing.lg),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineMedium)
        Text(
            text = body,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.padding(top = AirsoftSpacing.sm),
        )
        if (primaryActionLabel != null) {
            Button(
                onClick = onPrimaryAction,
                modifier = Modifier.padding(top = AirsoftSpacing.md),
            ) {
                Text(primaryActionLabel)
            }
        }
    }
}

@Composable
fun PlaceholderListScreen(
    title: String,
    rows: List<String>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(AirsoftSpacing.lg),
        verticalArrangement = Arrangement.spacedBy(AirsoftSpacing.sm),
    ) {
        Text(text = title, style = MaterialTheme.typography.headlineSmall)
        rows.forEach { row ->
            Text(
                text = "- $row",
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.fillMaxWidth(),
            )
        }
    }
}
