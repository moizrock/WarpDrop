package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentCopy
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.example.core.service.TransferForegroundService
import com.example.domain.model.BrowserHttpPortalState
import com.example.domain.model.EphemeralPairingSession
import com.example.domain.model.LiveTelemetrySnapshot
import com.example.domain.model.NonDfs5GhzChannel
import com.example.domain.model.StoragePartitionMetrics
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSlateCard
import com.example.ui.theme.CyberSlateElevated
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimaryIce
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.viewmodel.VayuDropViewModel

/**
 * Sleek Consumer QR Code & Instant Connect Modal:
 * - Explicit [←] Back icon in top-left and [X] Close icon in top-right so user is never trapped
 * 1. Instant QR Code scan for nearby devices
 * 2. 6-Digit Pairing Code for quick manual connection
 * 3. Web Browser Receiver toggle for sharing with PCs, Macs & iPhones without an app
 */
@Composable
fun InteractivePairingModal(
    session: EphemeralPairingSession,
    selectedChannel: NonDfs5GhzChannel,
    spake2StatusMessage: String?,
    browserPortalState: BrowserHttpPortalState,
    onDismiss: () -> Unit,
    onSelectChannel: (NonDfs5GhzChannel) -> Unit,
    onRotateSession: () -> Unit,
    onVerifySpake2Pin: (String) -> Unit,
    onToggleBrowserPortal: (Boolean) -> Unit,
    isScanTabSelected: Boolean = false,
    onSelectScanTab: (Boolean) -> Unit = {},
    onLaunchCameraScanner: () -> Unit = {}
) {
    val clipboard = LocalClipboardManager.current
    var enteredPeerPin by remember { mutableStateOf("") }

    BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = CyberSlateCard,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                .testTag("interactive_pairing_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Header with [←] Back (left) and [X] Close (right)
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberObsidian)
                                .testTag("back_pairing_modal_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Connect Nearby Device",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Scan QR code or enter 6-digit PIN",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondarySlate
                            )
                        }
                    }

                    Row {
                        IconButton(
                            onClick = onRotateSession,
                            modifier = Modifier.testTag("modal_rotate_keys_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Refresh,
                                contentDescription = "Refresh Code",
                                tint = ElectricSky
                            )
                        }
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberObsidian)
                                .testTag("close_pairing_modal_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close",
                                tint = TextPrimaryIce
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Top Toggle: [ Show My QR Code ] | [ 📷 Scan Partner's QR ]
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(18.dp))
                        .background(CyberObsidian)
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(4.dp)
                        .testTag("pairing_modal_top_toggle"),
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Surface(
                        color = if (!isScanTabSelected) NeonCyan else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectScanTab(false) }
                            .testTag("pairing_modal_show_qr_tab")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCode2,
                                contentDescription = null,
                                tint = if (!isScanTabSelected) CyberObsidian else TextSecondarySlate,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Show My QR Code",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (!isScanTabSelected) CyberObsidian else TextSecondarySlate,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }

                    Surface(
                        color = if (isScanTabSelected) NeonCyan else androidx.compose.ui.graphics.Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(14.dp))
                            .clickable {
                                onSelectScanTab(true)
                                onLaunchCameraScanner()
                            }
                            .testTag("pairing_modal_scan_qr_tab")
                    ) {
                        Row(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp),
                            horizontalArrangement = Arrangement.Center,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                tint = if (isScanTabSelected) CyberObsidian else NeonCyan,
                                modifier = Modifier.size(16.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "📷 Scan Partner's QR",
                                style = MaterialTheme.typography.labelMedium,
                                color = if (isScanTabSelected) CyberObsidian else TextPrimaryIce,
                                fontWeight = FontWeight.ExtraBold,
                                maxLines = 1
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                if (isScanTabSelected) {
                    Surface(
                        color = CyberObsidian,
                        shape = RoundedCornerShape(22.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.5.dp, NeonCyan, RoundedCornerShape(22.dp))
                            .padding(18.dp)
                    ) {
                        Column(
                            horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.QrCodeScanner,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(48.dp)
                            )
                            Text(
                                text = "Point camera at the QR code on the other phone",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.Bold,
                                textAlign = TextAlign.Center
                            )
                            Button(
                                onClick = onLaunchCameraScanner,
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = CyberObsidian
                                ),
                                shape = RoundedCornerShape(16.dp),
                                contentPadding = PaddingValues(horizontal = 18.dp, vertical = 12.dp),
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .testTag("pairing_modal_open_camera_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CameraAlt,
                                    contentDescription = null,
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(8.dp))
                                Text(
                                    text = "📷 SCAN PARTNER'S QR",
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(14.dp))
                } else {
                    // QR Code Canvas
                    Mp2pQrCodeCanvas(qrMatrix = session.qrMatrix)

                    Spacer(modifier = Modifier.height(12.dp))

                    Text(
                        text = "Point the other phone's camera at this QR code",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(18.dp))
                }

                // 6-Digit Pairing Code Section
                Surface(
                    color = CyberObsidian,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
                        .testTag("spake2_pin_section")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Lock,
                                contentDescription = "Pairing Code",
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "One-Time 6-Digit Pairing Code",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimaryIce
                            )
                        }

                        Spacer(modifier = Modifier.height(10.dp))

                        val formattedPin = session.spake2PinCode.chunked(3).joinToString("  ")
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .background(CyberSlateElevated)
                                .padding(horizontal = 28.dp, vertical = 10.dp)
                        ) {
                            Text(
                                text = formattedPin,
                                fontSize = 26.sp,
                                fontWeight = FontWeight.Bold,
                                color = NeonCyan,
                                letterSpacing = 3.sp,
                                textAlign = TextAlign.Center,
                                modifier = Modifier.testTag("spake2_pin_display")
                            )
                        }

                        Spacer(modifier = Modifier.height(12.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            OutlinedTextField(
                                value = enteredPeerPin,
                                onValueChange = { input ->
                                    if (input.length <= 6 && input.all { it.isDigit() }) {
                                        enteredPeerPin = input
                                    }
                                },
                                placeholder = { Text("Enter partner's 6-digit code") },
                                singleLine = true,
                                shape = RoundedCornerShape(14.dp),
                                colors = OutlinedTextFieldDefaults.colors(
                                    focusedBorderColor = NeonCyan,
                                    unfocusedBorderColor = CyberBorderSubtle,
                                    focusedTextColor = TextPrimaryIce,
                                    unfocusedTextColor = TextPrimaryIce
                                ),
                                modifier = Modifier
                                    .weight(1f)
                                    .testTag("spake2_pin_input")
                            )

                            Button(
                                onClick = {
                                    onVerifySpake2Pin(
                                        enteredPeerPin.ifBlank { session.spake2PinCode }
                                    )
                                },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = NeonCyan,
                                    contentColor = CyberObsidian
                                ),
                                shape = RoundedCornerShape(14.dp),
                                modifier = Modifier
                                    .height(52.dp)
                                    .testTag("verify_spake2_pin_button")
                            ) {
                                Text("Connect", fontWeight = FontWeight.Bold)
                            }
                        }

                        if (spake2StatusMessage != null) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = spake2StatusMessage,
                                style = MaterialTheme.typography.bodySmall,
                                color = EmeraldNominal,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Web Browser Sharing Toggle (PC / Mac / iPhone)
                Surface(
                    color = CyberObsidian,
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(
                            width = 1.dp,
                            color = if (browserPortalState.isRunning) EmeraldNominal else CyberBorderSubtle,
                            shape = RoundedCornerShape(20.dp)
                        )
                        .testTag("browser_http_portal_section")
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.weight(1f)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Language,
                                    contentDescription = "Share via Web Browser",
                                    tint = if (browserPortalState.isRunning) EmeraldNominal else ElectricSky,
                                    modifier = Modifier.size(22.dp)
                                )
                                Spacer(modifier = Modifier.width(10.dp))
                                Column {
                                    Text(
                                        text = "Share with PC, Mac or iPhone via WarpDrop Web",
                                        style = MaterialTheme.typography.titleSmall,
                                        color = TextPrimaryIce
                                    )
                                    Text(
                                        text = "No app needed — download in any browser",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = TextSecondarySlate
                                    )
                                }
                            }

                            Switch(
                                checked = browserPortalState.isRunning,
                                onCheckedChange = onToggleBrowserPortal,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberObsidian,
                                    checkedTrackColor = EmeraldNominal
                                ),
                                modifier = Modifier.testTag("modal_browser_portal_switch")
                            )
                        }

                        if (browserPortalState.isRunning) {
                            Spacer(modifier = Modifier.height(10.dp))
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(CyberSlateCard)
                                    .padding(horizontal = 12.dp, vertical = 10.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Column(modifier = Modifier.weight(1f)) {
                                    Text(
                                        text = browserPortalState.portalUrl,
                                        style = MaterialTheme.typography.titleMedium,
                                        color = EmeraldNominal,
                                        fontWeight = FontWeight.Bold,
                                        maxLines = 1,
                                        overflow = TextOverflow.Ellipsis
                                    )
                                    Text(
                                        text = "Open this link in Chrome or Safari on the same Wi-Fi",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = TextSecondarySlate
                                    )
                                }
                                IconButton(
                                    onClick = {
                                        clipboard.setText(AnnotatedString(browserPortalState.portalUrl))
                                    }
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.ContentCopy,
                                        contentDescription = "Copy Link",
                                        tint = EmeraldNominal,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

/**
 * Hidden "Advanced Settings" Modal containing technical telemetry, high-speed channel selection,
 * smart delta-sync toggle, background transfer keep-alive, and storage diagnostics.
 */
@Composable
fun AdvancedSettingsModal(
    selectedChannel: NonDfs5GhzChannel,
    isDeltaSyncEnabled: Boolean,
    telemetry: LiveTelemetrySnapshot,
    oemSurvivalStatus: TransferForegroundService.OemSurvivalStatus,
    storageMetrics: StoragePartitionMetrics?,
    onSelectChannel: (NonDfs5GhzChannel) -> Unit,
    onToggleDeltaSync: (Boolean) -> Unit,
    onToggleOemSurvival: (Boolean) -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler {
        onDismiss()
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            color = CyberSlateCard,
            shape = RoundedCornerShape(28.dp),
            modifier = Modifier
                .fillMaxWidth(0.94f)
                .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
                .testTag("advanced_settings_modal")
        ) {
            Column(
                modifier = Modifier
                    .padding(22.dp)
                    .verticalScroll(rememberScrollState()),
                verticalArrangement = Arrangement.spacedBy(14.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onDismiss,
                            modifier = Modifier
                                .size(40.dp)
                                .clip(CircleShape)
                                .background(CyberObsidian)
                                .testTag("back_advanced_settings_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = NeonCyan
                            )
                        }
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "Advanced Settings",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "Wireless band, background keep-alive & diagnostics",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondarySlate
                            )
                        }
                    }

                    IconButton(
                        onClick = onDismiss,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(CyberObsidian)
                            .testTag("close_advanced_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Close,
                            contentDescription = "Close Advanced Settings",
                            tint = TextPrimaryIce
                        )
                    }
                }

                // 1. Wireless Band Channel Selection
                Surface(
                    color = CyberObsidian,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text(
                            text = "High-Speed 5 GHz Wireless Band",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimaryIce
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            listOf(NonDfs5GhzChannel.CH_36, NonDfs5GhzChannel.CH_149).forEach { ch ->
                                val isSelected = selectedChannel.channelNumber == ch.channelNumber
                                FilterChip(
                                    selected = isSelected,
                                    onClick = { onSelectChannel(ch) },
                                    label = {
                                        Text(
                                            text = "5 GHz ${ch.bandLabel}",
                                            fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                        )
                                    },
                                    leadingIcon = {
                                        Icon(
                                            imageVector = Icons.Default.Wifi,
                                            contentDescription = null,
                                            tint = if (isSelected) NeonCyan else TextSecondarySlate,
                                            modifier = Modifier.size(16.dp)
                                        )
                                    },
                                    colors = FilterChipDefaults.filterChipColors(
                                        selectedContainerColor = NeonCyan.copy(alpha = 0.2f),
                                        selectedLabelColor = NeonCyan
                                    )
                                )
                            }
                        }
                    }
                }

                // 2. Smart Delta Sync & Background Protection Toggles
                Surface(
                    color = CyberObsidian,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(12.dp)
                    ) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Smart Delta Acceleration",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimaryIce
                                )
                                Text(
                                    text = "Only transfer changed parts of updated files",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                            Switch(
                                checked = isDeltaSyncEnabled,
                                onCheckedChange = onToggleDeltaSync,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberObsidian,
                                    checkedTrackColor = NeonCyan
                                )
                            )
                        }

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = "Keep Transfers Alive When Screen Off",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimaryIce
                                )
                                Text(
                                    text = "Prevents battery savers from interrupting large transfers",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                            Switch(
                                checked = oemSurvivalStatus.isServiceRunning,
                                onCheckedChange = onToggleOemSurvival,
                                colors = SwitchDefaults.colors(
                                    checkedThumbColor = CyberObsidian,
                                    checkedTrackColor = EmeraldNominal
                                )
                            )
                        }
                    }
                }

                // 3. Technical Telemetry Summary
                Surface(
                    color = CyberObsidian,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(18.dp))
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Icon(
                                imageVector = Icons.Default.Speed,
                                contentDescription = null,
                                tint = NeonCyan,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Live Hardware Diagnostics",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimaryIce
                            )
                        }
                        Text(
                            text = "Link Rate: ${telemetry.wifiLinkSpeedMbps} Mbps • Signal: ${telemetry.wifiRssiDbm} dBm",
                            style = MaterialTheme.typography.bodySmall,
                            color = ElectricSky
                        )
                        Text(
                            text = "Battery: ${telemetry.batterySoCPercent}% (${"%.1f".format(telemetry.batteryTempCelsius)} °C) • Thermal: ${telemetry.thermalLevel.label}",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                        if (storageMetrics != null) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(
                                    imageVector = Icons.Default.Storage,
                                    contentDescription = null,
                                    tint = EmeraldNominal,
                                    modifier = Modifier.size(15.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = "Free Space: ${VayuDropViewModel.formatBytes(storageMetrics.dataAvailableBytes)} of ${VayuDropViewModel.formatBytes(storageMetrics.dataTotalBytes)}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = EmeraldNominal
                                )
                            }
                        }
                    }
                }

                // 4. Settings & About Dialog Card
                Surface(
                    color = CyberObsidian,
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                        .testTag("settings_about_dialog_card")
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(
                            text = "About WarpDrop",
                            style = MaterialTheme.typography.titleSmall,
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "WarpDrop v1.0.0 — Built on 5 GHz P2P, X25519 Encryption & BLAKE3 Verified Streaming",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.SemiBold,
                            modifier = Modifier.testTag("settings_about_version_text")
                        )
                    }
                }
            }
        }
    }
}
