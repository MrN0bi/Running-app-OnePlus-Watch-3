package com.watchrunning.app.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.wear.compose.material3.MaterialTheme
import androidx.wear.compose.material3.Text
import com.watchrunning.app.BuildConfig
import com.watchrunning.app.calculation.MetricFormatters
import com.watchrunning.app.data.settings.MaximumHeartRateMode
import com.watchrunning.app.data.settings.RunningSettings
import com.watchrunning.app.data.settings.SettingsRepository
import com.watchrunning.app.model.ExerciseCapabilitiesSnapshot
import com.watchrunning.app.model.GpsStatus
import com.watchrunning.app.model.GpsSource
import com.watchrunning.app.model.HeartRateZone
import com.watchrunning.app.model.LiveMetrics
import com.watchrunning.app.model.WorkoutCommand
import com.watchrunning.app.model.WorkoutPhase
import com.watchrunning.app.model.WorkoutUiState
import java.time.Duration
import kotlin.math.roundToInt
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

private val Black = Color.Black
private val White = Color(0xFFF7F7F7)
private val Muted = Color(0xFFAAAAAA)
private val Green = Color(0xFF64D75B)
private val Orange = Color(0xFFFF7800)
private val Red = Color(0xFFFF3B24)
private val ZoneColors = listOf(
    Color(0xFF378ED7),
    Color(0xFF62C653),
    Color(0xFFFFCC16),
    Color(0xFFFF7A08),
    Color(0xFFF13722),
)

private enum class LocalScreen { HOME, SETTINGS }

@Composable
fun RunningApp(
    state: WorkoutUiState,
    settings: RunningSettings?,
    capabilities: ExerciseCapabilitiesSnapshot,
    permissionMessage: String?,
    onDismissMessage: () -> Unit,
    onDismissSummary: () -> Unit,
    onStartRun: () -> Unit,
    onCommand: (WorkoutCommand) -> Unit,
    settingsRepository: SettingsRepository,
) {
    var localScreen by remember { mutableStateOf(LocalScreen.HOME) }
    MaterialTheme {
        Box(Modifier.fillMaxSize().background(Black)) {
            when {
                localScreen == LocalScreen.SETTINGS && !state.hasActiveSession -> SettingsScreen(
                    settings = settings,
                    capabilities = capabilities,
                    repository = settingsRepository,
                    onBack = { localScreen = LocalScreen.HOME },
                )
                state.phase == WorkoutPhase.Preparing || state.phase == WorkoutPhase.Starting -> AcquisitionScreen(
                    state,
                    onCommand,
                )
                state.phase in setOf(WorkoutPhase.Active, WorkoutPhase.Pausing, WorkoutPhase.Resuming) ->
                    ActiveWorkoutScreen(state, settings?.paceWindowSeconds ?: 5, onCommand)
                state.phase == WorkoutPhase.Paused -> PausedScreen(state, onCommand)
                state.phase in setOf(WorkoutPhase.Ended, WorkoutPhase.Interrupted) -> SummaryScreen(
                    state,
                    onDone = onDismissSummary,
                )
                else -> StartScreen(
                    capabilities = capabilities,
                    error = state.error ?: state.warning,
                    onRun = onStartRun,
                    onSettings = { localScreen = LocalScreen.SETTINGS },
                )
            }

            if (permissionMessage != null) {
                MessageOverlay(permissionMessage, onDismissMessage)
            }
        }
    }
}

@Composable
private fun StartScreen(
    capabilities: ExerciseCapabilitiesSnapshot,
    error: String?,
    onRun: () -> Unit,
    onSettings: () -> Unit,
) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 28.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("RUN", color = Green, fontSize = 17.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(12.dp))
        RoundAction("Start", Green, Black, onRun, 104)
        Spacer(Modifier.height(12.dp))
        Text("Settings", modifier = Modifier.clickable(onClick = onSettings).padding(12.dp), color = White)
        if (error != null) {
            Text(error, color = Color(0xFFFFB74D), fontSize = 12.sp, textAlign = TextAlign.Center)
        }
        if (capabilities.apiLevel > 0) {
            Text(
                "${capabilities.manufacturer} ${capabilities.model} · Android ${capabilities.release} / API ${capabilities.apiLevel}",
                color = Color(0xFF777777),
                fontSize = 10.sp,
                textAlign = TextAlign.Center,
            )
            Text(
                "HS run ${yesNo(capabilities.runningSupported)} · GPS ${yesNo(capabilities.locationSupported)} · " +
                    "distance ${yesNo(capabilities.distanceSupported)} · HR ${yesNo(capabilities.heartRateSupported)} · " +
                    "buttons ${capabilities.buttonCount}",
                color = Color(0xFF777777),
                fontSize = 9.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text("v${BuildConfig.VERSION_NAME}", color = Color(0xFF666666), fontSize = 9.sp)
    }
}

@Composable
private fun AcquisitionScreen(state: WorkoutUiState, onCommand: (WorkoutCommand) -> Unit) {
    val metrics = state.metrics
    Column(
        Modifier.fillMaxSize().padding(horizontal = 34.dp, vertical = 36.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceEvenly,
    ) {
        Text("GET READY", color = White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        val gpsBaseDetail = when {
            metrics.gpsReady -> "Ready · ±${metrics.gpsHorizontalErrorMetres?.roundToInt()} m"
            metrics.gpsStatus == GpsStatus.NO_GNSS -> "GNSS unavailable"
            metrics.gpsStatus == GpsStatus.UNAVAILABLE -> "Location unavailable"
            metrics.gpsHorizontalErrorMetres != null ->
                "Fix ±${metrics.gpsHorizontalErrorMetres.roundToInt()} m · need ≤25 m"
            metrics.gpsStatus == GpsStatus.ACQUIRED -> "Fix received · accuracy unavailable"
            else -> "Acquiring outdoors…"
        }
        val gpsSource = when (metrics.gpsSource) {
            GpsSource.WATCH_GNSS -> "watch GNSS"
            GpsSource.FUSED -> "fused watch/phone"
            GpsSource.HEALTH_SERVICES -> "Health Services"
            GpsSource.HEALTH_SERVICES_WATCH -> "Health Services watch"
            GpsSource.HEALTH_SERVICES_PHONE -> "Health Services phone"
            GpsSource.NONE -> null
        }
        val gpsDetail = gpsSource?.let { "$gpsBaseDetail · $it" } ?: gpsBaseDetail
        SensorStatus("GPS", metrics.gpsReady, gpsDetail)
        SensorStatus("HEART", metrics.heartRateReady, metrics.heartRateBpm?.let { "$it bpm" } ?: "Acquiring…")
        when {
            metrics.gpsReady -> WideAction("START", Green, Black) { onCommand(WorkoutCommand.Start) }
            state.canStartWithoutFix -> WideAction("START ANYWAY", Orange, Black) {
                onCommand(WorkoutCommand.StartWithoutFix)
            }
            else -> Text(
                "Waiting for precise fix · degraded start in ${state.startWithoutFixRemainingSeconds}s",
                color = Muted,
                fontSize = 11.sp,
                textAlign = TextAlign.Center,
            )
        }
        Text(
            "Cancel",
            modifier = Modifier.clickable { onCommand(WorkoutCommand.CancelPrepare) }.padding(12.dp),
            color = Muted,
        )
    }
}

@Composable
private fun SensorStatus(label: String, ready: Boolean, detail: String) {
    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 13.sp)
        Column(horizontalAlignment = Alignment.End) {
            Text(if (ready) "READY" else "WAIT", color = if (ready) Green else Orange, fontWeight = FontWeight.Bold)
            Text(detail, color = White, fontSize = 11.sp)
        }
    }
}

@Composable
private fun ActiveWorkoutScreen(
    state: WorkoutUiState,
    paceWindowSeconds: Int,
    onCommand: (WorkoutCommand) -> Unit,
) {
    var controlsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(controlsVisible) {
        if (controlsVisible) {
            delay(5_000)
            controlsVisible = false
        }
    }
    Box(
        Modifier.fillMaxSize().background(Black).clickable { controlsVisible = true },
        contentAlignment = Alignment.Center,
    ) {
        WorkoutMetrics(state.metrics, paceWindowSeconds)
        if (controlsVisible) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.BottomCenter) {
                WideAction(
                    label = if (state.phase == WorkoutPhase.Active) "PAUSE" else "WORKING…",
                    background = Color(0xDD333333),
                    foreground = White,
                    modifier = Modifier.padding(bottom = 24.dp).width(180.dp),
                ) {
                    if (state.phase == WorkoutPhase.Active) onCommand(WorkoutCommand.Pause)
                }
            }
        }
    }
}

@Composable
private fun WorkoutMetrics(metrics: LiveMetrics, paceWindowSeconds: Int) {
    BoxWithConstraints(Modifier.fillMaxSize()) {
        val scale = (maxHeight / 233.dp).coerceIn(0.78f, 1.12f)
        ZoneArc(metrics.zoneIndicatorFraction, scale)
        Column(
            Modifier.fillMaxSize().padding(horizontal = 18.dp * scale, vertical = 7.dp * scale),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Spacer(Modifier.height(43.dp * scale))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("♥", color = Red, fontSize = 20.sp * scale)
                Spacer(Modifier.width(4.dp * scale))
                Text(metrics.heartRateBpm?.toString() ?: "—", color = White, fontSize = 28.sp * scale, fontWeight = FontWeight.Bold)
                Text(" bpm", color = White, fontSize = 10.sp * scale)
                Spacer(Modifier.width(8.dp * scale))
                Box(
                    Modifier.background(Color.Transparent, RoundedCornerShape(24.dp))
                        .padding(horizontal = 6.dp * scale, vertical = 3.dp * scale),
                ) {
                    val zone = metrics.heartRateZone.number.takeIf { it > 0 }?.let { "Zone $it" } ?: "Zone —"
                    Text(zone, color = zoneColor(metrics.heartRateZone), fontSize = 10.sp * scale, fontWeight = FontWeight.Bold)
                }
            }
            Spacer(Modifier.height(3.dp * scale))
            Text("${paceWindowSeconds}s pace", color = Muted, fontSize = 11.sp * scale)
            Row(verticalAlignment = Alignment.Bottom) {
                Text(
                    MetricFormatters.pace(metrics.pace?.secondsPerKilometre),
                    color = White,
                    fontSize = 46.sp * scale,
                    fontWeight = FontWeight.Bold,
                )
                Text(" /km", color = White, fontSize = 13.sp * scale, modifier = Modifier.padding(bottom = 7.dp * scale))
            }
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF3A3A3A)))
            Spacer(Modifier.height(3.dp * scale))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Avg ", color = Muted, fontSize = 11.sp * scale)
                Text(MetricFormatters.pace(metrics.averagePaceSecondsPerKm), color = White, fontSize = 17.sp * scale)
                Text(" /km", color = White, fontSize = 11.sp * scale)
            }
            Spacer(Modifier.height(3.dp * scale))
            Box(Modifier.fillMaxWidth().height(1.dp).background(Color(0xFF2A2A2A)))
            Spacer(Modifier.height(5.dp * scale))
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                Text(MetricFormatters.distanceKilometres(metrics.distanceMetres), color = Green, fontSize = 13.sp * scale)
                Box(Modifier.width(1.dp).height(20.dp * scale).background(Color(0xFF444444)))
                Text(MetricFormatters.duration(metrics.activeDuration), color = Green, fontSize = 13.sp * scale)
            }
        }
    }
}

@Composable
private fun ZoneArc(indicatorFraction: Float?, scale: Float) {
    Canvas(Modifier.fillMaxWidth().height(82.dp * scale).padding(horizontal = 18.dp * scale, vertical = 8.dp * scale)) {
        val strokeWidth = (7.dp * scale).toPx()
        val gap = 2.2f
        val segmentSweep = (140f - gap * 4) / 5f
        val arcSize = Size(size.width - strokeWidth, size.width - strokeWidth)
        val topLeft = Offset(strokeWidth / 2, (2.dp * scale).toPx())
        ZoneColors.forEachIndexed { index, color ->
            drawArc(
                color = color,
                startAngle = 200f + index * (segmentSweep + gap),
                sweepAngle = segmentSweep,
                useCenter = false,
                topLeft = topLeft,
                size = arcSize,
                style = Stroke(strokeWidth, cap = StrokeCap.Butt),
            )
        }
        indicatorFraction?.let { fraction ->
            val scaled = (fraction.coerceIn(0f, 1f) * 5f)
            val zoneIndex = scaled.toInt().coerceAtMost(4)
            val withinZone = if (fraction >= 1f) 1f else scaled - zoneIndex
            val indicatorAngle = 200f + zoneIndex * (segmentSweep + gap) + withinZone * segmentSweep
            val angle = Math.toRadians(indicatorAngle.toDouble())
            val radius = arcSize.width / 2
            val centre = Offset(topLeft.x + radius, topLeft.y + radius)
            val inner = radius - strokeWidth * 0.85f
            val outer = radius + strokeWidth * 0.25f
            drawLine(
                White,
                Offset(centre.x + kotlin.math.cos(angle).toFloat() * inner, centre.y + kotlin.math.sin(angle).toFloat() * inner),
                Offset(centre.x + kotlin.math.cos(angle).toFloat() * outer, centre.y + kotlin.math.sin(angle).toFloat() * outer),
                strokeWidth = (3.dp * scale).toPx(),
                cap = StrokeCap.Round,
            )
        }
    }
}

@Composable
private fun PausedScreen(state: WorkoutUiState, onCommand: (WorkoutCommand) -> Unit) {
    var confirmEnd by remember { mutableStateOf(false) }
    Column(
        Modifier.fillMaxSize().padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("PAUSED", color = Orange, fontSize = 20.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(8.dp))
        Text(MetricFormatters.duration(state.metrics.activeDuration), color = White, fontSize = 42.sp, fontWeight = FontWeight.Bold)
        Text(MetricFormatters.distanceKilometres(state.metrics.distanceMetres), color = Muted, fontSize = 20.sp)
        Spacer(Modifier.height(18.dp))
        if (confirmEnd) {
            Text("Stop this run?", color = White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(12.dp))
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PausedAction("CANCEL", Color(0xFF333333), White, Modifier.weight(1f)) {
                    confirmEnd = false
                }
                PausedAction("STOP", Red, White, Modifier.weight(1f)) {
                    onCommand(WorkoutCommand.ConfirmEnd)
                }
            }
        } else {
            Row(
                Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp),
            ) {
                PausedAction("RESUME", Green, Black, Modifier.weight(1f)) {
                    onCommand(WorkoutCommand.Resume)
                }
                PausedAction("STOP", Red, White, Modifier.weight(1f)) {
                    confirmEnd = true
                }
            }
        }
    }
}

@Composable
private fun PausedAction(
    label: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier.height(58.dp).background(background, RoundedCornerShape(29.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            label,
            color = foreground,
            fontWeight = FontWeight.Bold,
            fontSize = 12.sp,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SummaryScreen(state: WorkoutUiState, onDone: () -> Unit) {
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 34.dp, vertical = 34.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(if (state.phase == WorkoutPhase.Interrupted) "INTERRUPTED" else "RUN ENDED", color = Green, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        SummaryRow("Distance", MetricFormatters.distanceKilometres(state.metrics.distanceMetres))
        SummaryRow("Active", MetricFormatters.duration(state.metrics.activeDuration))
        SummaryRow("Elapsed", MetricFormatters.duration(state.metrics.elapsedDuration))
        SummaryRow("Average", "${MetricFormatters.pace(state.metrics.averagePaceSecondsPerKm)} /km")
        SummaryRow("Average HR", state.metrics.averageHeartRateBpm?.let { "${it.toInt()} bpm" } ?: "Unavailable")
        SummaryRow("Maximum HR", state.metrics.maximumHeartRateBpm?.let { "${it.toInt()} bpm" } ?: "Unavailable")
        state.zoneTimeMillis.forEachIndexed { index, millis ->
            SummaryRow("Zone ${index + 1}", MetricFormatters.duration(Duration.ofMillis(millis)))
        }
        SummaryRow("HR unclassified", MetricFormatters.duration(Duration.ofMillis(state.unclassifiedHeartRateMillis)))
        SummaryRow("Pauses", state.pauseCount.toString())
        if (state.warning != null) Text(state.warning, color = Color(0xFFFFB74D), fontSize = 11.sp, textAlign = TextAlign.Center)
        Spacer(Modifier.height(18.dp))
        WideAction("DONE", Color(0xFF333333), White, onClick = onDone)
    }
}

@Composable
private fun SettingsScreen(
    settings: RunningSettings?,
    capabilities: ExerciseCapabilitiesSnapshot,
    repository: SettingsRepository,
    onBack: () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val current = settings ?: RunningSettings()
    Column(
        Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(horizontal = 30.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("SETTINGS", color = White, fontSize = 18.sp, fontWeight = FontWeight.Bold)
        Spacer(Modifier.height(16.dp))
        SettingStepper("Age", current.age ?: 30, 13, 100) { scope.launch { repository.setAge(it) } }
        Spacer(Modifier.height(12.dp))
        Text("Maximum HR", color = Muted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Choice("Estimate", current.maximumHeartRateMode == MaximumHeartRateMode.AGE_ESTIMATE) {
                scope.launch { repository.setMaximumHeartRateMode(MaximumHeartRateMode.AGE_ESTIMATE) }
            }
            Choice("Manual", current.maximumHeartRateMode == MaximumHeartRateMode.MANUAL) {
                scope.launch { repository.setMaximumHeartRateMode(MaximumHeartRateMode.MANUAL) }
            }
        }
        if (current.maximumHeartRateMode == MaximumHeartRateMode.MANUAL) {
            SettingStepper("Max bpm", current.manualMaximumHeartRate ?: 190, 100, 240) {
                scope.launch { repository.setManualMaximumHeartRate(it) }
            }
        } else {
            Text("Effective: ${current.effectiveMaximumHeartRate ?: "—"} bpm", color = White, fontSize = 12.sp)
        }
        Spacer(Modifier.height(12.dp))
        Text("Pace smoothing", color = Muted, fontSize = 12.sp)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            listOf(3, 5, 10).forEach { seconds ->
                Choice("${seconds}s", current.paceWindowSeconds == seconds) {
                    scope.launch { repository.setPaceWindowSeconds(seconds) }
                }
            }
        }
        Spacer(Modifier.height(12.dp))
        Text(
            "Auto-pause: ${if (capabilities.autoPauseSupported) "supported, coming later" else "not reported"}",
            color = Muted,
            fontSize = 11.sp,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(12.dp))
        WideAction("BACK", Color(0xFF333333), White, onClick = onBack)
    }
}

@Composable
private fun SettingStepper(label: String, value: Int, minimum: Int, maximum: Int, onValue: (Int) -> Unit) {
    Text(label, color = Muted, fontSize = 12.sp)
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(16.dp)) {
        RoundAction("−", Color(0xFF292929), White, { onValue((value - 1).coerceAtLeast(minimum)) }, 48)
        Text(value.toString(), color = White, fontSize = 22.sp, fontWeight = FontWeight.Bold)
        RoundAction("+", Color(0xFF292929), White, { onValue((value + 1).coerceAtMost(maximum)) }, 48)
    }
}

@Composable
private fun Choice(label: String, selected: Boolean, onClick: () -> Unit) {
    Box(
        Modifier.background(if (selected) Green else Color(0xFF292929), RoundedCornerShape(18.dp))
            .clickable(role = Role.RadioButton, onClick = onClick).padding(horizontal = 12.dp, vertical = 8.dp),
    ) {
        Text(label, color = if (selected) Black else White, fontSize = 12.sp)
    }
}

@Composable
private fun SummaryRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 6.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = Muted, fontSize = 13.sp)
        Text(value, color = White, fontSize = 14.sp, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun MessageOverlay(message: String, onDismiss: () -> Unit) {
    Box(Modifier.fillMaxSize().background(Color(0xEE000000)).clickable(onClick = onDismiss), contentAlignment = Alignment.Center) {
        Column(Modifier.padding(42.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Text(message, color = White, textAlign = TextAlign.Center)
            Spacer(Modifier.height(14.dp))
            Text("Tap to close", color = Green, fontSize = 12.sp)
        }
    }
}

@Composable
private fun WideAction(
    label: String,
    background: Color,
    foreground: Color,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    Box(
        modifier.fillMaxWidth().height(52.dp).background(background, RoundedCornerShape(28.dp))
            .clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = foreground, fontWeight = FontWeight.Bold, fontSize = 15.sp)
    }
}

@Composable
private fun RoundAction(
    label: String,
    background: Color,
    foreground: Color,
    onClick: () -> Unit,
    size: Int,
) {
    Box(
        Modifier.size(size.dp).background(background, CircleShape).clickable(role = Role.Button, onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        Text(label, color = foreground, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
    }
}

private fun zoneColor(zone: HeartRateZone): Color = when (zone) {
    HeartRateZone.BELOW -> Muted
    else -> ZoneColors[zone.number - 1]
}

private fun yesNo(value: Boolean): String = if (value) "yes" else "no"
