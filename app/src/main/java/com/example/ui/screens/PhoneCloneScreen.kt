package com.example.ui.screens

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
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Chat
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Contacts
import androidx.compose.material.icons.filled.FolderSpecial
import androidx.compose.material.icons.filled.LockOpen
import androidx.compose.material.icons.filled.PhonelinkRing
import androidx.compose.material.icons.filled.PhonelinkSetup
import androidx.compose.material.icons.filled.PhotoLibrary
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.RocketLaunch
import androidx.compose.material.icons.filled.VerifiedUser
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.R
import com.example.domain.model.CloneCategorySummary
import com.example.domain.model.CloneCategoryType
import com.example.ui.components.Mp2pQrCodeCanvas
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PhoneCloneScreen(
    uiState: VayuUiState,
    localDeviceName: String,
    onSetCloneRole: (Boolean) -> Unit,
    onToggleCategory: (CloneCategoryType) -> Unit,
    onOpenWhatsAppPermissionDialog: () -> Unit,
    onDismissWhatsAppPermissionDialog: () -> Unit,
    onAuthorizeWhatsAppBackup: () -> Unit,
    onStartMigration: () -> Unit,
    onOpenQrScanner: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val selectedBytes = uiState.cloneCategories.filter { it.isSelected }.sumOf { it.totalBytes }
    val selectedItemsCount = uiState.cloneCategories.filter { it.isSelected }.sumOf { it.itemCount }

    // Interactive WhatsApp Scoped Storage Permission Bottom Sheet
    if (uiState.isWhatsAppPermissionSheetVisible) {
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
        ModalBottomSheet(
            onDismissRequest = onDismissWhatsAppPermissionDialog,
            sheetState = sheetState,
            containerColor = CyberSlateCard,
            contentColor = TextPrimaryIce,
            shape = RoundedCornerShape(topStart = 28.dp, topEnd = 28.dp),
            modifier = Modifier.testTag("whatsapp_storage_permission_sheet")
        ) {
            WhatsAppScopedStorageSheetContent(
                scopedStoragePath = uiState.whatsAppScopedStoragePath,
                isAuthorized = uiState.isWhatsAppBackupAuthorized,
                onAuthorizeAndStage = onAuthorizeWhatsAppBackup,
                onDismiss = onDismissWhatsAppPermissionDialog
            )
        }
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("phone_clone_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. TOP BANNER: "Switching to a New Phone?"
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(28.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.35f), RoundedCornerShape(28.dp))
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(22.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(NeonCyan.copy(alpha = 0.25f), ElectricViolet.copy(alpha = 0.25f))
                                )
                            )
                            .border(1.dp, NeonCyan.copy(alpha = 0.5f), CircleShape),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.PhonelinkSetup,
                            contentDescription = "Phone Clone",
                            tint = NeonCyan,
                            modifier = Modifier.size(28.dp)
                        )
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    Text(
                        text = stringResource(id = R.string.clone_banner),
                        style = MaterialTheme.typography.headlineMedium,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.ExtraBold,
                        textAlign = TextAlign.Center
                    )

                    Spacer(modifier = Modifier.height(6.dp))

                    Text(
                        text = stringResource(id = R.string.clone_sub),
                        style = MaterialTheme.typography.bodyMedium,
                        color = TextSecondarySlate,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.testTag("phone_clone_banner_text")
                    )
                }
            }
        }

        // 2. TWO CLEAR, MASSIVE ROLE CARDS:
        // [ I AM SENDING FROM OLD PHONE ] and [ I AM RECEIVING ON NEW PHONE ]
        item {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                // Card 1: [ I AM SENDING FROM OLD PHONE ]
                val isOldSelected = uiState.cloneRoleIsOldDevice
                Surface(
                    color = if (isOldSelected) NeonCyan.copy(alpha = 0.14f) else CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = if (isOldSelected) 2.dp else 1.dp,
                            color = if (isOldSelected) NeonCyan else CyberBorderSubtle,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onSetCloneRole(true) }
                        .testTag("clone_role_source_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isOldSelected) NeonCyan else CyberSlateElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhonelinkRing,
                                    contentDescription = "Old Phone",
                                    tint = if (isOldSelected) CyberObsidian else NeonCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "I AM SENDING FROM OLD PHONE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isOldSelected) NeonCyan else TextPrimaryIce,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Send everything from $localDeviceName",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isOldSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isOldSelected) NeonCyan else TextSecondarySlate,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }

                // Card 2: [ I AM RECEIVING ON NEW PHONE ]
                val isNewSelected = !uiState.cloneRoleIsOldDevice
                Surface(
                    color = if (isNewSelected) ElectricViolet.copy(alpha = 0.16f) else CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(24.dp))
                        .border(
                            width = if (isNewSelected) 2.dp else 1.dp,
                            color = if (isNewSelected) ElectricViolet else CyberBorderSubtle,
                            shape = RoundedCornerShape(24.dp)
                        )
                        .clickable { onSetCloneRole(false) }
                        .testTag("clone_role_target_button")
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(52.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .background(if (isNewSelected) ElectricViolet else CyberSlateElevated),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PhonelinkSetup,
                                    contentDescription = "New Phone",
                                    tint = TextPrimaryIce,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(16.dp))
                            Column {
                                Text(
                                    text = "I AM RECEIVING ON NEW PHONE",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = if (isNewSelected) ElectricSky else TextPrimaryIce,
                                    fontWeight = FontWeight.ExtraBold
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "Receive all data onto this device",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        Icon(
                            imageVector = if (isNewSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                            contentDescription = null,
                            tint = if (isNewSelected) ElectricSky else TextSecondarySlate,
                            modifier = Modifier.size(26.dp)
                        )
                    }
                }
            }
        }

        // When "I AM SENDING FROM OLD PHONE" is selected, show large button: [ 📷 SCAN NEW PHONE'S QR TO CONNECT ]
        if (uiState.cloneRoleIsOldDevice) {
            item {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.5.dp, NeonCyan.copy(alpha = 0.65f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Connect to Your New Phone",
                            style = MaterialTheme.typography.titleMedium,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Scan the QR code displayed on your new phone for instant 5 GHz pairing",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onOpenQrScanner,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = CyberObsidian
                            ),
                            shape = RoundedCornerShape(18.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(56.dp)
                                .testTag("clone_scan_new_phone_qr_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CameraAlt,
                                contentDescription = null,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "📷 SCAN NEW PHONE'S QR TO CONNECT",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }
            }
        }

        // If "I AM RECEIVING ON NEW PHONE" is selected, show clean QR code for the old phone to scan
        if (!uiState.cloneRoleIsOldDevice && uiState.pairingSession != null) {
            item {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(24.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, ElectricViolet.copy(alpha = 0.6f), RoundedCornerShape(24.dp))
                ) {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(20.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Scan with Your Old Phone",
                            style = MaterialTheme.typography.titleLarge,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Open WarpDrop on your old phone and scan this code to connect",
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Mp2pQrCodeCanvas(qrMatrix = uiState.pairingSession.qrMatrix)
                    }
                }
            }
        }

        // 3. CLEAN CHECKLISTS Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Select What to Copy",
                    style = MaterialTheme.typography.titleLarge,
                    color = TextPrimaryIce,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "${VayuDropViewModel.formatBytes(selectedBytes)} selected",
                    style = MaterialTheme.typography.labelLarge,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // Clean Checklists for: WhatsApp Chats, Contacts, Photos & Videos, Installed Apps
        items(uiState.cloneCategories, key = { it.category.name }) { item ->
            val isWhatsApp = item.category == CloneCategoryType.WHATSAPP_CHATS
            ConsumerCloneChecklistCard(
                item = item,
                isWhatsAppBackupAuthorized = uiState.isWhatsAppBackupAuthorized,
                onCardClick = {
                    if (isWhatsApp) {
                        onOpenWhatsAppPermissionDialog()
                    } else {
                        onToggleCategory(item.category)
                    }
                },
                onCheckboxClick = { onToggleCategory(item.category) }
            )
        }

        // 4. SINGLE LARGE [START ZERO-LOSS 5 GHz PHONE CLONE] BUTTON
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Column {
                            Text(
                                text = "Total Selected",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondarySlate
                            )
                            Text(
                                text = "${VayuDropViewModel.formatBytes(selectedBytes)} • $selectedItemsCount items",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        val estMinutes = ((selectedBytes / (1024.0 * 1024.0 * 1024.0)) * 0.8).toInt().coerceAtLeast(1)
                        Surface(
                            color = EmeraldNominal.copy(alpha = 0.16f),
                            shape = RoundedCornerShape(12.dp)
                        ) {
                            Text(
                                text = "~$estMinutes min",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldNominal,
                                fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                            )
                        }
                    }

                    if (uiState.isCloningInProgress || uiState.cloneOverallProgress > 0f) {
                        Spacer(modifier = Modifier.height(14.dp))
                        LinearProgressIndicator(
                            progress = { uiState.cloneOverallProgress },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = EmeraldNominal,
                            trackColor = CyberObsidian
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = if (uiState.cloneOverallProgress >= 1f) {
                                "Phone Clone Complete! All selected data transferred."
                            } else {
                                "Copying your data... ${(uiState.cloneOverallProgress * 100).toInt()}%"
                            },
                            style = MaterialTheme.typography.bodySmall,
                            color = EmeraldNominal,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Button(
                        onClick = onStartMigration,
                        enabled = true,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian
                        ),
                        shape = RoundedCornerShape(18.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(58.dp)
                            .testTag("start_phone_clone_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.RocketLaunch,
                            contentDescription = null,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = stringResource(id = R.string.btn_start_clone),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConsumerCloneChecklistCard(
    item: CloneCategorySummary,
    isWhatsAppBackupAuthorized: Boolean,
    onCardClick: () -> Unit,
    onCheckboxClick: () -> Unit
) {
    val (icon, iconTint) = when (item.category) {
        CloneCategoryType.WHATSAPP_CHATS -> Icons.AutoMirrored.Filled.Chat to EmeraldNominal
        CloneCategoryType.CONTACTS -> Icons.Default.Contacts to ElectricSky
        CloneCategoryType.MEDIA_GALLERY -> Icons.Default.PhotoLibrary to NeonCyan
        CloneCategoryType.INSTALLED_APKS -> Icons.Default.Apps to ElectricViolet
    }

    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(22.dp))
            .border(
                width = if (item.isSelected) 1.5.dp else 1.dp,
                color = if (item.isSelected) NeonCyan.copy(alpha = 0.6f) else CyberBorderSubtle,
                shape = RoundedCornerShape(22.dp)
            )
            .clickable { onCardClick() }
            .testTag("clone_category_${item.category.name.lowercase()}")
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
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
                            .size(48.dp)
                            .clip(RoundedCornerShape(14.dp))
                            .background(iconTint.copy(alpha = 0.16f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = icon,
                            contentDescription = item.category.title,
                            tint = iconTint,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(
                                text = item.category.title,
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.Bold
                            )
                            if (item.category == CloneCategoryType.WHATSAPP_CHATS) {
                                Spacer(modifier = Modifier.width(8.dp))
                                Surface(
                                    color = if (isWhatsAppBackupAuthorized) {
                                        EmeraldNominal.copy(alpha = 0.22f)
                                    } else {
                                        NeonCyan.copy(alpha = 0.15f)
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.testTag("whatsapp_staged_badge")
                                ) {
                                    Text(
                                        text = if (isWhatsAppBackupAuthorized) "Staged & Ready ✓" else "Tap to Authorize",
                                        style = MaterialTheme.typography.labelSmall,
                                        color = if (isWhatsAppBackupAuthorized) EmeraldNominal else NeonCyan,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp)
                                    )
                                }
                            }
                        }
                        Text(
                            text = item.category.subtitle,
                            style = MaterialTheme.typography.bodySmall,
                            color = TextSecondarySlate
                        )
                    }
                }

                Spacer(modifier = Modifier.width(8.dp))

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = VayuDropViewModel.formatBytes(item.totalBytes),
                        style = MaterialTheme.typography.titleSmall,
                        color = NeonCyan,
                        fontWeight = FontWeight.Bold
                    )
                    Text(
                        text = "${item.itemCount} items",
                        style = MaterialTheme.typography.labelSmall,
                        color = TextSecondarySlate
                    )
                }

                Spacer(modifier = Modifier.width(8.dp))

                IconButton(
                    onClick = onCheckboxClick,
                    modifier = Modifier.size(36.dp)
                ) {
                    Icon(
                        imageVector = if (item.isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                        contentDescription = "Select Category",
                        tint = if (item.isSelected) EmeraldNominal else TextSecondarySlate,
                        modifier = Modifier.size(26.dp)
                    )
                }
            }

            if (item.transferProgress > 0f) {
                Spacer(modifier = Modifier.height(10.dp))
                LinearProgressIndicator(
                    progress = { item.transferProgress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(6.dp)
                        .clip(RoundedCornerShape(3.dp)),
                    color = EmeraldNominal,
                    trackColor = CyberObsidian
                )
            }
        }
    }
}

@Composable
private fun WhatsAppScopedStorageSheetContent(
    scopedStoragePath: String,
    isAuthorized: Boolean,
    onAuthorizeAndStage: () -> Unit,
    onDismiss: () -> Unit
) {
    BackHandler {
        onDismiss()
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(40.dp)
                        .clip(CircleShape)
                        .background(CyberObsidian)
                        .testTag("back_whatsapp_sheet_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = NeonCyan
                    )
                }
                Spacer(modifier = Modifier.width(10.dp))
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(EmeraldNominal.copy(alpha = 0.18f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.FolderSpecial,
                        contentDescription = "WhatsApp Scoped Storage",
                        tint = EmeraldNominal,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = "WhatsApp Chats & Media Access",
                        style = MaterialTheme.typography.titleMedium,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.ExtraBold
                    )
                    Text(
                        text = "Android Scoped Storage Authorization",
                        style = MaterialTheme.typography.bodySmall,
                        color = EmeraldNominal
                    )
                }
            }

            IconButton(
                onClick = onDismiss,
                modifier = Modifier
                    .size(40.dp)
                    .clip(CircleShape)
                    .background(CyberObsidian)
                    .testTag("close_whatsapp_sheet_button")
            ) {
                Icon(
                    imageVector = Icons.Default.Close,
                    contentDescription = "Close",
                    tint = TextPrimaryIce
                )
            }
        }

        Text(
            text = "To copy your complete WhatsApp chat history, voice notes, and media without cloud upload delays, WarpDrop needs Scoped Storage access to your local WhatsApp backup folder:",
            style = MaterialTheme.typography.bodyMedium,
            color = TextSecondarySlate
        )

        // Highlighted Scoped Storage Path Box: "Android/media/com.whatsapp/WhatsApp/Databases"
        Surface(
            color = CyberObsidian,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .border(1.dp, EmeraldNominal.copy(alpha = 0.5f), RoundedCornerShape(16.dp))
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "TARGET SCOPED STORAGE DIRECTORY",
                    style = MaterialTheme.typography.labelSmall,
                    color = EmeraldNominal,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = scopedStoragePath,
                    style = MaterialTheme.typography.titleSmall,
                    fontFamily = FontFamily.Monospace,
                    color = TextPrimaryIce,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.testTag("whatsapp_scoped_storage_path_text")
                )
                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    text = "Includes msgstore.db.crypt15, Stickers, Voice Notes & Video Notes",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
            }
        }

        // Step-by-step instructions
        Surface(
            color = CyberSlateElevated,
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "How it works:",
                    style = MaterialTheme.typography.titleSmall,
                    color = TextPrimaryIce,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "1. Tap 'Authorize & Stage Local Backup' below to grant folder access.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
                Text(
                    text = "2. WarpDrop stages your encrypted msgstore.db.crypt15 database locally.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
                Text(
                    text = "3. Your chats transfer directly over 5 GHz Wi-Fi Direct with zero data loss.",
                    style = MaterialTheme.typography.bodySmall,
                    color = TextSecondarySlate
                )
            }
        }

        // Primary Action Button: "Authorize & Stage Local Backup"
        Button(
            onClick = onAuthorizeAndStage,
            colors = ButtonDefaults.buttonColors(
                containerColor = EmeraldNominal,
                contentColor = CyberObsidian
            ),
            shape = RoundedCornerShape(18.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
                .testTag("authorize_whatsapp_backup_button")
        ) {
            Icon(
                imageVector = if (isAuthorized) Icons.Default.VerifiedUser else Icons.Default.LockOpen,
                contentDescription = null,
                modifier = Modifier.size(20.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = "Authorize & Stage Local Backup",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.ExtraBold
            )
        }

        OutlinedButton(
            onClick = onDismiss,
            shape = RoundedCornerShape(16.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(48.dp)
        ) {
            Text(
                text = "Cancel",
                color = TextSecondarySlate,
                fontWeight = FontWeight.SemiBold
            )
        }

        Spacer(modifier = Modifier.height(12.dp))
    }
}
