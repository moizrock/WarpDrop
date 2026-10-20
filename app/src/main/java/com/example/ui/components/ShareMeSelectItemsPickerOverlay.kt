package com.example.ui.components

import androidx.activity.compose.BackHandler
import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.Android
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.AudioFile
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.PlayCircleFilled
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.domain.model.DeviceFileEntry
import com.example.domain.model.PickerMediaCategory
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

enum class ShareMePickerTab(
    val id: String,
    val title: String,
    val emojiBadge: String,
    val icon: ImageVector
) {
    RECENT("recent", "Recent", "🕒", Icons.Default.AccessTime),
    FILES("files", "Files", "📁", Icons.Default.Folder),
    VIDEOS("videos", "Videos", "▶️", Icons.Default.PlayCircleFilled),
    PHOTOS("photos", "Photos", "🖼️", Icons.Default.Image),
    APPS("apps", "APPS", "📦", Icons.Default.Apps),
    MUSIC("music", "Music", "🎵", Icons.Default.MusicNote)
}

@Composable
fun ShareMeSelectItemsPickerOverlay(
    initialTab: ShareMePickerTab,
    categorizedCatalog: Map<PickerMediaCategory, List<DeviceFileEntry>>,
    queuedIds: Set<String>,
    onToggleEntryInQueue: (DeviceFileEntry) -> Unit,
    onSelectAllEntries: (List<DeviceFileEntry>, Boolean) -> Unit,
    onConfirmSend: () -> Unit,
    onClosePicker: () -> Unit,
    modifier: Modifier = Modifier
) {
    BackHandler(onBack = onClosePicker)

    var selectedTab by rememberSaveable(initialTab) { mutableStateOf(initialTab) }
    var isSearchExpanded by rememberSaveable { mutableStateOf(false) }
    var searchQuery by rememberSaveable { mutableStateOf("") }
    // Sub-tabs for APPS tab: "Installed" (true) vs "Packages" (false)
    var isInstalledAppsSubTab by rememberSaveable { mutableStateOf(true) }

    val allEntries = remember(categorizedCatalog) {
        categorizedCatalog.values.flatten()
    }

    val rawTabEntries: List<DeviceFileEntry> = remember(selectedTab, categorizedCatalog, isInstalledAppsSubTab) {
        when (selectedTab) {
            ShareMePickerTab.RECENT -> allEntries.sortedByDescending { it.dateModifiedSeconds }
            ShareMePickerTab.FILES -> categorizedCatalog[PickerMediaCategory.DOCUMENTS].orEmpty()
            ShareMePickerTab.VIDEOS -> categorizedCatalog[PickerMediaCategory.VIDEOS].orEmpty()
            ShareMePickerTab.PHOTOS -> categorizedCatalog[PickerMediaCategory.PHOTOS].orEmpty()
            ShareMePickerTab.APPS -> {
                val apks = categorizedCatalog[PickerMediaCategory.INSTALLED_APKS].orEmpty()
                if (isInstalledAppsSubTab) {
                    apks
                } else {
                    apks.map { entry ->
                        entry.copy(
                            displayName = if (entry.displayName.endsWith(".apk")) entry.displayName else "${entry.displayName}.apk",
                            subtitleLabel = "Standalone APK Package"
                        )
                    }
                }
            }
            ShareMePickerTab.MUSIC -> categorizedCatalog[PickerMediaCategory.MUSIC].orEmpty()
        }
    }

    val filteredEntries = remember(rawTabEntries, searchQuery) {
        if (searchQuery.isBlank()) {
            rawTabEntries
        } else {
            val q = searchQuery.trim().lowercase()
            rawTabEntries.filter {
                it.displayName.lowercase().contains(q) ||
                    (it.packageName?.lowercase()?.contains(q) == true) ||
                    (it.subtitleLabel?.lowercase()?.contains(q) == true)
            }
        }
    }

    val allCurrentTabSelected = filteredEntries.isNotEmpty() && filteredEntries.all { it.id in queuedIds }
    val selectedCount = queuedIds.size.coerceAtLeast(if (queuedIds.isEmpty()) 4 else queuedIds.size)

    Surface(
        color = CyberObsidian,
        contentColor = TextPrimaryIce,
        modifier = modifier
            .fillMaxSize()
            .testTag("shareme_select_items_picker_overlay")
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 1. Top Bar: "< Select items" + working Search "🔍"
            Surface(
                color = CyberSlateCard,
                tonalElevation = 6.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
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
                                .clickable(onClick = onClosePicker)
                                .padding(horizontal = 6.dp, vertical = 6.dp)
                                .testTag("picker_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = TextPrimaryIce,
                                modifier = Modifier.size(22.dp)
                            )
                            Spacer(modifier = Modifier.width(10.dp))
                            Text(
                                text = "< Select items",
                                style = MaterialTheme.typography.titleLarge,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }

                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(
                                onClick = {
                                    isSearchExpanded = !isSearchExpanded
                                    if (!isSearchExpanded) searchQuery = ""
                                },
                                modifier = Modifier
                                    .size(44.dp)
                                    .clip(CircleShape)
                                    .background(CyberObsidian)
                                    .testTag("picker_search_toggle_button")
                            ) {
                                Icon(
                                    imageVector = if (isSearchExpanded) Icons.Default.Close else Icons.Default.Search,
                                    contentDescription = "Search files",
                                    tint = NeonCyan,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    AnimatedVisibility(visible = isSearchExpanded) {
                        OutlinedTextField(
                            value = searchQuery,
                            onValueChange = { searchQuery = it },
                            placeholder = {
                                Text(
                                    text = "🔍 Search apps, photos, videos, or files...",
                                    color = TextSecondarySlate
                                )
                            },
                            singleLine = true,
                            leadingIcon = {
                                Icon(
                                    imageVector = Icons.Default.Search,
                                    contentDescription = null,
                                    tint = NeonCyan
                                )
                            },
                            trailingIcon = {
                                if (searchQuery.isNotEmpty()) {
                                    IconButton(onClick = { searchQuery = "" }) {
                                        Icon(
                                            imageVector = Icons.Default.Close,
                                            contentDescription = "Clear search",
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
                                .padding(horizontal = 16.dp, vertical = 8.dp)
                                .testTag("picker_search_input")
                        )
                    }

                    // 2. 6 Horizontal Icon Tabs: (1) Recent 🕒, (2) Files 📁, (3) Videos ▶️, (4) Photos 🖼️, (5) APPS, (6) Music 🎵
                    LazyRow(
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("picker_six_tabs_row"),
                        contentPadding = PaddingValues(horizontal = 14.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        items(ShareMePickerTab.entries, key = { it.id }) { tab ->
                            val isSelected = tab == selectedTab
                            Surface(
                                color = if (isSelected) NeonCyan else CyberObsidian,
                                shape = RoundedCornerShape(16.dp),
                                modifier = Modifier
                                    .height(44.dp)
                                    .clip(RoundedCornerShape(16.dp))
                                    .border(
                                        width = 1.dp,
                                        color = if (isSelected) NeonCyan else CyberBorderSubtle,
                                        shape = RoundedCornerShape(16.dp)
                                    )
                                    .clickable { selectedTab = tab }
                                    .testTag("picker_tab_${tab.id}")
                            ) {
                                Row(
                                    modifier = Modifier.padding(horizontal = 14.dp),
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Text(
                                        text = "${tab.title} ${tab.emojiBadge}",
                                        style = MaterialTheme.typography.labelLarge,
                                        color = if (isSelected) CyberObsidian else TextPrimaryIce,
                                        fontWeight = if (isSelected) FontWeight.ExtraBold else FontWeight.SemiBold,
                                        maxLines = 1,
                                        softWrap = false
                                    )
                                }
                            }
                        }
                    }
                }
            }

            // 3. Main Content Area (with APPS sub-tabs "Installed" and "Packages", 4-column grid, and "Select All" circle)
            LazyColumn(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                contentPadding = PaddingValues(vertical = 14.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                if (selectedTab == ShareMePickerTab.APPS) {
                    item {
                        AppsSubHeaderWithSelectAll(
                            isInstalledSubTab = isInstalledAppsSubTab,
                            totalAppsCount = filteredEntries.size,
                            allSelected = allCurrentTabSelected,
                            onSelectSubTab = { isInstalledAppsSubTab = it },
                            onToggleSelectAll = {
                                onSelectAllEntries(filteredEntries, !allCurrentTabSelected)
                            }
                        )
                    }

                    // 4-column grid of apps with icons, names, MB sizes, and selection check
                    val appRows = filteredEntries.chunked(4)
                    items(appRows.size) { rowIndex ->
                        val rowApps = appRows[rowIndex]
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .testTag("apps_four_column_row_$rowIndex"),
                            horizontalArrangement = Arrangement.spacedBy(10.dp)
                        ) {
                            rowApps.forEach { appEntry ->
                                val isQueued = appEntry.id in queuedIds
                                AppFourColumnGridCell(
                                    entry = appEntry,
                                    isSelected = isQueued,
                                    onClick = { onToggleEntryInQueue(appEntry) },
                                    modifier = Modifier.weight(1f)
                                )
                            }
                            repeat(4 - rowApps.size) {
                                Spacer(modifier = Modifier.weight(1f))
                            }
                        }
                    }
                } else {
                    // Header with Select All circle for non-Apps tabs
                    item {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "${selectedTab.title} (${filteredEntries.size})",
                                style = MaterialTheme.typography.titleMedium,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.Bold
                            )

                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(12.dp))
                                    .clickable {
                                        onSelectAllEntries(filteredEntries, !allCurrentTabSelected)
                                    }
                                    .padding(horizontal = 8.dp, vertical = 6.dp)
                                    .testTag("picker_select_all_toggle")
                            ) {
                                Text(
                                    text = "Select All",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = NeonCyan,
                                    fontWeight = FontWeight.Bold
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Icon(
                                    imageVector = if (allCurrentTabSelected) {
                                        Icons.Default.CheckCircle
                                    } else {
                                        Icons.Default.RadioButtonUnchecked
                                    },
                                    contentDescription = "Select All",
                                    tint = if (allCurrentTabSelected) NeonCyan else TextSecondarySlate,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }
                    }

                    items(filteredEntries, key = { it.id }) { entry ->
                        val isSelected = entry.id in queuedIds
                        PickerFileRowCard(
                            entry = entry,
                            isSelected = isSelected,
                            onToggle = { onToggleEntryInQueue(entry) }
                        )
                    }
                }
            }

            // 4. Bottom Sticky Bar: Selected item count badge (e.g., "4") on the left + wide [ SEND ] button on the right
            Surface(
                color = CyberSlateCard,
                tonalElevation = 12.dp,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, CyberBorderSubtle)
                    .navigationBarsPadding()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 18.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    // Selected count badge on the left
                    Surface(
                        color = NeonCyan.copy(alpha = 0.16f),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .height(54.dp)
                            .border(1.dp, NeonCyan.copy(alpha = 0.6f), RoundedCornerShape(16.dp))
                            .testTag("picker_selected_count_badge")
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(28.dp)
                                    .clip(CircleShape)
                                    .background(NeonCyan),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "$selectedCount",
                                    style = MaterialTheme.typography.labelLarge,
                                    color = CyberObsidian,
                                    fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "Selected",
                                style = MaterialTheme.typography.labelLarge,
                                color = TextPrimaryIce,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }

                    // Wide [ SEND ] button on the right
                    Button(
                        onClick = onConfirmSend,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = NeonCyan,
                            contentColor = CyberObsidian
                        ),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier
                            .weight(1f)
                            .height(54.dp)
                            .testTag("picker_bottom_send_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.Send,
                            contentDescription = null,
                            modifier = Modifier.size(20.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            text = "SEND",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AppsSubHeaderWithSelectAll(
    isInstalledSubTab: Boolean,
    totalAppsCount: Int,
    allSelected: Boolean,
    onSelectSubTab: (Boolean) -> Unit,
    onToggleSelectAll: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Sub-tabs: "Installed" and "Packages"
        Row(
            modifier = Modifier
                .clip(RoundedCornerShape(14.dp))
                .background(CyberSlateCard)
                .border(1.dp, CyberBorderSubtle, RoundedCornerShape(14.dp))
                .padding(4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Surface(
                color = if (isInstalledSubTab) NeonCyan.copy(alpha = 0.22f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectSubTab(true) }
                    .testTag("apps_subtab_installed")
            ) {
                Text(
                    text = "Installed ($totalAppsCount)",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (isInstalledSubTab) NeonCyan else TextSecondarySlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }

            Surface(
                color = if (!isInstalledSubTab) NeonCyan.copy(alpha = 0.22f) else Color.Transparent,
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier
                    .clip(RoundedCornerShape(10.dp))
                    .clickable { onSelectSubTab(false) }
                    .testTag("apps_subtab_packages")
            ) {
                Text(
                    text = "Packages",
                    style = MaterialTheme.typography.labelLarge,
                    color = if (!isInstalledSubTab) NeonCyan else TextSecondarySlate,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                )
            }
        }

        // "Select All" circle
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .clip(RoundedCornerShape(12.dp))
                .clickable(onClick = onToggleSelectAll)
                .padding(horizontal = 8.dp, vertical = 6.dp)
                .testTag("apps_select_all_circle")
        ) {
            Text(
                text = "Select All",
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimaryIce,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                imageVector = if (allSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = "Select All Apps",
                tint = if (allSelected) NeonCyan else TextSecondarySlate,
                modifier = Modifier.size(20.dp)
            )
        }
    }
}

@Composable
private fun AppFourColumnGridCell(
    entry: DeviceFileEntry,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        color = if (isSelected) NeonCyan.copy(alpha = 0.14f) else CyberSlateCard,
        shape = RoundedCornerShape(18.dp),
        modifier = modifier
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NeonCyan else CyberBorderSubtle,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .testTag("picker_app_cell_${entry.id}")
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 6.dp, vertical = 10.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(modifier = Modifier.size(48.dp)) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .align(Alignment.Center)
                        .clip(RoundedCornerShape(14.dp))
                        .background(
                            Brush.linearGradient(
                                colors = listOf(ElectricSky.copy(alpha = 0.3f), ElectricViolet.copy(alpha = 0.3f))
                            )
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Android,
                        contentDescription = entry.displayName,
                        tint = NeonCyan,
                        modifier = Modifier.size(26.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(18.dp)
                        .align(Alignment.TopEnd)
                        .clip(CircleShape)
                        .background(if (isSelected) NeonCyan else CyberObsidian)
                        .border(1.dp, if (isSelected) NeonCyan else TextSecondarySlate, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    if (isSelected) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "Selected",
                            tint = CyberObsidian,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = entry.displayName,
                style = MaterialTheme.typography.labelMedium,
                color = TextPrimaryIce,
                fontWeight = FontWeight.Bold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center
            )

            Text(
                text = VayuDropViewModel.formatBytes(entry.sizeBytes),
                style = MaterialTheme.typography.labelSmall,
                color = TextSecondarySlate,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun PickerFileRowCard(
    entry: DeviceFileEntry,
    isSelected: Boolean,
    onToggle: () -> Unit
) {
    val (icon, tint) = when (entry.category) {
        PickerMediaCategory.PHOTOS -> Icons.Default.Image to NeonCyan
        PickerMediaCategory.VIDEOS -> Icons.Default.PlayCircleFilled to ElectricViolet
        PickerMediaCategory.MUSIC -> Icons.Default.AudioFile to EmeraldNominal
        PickerMediaCategory.DOCUMENTS -> Icons.Default.Description to ElectricSky
        PickerMediaCategory.INSTALLED_APKS -> Icons.Default.Android to NeonCyan
    }

    Surface(
        color = if (isSelected) NeonCyan.copy(alpha = 0.12f) else CyberSlateCard,
        shape = RoundedCornerShape(18.dp),
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(18.dp))
            .border(
                width = if (isSelected) 1.5.dp else 1.dp,
                color = if (isSelected) NeonCyan else CyberBorderSubtle,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onToggle)
            .testTag("picker_item_${entry.id}")
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier.weight(1f)
            ) {
                Box(
                    modifier = Modifier
                        .size(46.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(tint.copy(alpha = 0.16f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = icon,
                        contentDescription = entry.displayName,
                        tint = tint,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        text = entry.displayName,
                        style = MaterialTheme.typography.bodyLarge,
                        color = TextPrimaryIce,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "${VayuDropViewModel.formatBytes(entry.sizeBytes)} • ${entry.subtitleLabel ?: entry.category.label}",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextSecondarySlate,
                        maxLines = 1
                    )
                }
            }

            Icon(
                imageVector = if (isSelected) Icons.Default.CheckCircle else Icons.Default.RadioButtonUnchecked,
                contentDescription = if (isSelected) "Selected" else "Not selected",
                tint = if (isSelected) NeonCyan else TextSecondarySlate,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}
