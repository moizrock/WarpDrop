package com.example

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.outlined.PhonelinkSetup
import androidx.compose.material.icons.outlined.PlayCircleOutline
import androidx.compose.material.icons.outlined.Shield
import androidx.compose.material.icons.outlined.Speed
import androidx.compose.material.icons.outlined.SwapHoriz
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.compose.ui.platform.LocalContext
import com.example.core.cloud.GoogleAuthManager
import com.example.domain.model.TransferItemStatus
import com.example.domain.model.VayuTab
import com.example.ui.components.CameraQrScannerOverlay
import com.example.ui.components.LiveTransferProgressOverlay
import com.example.ui.components.ShareMeSelectItemsPickerOverlay
import com.example.ui.components.ShareMeSystemMenuOverlay
import com.example.ui.components.SystemMenuScreen
import com.example.ui.components.WarpDropAuthGateScreen
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.example.ui.screens.DuplicateVaultScreen
import com.example.ui.screens.InbuiltHubScreen
import com.example.ui.screens.PhoneCloneScreen
import com.example.ui.screens.RadarTransferScreen
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSlateCard
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimaryIce
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.theme.VayuDropTheme
import com.example.ui.viewmodel.VayuDropViewModel
import kotlinx.coroutines.delay

class MainActivity : ComponentActivity() {

    private val viewModel: VayuDropViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        handleIncomingMp2pDeepLink(intent)

        setContent {
            VayuDropTheme {
                WarpDropRootAuthGate(viewModel = viewModel)
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleIncomingMp2pDeepLink(intent)
    }

    private fun handleIncomingMp2pDeepLink(intent: Intent?) {
        val dataUri = intent?.data?.toString()
        if (dataUri != null && dataUri.startsWith("mp2p://")) {
            viewModel.setTransferMode(false)
            viewModel.selectTab(VayuTab.TRANSFER)
            viewModel.verifyAndConnectMp2pUri(dataUri)
        }
    }
}

@Composable
fun WarpDropRootAuthGate(
    viewModel: VayuDropViewModel
) {
    val context = LocalContext.current
    var isSessionAuthorized by remember {
        mutableStateOf(GoogleAuthManager.isSessionAuthorizedOrSkipped(context))
    }
    var showSplashLogoOverlay by rememberSaveable { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        delay(1500L)
        showSplashLogoOverlay = false
    }

    DisposableEffect(context) {
        val authInstance = runCatching { Firebase.auth }.getOrNull()
        val listener = FirebaseAuth.AuthStateListener { auth ->
            val user = auth.currentUser
            if (user != null) {
                isSessionAuthorized = true
                viewModel.startCloudSyncForAuthenticatedUser(user.email)
            } else {
                isSessionAuthorized = GoogleAuthManager.isSessionAuthorizedOrSkipped(context)
                viewModel.stopCloudSyncOnSignOut()
            }
        }
        authInstance?.addAuthStateListener(listener)
        onDispose {
            authInstance?.removeAuthStateListener(listener)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        if (!isSessionAuthorized) {
            WarpDropAuthGateScreen(
                onAuthSuccess = {
                    isSessionAuthorized = true
                    val user = runCatching { Firebase.auth.currentUser }.getOrNull()
                    if (user != null) {
                        viewModel.startCloudSyncForAuthenticatedUser(user.email)
                    }
                }
            )
        } else {
            VayuDropDashboardApp(
                viewModel = viewModel,
                isRootSplashVisible = showSplashLogoOverlay
            )
        }

        // Animated 1.5-Second Global English Splash Screen transitioning smoothly into Login & Unified Home Screen
        AnimatedVisibility(
            visible = showSplashLogoOverlay,
            enter = fadeIn(animationSpec = tween(250)) + scaleIn(initialScale = 0.94f),
            exit = fadeOut(animationSpec = tween(400))
        ) {
            WarpDropSplashOverlay(
                onDismiss = { showSplashLogoOverlay = false }
            )
        }
    }
}

@Composable
fun VayuDropDashboardApp(
    viewModel: VayuDropViewModel,
    isRootSplashVisible: Boolean = false
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val groupState by viewModel.groupState.collectAsStateWithLifecycle()
    val discoveredPeers by viewModel.discoveredPeers.collectAsStateWithLifecycle()
    val oemSurvivalState by viewModel.oemSurvivalState.collectAsStateWithLifecycle()
    val browserPortalState by viewModel.browserPortalState.collectAsStateWithLifecycle()
    val localDeviceName by viewModel.localDeviceName.collectAsStateWithLifecycle()
    val telemetry by viewModel.liveTelemetry.collectAsStateWithLifecycle()

    if (uiState.activeTab != VayuTab.TRANSFER &&
        !uiState.isTransferProgressOverlayVisible &&
        !uiState.isSelectItemsPickerVisible &&
        !uiState.isSystemMenuOverlayVisible &&
        !uiState.isQrScannerOverlayVisible
    ) {
        BackHandler {
            viewModel.selectTab(VayuTab.TRANSFER)
        }
    }

    Scaffold(
        modifier = Modifier
            .fillMaxSize()
            .background(CyberObsidian),
        containerColor = CyberObsidian,
        contentWindowInsets = WindowInsets.safeDrawing,
        bottomBar = {
            if (!uiState.isTransferProgressOverlayVisible &&
                !uiState.isSelectItemsPickerVisible &&
                !uiState.isSystemMenuOverlayVisible &&
                !uiState.isQrScannerOverlayVisible &&
                !isRootSplashVisible
            ) {
                VayuBottomNavigationBar(
                    activeTab = uiState.activeTab,
                    onTabSelected = { viewModel.selectTab(it) }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(CyberObsidian)
        ) {
            Crossfade(
                targetState = uiState.activeTab,
                label = "vayu_tab_crossfade"
            ) { tab ->
                when (tab) {
                    VayuTab.TRANSFER -> {
                        RadarTransferScreen(
                            uiState = uiState,
                            localDeviceName = localDeviceName,
                            groupState = groupState,
                            discoveredPeers = discoveredPeers,
                            browserPortalState = browserPortalState,
                            telemetry = telemetry,
                            oemSurvivalState = oemSurvivalState,
                            onRequestPermissionResult = viewModel::onRuntimePermissionResult,
                            onSelectChannel = viewModel::selectNonDfsChannel,
                            onStartGroupOwner = viewModel::start5GhzGroupOwner,
                            onScanPeers = viewModel::scanFor5GhzPeers,
                            onRotateKeypair = { viewModel.rotateEphemeralX25519Session() },
                            onSetTransferMode = viewModel::setTransferMode,
                            onTriggerHeroSend = viewModel::triggerHeroSendAndOpenOverlay,
                            onOpenSelectItemsPicker = viewModel::openSelectItemsPicker,
                            onNavigateToPhoneClone = {
                                viewModel.selectTab(VayuTab.CLONE)
                            },
                            onNavigateToCrossPlatformHub = {
                                viewModel.toggleBrowserHttpPortal(true)
                                viewModel.openSystemMenu(SystemMenuScreen.WEBSHARE)
                            },
                            onSetFrontPageHistoryMode = viewModel::setFrontPageHistoryMode,
                            onSetHistoryReceivedFilter = viewModel::setHistoryReceivedFilter,
                            onResumePendingTransfers = viewModel::resumePausedTransfersAndOpenOverlay,
                            onTriggerApkInstallOrRun = viewModel::triggerApkInstallOrRun,
                            onOpenTransferProgressOverlay = {
                                viewModel.setTransferProgressOverlayVisible(true)
                            },
                            onTriggerHindiVoiceGuide = { viewModel.triggerHindiVoiceGuidance() },
                            onSelectPickerCategory = viewModel::selectPickerCategory,
                            onRefreshCatalog = viewModel::refreshRealDeviceCatalog,
                            onToggleDeviceEntryInQueue = viewModel::toggleDeviceEntryInQueue,
                            onOpenInMediaViewer = viewModel::openInMediaViewer,
                            onPickExternalFiles = viewModel::onFilesPickedForTransfer,
                            onStartOrResumeItem = viewModel::startOrResumeQueueItemTransfer,
                            onPauseItem = viewModel::pauseQueueItemTransfer,
                            onRemoveItem = viewModel::removeQueueItem,
                            onStartAllQueue = viewModel::startAllQueuedTransfers,
                            onPauseAllQueue = viewModel::pauseAllQueuedTransfers,
                            onToggleDeltaSync = viewModel::toggleDeltaSyncMode,
                            onToggleOemSurvival = viewModel::toggleOemSurvivalForegroundService,
                            onSetPairingModalVisible = viewModel::setPairingModalVisible,
                            onSetAdvancedSettingsVisible = viewModel::setAdvancedSettingsVisible,
                            onOpenSystemMenu = { viewModel.openSystemMenu() },
                            onVerifySpake2Pin = viewModel::verifySpake2PinEntry,
                            onToggleBrowserPortal = viewModel::toggleBrowserHttpPortal,
                            onOpenQrScanner = { viewModel.openQrScannerOverlay(fromPhoneClone = false) },
                            onSelectPairingScanTab = viewModel::setPairingModalScanTabSelected
                        )
                    }

                    VayuTab.CLONE -> {
                        PhoneCloneScreen(
                            uiState = uiState,
                            localDeviceName = localDeviceName,
                            onSetCloneRole = viewModel::setCloneRole,
                            onToggleCategory = viewModel::toggleCloneCategory,
                            onOpenWhatsAppPermissionDialog = viewModel::openWhatsAppPermissionDialog,
                            onDismissWhatsAppPermissionDialog = viewModel::dismissWhatsAppPermissionDialog,
                            onAuthorizeWhatsAppBackup = viewModel::authorizeAndStageWhatsAppBackup,
                            onStartMigration = viewModel::startFullPhoneCloneMigration,
                            onOpenQrScanner = { viewModel.openQrScannerOverlay(fromPhoneClone = true) }
                        )
                    }

                    VayuTab.AI_SHIELD -> {
                        DuplicateVaultScreen(
                            uiState = uiState,
                            onRunDuplicateScan = viewModel::runSha256DuplicateScan,
                            onPurgeDuplicates = viewModel::purgeAllDuplicates,
                            onLockSessionSnapshotInVault = viewModel::lockNewKeypairSnapshotInVault,
                            onRunDeltaSyncBenchmark = viewModel::runIncrementalDeltaSyncOnRealDeviceFile
                        )
                    }

                    VayuTab.INBUILT_HUB -> {
                        InbuiltHubScreen(
                            uiState = uiState,
                            browserPortalState = browserPortalState,
                            onSelectMediaPreview = viewModel::selectMediaPreviewItem,
                            onToggleBrowserPortal = viewModel::toggleBrowserHttpPortal,
                            onQueueMediaEntry = { entry ->
                                viewModel.toggleDeviceEntryInQueue(entry)
                                viewModel.selectTab(VayuTab.TRANSFER)
                            }
                        )
                    }
                }
            }

            // ShareMe-Style 6-Tab "Select items" File Picker Overlay
            AnimatedVisibility(
                visible = uiState.isSelectItemsPickerVisible,
                enter = slideInVertically(initialOffsetY = { it / 3 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 3 }) + fadeOut()
            ) {
                val queuedIds = remember(uiState.transferQueue) {
                    uiState.transferQueue.map { it.id }.toSet()
                }
                ShareMeSelectItemsPickerOverlay(
                    initialTab = uiState.activeSelectItemsTab,
                    categorizedCatalog = uiState.categorizedCatalog,
                    queuedIds = queuedIds,
                    onToggleEntryInQueue = viewModel::toggleDeviceEntryInQueue,
                    onSelectAllEntries = viewModel::selectAllPickerEntries,
                    onConfirmSend = viewModel::triggerHeroSendAndOpenOverlay,
                    onClosePicker = viewModel::closeSelectItemsPicker
                )
            }

            // Step B: Systematic 5-Screen "☰" Menu Drawer Suite Overlay
            AnimatedVisibility(
                visible = uiState.isSystemMenuOverlayVisible,
                enter = slideInVertically(initialOffsetY = { it / 4 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 4 }) + fadeOut()
            ) {
                ShareMeSystemMenuOverlay(
                    activeScreen = uiState.activeSystemMenuScreen,
                    currentNickname = uiState.customProfileNickname.ifBlank { localDeviceName },
                    currentAvatarIndex = uiState.selectedAvatarIndex,
                    ftpServerRunning = uiState.isFtpServerRunning,
                    ftpServerAddress = uiState.ftpServerAddress,
                    ftpPortNumber = uiState.ftpPortNumber,
                    ftpKeepDisplayAwake = uiState.ftpKeepDisplayAwake,
                    ftpTransferEncoding = uiState.ftpTransferEncoding,
                    ftpAnonymousSignIn = uiState.ftpAnonymousSignIn,
                    ftpUsername = uiState.ftpUsername,
                    DestinationFolder = uiState.destinationFolder,
                    selectedLanguageLabel = uiState.selectedLanguagePreference,
                    updateDuringTransfers = uiState.updateWarpDropDuringTransfers,
                    browserPortalState = browserPortalState,
                    pairingSession = uiState.pairingSession,
                    onNavigateScreen = viewModel::navigateSystemMenu,
                    onSaveProfile = viewModel::saveUserProfile,
                    onToggleFtpServer = viewModel::togglePcFtpServer,
                    onSetFtpKeepAwake = viewModel::setFtpKeepDisplayAwake,
                    onSetFtpEncoding = viewModel::setFtpTransferEncoding,
                    onSetFtpPortNumber = viewModel::setFtpPortNumber,
                    onSetFtpAnonymous = viewModel::setFtpAnonymousSignIn,
                    onSetFtpCredentials = viewModel::setFtpCredentials,
                    onStartWebsharePortal = { viewModel.toggleBrowserHttpPortal(true) },
                    onResumePendingTransfers = viewModel::resumePausedTransfersAndOpenOverlay,
                    onSetDestinationFolder = viewModel::setDestinationFolder,
                    onSetLanguagePreference = viewModel::setLanguagePreference,
                    onSetUpdateDuringTransfers = viewModel::setUpdateDuringTransfers,
                    onShowToastMessage = viewModel::showStatusMessageToast,
                    onCloseMenu = viewModel::closeSystemMenu,
                    telemetry = telemetry,
                    oemSurvivalState = oemSurvivalState,
                    onToggleOemSurvivalShield = viewModel::toggleOemSurvivalForegroundService,
                    selectedChannel = uiState.selectedChannel,
                    onSelectChannel = viewModel::selectNonDfsChannel
                )
            }

            // Single Sleek Animated Floating Top Banner for Multi-Language Voice Assistant
            AnimatedVisibility(
                visible = uiState.voiceAssistantBannerText != null,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
            ) {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(20.dp),
                    shadowElevation = 12.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NeonCyan, RoundedCornerShape(20.dp))
                        .testTag("floating_voice_assistant_snackbar")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(40.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.AutoMirrored.Filled.VolumeUp,
                                    contentDescription = "Voice Assistant Active",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(22.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = uiState.voiceAssistantBannerText.orEmpty(),
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(8.dp))

                        IconButton(
                            onClick = viewModel::dismissVoiceGuidanceBanner,
                            modifier = Modifier
                                .size(36.dp)
                                .clip(CircleShape)
                                .background(CyberObsidian)
                                .testTag("close_floating_voice_snackbar_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Voice Banner",
                                tint = TextPrimaryIce,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                }
            }

            // Full-Screen Dedicated Live Transfer Progress Overlay
            AnimatedVisibility(
                visible = uiState.isTransferProgressOverlayVisible,
                enter = slideInVertically(initialOffsetY = { it / 2 }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { it / 2 }) + fadeOut()
            ) {
                LiveTransferProgressOverlay(
                    title = uiState.transferOverlayTitle,
                    items = uiState.transferQueue,
                    selectedChannel = uiState.selectedChannel,
                    isPaused = uiState.isTransferPaused || uiState.transferQueue.any {
                        it.status == TransferItemStatus.PAUSED_CHECKPOINT
                    },
                    onPauseAll = viewModel::pauseAllQueuedTransfers,
                    onResumeAll = viewModel::startAllQueuedTransfers,
                    onToggleSingleItemPauseResume = { item ->
                        if (item.status == TransferItemStatus.STREAMING_NIO) {
                            viewModel.pauseQueueItemTransfer(item.id)
                        } else {
                            viewModel.startOrResumeQueueItemTransfer(item.id)
                        }
                    },
                    onCloseOverlay = {
                        viewModel.setTransferProgressOverlayVisible(false)
                    }
                )
            }

            // Full-Screen Real-Time CameraX + ML Kit QR Scanner Viewfinder Overlay
            AnimatedVisibility(
                visible = uiState.isQrScannerOverlayVisible,
                enter = fadeIn(animationSpec = tween(220)),
                exit = fadeOut(animationSpec = tween(220))
            ) {
                CameraQrScannerOverlay(
                    fallbackSampleMp2pUri = uiState.pairingSession?.mp2pUriPayload.orEmpty(),
                    isTorchEnabled = uiState.isQrScannerTorchEnabled,
                    onToggleTorch = viewModel::toggleQrScannerTorch,
                    onQrCodeScanned = viewModel::onQrCodeScanned,
                    onClose = viewModel::closeQrScannerOverlay
                )
            }

            // Floating Top 5 GHz Instant Peer Connected Success Banner
            AnimatedVisibility(
                visible = uiState.connectedPeerBannerMessage != null && !uiState.isQrScannerOverlayVisible,
                enter = slideInVertically(initialOffsetY = { -it }) + fadeIn(),
                exit = slideOutVertically(targetOffsetY = { -it }) + fadeOut(),
                modifier = Modifier
                    .align(Alignment.TopCenter)
                    .padding(horizontal = 16.dp, vertical = 10.dp)
            ) {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(18.dp),
                    shadowElevation = 10.dp,
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, EmeraldNominal, RoundedCornerShape(18.dp))
                        .testTag("global_connected_peer_banner")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 14.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = EmeraldNominal,
                                modifier = Modifier.size(20.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = uiState.connectedPeerBannerMessage.orEmpty(),
                                style = MaterialTheme.typography.bodySmall,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                        IconButton(
                            onClick = viewModel::dismissConnectedPeerBanner,
                            modifier = Modifier.size(30.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Dismiss Connected Banner",
                                tint = TextSecondarySlate,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun WarpDropSplashOverlay(
    onDismiss: () -> Unit
) {
    val deepCyberSlate = Color(0xFF0B132B)
    val infiniteTransition = rememberInfiniteTransition(label = "warpdrop_splash_pulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.04f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 750, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "splash_logo_scale"
    )
    val floatingProgress by infiniteTransition.animateFloat(
        initialValue = -28f,
        targetValue = 28f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 900, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "floating_media_offset"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(deepCyberSlate)
            .clickable(onClick = onDismiss)
            .padding(horizontal = 24.dp, vertical = 32.dp)
            .testTag("warpdrop_splash_screen")
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.align(Alignment.Center)
        ) {
            // Circular WarpDrop Google-Style White Emblem with subtle pulsing animation
            Box(
                modifier = Modifier
                    .size(116.dp)
                    .scale(pulseScale)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(2.5.dp, NeonCyan.copy(alpha = 0.7f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Image(
                    painter = painterResource(id = R.drawable.ic_warpdrop_speed_logo),
                    contentDescription = stringResource(id = R.string.app_title),
                    modifier = Modifier
                        .size(116.dp)
                        .clip(CircleShape)
                        .testTag("warpdrop_splash_logo_image")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Two sleek phones (dark slate & glowing cyan) transferring floating media icons between them
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("splash_dual_phones_media_animation")
            ) {
                // Left Phone (Dark Slate)
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(Color(0xFF1E293B))
                        .border(2.dp, Color(0xFF0066FF), RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Sender Phone",
                        tint = Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }

                // Floating Media Icons Streaming Between Phones
                Row(
                    modifier = Modifier
                        .width(126.dp)
                        .padding(horizontal = 8.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    listOf(
                        Icons.Default.Image to NeonCyan,
                        Icons.Default.PlayCircleFilled to ElectricViolet,
                        Icons.Default.MusicNote to EmeraldNominal,
                        Icons.Default.Apps to ElectricSky
                    ).forEachIndexed { idx, (iconVec, tintColor) ->
                        Box(
                            modifier = Modifier
                                .offset(
                                    x = (floatingProgress * 0.25f).dp,
                                    y = if (idx % 2 == 0) (-4).dp else 4.dp
                                )
                                .size(24.dp)
                                .clip(CircleShape)
                                .background(tintColor.copy(alpha = 0.22f))
                                .border(1.dp, tintColor, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = iconVec,
                                contentDescription = null,
                                tint = tintColor,
                                modifier = Modifier.size(13.dp)
                            )
                        }
                    }
                }

                // Right Phone (Glowing Cyan)
                Box(
                    modifier = Modifier
                        .size(width = 52.dp, height = 84.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0099CC), Color(0xFF00E5FF))
                            )
                        )
                        .border(2.dp, NeonCyan, RoundedCornerShape(14.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Receiver Phone",
                        tint = deepCyberSlate,
                        modifier = Modifier.size(28.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(22.dp))

            Text(
                text = stringResource(id = R.string.app_title),
                style = MaterialTheme.typography.headlineLarge,
                color = Color.White,
                fontWeight = FontWeight(800),
                letterSpacing = 0.8.sp,
                modifier = Modifier.testTag("warpdrop_splash_title")
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = stringResource(id = R.string.splash_tagline),
                style = MaterialTheme.typography.bodyLarge,
                color = TextPrimaryIce.copy(alpha = 0.88f),
                fontWeight = FontWeight.Medium,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("warpdrop_splash_tagline")
            )
        }

        // Bottom Footer: "🔒 5 GHz Direct • End-to-End Encrypted • 100% Offline"
        Text(
            text = stringResource(id = R.string.splash_footer),
            style = MaterialTheme.typography.labelMedium,
            color = TextSecondarySlate,
            fontWeight = FontWeight.SemiBold,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(bottom = 12.dp)
                .testTag("warpdrop_splash_footer")
        )
    }
}

@Composable
private fun VayuBottomNavigationBar(
    activeTab: VayuTab,
    onTabSelected: (VayuTab) -> Unit
) {
    NavigationBar(
        containerColor = CyberSlateCard,
        contentColor = TextPrimaryIce,
        tonalElevation = 8.dp,
        windowInsets = WindowInsets.navigationBars,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorderSubtle)
            .testTag("vayu_bottom_nav")
    ) {
        VayuTab.entries.forEach { tab ->
            val selected = tab == activeTab
            val (filledIcon, outlinedIcon) = when (tab) {
                VayuTab.TRANSFER -> Icons.Default.SwapHoriz to Icons.Outlined.SwapHoriz
                VayuTab.CLONE -> Icons.Default.PhonelinkSetup to Icons.Outlined.PhonelinkSetup
                VayuTab.AI_SHIELD -> Icons.Default.Shield to Icons.Outlined.Shield
                VayuTab.INBUILT_HUB -> Icons.Default.PlayCircleFilled to Icons.Outlined.PlayCircleOutline
            }
            val tabTitle = when (tab) {
                VayuTab.TRANSFER -> stringResource(id = R.string.transfer_tab)
                VayuTab.CLONE -> stringResource(id = R.string.clone_tab)
                VayuTab.AI_SHIELD -> stringResource(id = R.string.shield_tab)
                VayuTab.INBUILT_HUB -> stringResource(id = R.string.hub_tab)
            }

            NavigationBarItem(
                selected = selected,
                onClick = { onTabSelected(tab) },
                icon = {
                    Icon(
                        imageVector = if (selected) filledIcon else outlinedIcon,
                        contentDescription = tabTitle,
                        modifier = Modifier.size(22.dp)
                    )
                },
                label = {
                    Text(
                        text = tabTitle,
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
                        maxLines = 1,
                        softWrap = false,
                        overflow = TextOverflow.Visible
                    )
                },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = CyberObsidian,
                    selectedTextColor = NeonCyan,
                    indicatorColor = NeonCyan,
                    unselectedIconColor = TextSecondarySlate,
                    unselectedTextColor = TextSecondarySlate
                ),
                modifier = Modifier.testTag("nav_tab_${tab.route}")
            )
        }
    }
}
