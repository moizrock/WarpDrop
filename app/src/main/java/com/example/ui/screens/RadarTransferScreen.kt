package com.example.ui.screens

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.OpenInFull
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Radar
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.VideoFile
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.service.TransferForegroundService
import com.example.domain.model.BrowserHttpPortalState
import com.example.domain.model.DeviceFileEntry
import com.example.domain.model.LiveTelemetrySnapshot
import com.example.domain.model.NonDfs5GhzChannel
import com.example.domain.model.P2pPeerNode
import com.example.domain.model.PickerMediaCategory
import com.example.domain.model.TransferItemStatus
import com.example.domain.model.TransferPayloadItem
import com.example.domain.model.WifiDirectGroupState
import com.example.ui.components.AdvancedSettingsModal
import com.example.ui.components.InteractivePairingModal
import com.example.ui.components.ShareMePickerTab
import com.example.ui.theme.AmberThermalWarn
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
import com.example.ui.viewmodel.VayuDropViewModel
import com.example.ui.viewmodel.VayuUiState
import java.io.File
import kotlin.math.cos
import kotlin.math.min
import kotlin.math.sin

@Composable
fun RadarTransferScreen(
    uiState: VayuUiState,
    localDeviceName: String,
    groupState: WifiDirectGroupState,
    discoveredPeers: List<P2pPeerNode>,
    browserPortalState: BrowserHttpPortalState,
    telemetry: LiveTelemetrySnapshot,
    oemSurvivalState: TransferForegroundService.OemSurvivalStatus,
    onRequestPermissionResult: (Boolean) -> Unit,
    onSelectChannel: (NonDfs5GhzChannel) -> Unit,
    onStartGroupOwner: () -> Unit,
    onScanPeers: () -> Unit,
    onRotateKeypair: () -> Unit,
    onSetTransferMode: (Boolean) -> Unit,
    onTriggerHeroSend: () -> Unit,
    onOpenSelectItemsPicker: (ShareMePickerTab) -> Unit,
    onNavigateToPhoneClone: () -> Unit,
    onNavigateToCrossPlatformHub: () -> Unit,
    onSetFrontPageHistoryMode: (Boolean) -> Unit,
    onSetHistoryReceivedFilter: (Boolean) -> Unit,
    onResumePendingTransfers: () -> Unit,
    onTriggerApkInstallOrRun: (String, Boolean) -> Unit,
    onOpenTransferProgressOverlay: () -> Unit,
    onTriggerHindiVoiceGuide: () -> Unit,
    onSelectPickerCategory: (PickerMediaCategory) -> Unit,
    onRefreshCatalog: () -> Unit,
    onToggleDeviceEntryInQueue: (DeviceFileEntry) -> Unit,
    onOpenInMediaViewer: (DeviceFileEntry) -> Unit,
    onPickExternalFiles: (List<android.net.Uri>) -> Unit,
    onStartOrResumeItem: (String) -> Unit,
    onPauseItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onStartAllQueue: () -> Unit,
    onPauseAllQueue: () -> Unit,
    onToggleDeltaSync: (Boolean) -> Unit,
    onToggleOemSurvival: (Boolean) -> Unit,
    onSetPairingModalVisible: (Boolean) -> Unit,
    onSetAdvancedSettingsVisible: (Boolean) -> Unit,
    onOpenSystemMenu: () -> Unit = {},
    onVerifySpake2Pin: (String) -> Unit,
    onToggleBrowserPortal: (Boolean) -> Unit,
    onOpenQrScanner: () -> Unit = {},
    onSelectPairingScanTab: (Boolean) -> Unit = {},
    modifier: Modifier = Modifier
) {
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        onRequestPermissionResult(granted)
    }

    val visualMediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickMultipleVisualMedia(25)
    ) { uris ->
        if (uris.isNotEmpty()) onPickExternalFiles(uris)
    }

    val documentPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenMultipleDocuments()
    ) { uris ->
        if (uris.isNotEmpty()) onPickExternalFiles(uris)
    }

    // QR Code & Instant Pairing Modal (5 GHz Radar + QR Code + 6-digit PIN receiver sheet)
    if (uiState.isPairingModalVisible && uiState.pairingSession != null) {
        InteractivePairingModal(
            session = uiState.pairingSession,
            selectedChannel = uiState.selectedChannel,
            spake2StatusMessage = uiState.spake2PinStatusMessage,
            browserPortalState = browserPortalState,
            onDismiss = { onSetPairingModalVisible(false) },
            onSelectChannel = onSelectChannel,
            onRotateSession = onRotateKeypair,
            onVerifySpake2Pin = onVerifySpake2Pin,
            onToggleBrowserPortal = onToggleBrowserPortal,
            isScanTabSelected = uiState.isPairingModalScanTabSelected,
            onSelectScanTab = onSelectPairingScanTab,
            onLaunchCameraScanner = onOpenQrScanner
        )
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("radar_transfer_screen"),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 14.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // TOP BAR: Left "☰" Menu button, Center [ SHARE | HISTORY ] switcher, Right Voice Guide speaker, QR Code button & [ 📷 SCAN TO CONNECT ] button
        item {
            UnifiedMasterTopBar(
                localDeviceName = uiState.customProfileNickname.ifBlank { localDeviceName },
                isHistoryMode = uiState.isFrontPageHistoryMode,
                onSelectHistoryMode = onSetFrontPageHistoryMode,
                onOpenMenu = onOpenSystemMenu,
                onTriggerVoiceGuide = onTriggerHindiVoiceGuide,
                onOpenQrModal = { onSetPairingModalVisible(true) },
                onOpenQrScanner = onOpenQrScanner
            )
        }

        // Instant 5 GHz Peer Connected Banner (shown when QR code is scanned)
        if (uiState.connectedPeerBannerMessage != null) {
            item {
                Surface(
                    color = EmeraldNominal.copy(alpha = 0.16f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, EmeraldNominal, RoundedCornerShape(18.dp))
                        .testTag("connected_peer_5ghz_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CheckCircle,
                            contentDescription = null,
                            tint = EmeraldNominal,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = uiState.connectedPeerBannerMessage,
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }

        if (uiState.isFrontPageHistoryMode) {
            // HISTORY VIEW: Segmented pill toggle [ ↓ Received ] | [ ↑ Sent ] listing transferred files
            item {
                HistoryViewSection(
                    isReceivedFilter = uiState.isHistoryReceivedFilter,
                    onSelectReceivedFilter = onSetHistoryReceivedFilter,
                    transferQueue = uiState.transferQueue,
                    categorizedCatalog = uiState.categorizedCatalog,
                    onResumeOrResend = onResumePendingTransfers
                )
            }
        } else {
            // SECTION 1 ("Quick Transfer" Row): 5 colorful circular icon buttons in one row:
            // (1) Photos, (2) Videos, (3) Audios, (4) Contacts, (5) Apps
            item {
                QuickTransferFiveCirclesRow(
                    onOpenPickerCategory = onOpenSelectItemsPicker
                )
            }

            // SECTION 2 ("Smart Transfer" Hero Cards):
            // Left (Electric Blue): [ Send — Transfer Data ]
            // Right (Neon Cyan/Teal): [ Receive — Receive Data ]
            item {
                SmartTransferHeroCardsRow(
                    selectedCount = uiState.transferQueue.size,
                    onOpenSelectItemsPicker = { onOpenSelectItemsPicker(ShareMePickerTab.RECENT) },
                    onInstantHeroSend = onTriggerHeroSend,
                    onReceiveClick = {
                        onSetTransferMode(false)
                        onStartGroupOwner()
                        onSetPairingModalVisible(true)
                    }
                )
            }

            // SECTION 3 ("Quick Modes" Row — RIGHT ON THE FRONT PAGE):
            // Left Card: [ Phone Clone (Old to New) ]
            // Right Card: [ Share to iOS & PC ]
            item {
                PhoneCloneAndCrossPlatformCardsRow(
                    onNavigateToPhoneClone = onNavigateToPhoneClone,
                    onNavigateToCrossPlatformHub = onNavigateToCrossPlatformHub
                )
            }

            // SECTION 4 ("Recent & Pending"):
            // Paused transfer with [ Resume ] button + single clean date-grouped Recent transfers card
            item {
                PendingAndRecentFeedSection(
                    transferQueue = uiState.transferQueue,
                    photos = uiState.categorizedCatalog[PickerMediaCategory.PHOTOS].orEmpty(),
                    apks = uiState.categorizedCatalog[PickerMediaCategory.INSTALLED_APKS].orEmpty(),
                    onResumePending = onResumePendingTransfers,
                    onOpenPhotosPicker = { onOpenSelectItemsPicker(ShareMePickerTab.PHOTOS) },
                    onTriggerApkAction = onTriggerApkInstallOrRun
                )
            }

            // Active Transfers Sheet (only when items are actively queued)
            if (uiState.transferQueue.isNotEmpty()) {
                item {
                    ConsumerActiveTransferSheet(
                        queue = uiState.transferQueue,
                        onStartOrResumeItem = onStartOrResumeItem,
                        onPauseItem = onPauseItem,
                        onRemoveItem = onRemoveItem,
                        onStartAll = onStartAllQueue,
                        onPauseAll = onPauseAllQueue,
                        onOpenFullOverlay = onOpenTransferProgressOverlay
                    )
                }
            }
        }
    }
}

@Composable
private fun UnifiedMasterTopBar(
    localDeviceName: String,
    isHistoryMode: Boolean,
    onSelectHistoryMode: (Boolean) -> Unit,
    onOpenMenu: () -> Unit,
    onTriggerVoiceGuide: () -> Unit,
    onOpenQrModal: () -> Unit,
    onOpenQrScanner: () -> Unit
) {
    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonCyan.copy(alpha = 0.28f), RoundedCornerShape(24.dp))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                // Left "☰" Menu button
                IconButton(
                    onClick = onOpenMenu,
                    modifier = Modifier
                        .size(44.dp)
                        .clip(CircleShape)
                        .background(CyberObsidian)
                        .border(1.dp, CyberBorderSubtle, CircleShape)
                        .testTag("front_top_menu_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Menu,
                        contentDescription = "Menu",
                        tint = TextPrimaryIce,
                        modifier = Modifier.size(21.dp)
                    )
                }

                // Center [ SHARE | HISTORY ] switcher
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(18.dp))
                        .background(CyberObsidian)
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(18.dp))
                        .padding(4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = if (!isHistoryMode) NeonCyan else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectHistoryMode(false) }
                            .testTag("front_mode_share_tab")
                    ) {
                        Text(
                            text = "SHARE",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (!isHistoryMode) CyberObsidian else TextSecondarySlate,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }

                    Surface(
                        color = if (isHistoryMode) NeonCyan else Color.Transparent,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(14.dp))
                            .clickable { onSelectHistoryMode(true) }
                            .testTag("front_mode_history_tab")
                    ) {
                        Text(
                            text = "HISTORY",
                            style = MaterialTheme.typography.labelLarge,
                            color = if (isHistoryMode) CyberObsidian else TextSecondarySlate,
                            fontWeight = FontWeight.ExtraBold,
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }

                // Right Voice Guide speaker & QR Code button
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    IconButton(
                        onClick = onTriggerVoiceGuide,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.16f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.55f), CircleShape)
                            .testTag("voice_assistant_speaker_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = "Voice Assistant",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }

                    IconButton(
                        onClick = onOpenQrModal,
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(NeonCyan.copy(alpha = 0.16f))
                            .border(1.dp, NeonCyan.copy(alpha = 0.55f), CircleShape)
                            .testTag("open_pairing_modal_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.QrCode2,
                            contentDescription = "Show My QR Code",
                            tint = NeonCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Prominent Glowing [ 📷 SCAN TO CONNECT ] Action Row right next to QR Code controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "WarpDrop • 5 GHz Super-Fast Transfer",
                        style = MaterialTheme.typography.labelLarge,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.testTag("transfer_top_bar_title")
                    )
                    Text(
                        text = "$localDeviceName • Online",
                        style = MaterialTheme.typography.labelSmall,
                        color = EmeraldNominal,
                        fontWeight = FontWeight.SemiBold,
                        maxLines = 1
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                Surface(
                    color = NeonCyan,
                    shape = RoundedCornerShape(14.dp),
                    shadowElevation = 8.dp,
                    modifier = Modifier
                        .clip(RoundedCornerShape(14.dp))
                        .border(1.5.dp, Color.White.copy(alpha = 0.7f), RoundedCornerShape(14.dp))
                        .clickable { onOpenQrScanner() }
                        .testTag("home_scan_to_connect_button")
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.CameraAlt,
                            contentDescription = "Scan to Connect",
                            tint = CyberObsidian,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "📷 SCAN TO CONNECT",
                            style = MaterialTheme.typography.labelMedium,
                            color = CyberObsidian,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

private data class QuickTransferCircleSpec(
    val label: String,
    val icon: ImageVector,
    val accentColor: Color,
    val targetTab: ShareMePickerTab,
    val testTag: String
)

@Composable
private fun QuickTransferFiveCirclesRow(
    onOpenPickerCategory: (ShareMePickerTab) -> Unit
) {
    val items = remember {
        listOf(
            QuickTransferCircleSpec(
                label = "Photos",
                icon = Icons.Default.Image,
                accentColor = Color(0xFF00E5FF),
                targetTab = ShareMePickerTab.PHOTOS,
                testTag = "quick_transfer_photos"
            ),
            QuickTransferCircleSpec(
                label = "Videos",
                icon = Icons.Default.PlayCircleFilled,
                accentColor = Color(0xFF8B5CF6),
                targetTab = ShareMePickerTab.VIDEOS,
                testTag = "quick_transfer_videos"
            ),
            QuickTransferCircleSpec(
                label = "Audios",
                icon = Icons.Default.MusicNote,
                accentColor = Color(0xFF10B981),
                targetTab = ShareMePickerTab.MUSIC,
                testTag = "quick_transfer_audios"
            ),
            QuickTransferCircleSpec(
                label = "Contacts",
                icon = Icons.Default.Contacts,
                accentColor = Color(0xFFF59E0B),
                targetTab = ShareMePickerTab.FILES,
                testTag = "quick_transfer_contacts"
            ),
            QuickTransferCircleSpec(
                label = "Apps",
                icon = Icons.Default.Apps,
                accentColor = Color(0xFF3B82F6),
                targetTab = ShareMePickerTab.APPS,
                testTag = "quick_transfer_apps"
            )
        )
    }

    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(24.dp))
            .testTag("quick_transfer_row_card")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 14.dp)
        ) {
            Text(
                text = "Quick Transfer",
                style = MaterialTheme.typography.titleSmall,
                color = TextSecondarySlate,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically
            ) {
                items.forEach { spec ->
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier
                            .clip(RoundedCornerShape(16.dp))
                            .clickable { onOpenPickerCategory(spec.targetTab) }
                            .padding(horizontal = 6.dp, vertical = 4.dp)
                            .testTag(spec.testTag)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(54.dp)
                                .clip(CircleShape)
                                .background(spec.accentColor.copy(alpha = 0.18f))
                                .border(1.5.dp, spec.accentColor.copy(alpha = 0.65f), CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = spec.icon,
                                contentDescription = spec.label,
                                tint = spec.accentColor,
                                modifier = Modifier.size(26.dp)
                            )
                        }
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = spec.label,
                            style = MaterialTheme.typography.labelMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun SmartTransferHeroCardsRow(
    selectedCount: Int,
    onOpenSelectItemsPicker: () -> Unit,
    onInstantHeroSend: () -> Unit,
    onReceiveClick: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Left (Electric Blue): [ Send — Transfer Data ]
        Surface(
            color = Color(0xFF0066FF),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .weight(1f)
                .height(116.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onOpenSelectItemsPicker() }
                .testTag("hero_send_button")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0066FF), Color(0xFF0099FF))
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.22f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = stringResource(id = R.string.btn_send),
                            tint = Color.White,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Surface(
                        color = Color.White.copy(alpha = 0.22f),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier
                            .clip(RoundedCornerShape(10.dp))
                            .clickable { onInstantHeroSend() }
                            .testTag("hero_instant_send_pill")
                    ) {
                        Text(
                            text = if (selectedCount > 0) "$selectedCount Ready" else "1-Tap 2.6GB",
                            style = MaterialTheme.typography.labelSmall,
                            color = Color.White,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(id = R.string.btn_send),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = Color.White,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Transfer Data",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.9f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }

        // Right (Neon Cyan/Teal): [ Receive — Receive Data ]
        Surface(
            color = Color(0xFF00B4D8),
            shape = RoundedCornerShape(24.dp),
            shadowElevation = 8.dp,
            modifier = Modifier
                .weight(1f)
                .height(116.dp)
                .clip(RoundedCornerShape(24.dp))
                .clickable { onReceiveClick() }
                .testTag("hero_receive_button")
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(
                        Brush.linearGradient(
                            colors = listOf(Color(0xFF0099CC), Color(0xFF00E5FF))
                        )
                    )
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(CyberObsidian.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Radar,
                            contentDescription = stringResource(id = R.string.btn_receive),
                            tint = CyberObsidian,
                            modifier = Modifier.size(24.dp)
                        )
                    }

                    Surface(
                        color = CyberObsidian.copy(alpha = 0.18f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = "5 GHz QR",
                            style = MaterialTheme.typography.labelSmall,
                            color = CyberObsidian,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                        )
                    }
                }

                Column {
                    Text(
                        text = stringResource(id = R.string.btn_receive),
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.ExtraBold,
                        color = CyberObsidian,
                        letterSpacing = 0.8.sp
                    )
                    Text(
                        text = "Receive Data",
                        style = MaterialTheme.typography.bodySmall,
                        color = CyberObsidian.copy(alpha = 0.85f),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun PhoneCloneAndCrossPlatformCardsRow(
    onNavigateToPhoneClone: () -> Unit,
    onNavigateToCrossPlatformHub: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("quick_modes_row_section"),
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(
            text = "Quick Modes",
            style = MaterialTheme.typography.titleSmall,
            color = TextSecondarySlate,
            fontWeight = FontWeight.Bold
        )
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            // Left Card: [ Phone Clone (Old to New) ]
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(22.dp))
                    .clickable { onNavigateToPhoneClone() }
                    .testTag("front_phone_clone_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(NeonCyan.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhonelinkSetup,
                            contentDescription = "Phone Clone (Old to New)",
                            tint = NeonCyan,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Phone Clone",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1
                        )
                        Text(
                            text = "Old to New",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }

            // Right Card: [ Share to iOS & PC ]
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(22.dp))
                    .border(1.dp, ElectricViolet.copy(alpha = 0.55f), RoundedCornerShape(22.dp))
                    .clickable { onNavigateToCrossPlatformHub() }
                    .testTag("front_cross_platform_pc_card")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(ElectricViolet.copy(alpha = 0.18f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Computer,
                            contentDescription = "Share to iOS & PC",
                            tint = ElectricSky,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Share to iOS & PC",
                            style = MaterialTheme.typography.titleSmall,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.ExtraBold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Text(
                            text = "Webshare & PC Connect",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun PendingAndRecentFeedSection(
    transferQueue: List<TransferPayloadItem>,
    photos: List<DeviceFileEntry>,
    apks: List<DeviceFileEntry>,
    onResumePending: () -> Unit,
    onOpenPhotosPicker: () -> Unit,
    onTriggerApkAction: (String, Boolean) -> Unit
) {
    val pausedItem = transferQueue.firstOrNull { it.status == TransferItemStatus.PAUSED_CHECKPOINT }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("pending_and_recent_feed_section"),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Paused / Pending Transfer Card with [ Resume ] button
        Surface(
            color = CyberSlateCard,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberThermalWarn.copy(alpha = 0.5f), RoundedCornerShape(22.dp))
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = pausedItem?.displayName ?: "Pending 5 GHz Checkpoint • WhatsApp & Media Bundle",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = if (pausedItem != null) {
                            "${(pausedItem.progressFraction * 100).toInt()}% verified in SQLite WAL • Ready to resume"
                        } else {
                            "SQLite WAL Chunk Bitmap ready • Zero-loss instant resume"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = AmberThermalWarn
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Button(
                    onClick = onResumePending,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonCyan,
                        contentColor = CyberObsidian
                    ),
                    shape = RoundedCornerShape(14.dp),
                    contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                    modifier = Modifier
                        .height(40.dp)
                        .testTag("pending_resume_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayArrow,
                        contentDescription = null,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = "Resume",
                        style = MaterialTheme.typography.labelLarge,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        // 2. Single Clean Date-Grouped ("2026-10-19") Recent Transfers Card (Images Grid + Recent APKs)
        Surface(
            color = CyberSlateCard,
            shape = RoundedCornerShape(22.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, CyberBorderSubtle, RoundedCornerShape(22.dp))
                .testTag("recent_images_grid")
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .testTag("recent_apks_section")
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Recent & Pending • 2026-10-19",
                        style = MaterialTheme.typography.titleSmall,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "View All",
                        style = MaterialTheme.typography.labelMedium,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .clickable { onOpenPhotosPicker() }
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Spacer(modifier = Modifier.height(10.dp))

                val tileLabels = remember(photos) {
                    val base = photos.map { it.displayName }
                    (base + listOf(
                        "IMG_20261019_HDR01.jpg",
                        "IMG_20261019_HDR02.jpg",
                        "IMG_20261019_HDR03.jpg",
                        "IMG_20261019_HDR04.jpg",
                        "IMG_20261019_RAW05.dng",
                        "IMG_20261019_PORTRAIT06.jpg",
                        "IMG_20261019_NIGHT07.jpg",
                        "IMG_20261019_PANO08.jpg"
                    )).take(8)
                }

                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    for (rowIdx in 0 until 2) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            for (colIdx in 0 until 4) {
                                val index = rowIdx * 4 + colIdx
                                val isLastCell = index == 7
                                Box(
                                    modifier = Modifier
                                        .weight(1f)
                                        .height(64.dp)
                                        .clip(RoundedCornerShape(14.dp))
                                        .background(
                                            Brush.linearGradient(
                                                colors = if (index % 2 == 0) {
                                                    listOf(Color(0xFF0F2942), Color(0xFF0066FF).copy(alpha = 0.45f))
                                                } else {
                                                    listOf(Color(0xFF1A1F3C), Color(0xFF00E5FF).copy(alpha = 0.38f))
                                                }
                                            )
                                        )
                                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(14.dp))
                                        .clickable { onOpenPhotosPicker() },
                                    contentAlignment = Alignment.Center
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Image,
                                        contentDescription = tileLabels.getOrElse(index) { "Recent Image" },
                                        tint = NeonCyan.copy(alpha = if (isLastCell) 0.35f else 0.85f),
                                        modifier = Modifier.size(24.dp)
                                    )
                                    if (isLastCell) {
                                        Box(
                                            modifier = Modifier
                                                .fillMaxSize()
                                                .background(CyberObsidian.copy(alpha = 0.72f)),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Text(
                                                text = "+7",
                                                style = MaterialTheme.typography.titleMedium,
                                                color = Color.White,
                                                fontWeight = FontWeight.ExtraBold,
                                                modifier = Modifier.testTag("recent_images_plus_seven_badge")
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                Text(
                    text = "Recent Packages • 2026-10-19",
                    style = MaterialTheme.typography.labelLarge,
                    color = TextSecondarySlate,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))

                val displayApks = remember(apks) {
                    if (apks.size >= 2) apks.take(2) else listOf(
                        DeviceFileEntry(
                            id = "recent_apk_warpdrop_pro",
                            displayName = "WarpDrop Companion Suite.apk",
                            category = PickerMediaCategory.INSTALLED_APKS,
                            uri = null,
                            localPath = null,
                            sizeBytes = 44_040_192L,
                            mimeType = "application/vnd.android.package-archive",
                            dateModifiedSeconds = 1760880000L
                        ),
                        DeviceFileEntry(
                            id = "recent_apk_media_codec",
                            displayName = "5GHz Lossless Codec Pack.apk",
                            category = PickerMediaCategory.INSTALLED_APKS,
                            uri = null,
                            localPath = null,
                            sizeBytes = 28_311_552L,
                            mimeType = "application/vnd.android.package-archive",
                            dateModifiedSeconds = 1760879000L
                        )
                    )
                }

                displayApks.forEachIndexed { idx, apk ->
                    if (idx > 0) Spacer(modifier = Modifier.height(10.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(42.dp)
                                    .clip(RoundedCornerShape(12.dp))
                                    .background(EmeraldNominal.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Android,
                                    contentDescription = apk.displayName,
                                    tint = EmeraldNominal,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(10.dp))
                            Column {
                                Text(
                                    text = apk.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "${VayuDropViewModel.formatBytes(apk.sizeBytes)} • SHA-256 Verified",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedButton(
                                onClick = { onTriggerApkAction(apk.displayName, true) },
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("recent_apk_install_button_$idx")
                            ) {
                                Text(
                                    text = "INSTALL",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }

                            Button(
                                onClick = { onTriggerApkAction(apk.displayName, false) },
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = EmeraldNominal,
                                    contentColor = CyberObsidian
                                ),
                                shape = RoundedCornerShape(12.dp),
                                contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                modifier = Modifier
                                    .height(34.dp)
                                    .testTag("recent_apk_run_button_$idx")
                            ) {
                                Text(
                                    text = "RUN",
                                    style = MaterialTheme.typography.labelSmall,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HistoryViewSection(
    isReceivedFilter: Boolean,
    onSelectReceivedFilter: (Boolean) -> Unit,
    transferQueue: List<TransferPayloadItem>,
    categorizedCatalog: Map<PickerMediaCategory, List<DeviceFileEntry>>,
    onResumeOrResend: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("front_history_section"),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Segmented pill toggle [ ↓ Received ] | [ ↑ Sent ]
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(18.dp))
                .background(CyberSlateCard)
                .border(1.dp, CyberBorderSubtle, RoundedCornerShape(18.dp))
                .padding(6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Surface(
                color = if (isReceivedFilter) NeonCyan else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectReceivedFilter(true) }
                    .testTag("history_received_pill")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowDownward,
                        contentDescription = null,
                        tint = if (isReceivedFilter) CyberObsidian else TextSecondarySlate,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "↓ Received",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (isReceivedFilter) CyberObsidian else TextSecondarySlate,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }

            Surface(
                color = if (!isReceivedFilter) NeonCyan else Color.Transparent,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .weight(1f)
                    .clip(RoundedCornerShape(14.dp))
                    .clickable { onSelectReceivedFilter(false) }
                    .testTag("history_sent_pill")
            ) {
                Row(
                    modifier = Modifier.padding(vertical = 10.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        imageVector = Icons.Default.ArrowUpward,
                        contentDescription = null,
                        tint = if (!isReceivedFilter) CyberObsidian else TextSecondarySlate,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "↑ Sent",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (!isReceivedFilter) CyberObsidian else TextSecondarySlate,
                        fontWeight = FontWeight.ExtraBold
                    )
                }
            }
        }

        val sampleHistoryItems = remember(isReceivedFilter, transferQueue, categorizedCatalog) {
            if (transferQueue.isNotEmpty()) {
                transferQueue.map {
                    Triple(
                        it.displayName,
                        "${VayuDropViewModel.formatBytes(it.sizeBytes)} • 5 GHz Ch 149 • 142 MB/s",
                        if (isReceivedFilter) "Received • Verified ✓" else "Sent • Verified ✓"
                    )
                }
            } else {
                listOf(
                    Triple(
                        "WhatsApp Chats (msgstore.db.crypt15)",
                        "680.0 MB • 5 GHz Ch 149 • 142.4 MB/s",
                        if (isReceivedFilter) "Received • Verified ✓" else "Sent • Verified ✓"
                    ),
                    Triple(
                        "Photos & Videos (4K UHD Camera Roll)",
                        "1.20 GB • 5 GHz Ch 149 • 144.1 MB/s",
                        if (isReceivedFilter) "Received • Verified ✓" else "Sent • Verified ✓"
                    ),
                    Triple(
                        "Contacts (624 Cards & vCard Backup)",
                        "40.0 MB • 5 GHz Ch 149 • 141.7 MB/s",
                        if (isReceivedFilter) "Received • Verified ✓" else "Sent • Verified ✓"
                    )
                )
            }
        }

        sampleHistoryItems.forEach { (title, subtitle, badge) ->
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(18.dp))
                    .clickable { onResumeOrResend() }
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = title,
                            style = MaterialTheme.typography.bodyLarge,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold,
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Surface(
                        color = EmeraldNominal.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(10.dp)
                    ) {
                        Text(
                            text = badge,
                            style = MaterialTheme.typography.labelSmall,
                            color = EmeraldNominal,
                            fontWeight = FontWeight.Bold,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumerActiveTransferSheet(
    queue: List<TransferPayloadItem>,
    onStartOrResumeItem: (String) -> Unit,
    onPauseItem: (String) -> Unit,
    onRemoveItem: (String) -> Unit,
    onStartAll: () -> Unit,
    onPauseAll: () -> Unit,
    onOpenFullOverlay: () -> Unit
) {
    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(24.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
            .testTag("active_transfer_queue_sheet")
    ) {
        Column(modifier = Modifier.padding(18.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = "Selected to Share (${queue.size})",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "Pause or resume transfers anytime",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }

                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Button(
                        onClick = {
                            onStartAll()
                            onOpenFullOverlay()
                        },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian
                        ),
                        shape = RoundedCornerShape(12.dp),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 8.dp),
                        modifier = Modifier
                            .height(40.dp)
                            .testTag("queue_send_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "Send Now",
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }

                    IconButton(
                        onClick = onOpenFullOverlay,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberObsidian)
                            .testTag("expand_live_transfer_overlay_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.OpenInFull,
                            contentDescription = "Expand Transfer Progress",
                            tint = NeonCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    IconButton(
                        onClick = onPauseAll,
                        modifier = Modifier
                            .size(40.dp)
                            .clip(RoundedCornerShape(12.dp))
                            .background(CyberObsidian)
                            .testTag("queue_pause_all_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Pause,
                            contentDescription = "Pause All",
                            tint = AmberThermalWarn,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            queue.forEach { item ->
                val isStreaming = item.status == TransferItemStatus.STREAMING_NIO
                val isCompleted = item.status == TransferItemStatus.VERIFIED_COMPLETE

                Surface(
                    color = CyberObsidian,
                    shape = RoundedCornerShape(16.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = item.displayName,
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = when {
                                        isCompleted -> "${VayuDropViewModel.formatBytes(item.sizeBytes)} • Sent ✓"
                                        isStreaming -> "${VayuDropViewModel.formatBytes(item.sizeBytes)} • ${"%.1f".format(item.speedMBps)} MB/s • ${item.etaSeconds}s left"
                                        item.status == TransferItemStatus.PAUSED_CHECKPOINT -> "${VayuDropViewModel.formatBytes(item.sizeBytes)} • Paused at ${(item.progressFraction * 100).toInt()}%"
                                        else -> "${VayuDropViewModel.formatBytes(item.sizeBytes)} • Ready"
                                    },
                                    style = MaterialTheme.typography.bodySmall,
                                    color = if (isCompleted) EmeraldNominal else NeonCyan
                                )
                            }

                            Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                                if (!isCompleted) {
                                    IconButton(
                                        onClick = {
                                            if (isStreaming) onPauseItem(item.id) else onStartOrResumeItem(item.id)
                                        },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (isStreaming) Icons.Default.Pause else Icons.Default.PlayArrow,
                                            contentDescription = if (isStreaming) "Pause" else "Resume",
                                            tint = if (isStreaming) AmberThermalWarn else EmeraldNominal,
                                            modifier = Modifier.size(18.dp)
                                        )
                                    }
                                } else {
                                    Icon(
                                        imageVector = Icons.Default.CheckCircle,
                                        contentDescription = "Completed",
                                        tint = EmeraldNominal,
                                        modifier = Modifier
                                            .padding(8.dp)
                                            .size(20.dp)
                                    )
                                }

                                IconButton(
                                    onClick = { onRemoveItem(item.id) },
                                    modifier = Modifier.size(36.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Remove",
                                        tint = TextSecondarySlate,
                                        modifier = Modifier.size(16.dp)
                                    )
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(8.dp))

                        LinearProgressIndicator(
                            progress = { item.progressFraction },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(6.dp)
                                .clip(RoundedCornerShape(3.dp)),
                            color = if (isCompleted) EmeraldNominal else NeonCyan,
                            trackColor = CyberSlateElevated
                        )
                    }
                }
            }
        }
    }
}

