package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.automirrored.filled.CompareArrows
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.EnhancedEncryption
import androidx.compose.material.icons.filled.FindReplace
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.Verified
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.example.domain.model.DuplicateCluster
import com.example.domain.model.VaultedItemRecord
import com.example.ui.theme.AmberThermalWarn
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSlateCard
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.ElectricViolet
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimaryIce
import com.example.ui.theme.TextSecondarySlate
import com.example.ui.viewmodel.VayuDropViewModel
import com.example.ui.viewmodel.VayuUiState

@Composable
fun DuplicateVaultScreen(
    uiState: VayuUiState,
    onRunDuplicateScan: () -> Unit,
    onPurgeDuplicates: () -> Unit,
    onLockSessionSnapshotInVault: () -> Unit,
    onRunDeltaSyncBenchmark: () -> Unit,
    modifier: Modifier = Modifier
) {
    val totalReclaimable = uiState.duplicateClusters.sumOf { it.reclaimableBytes }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .testTag("duplicate_vault_screen"),
        contentPadding = PaddingValues(horizontal = 18.dp, vertical = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // 1. Smart Duplicate Cleaner Header Card
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
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan.copy(alpha = 0.16f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.Shield,
                                    contentDescription = "Smart Storage Shield",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Smart Duplicate Cleaner",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Find and remove duplicate files to free up space",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        if (uiState.totalReclaimedBytesSession > 0L) {
                            Surface(
                                color = EmeraldNominal.copy(alpha = 0.16f),
                                shape = RoundedCornerShape(10.dp)
                            ) {
                                Text(
                                    text = "Freed ${VayuDropViewModel.formatBytes(uiState.totalReclaimedBytesSession)}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = EmeraldNominal,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        Button(
                            onClick = onRunDuplicateScan,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = NeonCyan,
                                contentColor = CyberObsidian
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("scan_sha256_duplicates_button")
                        ) {
                            Icon(Icons.Default.FindReplace, contentDescription = null, modifier = Modifier.size(18.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (uiState.isScanningDuplicates) "Scanning..." else "Scan Storage",
                                fontWeight = FontWeight.Bold
                            )
                        }

                        OutlinedButton(
                            onClick = onPurgeDuplicates,
                            enabled = uiState.duplicateClusters.isNotEmpty(),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .weight(1f)
                                .height(48.dp)
                                .testTag("purge_duplicates_button")
                        ) {
                            Icon(
                                imageVector = Icons.Default.CleaningServices,
                                contentDescription = null,
                                tint = AmberThermalWarn,
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Clean (${VayuDropViewModel.formatBytes(totalReclaimable)})",
                                color = TextPrimaryIce
                            )
                        }
                    }
                }
            }
        }

        // 2. Duplicate Scan Results
        if (uiState.duplicateClusters.isEmpty()) {
            item {
                Surface(
                    color = CyberSlateCard,
                    shape = RoundedCornerShape(22.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, CyberBorderSubtle, RoundedCornerShape(22.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(18.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Verified,
                            contentDescription = "Storage Clean",
                            tint = EmeraldNominal,
                            modifier = Modifier.size(30.dp)
                        )
                        Spacer(modifier = Modifier.width(14.dp))
                        Column {
                            Text(
                                text = "Your Storage is Clean",
                                style = MaterialTheme.typography.titleMedium,
                                color = EmeraldNominal,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = "No duplicate photos or extra file copies were found.",
                                style = MaterialTheme.typography.bodySmall,
                                color = TextSecondarySlate
                            )
                        }
                    }
                }
            }
        } else {
            items(uiState.duplicateClusters, key = { it.sha256Hash }) { cluster ->
                DuplicateClusterCard(cluster = cluster)
            }
        }

        // 3. Private File Safe (Consumer Encrypted Folder)
        item {
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(26.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, ElectricViolet.copy(alpha = 0.55f), RoundedCornerShape(26.dp))
            ) {
                Column(modifier = Modifier.padding(20.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.weight(1f)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(48.dp)
                                    .clip(CircleShape)
                                    .background(ElectricViolet.copy(alpha = 0.18f)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.EnhancedEncryption,
                                    contentDescription = "Private Safe",
                                    tint = ElectricViolet,
                                    modifier = Modifier.size(24.dp)
                                )
                            }
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Private File Safe",
                                    style = MaterialTheme.typography.titleLarge,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Keep personal photos & files locked on this phone",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        Button(
                            onClick = onLockSessionSnapshotInVault,
                            colors = ButtonDefaults.buttonColors(
                                containerColor = ElectricViolet,
                                contentColor = TextPrimaryIce
                            ),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .height(46.dp)
                                .testTag("lock_vault_snapshot_button")
                        ) {
                            Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text("Lock Backup", fontWeight = FontWeight.Bold)
                        }
                    }

                    if (uiState.vaultedRecords.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(14.dp))
                        uiState.vaultedRecords.forEach { record ->
                            VaultRecordRow(record = record)
                            Spacer(modifier = Modifier.height(8.dp))
                        }
                    }
                }
            }
        }

        // 4. Smart Update Transfer Saver Card
        item {
            val deltaReport = uiState.latestDeltaSyncReport
            Surface(
                color = CyberSlateCard,
                shape = RoundedCornerShape(24.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorderSubtle, RoundedCornerShape(24.dp))
                    .testTag("delta_sync_engine_card")
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
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
                                imageVector = Icons.AutoMirrored.Filled.CompareArrows,
                                contentDescription = "Smart Fast Sync",
                                tint = ElectricSky,
                                modifier = Modifier.size(24.dp)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column {
                                Text(
                                    text = "Smart Data Saver",
                                    style = MaterialTheme.typography.titleMedium,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Sends only updated parts of large files to save time",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                        }

                        OutlinedButton(
                            onClick = onRunDeltaSyncBenchmark,
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier
                                .height(42.dp)
                                .testTag("run_delta_sync_button")
                        ) {
                            Text("Optimize", color = NeonCyan, fontWeight = FontWeight.Bold)
                        }
                    }

                    if (deltaReport != null) {
                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(14.dp))
                                .background(CyberObsidian)
                                .padding(14.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = deltaReport.fileName,
                                    style = MaterialTheme.typography.titleSmall,
                                    color = TextPrimaryIce,
                                    fontWeight = FontWeight.SemiBold,
                                    maxLines = 1,
                                    overflow = TextOverflow.Ellipsis
                                )
                                Text(
                                    text = "Sent ${VayuDropViewModel.formatBytes(deltaReport.compressedTransmittedBytes.toLong())} instead of ${VayuDropViewModel.formatBytes(deltaReport.modifiedTotalBytes.toLong())}",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = TextSecondarySlate
                                )
                            }
                            Text(
                                text = "Saved ${"%.0f".format(deltaReport.bandwidthSavedPercent)}%",
                                style = MaterialTheme.typography.labelLarge,
                                color = EmeraldNominal,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DuplicateClusterCard(cluster: DuplicateCluster) {
    Surface(
        color = CyberSlateCard,
        shape = RoundedCornerShape(20.dp),
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(20.dp))
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Duplicate File Group",
                    style = MaterialTheme.typography.titleSmall,
                    color = NeonCyan,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    text = "Free up ${VayuDropViewModel.formatBytes(cluster.reclaimableBytes)}",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberThermalWarn,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            cluster.entries.forEach { entry ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                        .clip(RoundedCornerShape(10.dp))
                        .background(CyberObsidian)
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = entry.fileName,
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryIce,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis,
                        modifier = Modifier.weight(1f)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = if (entry.isOriginalKeep) "Keep Original" else "Extra Copy",
                        style = MaterialTheme.typography.labelSmall,
                        color = if (entry.isOriginalKeep) EmeraldNominal else AmberThermalWarn,
                        fontWeight = FontWeight.Bold
                    )
                }
            }
        }
    }
}

@Composable
private fun VaultRecordRow(record: VaultedItemRecord) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(CyberObsidian)
            .border(1.dp, CyberBorderSubtle, RoundedCornerShape(14.dp))
            .padding(14.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = record.originalName,
                style = MaterialTheme.typography.titleSmall,
                color = TextPrimaryIce,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = "Protected in Private Safe • ${VayuDropViewModel.formatBytes(record.sizeBytes)}",
                style = MaterialTheme.typography.bodySmall,
                color = TextSecondarySlate
            )
        }
        Icon(
            imageVector = Icons.Default.Lock,
            contentDescription = "Locked",
            tint = ElectricViolet,
            modifier = Modifier.size(18.dp)
        )
    }
}
