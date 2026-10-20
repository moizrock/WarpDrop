package com.example.ui.components

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.BatteryChargingFull
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.DeviceThermostat
import androidx.compose.material.icons.filled.NetworkCheck
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.BottleneckKind
import com.example.domain.model.LiveTelemetrySnapshot
import com.example.domain.model.ThermalStateLevel
import com.example.ui.theme.AmberThermalWarn
import com.example.ui.theme.CrimsonCritical
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSlateCard
import com.example.ui.theme.CyberSlateElevated
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimaryIce
import com.example.ui.theme.TextSecondarySlate
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

/**
 * Persistent Top Dashboard Live Bottleneck & Thermal/Battery HUD Strip.
 * Displays live Battery Temperature (°C), SoC %, 5 GHz Link Speed (Mbps),
 * Thermal Headroom ratio, and Bottleneck status badge.
 */
@Composable
fun LiveBottleneckTopHud(
    telemetry: LiveTelemetrySnapshot,
    modifier: Modifier = Modifier
) {
    val thermalAccent = when (telemetry.thermalLevel) {
        ThermalStateLevel.NONE, ThermalStateLevel.LIGHT -> EmeraldNominal
        ThermalStateLevel.MODERATE -> AmberThermalWarn
        else -> CrimsonCritical
    }

    val bottleneckAccent = when (telemetry.bottleneckKind) {
        BottleneckKind.OPTIMAL_5GHZ -> NeonCyan
        BottleneckKind.WIFI_24GHZ_BAND, BottleneckKind.WEAK_RSSI_SIGNAL -> AmberThermalWarn
        BottleneckKind.THERMAL_HEADROOM_WARN, BottleneckKind.LOW_BATTERY_SAVER -> CrimsonCritical
    }

    Surface(
        modifier = modifier
            .fillMaxWidth()
            .testTag("live_bottleneck_top_hud"),
        color = CyberSlateCard.copy(alpha = 0.94f),
        shape = RoundedCornerShape(16.dp),
        tonalElevation = 6.dp
    ) {
        Column(
            modifier = Modifier
                .border(1.dp, CyberBorderSubtle, RoundedCornerShape(16.dp))
                .padding(horizontal = 14.dp, vertical = 10.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(bottleneckAccent)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "VAYUDROP 5 GHz HUD",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(bottleneckAccent.copy(alpha = 0.16f))
                        .border(1.dp, bottleneckAccent.copy(alpha = 0.5f), RoundedCornerShape(6.dp))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = telemetry.bottleneckKind.badge,
                        style = MaterialTheme.typography.labelSmall,
                        color = bottleneckAccent,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Pill 1: Battery Temp (°C)
                HudMetricPill(
                    icon = Icons.Default.DeviceThermostat,
                    label = "TEMP",
                    value = "%.1f°C".format(telemetry.batteryTempCelsius),
                    accentColor = thermalAccent,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hud_pill_temp")
                )

                // Pill 2: Battery SoC %
                HudMetricPill(
                    icon = Icons.Default.BatteryChargingFull,
                    label = if (telemetry.isCharging) "SoC ⚡" else "SoC",
                    value = "${telemetry.batterySoCPercent}%",
                    accentColor = if (telemetry.batterySoCPercent > 25) EmeraldNominal else AmberThermalWarn,
                    modifier = Modifier
                        .weight(1f)
                        .testTag("hud_pill_soc")
                )

                // Pill 3: 5 GHz Link Speed (Mbps)
                HudMetricPill(
                    icon = Icons.Default.Speed,
                    label = if (telemetry.is5GhzBandActive) "5GHz PHY" else "2.4GHz",
                    value = "${telemetry.wifiLinkSpeedMbps} Mbps",
                    accentColor = if (telemetry.is5GhzBandActive) NeonCyan else AmberThermalWarn,
                    modifier = Modifier
                        .weight(1.25f)
                        .testTag("hud_pill_speed")
                )

                // Pill 4: Thermal Status & Headroom
                HudMetricPill(
                    icon = Icons.Default.Bolt,
                    label = "THERMAL",
                    value = "HR %.2f".format(telemetry.thermalHeadroomRatio),
                    accentColor = thermalAccent,
                    modifier = Modifier
                        .weight(1.1f)
                        .testTag("hud_pill_thermal")
                )
            }
        }
    }
}

@Composable
private fun HudMetricPill(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    value: String,
    accentColor: Color,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(CyberObsidian.copy(alpha = 0.75f))
            .border(1.dp, CyberBorderSubtle.copy(alpha = 0.7f), RoundedCornerShape(10.dp))
            .padding(horizontal = 8.dp, vertical = 6.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = label,
                tint = accentColor,
                modifier = Modifier.size(13.dp)
            )
            Spacer(modifier = Modifier.width(4.dp))
            Text(
                text = label,
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondarySlate,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
        Spacer(modifier = Modifier.height(2.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.titleSmall,
            color = TextPrimaryIce,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

/**
 * Renders the deterministic ISO/IEC 18004 QR Code matrix for the `mp2p://` URI payload
 * using Jetpack Compose Canvas with high-contrast cyber-slate framing.
 */
@Composable
fun Mp2pQrCodeCanvas(
    qrMatrix: List<List<Boolean>>,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .background(Color.White)
            .border(3.dp, NeonCyan, RoundedCornerShape(18.dp))
            .padding(12.dp)
            .testTag("mp2p_qr_code_canvas"),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.size(196.dp)) {
            val matrixSize = qrMatrix.size.coerceAtLeast(1)
            val cellSize = size.width / matrixSize

            for (r in 0 until matrixSize) {
                val rowList = qrMatrix[r]
                for (c in 0 until rowList.size) {
                    if (rowList[c]) {
                        val isFinderCorner = (r < 7 && c < 7) ||
                            (r < 7 && c >= matrixSize - 7) ||
                            (r >= matrixSize - 7 && c < 7)

                        drawRoundRect(
                            color = if (isFinderCorner) Color(0xFF0A192F) else Color(0xFF070B14),
                            topLeft = Offset(c * cellSize, r * cellSize),
                            size = Size(cellSize * 0.96f, cellSize * 0.96f),
                            cornerRadius = CornerRadius(cellSize * 0.18f, cellSize * 0.18f)
                        )
                    }
                }
            }
        }
    }
}

/**
 * Interactive 5 GHz Non-DFS Sonar Radar Visualizer Canvas.
 */
@Composable
fun RadarSonarCanvas(
    peerCount: Int,
    channelNumber: Int,
    frequencyMhz: Int,
    isGroupActive: Boolean,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "radar_sweep")
    val sweepAngle by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 3600, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "sweep_angle"
    )

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.radialGradient(
                    colors = listOf(
                        CyberSlateElevated,
                        CyberObsidian
                    )
                )
            )
            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
            .padding(12.dp),
        contentAlignment = Alignment.Center
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(170.dp)
        ) {
            val center = Offset(size.width / 2f, size.height / 2f)
            val maxRadius = min(size.width, size.height) * 0.46f

            // Concentric 5 GHz sonar rings
            listOf(0.33f, 0.66f, 1.0f).forEach { ratio ->
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.22f),
                    radius = maxRadius * ratio,
                    center = center,
                    style = Stroke(width = 1.5f)
                )
            }

            // Crosshair axes
            drawLine(
                color = ElectricSky.copy(alpha = 0.18f),
                start = Offset(center.x - maxRadius, center.y),
                end = Offset(center.x + maxRadius, center.y),
                strokeWidth = 1f
            )
            drawLine(
                color = ElectricSky.copy(alpha = 0.18f),
                start = Offset(center.x, center.y - maxRadius),
                end = Offset(center.x, center.y + maxRadius),
                strokeWidth = 1f
            )

            // Rotating radar sweep beam
            val rad = Math.toRadians(sweepAngle.toDouble())
            val beamEnd = Offset(
                x = center.x + (maxRadius * cos(rad)).toFloat(),
                y = center.y + (maxRadius * sin(rad)).toFloat()
            )
            drawLine(
                brush = Brush.linearGradient(
                    colors = listOf(NeonCyan, ElectricViolet.copy(alpha = 0.1f)),
                    start = center,
                    end = beamEnd
                ),
                start = center,
                end = beamEnd,
                strokeWidth = 4f,
                cap = StrokeCap.Round
            )

            // Center 5GHz Group Owner Core Node
            drawCircle(
                color = if (isGroupActive) EmeraldNominal else NeonCyan,
                radius = 10f,
                center = center
            )
            drawCircle(
                color = (if (isGroupActive) EmeraldNominal else NeonCyan).copy(alpha = 0.3f),
                radius = 22f,
                center = center
            )

            // Plot discovered peers around the 5GHz ring
            val displayNodes = peerCount.coerceAtLeast(1)
            for (i in 0 until displayNodes) {
                val nodeAngle = Math.toRadians((i * 115.0 + 45.0) % 360.0)
                val dist = maxRadius * (0.58f + (i % 2) * 0.22f)
                val peerOffset = Offset(
                    x = center.x + (dist * cos(nodeAngle)).toFloat(),
                    y = center.y + (dist * sin(nodeAngle)).toFloat()
                )
                drawCircle(
                    color = ElectricViolet,
                    radius = 8f,
                    center = peerOffset
                )
                drawCircle(
                    color = NeonCyan.copy(alpha = 0.45f),
                    radius = 15f,
                    center = peerOffset,
                    style = Stroke(width = 1.5f)
                )
            }
        }

        // Bottom Corner Spectrum Overlay
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.NetworkCheck,
                    contentDescription = "5GHz Spectrum",
                    tint = NeonCyan,
                    modifier = Modifier.size(14.dp)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "CH $channelNumber • $frequencyMhz MHz NON-DFS",
                    style = MaterialTheme.typography.labelSmall,
                    color = NeonCyan
                )
            }

            Text(
                text = if (isGroupActive) "GO ACTIVE (8988)" else "STANDBY SWEEP",
                style = MaterialTheme.typography.labelSmall,
                color = if (isGroupActive) EmeraldNominal else TextSecondarySlate
            )
        }
    }
}
