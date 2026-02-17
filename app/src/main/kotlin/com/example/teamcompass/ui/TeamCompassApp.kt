package com.example.teamcompass.ui

import android.Manifest
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Build
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.ExitToApp
import androidx.compose.material.icons.filled.GpsFixed
import androidx.compose.material.icons.filled.Groups
import androidx.compose.material.icons.filled.Warning
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.teamcompass.R
import com.example.teamcompass.core.CompassTarget
import com.example.teamcompass.core.Staleness
import com.example.teamcompass.core.TrackingMode
import kotlinx.coroutines.delay
import kotlin.math.roundToInt

@Composable
fun TeamCompassApp(vm: TeamCompassViewModel = viewModel()) {
    val state by vm.ui.collectAsState()

    val ctx = LocalContext.current
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(state.lastError) {
        val msg = state.lastError ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(msg)
        vm.dismissError()
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            when {
                !state.isAuthReady -> LoadingScreen()
                state.teamCode == null -> JoinScreen(
                    callsign = state.callsign,
                    onCallsignChange = vm::setCallsign,
                    onCreate = { vm.createTeam() },
                    onJoin = { code -> vm.joinTeam(code) },
                    savedCodeHint = null
                )
                else -> CompassScreen(
                    state = state,
                    onRequestPermission = { granted -> vm.setLocationPermission(granted) },
                    onStartTracking = { vm.startTracking(TrackingMode.GAME) },
                    onStopTracking = { vm.stopTracking() },
                    onLeave = { vm.leaveTeam() },
                    targetsProvider = { now -> vm.computeTargets(now) },
                    onCopyCode = { copyToClipboard(ctx, state.teamCode ?: "") }
                )
            }
        }
    }
}

private fun copyToClipboard(context: Context, text: String) {
    val cm = context.getSystemService(ClipboardManager::class.java)
    cm?.setPrimaryClip(ClipData.newPlainText("Team code", text))
}

@Composable
private fun LoadingScreen() {
    Column(
        Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        CircularProgressIndicator()
        Spacer(Modifier.height(16.dp))
        Text("Подключаемся…", style = MaterialTheme.typography.bodyMedium)
    }
}

@Composable
private fun JoinScreen(
    callsign: String,
    onCallsignChange: (String) -> Unit,
    onCreate: () -> Unit,
    onJoin: (String) -> Unit,
    savedCodeHint: String?
) {
    var code by remember { mutableStateOf(savedCodeHint ?: "") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(20.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                Modifier
                    .size(52.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(R.drawable.ic_compass),
                    contentDescription = null,
                    modifier = Modifier.size(36.dp)
                )
            }
            Spacer(Modifier.width(12.dp))
            Column {
                Text("TeamCompass", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.SemiBold)
                Text("Командный компас для леса", color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }

        Spacer(Modifier.height(18.dp))

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.padding(16.dp)) {
                Text("Вход в команду", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(12.dp))

                OutlinedTextField(
                    value = callsign,
                    onValueChange = onCallsignChange,
                    label = { Text("Позывной") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(Modifier.height(10.dp))
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it.filter(Char::isDigit).take(6) },
                    label = { Text("Код команды (6 цифр)") },
                    singleLine = true,
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(Modifier.height(14.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    Button(
                        onClick = onCreate,
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp)
                    ) {
                        Icon(Icons.Default.Groups, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Создать")
                    }
                    FilledTonalButton(
                        onClick = { onJoin(code) },
                        modifier = Modifier.weight(1f),
                        shape = RoundedCornerShape(14.dp),
                        enabled = code.length == 6
                    ) {
                        Icon(Icons.Default.GpsFixed, contentDescription = null)
                        Spacer(Modifier.width(8.dp))
                        Text("Войти")
                    }
                }

                Spacer(Modifier.height(8.dp))
                Text(
                    "Позывной сохранится на устройстве. Код можно просто скинуть в чат.",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun CompassScreen(
    state: UiState,
    onRequestPermission: (Boolean) -> Unit,
    onStartTracking: () -> Unit,
    onStopTracking: () -> Unit,
    onLeave: () -> Unit,
    targetsProvider: (Long) -> List<CompassTarget>,
    onCopyCode: () -> Unit,
) {
    val ctx = LocalContext.current

    // Permission request
    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { res ->
        val granted = (res[Manifest.permission.ACCESS_FINE_LOCATION] == true) ||
                (res[Manifest.permission.ACCESS_COARSE_LOCATION] == true)
        onRequestPermission(granted)
    }

    var now by remember { mutableStateOf(System.currentTimeMillis()) }
    LaunchedEffect(Unit) {
        while (true) {
            now = System.currentTimeMillis()
            delay(1000)
        }
    }

    val targets = remember(state.players, state.me, state.myHeadingDeg, now) { targetsProvider(now) }

    Column(Modifier.fillMaxSize()) {
        CenterAlignedTopAppBar(
            title = {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Команда ${state.teamCode}", fontWeight = FontWeight.SemiBold)
                    val me = state.me
                    val acc = me?.accMeters?.roundToInt()
                    val heading = state.myHeadingDeg?.roundToInt()
                    Text(
                        buildString {
                            append(state.callsign.ifBlank { "Игрок" })
                            if (acc != null) append(" • ±${acc}м")
                            if (heading != null) append(" • ${heading}°")
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            },
            actions = {
                IconButton(onClick = onCopyCode) {
                    Icon(Icons.Default.ContentCopy, contentDescription = "Copy code")
                }
                IconButton(onClick = onLeave) {
                    Icon(Icons.Default.ExitToApp, contentDescription = "Leave")
                }
            }
        )

        // Permission / tracking controls
        Row(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 10.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            if (!state.hasLocationPermission) {
                FilledTonalButton(
                    onClick = {
                        launcher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    },
                    shape = RoundedCornerShape(14.dp)
                ) {
                    Icon(Icons.Default.GpsFixed, contentDescription = null)
                    Spacer(Modifier.width(8.dp))
                    Text("Разрешить GPS")
                }
            } else {
                if (!state.isTracking) {
                    Button(onClick = onStartTracking, shape = RoundedCornerShape(14.dp)) {
                        Text("Старт")
                    }
                } else {
                    FilledTonalButton(onClick = onStopTracking, shape = RoundedCornerShape(14.dp)) {
                        Text("Стоп")
                    }
                }
            }

            Spacer(Modifier.weight(1f))

            if (state.myHeadingDeg == null) {
                AssistChip(
                    onClick = { /* info */ },
                    label = { Text("Нет компаса: двигайся") },
                    leadingIcon = { Icon(Icons.Default.Warning, contentDescription = null) }
                )
            } else {
                AssistChip(
                    onClick = { /* info */ },
                    label = { Text("Компас активен") },
                    leadingIcon = { Icon(Icons.Default.GpsFixed, contentDescription = null) }
                )
            }
        }

        // Compass card
        Card(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .weight(1f),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
        ) {
            Column(Modifier.fillMaxSize().padding(16.dp)) {
                Text("Компас команды", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(10.dp))

                CompassRing(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    targets = targets
                )

                Spacer(Modifier.height(12.dp))

                Text(
                    "Точки обновляются при наличии связи. Смотри “обновлено N сек назад”.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // List
        Spacer(Modifier.height(12.dp))
        Card(
            Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 0.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
        ) {
            Column(Modifier.padding(12.dp)) {
                Text("Список", fontWeight = FontWeight.SemiBold)
                Spacer(Modifier.height(8.dp))
                if (targets.isEmpty()) {
                    Text("Пока никого не видно. Проверь, что другие вошли в команду и включили трекинг.",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
                } else {
                    LazyColumn(
                        modifier = Modifier.heightIn(max = 220.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        items(targets) { t ->
                            TargetRow(t)
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(12.dp))
    }
}

@Composable
private fun TargetRow(t: CompassTarget) {
    val tone = when (t.staleness) {
        Staleness.FRESH -> MaterialTheme.colorScheme.primary
        Staleness.SUSPECT -> MaterialTheme.colorScheme.secondary
        Staleness.STALE -> MaterialTheme.colorScheme.tertiary
        Staleness.HIDDEN -> MaterialTheme.colorScheme.onSurfaceVariant
    }

    Row(
        Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(tone.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center
        ) {
            Text(t.nick.take(1).uppercase(), color = tone, fontWeight = FontWeight.Bold)
        }

        Spacer(Modifier.width(10.dp))

        Column(Modifier.weight(1f)) {
            Text(t.nick, fontWeight = FontWeight.SemiBold)
            val dist = if (t.staleness == Staleness.HIDDEN) "—" else "${t.distanceMeters.roundToInt()} м"
            val seen = "${t.lastSeenSec} сек назад"
            val acc = if (t.lowAccuracy) " • низкая точность" else ""
            Text("$dist • $seen$acc", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }

        if (t.lowAccuracy) {
            Icon(Icons.Default.Warning, contentDescription = null, tint = MaterialTheme.colorScheme.tertiary)
        }
    }
}
