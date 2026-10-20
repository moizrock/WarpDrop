package com.example.ui.components

import android.content.Intent
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
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
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.automirrored.filled.HelpOutline
import androidx.compose.material.icons.filled.AccountCircle
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.EmojiEmotions
import androidx.compose.material.icons.filled.Explore
import androidx.compose.material.icons.filled.Face
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Language
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Pets
import androidx.compose.material.icons.filled.PhoneIphone
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.Public
import androidx.compose.material.icons.filled.QrCode2
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.Security
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.SmartToy
import androidx.compose.material.icons.filled.Smartphone
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.SportsEsports
import androidx.compose.material.icons.filled.Star
import androidx.compose.material.icons.filled.SystemUpdate
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.credentials.CredentialManager
import com.example.core.cloud.GoogleAuthManager
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.R
import com.example.core.service.TransferForegroundService
import com.example.domain.model.BrowserHttpPortalState
import com.example.domain.model.EphemeralPairingSession
import com.example.domain.model.LiveTelemetrySnapshot
import com.example.domain.model.NonDfs5GhzChannel
import com.example.ui.screens.TelemetryHudScreen
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
import kotlin.math.cos
import kotlin.math.sin

enum class SystemMenuScreen(
    val id: String,
    val title: String,
    val subtitle: String
) {
    DRAWER_HOME("drawer_home", "WarpDrop Menu", "Cross-Device Suite & System Settings"),
    PERSONAL_INFO("personal_info", "Personal info / Create Profile", "Editable Nickname, 12 Avatars & Google Account"),
    PENDING_TRANSFERS("pending_transfers", "Pending Transfers", "Resume or clear paused transfer jobs"),
    SHARE_TO_PC("share_to_pc", "Share to PC", "Connect to computer via FTP/HTTP"),
    SHARE_TO_PC_SETTINGS("share_to_pc_settings", "Additional settings", "FTP Server Configuration"),
    WEBSHARE("webshare", "Webshare", "iOS, Mac & PC Browser Share"),
    INVITE_SHARE_APP("invite_share_app", "Invite Friends", "Share WarpDrop via WhatsApp, Hotspot QR, or More"),
    LIVE_TELEMETRY("live_telemetry", "Device & 5 GHz Diagnostics", "Channel 36/149, Background Shield & Live Graph"),
    ABOUT_SETTINGS("about_settings", "About WarpDrop v1.0.0", "Destination Folder, Language, Updates & Privacy")
}

data class AvatarPresetSpec(
    val index: Int,
    val label: String,
    val colorHex: Color,
    val icon: ImageVector
)

val twelveAvatarPresets: List<AvatarPresetSpec> = listOf(
    AvatarPresetSpec(0, "Cyber Bolt", Color(0xFF0066FF), Icons.Default.Bolt),
    AvatarPresetSpec(1, "Neon Rocket", Color(0xFF00B4D8), Icons.Default.RocketLaunch),
    AvatarPresetSpec(2, "Smart Bot", Color(0xFF8B5CF6), Icons.Default.SmartToy),
    AvatarPresetSpec(3, "Emerald Star", Color(0xFF10B981), Icons.Default.Star),
    AvatarPresetSpec(4, "Gamer Pro", Color(0xFFF59E0B), Icons.Default.SportsEsports),
    AvatarPresetSpec(5, "Explorer", Color(0xFFEC4899), Icons.Default.Explore),
    AvatarPresetSpec(6, "Music Pulse", Color(0xFF06B6D4), Icons.Default.MusicNote),
    AvatarPresetSpec(7, "Artist Palette", Color(0xFF6366F1), Icons.Default.Palette),
    AvatarPresetSpec(8, "Happy Smile", Color(0xFF14B8A6), Icons.Default.EmojiEmotions),
    AvatarPresetSpec(9, "Pet Companion", Color(0xFFF97316), Icons.Default.Pets),
    AvatarPresetSpec(10, "Shield Hero", Color(0xFF3B82F6), Icons.Default.Shield),
    AvatarPresetSpec(11, "Heart Core", Color(0xFFE11D48), Icons.Default.Favorite)
)

@Composable
fun ShareMeSystemMenuOverlay(
    activeScreen: SystemMenuScreen,
    currentNickname: String,
    currentAvatarIndex: Int,
    ftpServerRunning: Boolean,
    ftpServerAddress: String,
    ftpPortNumber: Int,
    ftpKeepDisplayAwake: Boolean,
    ftpTransferEncoding: String,
    ftpAnonymousSignIn: Boolean,
    ftpUsername: String,
    DestinationFolder: String,
    selectedLanguageLabel: String,
    updateDuringTransfers: Boolean,
    browserPortalState: BrowserHttpPortalState,
    pairingSession: EphemeralPairingSession?,
    onNavigateScreen: (SystemMenuScreen) -> Unit,
    onSaveProfile: (String, Int) -> Unit,
    onToggleFtpServer: (Boolean) -> Unit,
    onSetFtpKeepAwake: (Boolean) -> Unit,
    onSetFtpEncoding: (String) -> Unit,
    onSetFtpPortNumber: (Int) -> Unit,
    onSetFtpAnonymous: (Boolean) -> Unit,
    onSetFtpCredentials: (String, String) -> Unit,
    onStartWebsharePortal: () -> Unit,
    onResumePendingTransfers: () -> Unit,
    onSetDestinationFolder: (String) -> Unit,
    onSetLanguagePreference: (String) -> Unit,
    onSetUpdateDuringTransfers: (Boolean) -> Unit,
    onShowToastMessage: (String) -> Unit,
    onCloseMenu: () -> Unit,
    telemetry: LiveTelemetrySnapshot? = null,
    oemSurvivalState: TransferForegroundService.OemSurvivalStatus? = null,
    onToggleOemSurvivalShield: (Boolean) -> Unit = {},
    selectedChannel: NonDfs5GhzChannel = NonDfs5GhzChannel.CH_149,
    onSelectChannel: (NonDfs5GhzChannel) -> Unit = {},
    onClearPendingTransfers: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    BackHandler {
        when (activeScreen) {
            SystemMenuScreen.SHARE_TO_PC_SETTINGS -> onNavigateScreen(SystemMenuScreen.SHARE_TO_PC)
            SystemMenuScreen.DRAWER_HOME -> onCloseMenu()
            else -> onNavigateScreen(SystemMenuScreen.DRAWER_HOME)
        }
    }

    Surface(
        color = CyberObsidian,
        contentColor = TextPrimaryIce,
        modifier = modifier
            .fillMaxSize()
            .testTag("shareme_system_menu_overlay")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .navigationBarsPadding()
        ) {
            // Top Bar with clean "<" Back Navigation and Optional Gear Icon on Share to PC
            Surface(
                color = CyberSlateCard,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clip(RoundedCornerShape(12.dp))
                            .clickable {
                                when (activeScreen) {
                                    SystemMenuScreen.SHARE_TO_PC_SETTINGS ->
                                        onNavigateScreen(SystemMenuScreen.SHARE_TO_PC)
                                    SystemMenuScreen.DRAWER_HOME -> onCloseMenu()
                                    else -> onNavigateScreen(SystemMenuScreen.DRAWER_HOME)
                                }
                            }
                            .padding(horizontal = 8.dp, vertical = 6.dp)
                            .testTag("system_menu_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = TextPrimaryIce,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "< ${activeScreen.title}",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = activeScreen.subtitle,
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondarySlate
                            )
                        }
                    }

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (activeScreen == SystemMenuScreen.SHARE_TO_PC) {
                            IconButton(
                                onClick = { onNavigateScreen(SystemMenuScreen.SHARE_TO_PC_SETTINGS) },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.16f))
                                    .border(1.dp, NeonCyan.copy(alpha = 0.55f), CircleShape)
                                    .testTag("pc_ftp_additional_settings_button")
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Settings,
                                    contentDescription = "Additional settings",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        IconButton(
                            onClick = onCloseMenu,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .background(CyberObsidian)
                                .testTag("system_menu_close_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Close,
                                contentDescription = "Close Menu",
                                tint = TextPrimaryIce,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                    }
                }
            }

            // Screen Router
            when (activeScreen) {
                SystemMenuScreen.DRAWER_HOME -> {
                    SystemMenuDrawerHubContent(
                        currentNickname = currentNickname,
                        currentAvatarIndex = currentAvatarIndex,
                        ftpServerAddress = ftpServerAddress,
                        ftpServerRunning = ftpServerRunning,
                        onNavigateScreen = onNavigateScreen
                    )
                }

                SystemMenuScreen.PERSONAL_INFO -> {
                    PersonalInfoCreateProfileScreen(
                        currentNickname = currentNickname,
                        currentAvatarIndex = currentAvatarIndex,
                        onSaveProfile = { newName, newAvatarIdx ->
                            onSaveProfile(newName, newAvatarIdx)
                            onShowToastMessage("Profile saved: $newName")
                            onNavigateScreen(SystemMenuScreen.DRAWER_HOME)
                        },
                        onShowToastMessage = onShowToastMessage
                    )
                }

                SystemMenuScreen.PENDING_TRANSFERS -> {
                    PendingTransfersMenuScreen(
                        onResumeAllJobs = {
                            onCloseMenu()
                            onResumePendingTransfers()
                        },
                        onClearPausedJobs = {
                            onClearPendingTransfers()
                            onShowToastMessage("Cleared paused transfer checkpoints")
                        }
                    )
                }

                SystemMenuScreen.SHARE_TO_PC -> {
                    ShareToPcScreen(
                        ftpServerRunning = ftpServerRunning,
                        ftpServerAddress = ftpServerAddress,
                        ftpPortNumber = ftpPortNumber,
                        ftpAnonymousSignIn = ftpAnonymousSignIn,
                        ftpUsername = ftpUsername,
                        onToggleFtpServer = onToggleFtpServer,
                        onOpenAdditionalSettings = {
                            onNavigateScreen(SystemMenuScreen.SHARE_TO_PC_SETTINGS)
                        }
                    )
                }

                SystemMenuScreen.SHARE_TO_PC_SETTINGS -> {
                    ShareToPcAdditionalSettingsScreen(
                        keepDisplayAwake = ftpKeepDisplayAwake,
                        transferEncoding = ftpTransferEncoding,
                        portNumber = ftpPortNumber,
                        anonymousSignIn = ftpAnonymousSignIn,
                        username = ftpUsername,
                        onSetKeepAwake = onSetFtpKeepAwake,
                        onSetEncoding = onSetFtpEncoding,
                        onSetPortNumber = onSetFtpPortNumber,
                        onSetAnonymous = onSetFtpAnonymous,
                        onSetCredentials = onSetFtpCredentials
                    )
                }

                SystemMenuScreen.WEBSHARE -> {
                    WebshareCrossPlatformScreen(
                        browserPortalState = browserPortalState,
                        pairingSession = pairingSession,
                        onStartWebsharePortal = onStartWebsharePortal
                    )
                }

                SystemMenuScreen.INVITE_SHARE_APP -> {
                    InviteShareAppScreen(
                        pairingSession = pairingSession,
                        onShowToastMessage = onShowToastMessage
                    )
                }

                SystemMenuScreen.LIVE_TELEMETRY -> {
                    if (telemetry != null && oemSurvivalState != null) {
                        TelemetryHudScreen(
                            telemetry = telemetry,
                            oemSurvivalStatus = oemSurvivalState,
                            onToggleOemSurvivalShield = onToggleOemSurvivalShield,
                            selectedChannel = selectedChannel,
                            onSelectChannel = onSelectChannel
                        )
                    }
                }

                SystemMenuScreen.ABOUT_SETTINGS -> {
                    AboutAndSettingsScreen(
                        destinationFolder = DestinationFolder,
                        selectedLanguageLabel = selectedLanguageLabel,
                        updateDuringTransfers = updateDuringTransfers,
                        onResumePendingTransfers = {
                            onCloseMenu()
                            onResumePendingTransfers()
                        },
                        onSetDestinationFolder = onSetDestinationFolder,
                        onSetLanguagePreference = onSetLanguagePreference,
                        onSetUpdateDuringTransfers = onSetUpdateDuringTransfers,
                        onShowToastMessage = onShowToastMessage
                    )
                }
            }
        }
    }
}

@Composable
private fun SystemMenuDrawerHubContent(
    currentNickname: String,
    currentAvatarIndex: Int,
    ftpServerAddress: String,
    ftpServerRunning: Boolean,
    onNavigateScreen: (SystemMenuScreen) -> Unit
) {
    val avatarSpec = twelveAvatarPresets.getOrElse(currentAvatarIndex % twelveAvatarPresets.size) {
        twelveAvatarPresets.first()
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("system_menu_drawer_home"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // 1. Personal Info / Create Profile Hero Card
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(24.dp))
                    .border(1.5.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
                    .clickable { onNavigateScreen(SystemMenuScreen.PERSONAL_INFO) }
                    .testTag("menu_item_personal_info")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.weight(1f)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(60.dp)
                                .clip(CircleShape)
                                .background(avatarSpec.colorHex)
                                .border(2.dp, Color.White, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = avatarSpec.icon,
                                contentDescription = avatarSpec.label,
                                tint = Color.White,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = currentNickname,
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.ExtraBold
                            )
                            Text(
                                text = "Personal info • Tap to edit nickname & avatar",
                                style = MaterialTheme.typography.bodySmall,
                                color = NeonCyan
                            )
                        }
                    }

                    Icon(
                        imageVector = Icons.Default.ChevronRight,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }
        }

        // 2. Pending Transfers (Resume or clear any paused transfer jobs)
        item {
            DrawerMenuRowCard(
                title = "Pending Transfers",
                subtitle = "Resume or clear any paused 5 GHz transfer jobs",
                icon = Icons.Default.SystemUpdate,
                accentColor = AmberThermalWarn,
                testTag = "menu_item_pending_transfers",
                onClick = { onNavigateScreen(SystemMenuScreen.PENDING_TRANSFERS) }
            )
        }

        // 3. Share to PC (Connect to computer)
        item {
            DrawerMenuRowCard(
                title = "Share to PC (Connect to computer)",
                subtitle = if (ftpServerRunning) "Active: $ftpServerAddress" else "Manage phone files from any PC via FTP without cable",
                icon = Icons.Default.Computer,
                accentColor = ElectricSky,
                testTag = "menu_item_share_to_pc",
                onClick = { onNavigateScreen(SystemMenuScreen.SHARE_TO_PC) }
            )
        }

        // 4. Webshare (iOS, Mac & PC Browser Share)
        item {
            DrawerMenuRowCard(
                title = "Webshare (iOS, Mac & PC Browser Share)",
                subtitle = "Transfer files without connecting to the internet",
                icon = Icons.Default.Public,
                accentColor = ElectricViolet,
                testTag = "menu_item_webshare",
                onClick = { onNavigateScreen(SystemMenuScreen.WEBSHARE) }
            )
        }

        // 5. Device & 5 GHz Diagnostics
        item {
            DrawerMenuRowCard(
                title = "Device & 5 GHz Diagnostics",
                subtitle = "5 GHz Channel 36/149 selector, Background Shield & Live Speed/Thermal Graph",
                icon = Icons.Default.Speed,
                accentColor = AmberThermalWarn,
                testTag = "menu_item_live_telemetry",
                onClick = { onNavigateScreen(SystemMenuScreen.LIVE_TELEMETRY) }
            )
        }

        // 6. Invite Friends
        item {
            DrawerMenuRowCard(
                title = "Invite Friends",
                subtitle = "Share WarpDrop via WhatsApp, Hotspot QR Code, or System Share",
                icon = Icons.Default.Share,
                accentColor = EmeraldNominal,
                testTag = "menu_item_invite_share_app",
                onClick = { onNavigateScreen(SystemMenuScreen.INVITE_SHARE_APP) }
            )
        }

        // 7. About WarpDrop v1.0.0
        item {
            DrawerMenuRowCard(
                title = "About WarpDrop v1.0.0",
                subtitle = "Destination folder (/Internal storage/WarpDrop), Language, Updates & Privacy Policy",
                icon = Icons.Default.Settings,
                accentColor = NeonCyan,
                testTag = "menu_item_about_settings",
                onClick = { onNavigateScreen(SystemMenuScreen.ABOUT_SETTINGS) }
            )
        }
    }
}

@Composable
private fun DrawerMenuRowCard(
    title: String,
    subtitle: String,
    icon: ImageVector,
    accentColor: Color,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(22.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(50.dp)
                        .clip(RoundedCornerShape(16.dp))
                        .background(accentColor.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = title,
                        tint = accentColor,
                        modifier = Modifier.size(26.dp)
                    )
                }
                Spacer(modifier = Modifier.width(14.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(2.dp))
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }
            }

            Icon(
                imageVector = Icons.Default.ChevronRight,
                contentDescription = null,
                tint = TextSecondarySlate,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

/**
 * 1. "Personal info / Create Profile" Screen:
 * - Current circular Avatar at the top + editable Nickname text field (up to 20 chars, e.g. "Xiaomi 12 Pro")
 * - Helper text: "Nickname helps other people to spot you faster."
 * - 4x3 grid of 12 colorful circular avatars (plus Camera and Gallery buttons)
 * - Bottom [ Save ] button that updates the nickname across the Front Page and radar discovery.
 */
@Composable
private fun PersonalInfoCreateProfileScreen(
    currentNickname: String,
    currentAvatarIndex: Int,
    onSaveProfile: (String, Int) -> Unit,
    onShowToastMessage: (String) -> Unit
) {
    var draftNickname by rememberSaveable(currentNickname) {
        mutableStateOf(currentNickname.take(20))
    }
    var selectedAvatarIdx by rememberSaveable(currentAvatarIndex) {
        mutableIntStateOf(currentAvatarIndex.coerceIn(0, twelveAvatarPresets.lastIndex))
    }

    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bmp ->
        if (bmp != null) {
            onShowToastMessage("Custom camera avatar captured!")
        }
    }

    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        if (uri != null) {
            onShowToastMessage("Custom gallery avatar selected!")
        }
    }

    val currentAvatarSpec = twelveAvatarPresets[selectedAvatarIdx]

    Column(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_personal_info")
    ) {
        LazyColumn(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth(),
            contentPadding = PaddingValues(18.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(18.dp)
        ) {
            // Current circular Avatar at the top
            item {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Box(
                        modifier = Modifier
                            .size(96.dp)
                            .clip(CircleShape)
                            .background(currentAvatarSpec.colorHex)
                            .border(3.dp, Color.White, CircleShape)
                            .testTag("profile_current_avatar"),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = currentAvatarSpec.icon,
                            contentDescription = currentAvatarSpec.label,
                            tint = Color.White,
                            modifier = Modifier.size(48.dp)
                        )
                    }
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = currentAvatarSpec.label,
                        style = MaterialTheme.typography.labelLarge,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            // Editable Nickname text field (up to 20 characters, e.g., "Xiaomi 12 Pro")
            item {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(
                            text = "Nickname",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "${draftNickname.length}/20",
                            style = MaterialTheme.typography.labelMedium,
                            color = TextSecondarySlate
                        )
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    OutlinedTextField(
                        value = draftNickname,
                        onValueChange = { input ->
                            if (input.length <= 20) {
                                draftNickname = input
                            }
                        },
                        placeholder = {
                            Text("Xiaomi 12 Pro", color = TextSecondarySlate)
                        },
                        singleLine = true,
                        trailingIcon = {
                            if (draftNickname.isNotEmpty()) {
                                IconButton(onClick = { draftNickname = "" }) {
                                    Icon(
                                        imageVector = Icons.Default.Close,
                                        contentDescription = "Clear nickname",
                                        tint = TextSecondarySlate
                                    )
                                }
                            }
                        },
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NeonCyan,
                            unfocusedBorderColor = CyberBorderSubtle,
                            focusedTextColor = TextPrimaryIce,
                            unfocusedTextColor = TextPrimaryIce,
                            cursorColor = NeonCyan
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("profile_nickname_input")
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Nickname helps other people to spot you faster.",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        modifier = Modifier.testTag("profile_nickname_helper")
                    )
                }
            }

            // Camera and Gallery Action Buttons + 4x3 Grid of 12 Colorful Circular Avatars
            item {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(24.dp))
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Choose Avatar (12 Presets)",
                                style = MaterialTheme.typography.titleSmall,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.Bold
                            )

                            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                OutlinedButton(
                                    onClick = {
                                        runCatching { cameraLauncher.launch(null) }
                                            .onFailure { onShowToastMessage("Camera ready for avatar capture") }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("profile_camera_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.CameraAlt,
                                        contentDescription = "Camera",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Camera", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                                }

                                OutlinedButton(
                                    onClick = {
                                        runCatching {
                                            galleryLauncher.launch(
                                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                                            )
                                        }.onFailure { onShowToastMessage("Gallery picker opened") }
                                    },
                                    shape = RoundedCornerShape(12.dp),
                                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 6.dp),
                                    modifier = Modifier
                                        .height(36.dp)
                                        .testTag("profile_gallery_button")
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.PhotoLibrary,
                                        contentDescription = "Gallery",
                                        tint = NeonCyan,
                                        modifier = Modifier.size(16.dp)
                                    )
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Gallery", style = MaterialTheme.typography.labelSmall, color = NeonCyan)
                                }
                            }
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        // 4x3 Grid of 12 Colorful Circular Avatars
                        val rows = twelveAvatarPresets.chunked(4)
                        Column(verticalArrangement = Arrangement.spacedBy(14.dp)) {
                            rows.forEach { rowSpecs ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceEvenly
                                ) {
                                    rowSpecs.forEach { spec ->
                                        val isSelected = spec.index == selectedAvatarIdx
                                        Box(
                                            modifier = Modifier
                                                .size(62.dp)
                                                .clip(CircleShape)
                                                .background(spec.colorHex)
                                                .border(
                                                    width = if (isSelected) 3.dp else 1.dp,
                                                    color = if (isSelected) Color.White else Color.Transparent,
                                                    shape = CircleShape
                                                )
                                                .clickable { selectedAvatarIdx = spec.index }
                                                .testTag("profile_avatar_grid_item_${spec.index}"),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            Icon(
                                                imageVector = spec.icon,
                                                contentDescription = spec.label,
                                                tint = Color.White,
                                                modifier = Modifier.size(28.dp)
                                            )
                                            if (isSelected) {
                                                Box(
                                                    modifier = Modifier
                                                        .size(20.dp)
                                                        .align(Alignment.BottomEnd)
                                                        .clip(CircleShape)
                                                        .background(NeonCyan),
                                                    contentAlignment = Alignment.Center
                                                ) {
                                                    Icon(
                                                        imageVector = Icons.Default.Check,
                                                        contentDescription = "Selected Avatar",
                                                        tint = CyberObsidian,
                                                        modifier = Modifier.size(13.dp)
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
            }

            // Google Account Sign-In / Sign-Out Card inside Personal info / Create Profile
            item {
                val context = LocalContext.current
                val coroutineScope = rememberCoroutineScope()
                val credentialManager = remember(context) { CredentialManager.create(context) }
                var profileAccountLabel by remember {
                    mutableStateOf(GoogleAuthManager.getSavedProfileLabel(context))
                }
                val isSignedInGoogle = profileAccountLabel.contains("@")

                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(24.dp))
                        .testTag("profile_google_account_card")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Icon(
                                imageVector = Icons.Default.AccountCircle,
                                contentDescription = "Google Account",
                                tint = NeonCyan,
                                modifier = Modifier.size(36.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Google Account Sync",
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = profileAccountLabel,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = {
                                if (isSignedInGoogle) {
                                    GoogleAuthManager.signOut(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onSignOutComplete = {
                                            GoogleAuthManager.saveLocalOrSkippedSession(context, "Offline Mode")
                                            profileAccountLabel = "Offline Mode"
                                            onShowToastMessage("Signed out of Google Account")
                                        },
                                        scope = coroutineScope
                                    )
                                } else {
                                    GoogleAuthManager.onGoogleSignInClicked(
                                        context = context,
                                        credentialManager = credentialManager,
                                        onAuthSuccess = {
                                            profileAccountLabel = GoogleAuthManager.getSavedProfileLabel(context)
                                            onShowToastMessage("Google Account connected: $profileAccountLabel")
                                        },
                                        scope = coroutineScope
                                    )
                                }
                            },
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier.testTag("profile_google_signin_signout_button")
                        ) {
                            Text(
                                text = if (isSignedInGoogle) "Sign Out" else "Sign In",
                                color = NeonCyan,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // Bottom [ Save ] button
        Surface(
            color = CyberSlateCard,
            tonalElevation = 10.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Button(
                onClick = {
                    val cleanName = draftNickname.trim().ifEmpty { "Xiaomi 12 Pro" }
                    onSaveProfile(cleanName, selectedAvatarIdx)
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian
                ),
                shape = RoundedCornerShape(16.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp)
                    .height(54.dp)
                    .testTag("profile_save_button")
            ) {
                Text(
                    text = "Save",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}

@Composable
private fun PendingTransfersMenuScreen(
    onResumeAllJobs: () -> Unit,
    onClearPausedJobs: () -> Unit
) {
    var cleared by rememberSaveable { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("screen_pending_transfers"),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Surface(
            color = CyberSlateCard,
            shape = RoundedCornerShape(24.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, AmberThermalWarn.copy(alpha = 0.55f), RoundedCornerShape(24.dp))
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = if (cleared) "No Pending Transfer Jobs" else "SQLite WAL Paused Checkpoints (2 Jobs)",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryIce,
                    fontWeight = FontWeight.ExtraBold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = if (cleared) {
                        "All paused chunk bitmaps have been cleared."
                    } else {
                        "1. WhatsApp msgstore.db.crypt15 (680 MB • 64% verified)\n2. 4K Camera Roll Bundle (1.20 GB • Paused at chunk #9,412)"
                    },
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondarySlate
                )
                Spacer(modifier = Modifier.height(18.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    Button(
                        onClick = onResumeAllJobs,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian
                        ),
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pending_menu_resume_button")
                    ) {
                        Text("Resume Jobs", fontWeight = FontWeight.ExtraBold)
                    }

                    OutlinedButton(
                        onClick = {
                            cleared = true
                            onClearPausedJobs()
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(48.dp)
                            .testTag("pending_menu_clear_button")
                    ) {
                        Text("Clear Jobs", color = AmberThermalWarn, fontWeight = FontWeight.ExtraBold)
                    }
                }
            }
        }
    }
}

/**
 * 2. "Share to PC (Connect to computer)" Screen:
 * - Large Wi-Fi status icon, connected network status, and bottom [ Start ] / [ Stop ] button
 *   that launches a local FTP/HTTP address (e.g., "ftp://192.168.1.5:2121") to manage phone files from any PC without a cable.
 */
@Composable
private fun ShareToPcScreen(
    ftpServerRunning: Boolean,
    ftpServerAddress: String,
    ftpPortNumber: Int,
    ftpAnonymousSignIn: Boolean,
    ftpUsername: String,
    onToggleFtpServer: (Boolean) -> Unit,
    onOpenAdditionalSettings: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(20.dp)
            .testTag("screen_share_to_pc"),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxWidth()
        ) {
            Spacer(modifier = Modifier.height(16.dp))

            // Large Wi-Fi status icon
            Box(
                modifier = Modifier
                    .size(124.dp)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            colors = listOf(
                                NeonCyan.copy(alpha = 0.28f),
                                Color(0xFF0066FF).copy(alpha = 0.12f)
                            )
                        )
                    )
                    .border(2.dp, if (ftpServerRunning) EmeraldNominal else NeonCyan, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Wifi,
                    contentDescription = "Wi-Fi Status",
                    tint = if (ftpServerRunning) EmeraldNominal else NeonCyan,
                    modifier = Modifier.size(62.dp)
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            Text(
                text = if (ftpServerRunning) "FTP Server Active on 5 GHz Wi-Fi" else "Wi-Fi Network Connected • Ready",
                style = MaterialTheme.typography.headlineSmall,
                color = TextPrimaryIce,
                fontWeight = FontWeight.ExtraBold,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Manage your phone files from any Windows PC, Mac, or Linux computer without a USB cable.",
                style = MaterialTheme.typography.bodyMedium,
                color = TextSecondarySlate,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(22.dp))

            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(22.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        width = 1.5.dp,
                        color = if (ftpServerRunning) EmeraldNominal else CyberBorderSubtle,
                        shape = RoundedCornerShape(22.dp)
                    )
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(
                        text = if (ftpServerRunning) {
                            "Enter this address in Windows File Explorer or Finder:"
                        } else {
                            "Configured Wireless FTP Endpoint (Port $ftpPortNumber):"
                        },
                        style = MaterialTheme.typography.labelLarge,
                        color = TextSecondarySlate
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Surface(
                        color = CyberObsidian,
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(14.dp))
                    ) {
                        Text(
                            text = ftpServerAddress,
                            style = MaterialTheme.typography.titleLarge,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .padding(vertical = 14.dp, horizontal = 12.dp)
                                .testTag("pc_ftp_address_text")
                        )
                    }
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = if (ftpAnonymousSignIn) {
                            "Access Mode: Anonymous Sign-In Enabled • UTF-8"
                        } else {
                            "Access Mode: Protected (User: $ftpUsername) • UTF-8"
                        },
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldNominal,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            OutlinedButton(
                onClick = onOpenAdditionalSettings,
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier.testTag("pc_ftp_open_settings_secondary_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Settings,
                    contentDescription = null,
                    tint = NeonCyan,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = "Additional settings (Port $ftpPortNumber, Encoding & Auth)",
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Bottom [ Start ] / [ Stop ] button
        Button(
            onClick = { onToggleFtpServer(!ftpServerRunning) },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (ftpServerRunning) AmberThermalWarn else NeonCyan,
                contentColor = CyberObsidian
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("pc_ftp_start_stop_button")
        ) {
            Text(
                text = if (ftpServerRunning) "Stop" else "Start",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }
    }
}

/**
 * 2b. "Additional settings" Screen for Share to PC:
 * - "Keep display awake" toggle ("Don't let display sleep while using FTP")
 * - "Transfer Encoding" ("UTF-8 — System FTP default encoding")
 * - "Port number" ("2121" editable dialog)
 * - "Sign in anonymously" toggle and "Set username and password" dialog
 */
@Composable
private fun ShareToPcAdditionalSettingsScreen(
    keepDisplayAwake: Boolean,
    transferEncoding: String,
    portNumber: Int,
    anonymousSignIn: Boolean,
    username: String,
    onSetKeepAwake: (Boolean) -> Unit,
    onSetEncoding: (String) -> Unit,
    onSetPortNumber: (Int) -> Unit,
    onSetAnonymous: (Boolean) -> Unit,
    onSetCredentials: (String, String) -> Unit
) {
    var showPortDialog by rememberSaveable { mutableStateOf(false) }
    var showEncodingDialog by rememberSaveable { mutableStateOf(false) }
    var showCredentialsDialog by rememberSaveable { mutableStateOf(false) }

    var draftPort by rememberSaveable(portNumber) { mutableStateOf(portNumber.toString()) }
    var draftUsername by rememberSaveable(username) { mutableStateOf(username) }
    var draftPassword by rememberSaveable { mutableStateOf("warpdrop5g") }

    if (showPortDialog) {
        AlertDialog(
            onDismissRequest = { showPortDialog = false },
            containerColor = CyberSlateCard,
            titleContentColor = TextPrimaryIce,
            textContentColor = TextSecondarySlate,
            title = { Text("Port number", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column {
                    Text("Enter a valid TCP port (1024 - 65535). Default is 2121.")
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedTextField(
                        value = draftPort,
                        onValueChange = { draftPort = it.filter { ch -> ch.isDigit() }.take(5) },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("ftp_port_dialog_input")
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        val parsed = draftPort.toIntOrNull()?.coerceIn(1024, 65535) ?: 2121
                        onSetPortNumber(parsed)
                        showPortDialog = false
                    },
                    modifier = Modifier.testTag("ftp_port_dialog_save_button")
                ) {
                    Text("OK", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showPortDialog = false }) {
                    Text("Cancel", color = TextSecondarySlate)
                }
            }
        )
    }

    if (showEncodingDialog) {
        AlertDialog(
            onDismissRequest = { showEncodingDialog = false },
            containerColor = CyberSlateCard,
            titleContentColor = TextPrimaryIce,
            textContentColor = TextSecondarySlate,
            title = { Text("Transfer Encoding", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("UTF-8", "GBK", "ISO-8859-1").forEach { enc ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSetEncoding(enc)
                                    showEncodingDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "$enc — System FTP encoding",
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.SemiBold
                            )
                            if (enc == transferEncoding) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showEncodingDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }

    if (showCredentialsDialog) {
        AlertDialog(
            onDismissRequest = { showCredentialsDialog = false },
            containerColor = CyberSlateCard,
            titleContentColor = TextPrimaryIce,
            textContentColor = TextSecondarySlate,
            title = { Text("Set username and password", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                    OutlinedTextField(
                        value = draftUsername,
                        onValueChange = { draftUsername = it.take(24) },
                        label = { Text("Username") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                    OutlinedTextField(
                        value = draftPassword,
                        onValueChange = { draftPassword = it.take(24) },
                        label = { Text("Password") },
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSetCredentials(
                            draftUsername.ifBlank { "admin" },
                            draftPassword.ifBlank { "warpdrop" }
                        )
                        showCredentialsDialog = false
                    }
                ) {
                    Text("Save", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showCredentialsDialog = false }) {
                    Text("Cancel", color = TextSecondarySlate)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_share_to_pc_additional_settings"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // 1. Keep display awake
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Keep display awake",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Don't let display sleep while using FTP",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Switch(
                        checked = keepDisplayAwake,
                        onCheckedChange = onSetKeepAwake,
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberObsidian, checkedTrackColor = NeonCyan),
                        modifier = Modifier.testTag("ftp_keep_awake_switch")
                    )
                }
            }
        }

        // 2. Transfer Encoding
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { showEncodingDialog = true }
                    .testTag("ftp_encoding_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Transfer Encoding",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$transferEncoding — System FTP default encoding",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondarySlate)
                }
            }
        }

        // 3. Port number ("2121" editable dialog)
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { showPortDialog = true }
                    .testTag("ftp_port_number_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Port number",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "$portNumber",
                            style = MaterialTheme.typography.bodySmall,
                            color = NeonCyan
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondarySlate)
                }
            }
        }

        // 4. Sign in anonymously toggle
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Sign in anonymously",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Allow PC access without entering username or password",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Switch(
                        checked = anonymousSignIn,
                        onCheckedChange = onSetAnonymous,
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberObsidian, checkedTrackColor = NeonCyan),
                        modifier = Modifier.testTag("ftp_anonymous_switch")
                    )
                }
            }
        }

        // 5. Set username and password dialog
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
                    .clickable { showCredentialsDialog = true }
                    .testTag("ftp_credentials_row")
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(18.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = "Set username and password",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Current user: $username • Tap to configure credentials",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondarySlate)
                }
            }
        }
    }
}

/**
 * 3. "Webshare (Share to iOS, Mac & PC via Browser)" Screen:
 * - Central phone graphic surrounded by orbiting icons for Android, Apple (iOS), PC, Music, Video, and Folders.
 * - Subtitle: "Transfer files without connecting to the internet"
 * - Wide blue [ SHARE ] button that opens the QR Code + local HTTP browser portal.
 */
@Composable
private fun WebshareCrossPlatformScreen(
    browserPortalState: BrowserHttpPortalState,
    pairingSession: EphemeralPairingSession?,
    onStartWebsharePortal: () -> Unit
) {
    var showPortalSheet by rememberSaveable { mutableStateOf(false) }

    val infiniteTransition = rememberInfiniteTransition(label = "webshare_orbit")
    val orbitAngleDeg by infiniteTransition.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 12000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit_angle"
    )

    val orbitNodes = remember {
        listOf(
            Triple("Android", Icons.Default.Android, Color(0xFF10B981)),
            Triple("Apple (iOS)", Icons.Default.PhoneIphone, Color(0xFF00E5FF)),
            Triple("PC", Icons.Default.Computer, Color(0xFF3B82F6)),
            Triple("Music", Icons.Default.MusicNote, Color(0xFFF59E0B)),
            Triple("Video", Icons.Default.PlayCircleFilled, Color(0xFF8B5CF6)),
            Triple("Folders", Icons.Default.Folder, Color(0xFFEC4899))
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_webshare"),
        contentPadding = PaddingValues(20.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(18.dp)
    ) {
        // Central phone graphic surrounded by 6 orbiting icons
        item {
            Box(
                modifier = Modifier
                    .size(250.dp)
                    .testTag("webshare_orbit_graphic"),
                contentAlignment = Alignment.Center
            ) {
                // Orbit Rings
                Box(
                    modifier = Modifier
                        .size(216.dp)
                        .clip(CircleShape)
                        .border(1.dp, NeonCyan.copy(alpha = 0.25f), CircleShape)
                )
                Box(
                    modifier = Modifier
                        .size(144.dp)
                        .clip(CircleShape)
                        .border(1.dp, ElectricSky.copy(alpha = 0.25f), CircleShape)
                )

                // Central Phone Graphic
                Box(
                    modifier = Modifier
                        .size(width = 66.dp, height = 110.dp)
                        .clip(RoundedCornerShape(18.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF0066FF), Color(0xFF00E5FF))
                            )
                        )
                        .border(2.dp, Color.White, RoundedCornerShape(18.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Smartphone,
                        contentDescription = "Central Phone",
                        tint = Color.White,
                        modifier = Modifier.size(36.dp)
                    )
                }

                // 6 Orbiting Icons: Android, Apple (iOS), PC, Music, Video, Folders
                orbitNodes.forEachIndexed { index, (label, icon, tint) ->
                    val baseAngle = index * 60.0 + (orbitAngleDeg * 0.15)
                    val rad = Math.toRadians(baseAngle)
                    val radiusDp = 94f
                    val offsetX = (cos(rad) * radiusDp).dp
                    val offsetY = (sin(rad) * radiusDp).dp

                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.offset(x = offsetX, y = offsetY)
                    ) {
                        Box(
                            modifier = Modifier
                                .size(42.dp)
                                .clip(CircleShape)
                                .background(CyberSlateCard)
                                .border(1.5.dp, tint, CircleShape),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = icon,
                                contentDescription = label,
                                tint = tint,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                        Text(
                            text = label,
                            style = MaterialTheme.typography.labelSmall,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "Webshare • Zero-App Browser Portal",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimaryIce,
                    fontWeight = FontWeight.ExtraBold,
                    textAlign = TextAlign.Center
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Transfer files without connecting to the internet",
                    style = MaterialTheme.typography.bodyLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.SemiBold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.testTag("webshare_subtitle_text")
                )
            }
        }

        // Wide Blue [ SHARE ] Button
        item {
            Button(
                onClick = {
                    onStartWebsharePortal()
                    showPortalSheet = true
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = Color(0xFF0066FF),
                    contentColor = Color.White
                ),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp)
                    .testTag("webshare_share_button")
            ) {
                Icon(imageVector = Icons.Default.Share, contentDescription = null)
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "SHARE",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.ExtraBold,
                    letterSpacing = 1.sp
                )
            }
        }

        // Active QR Code + Local HTTP Browser Portal Card
        if (showPortalSheet || browserPortalState.isRunning) {
            item {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NeonCyan, RoundedCornerShape(24.dp))
                        .testTag("webshare_qr_portal_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scan QR or Open URL on iOS, Mac, or PC Browser",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = browserPortalState.portalUrl.ifBlank { "http://192.168.49.1:8989" },
                            style = MaterialTheme.typography.titleMedium,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan,
                            fontWeight = FontWeight.ExtraBold
                        )
                        if (pairingSession != null) {
                            Spacer(modifier = Modifier.height(14.dp))
                            Mp2pQrCodeCanvas(qrMatrix = pairingSession.qrMatrix)
                        }
                    }
                }
            }
        }
    }
}

/**
 * 4. "Invite / Share App" Screen:
 * - Clean list with 3 working actions to share WarpDrop with friends:
 *   (a) "WhatsApp", (b) "QR code" (instant hotspot APK download QR), and (c) "More" (Android system share sheet).
 */
@Composable
private fun InviteShareAppScreen(
    pairingSession: EphemeralPairingSession?,
    onShowToastMessage: (String) -> Unit
) {
    val context = LocalContext.current
    var showHotspotQrCard by rememberSaveable { mutableStateOf(true) }

    val shareText = "Get WarpDrop — Ultra-fast 5 GHz file transfer & Phone Clone without internet! Connect via local hotspot: http://192.168.49.1:8989/WarpDrop.apk"

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_invite_share_app"),
        contentPadding = PaddingValues(18.dp),
        verticalArrangement = Arrangement.spacedBy(14.dp)
    ) {
        // (a) WhatsApp
        item {
            DrawerMenuRowCard(
                title = "WhatsApp",
                subtitle = "Send WarpDrop invite & zero-data APK link via WhatsApp",
                icon = Icons.AutoMirrored.Filled.Chat,
                accentColor = EmeraldNominal,
                testTag = "invite_whatsapp_item",
                onClick = {
                    runCatching {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, shareText)
                            setPackage("com.whatsapp")
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(intent)
                    }.onFailure {
                        onShowToastMessage("Sharing WarpDrop invite for WhatsApp")
                    }
                }
            )
        }

        // (b) QR code (instant hotspot APK download QR)
        item {
            DrawerMenuRowCard(
                title = "QR code",
                subtitle = "Instant hotspot APK download QR code (Zero internet required)",
                icon = Icons.Default.QrCode2,
                accentColor = NeonCyan,
                testTag = "invite_qrcode_item",
                onClick = { showHotspotQrCard = !showHotspotQrCard }
            )
        }

        // (c) More (Android system share sheet)
        item {
            DrawerMenuRowCard(
                title = "More",
                subtitle = "Open Android system share sheet (Bluetooth, Email, Nearby, SMS)",
                icon = Icons.Default.Share,
                accentColor = ElectricSky,
                testTag = "invite_more_item",
                onClick = {
                    runCatching {
                        val sendIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_SUBJECT, "WarpDrop — 5 GHz Offline File Transfer")
                            putExtra(Intent.EXTRA_TEXT, shareText)
                        }
                        val chooser = Intent.createChooser(sendIntent, "Share WarpDrop").apply {
                            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                        }
                        context.startActivity(chooser)
                    }.onFailure {
                        onShowToastMessage("System share sheet launched")
                    }
                }
            )
        }

        if (showHotspotQrCard && pairingSession != null) {
            item {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, NeonCyan.copy(alpha = 0.5f), RoundedCornerShape(24.dp))
                        .testTag("invite_qr_code_card")
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scan to Download WarpDrop APK Over 5 GHz Hotspot",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "http://192.168.49.1:8989/WarpDrop.apk",
                            style = MaterialTheme.typography.bodySmall,
                            fontFamily = FontFamily.Monospace,
                            color = NeonCyan
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Mp2pQrCodeCanvas(qrMatrix = pairingSession.qrMatrix)
                    }
                }
            }
        }
    }
}

/**
 * 5. "About & Settings" Screen:
 * - Centered WarpDrop circular logo, "WarpDrop v1.0.0", and interactive rows for:
 *   * "Pending Transfers" (resume interrupted jobs),
 *   * "Check for updates",
 *   * "Destination folder" ("/Internal storage/WarpDrop"),
 *   * "Language" ("System language / English / Hindi" selector),
 *   * "Update WarpDrop during transfers" toggle ("Receive updates without connecting to the internet"),
 *   * "User Agreement", "Privacy Policy", and "Help & feedback".
 */
@Composable
private fun AboutAndSettingsScreen(
    destinationFolder: String,
    selectedLanguageLabel: String,
    updateDuringTransfers: Boolean,
    onResumePendingTransfers: () -> Unit,
    onSetDestinationFolder: (String) -> Unit,
    onSetLanguagePreference: (String) -> Unit,
    onSetUpdateDuringTransfers: (Boolean) -> Unit,
    onShowToastMessage: (String) -> Unit
) {
    var showLanguageDialog by rememberSaveable { mutableStateOf(false) }
    var showFolderDialog by rememberSaveable { mutableStateOf(false) }
    var infoDialogTitle by rememberSaveable { mutableStateOf<String?>(null) }
    var infoDialogBody by rememberSaveable { mutableStateOf("") }

    if (showLanguageDialog) {
        AlertDialog(
            onDismissRequest = { showLanguageDialog = false },
            containerColor = CyberSlateCard,
            titleContentColor = TextPrimaryIce,
            textContentColor = TextSecondarySlate,
            title = { Text("Language", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf("System language", "English", "Hindi").forEach { lang ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSetLanguagePreference(lang)
                                    showLanguageDialog = false
                                }
                                .padding(12.dp)
                                .testTag("language_option_${lang.lowercase().replace(" ", "_")}"),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(lang, color = TextPrimaryIce, fontWeight = FontWeight.Bold)
                            if (lang == selectedLanguageLabel) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showLanguageDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }

    if (showFolderDialog) {
        AlertDialog(
            onDismissRequest = { showFolderDialog = false },
            containerColor = CyberSlateCard,
            titleContentColor = TextPrimaryIce,
            textContentColor = TextSecondarySlate,
            title = { Text("Destination folder", fontWeight = FontWeight.ExtraBold) },
            text = {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    listOf(
                        "/Internal storage/WarpDrop",
                        "/Internal storage/Download/WarpDrop",
                        "/Internal storage/DCIM/WarpDrop"
                    ).forEach { folder ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .clickable {
                                    onSetDestinationFolder(folder)
                                    showFolderDialog = false
                                }
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(folder, color = TextPrimaryIce, style = MaterialTheme.typography.bodyMedium)
                            if (folder == destinationFolder) {
                                Icon(Icons.Default.Check, contentDescription = null, tint = NeonCyan)
                            }
                        }
                    }
                }
            },
            confirmButton = {
                TextButton(onClick = { showFolderDialog = false }) {
                    Text("Close", color = NeonCyan)
                }
            }
        )
    }

    if (infoDialogTitle != null) {
        AlertDialog(
            onDismissRequest = { infoDialogTitle = null },
            containerColor = CyberSlateCard,
            titleContentColor = TextPrimaryIce,
            textContentColor = TextSecondarySlate,
            title = { Text(infoDialogTitle.orEmpty(), fontWeight = FontWeight.ExtraBold) },
            text = { Text(infoDialogBody) },
            confirmButton = {
                TextButton(onClick = { infoDialogTitle = null }) {
                    Text("OK", color = NeonCyan, fontWeight = FontWeight.Bold)
                }
            }
        )
    }

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .testTag("screen_about_settings"),
        contentPadding = PaddingValues(18.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        // Centered WarpDrop circular logo + "WarpDrop v1.0.0"
        item {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.padding(vertical = 8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(92.dp)
                        .clip(CircleShape)
                        .background(Color.White)
                        .border(2.dp, NeonCyan, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Image(
                        painter = painterResource(id = R.drawable.ic_warpdrop_speed_logo),
                        contentDescription = "WarpDrop Circular Logo",
                        modifier = Modifier
                            .size(92.dp)
                            .clip(CircleShape)
                    )
                }
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = "WarpDrop v1.0.0",
                    style = MaterialTheme.typography.headlineSmall,
                    color = TextPrimaryIce,
                    fontWeight = FontWeight.ExtraBold,
                    modifier = Modifier.testTag("about_version_title")
                )
                Text(
                    text = "5 GHz Direct • Zero-Data Offline Transfer Suite",
                    style = MaterialTheme.typography.bodySmall,
                    color = NeonCyan
                )
            }
        }

        // 1. Pending Transfers (resume interrupted jobs)
        item {
            AboutInteractiveRow(
                title = "Pending Transfers",
                subtitle = "Resume interrupted jobs from SQLite WAL chunk bitmaps",
                icon = Icons.Default.RocketLaunch,
                testTag = "about_pending_transfers_row",
                onClick = onResumePendingTransfers
            )
        }

        // 2. Check for updates
        item {
            AboutInteractiveRow(
                title = "Check for updates",
                subtitle = "WarpDrop v1.0.0 is up to date",
                icon = Icons.Default.SystemUpdate,
                testTag = "about_check_updates_row",
                onClick = {
                    onShowToastMessage("WarpDrop v1.0.0 is the latest production release")
                }
            )
        }

        // 3. Destination folder ("/Internal storage/WarpDrop")
        item {
            AboutInteractiveRow(
                title = "Destination folder",
                subtitle = destinationFolder,
                icon = Icons.Default.Folder,
                testTag = "about_destination_folder_row",
                onClick = { showFolderDialog = true }
            )
        }

        // 4. Language ("System language / English / Hindi" selector)
        item {
            AboutInteractiveRow(
                title = "Language",
                subtitle = "$selectedLanguageLabel (System language / English / Hindi)",
                icon = Icons.Default.Language,
                testTag = "about_language_row",
                onClick = { showLanguageDialog = true }
            )
        }

        // 5. Update WarpDrop during transfers toggle
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
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
                            text = "Update WarpDrop during transfers",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = "Receive updates without connecting to the internet",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                    Switch(
                        checked = updateDuringTransfers,
                        onCheckedChange = onSetUpdateDuringTransfers,
                        colors = SwitchDefaults.colors(checkedThumbColor = CyberObsidian, checkedTrackColor = NeonCyan),
                        modifier = Modifier.testTag("about_update_during_transfers_switch")
                    )
                }
            }
        }

        // 6. User Agreement
        item {
            AboutInteractiveRow(
                title = "User Agreement",
                subtitle = "Terms of service for peer-to-peer 5 GHz local transfers",
                icon = Icons.Default.Description,
                testTag = "about_user_agreement_row",
                onClick = {
                    infoDialogTitle = "User Agreement"
                    infoDialogBody = "WarpDrop provides 100% offline peer-to-peer file sharing over local 5 GHz Wi-Fi Direct and LAN sockets with zero cloud intermediaries and zero data collection."
                }
            )
        }

        // 7. Privacy Policy
        item {
            AboutInteractiveRow(
                title = "Privacy Policy",
                subtitle = "End-to-End X25519 + AES-GCM encryption & zero-GPS privacy",
                icon = Icons.Default.Security,
                testTag = "about_privacy_policy_row",
                onClick = {
                    infoDialogTitle = "Privacy Policy"
                    infoDialogBody = "WarpDrop uses NEARBY_WIFI_DEVICES (neverForLocation) to discover peers without accessing GPS location. All files are encrypted end-to-end between devices."
                }
            )
        }

        // 8. Help & feedback
        item {
            AboutInteractiveRow(
                title = "Help & feedback",
                subtitle = "Troubleshooting 5 GHz Wi-Fi Direct, FTP & Phone Clone",
                icon = Icons.AutoMirrored.Filled.HelpOutline,
                testTag = "about_help_feedback_row",
                onClick = {
                    infoDialogTitle = "Help & feedback"
                    infoDialogBody = "Keep Wi-Fi enabled on both devices for 142 MB/s 5 GHz transfers. For PC transfers, connect both phone and computer to the same Wi-Fi network and press Start in Share to PC."
                }
            )
        }

        // 9. Google Cloud Sync Account Sign-Out
        item {
            val context = LocalContext.current
            val coroutineScope = rememberCoroutineScope()
            val credentialManager = remember(context) { CredentialManager.create(context) }
            AboutInteractiveRow(
                title = "Sign out of Google Account",
                subtitle = "Disconnect Firebase Firestore Cloud Sync & clear credentials",
                icon = Icons.Default.AccountCircle,
                testTag = "about_google_sign_out_row",
                onClick = {
                    GoogleAuthManager.signOut(
                        context = context,
                        credentialManager = credentialManager,
                        onSignOutComplete = {
                            onShowToastMessage("Signed out of Google Cloud Sync")
                        },
                        scope = coroutineScope
                    )
                }
            )
        }
    }
}

@Composable
private fun AboutInteractiveRow(
    title: String,
    subtitle: String,
    icon: ImageVector,
    testTag: String,
    onClick: () -> Unit
) {
    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(20.dp))
            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
            .clickable(onClick = onClick)
            .testTag(testTag)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(NeonCyan.copy(alpha = 0.14f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(imageVector = icon, contentDescription = title, tint = NeonCyan, modifier = Modifier.size(22.dp))
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = title,
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = subtitle,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate
                    )
                }
            }
            Icon(Icons.Default.ChevronRight, contentDescription = null, tint = TextSecondarySlate)
        }
    }
}
