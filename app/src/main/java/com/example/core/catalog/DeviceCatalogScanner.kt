package com.example.core.catalog

import android.content.ContentUris
import android.content.Context
import android.content.pm.ApplicationInfo
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Shader
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.os.StatFs
import android.provider.MediaStore
import android.provider.OpenableColumns
import com.example.domain.model.DeviceFileEntry
import com.example.domain.model.PickerMediaCategory
import com.example.domain.model.StoragePartitionMetrics
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.BufferedReader
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.nio.ByteBuffer
import java.nio.ByteOrder
import kotlin.math.sin

/**
 * Queries real device files and installed applications using Android `MediaStore`,
 * `ContentResolver`, `PackageManager`, and `StatFs`.
 * When `MediaStore` is empty (such as on a fresh emulator), populates realistic consumer
 * preview cards backed by generated media files on disk so the Transfer selector and
 * Inbuilt Media Viewer feel alive and immediately interactive.
 */
class DeviceCatalogScanner(
    private val context: Context
) {
    private val showcaseMediaDir: File by lazy {
        File(context.filesDir, "vayudrop_showcase_media").apply { mkdirs() }
    }

    /**
     * Scans all 5 categories (`PHOTOS`, `VIDEOS`, `MUSIC`, `DOCUMENTS`, `INSTALLED_APKS`)
     * from Android `MediaStore` and `PackageManager`, falling back to realistic preview items
     * when `MediaStore` is empty.
     */
    suspend fun scanAllCategories(): Map<PickerMediaCategory, List<DeviceFileEntry>> = withContext(Dispatchers.IO) {
        val photos = queryMediaStoreCollection(
            category = PickerMediaCategory.PHOTOS,
            collectionUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
            defaultMime = "image/jpeg"
        ).ifEmpty { buildRealisticPhotoPreviews() }

        val videos = queryMediaStoreCollection(
            category = PickerMediaCategory.VIDEOS,
            collectionUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            defaultMime = "video/mp4"
        ).ifEmpty { buildRealisticVideoPreviews() }

        val music = queryMediaStoreCollection(
            category = PickerMediaCategory.MUSIC,
            collectionUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
            defaultMime = "audio/mpeg"
        ).ifEmpty { buildRealisticMusicPreviews() }

        val documents = queryDocumentsCollection()
            .ifEmpty { buildRealisticDocumentPreviews() }

        val installedApks = queryInstalledApksWithSplits()

        mapOf(
            PickerMediaCategory.PHOTOS to photos,
            PickerMediaCategory.VIDEOS to videos,
            PickerMediaCategory.MUSIC to music,
            PickerMediaCategory.DOCUMENTS to documents,
            PickerMediaCategory.INSTALLED_APKS to installedApks
        )
    }

    /**
     * Queries `PackageManager` for real installed applications, loads their real app icon,
     * package name, version, and total size.
     */
    suspend fun queryInstalledApksWithSplits(): List<DeviceFileEntry> = withContext(Dispatchers.IO) {
        val pm = context.packageManager
        val packages = runCatching { pm.getInstalledPackages(0) }.getOrDefault(emptyList())

        val results = mutableListOf<DeviceFileEntry>()
        for (pkgInfo in packages) {
            val appInfo = pkgInfo.applicationInfo ?: continue
            val baseApkPath = appInfo.sourceDir ?: continue
            val baseFile = File(baseApkPath)
            if (!baseFile.exists()) continue

            val isUserApp = (appInfo.flags and ApplicationInfo.FLAG_SYSTEM) == 0 ||
                (appInfo.flags and ApplicationInfo.FLAG_UPDATED_SYSTEM_APP) != 0

            val splitDirs = appInfo.splitSourceDirs?.toList().orEmpty()
            val validSplitFiles = splitDirs.map { File(it) }.filter { it.exists() }
            val totalApkBytes = baseFile.length() + validSplitFiles.sumOf { it.length() }

            val rawLabel = runCatching { pm.getApplicationLabel(appInfo).toString() }
                .getOrDefault(pkgInfo.packageName.substringAfterLast('.').replaceFirstChar { it.uppercase() })
            val cleanLabel = rawLabel
                .removePrefix("com.android.")
                .removePrefix("com.google.android.")
                .replaceFirstChar { it.uppercase() }
            val iconDrawable = runCatching { pm.getApplicationIcon(appInfo) }.getOrNull()

            results.add(
                DeviceFileEntry(
                    id = "apk_${pkgInfo.packageName}",
                    displayName = cleanLabel,
                    category = PickerMediaCategory.INSTALLED_APKS,
                    uri = Uri.fromFile(baseFile),
                    localPath = baseFile.absolutePath,
                    sizeBytes = totalApkBytes,
                    mimeType = "application/vnd.android.package-archive",
                    dateModifiedSeconds = baseFile.lastModified() / 1000L,
                    packageName = pkgInfo.packageName,
                    versionName = pkgInfo.versionName ?: "1.0",
                    splitApkPaths = validSplitFiles.map { it.absolutePath },
                    appIconDrawable = iconDrawable,
                    subtitleLabel = if (isUserApp) "Installed App • v${pkgInfo.versionName ?: "1.0"}" else "System App • v${pkgInfo.versionName ?: "1.0"}"
                )
            )
        }

        results.sortedWith(
            compareByDescending<DeviceFileEntry> { it.subtitleLabel?.startsWith("Installed App") == true }
                .thenByDescending { it.sizeBytes }
        ).take(60)
    }

    private fun buildRealisticPhotoPreviews(): List<DeviceFileEntry> {
        data class PhotoSpec(
            val id: String,
            val fileName: String,
            val subtitle: String,
            val simulatedSizeBytes: Long,
            val colorStart: Int,
            val colorEnd: Int,
            val accentColor: Int,
            val paletteIdx: Int
        )

        val specs = listOf(
            PhotoSpec(
                id = "photo_sunset_horizon",
                fileName = "Sunset_Beach_Horizon_4K.jpg",
                subtitle = "4K UHD • 3840 × 2160 • HDR",
                simulatedSizeBytes = 6_840_000L,
                colorStart = 0xFF0F172A.toInt(),
                colorEnd = 0xFF0284C7.toInt(),
                accentColor = 0xFF00F0FF.toInt(),
                paletteIdx = 0
            ),
            PhotoSpec(
                id = "photo_mountain_peak",
                fileName = "Alpine_Glacier_Sunrise.jpg",
                subtitle = "RAW Portrait • 4032 × 3024",
                simulatedSizeBytes = 8_420_000L,
                colorStart = 0xFF1E1B4B.toInt(),
                colorEnd = 0xFF7C3AED.toInt(),
                accentColor = 0xFF38BDF8.toInt(),
                paletteIdx = 1
            ),
            PhotoSpec(
                id = "photo_cyber_skyline",
                fileName = "Downtown_Night_Lights.jpg",
                subtitle = "Night Mode • 3840 × 2160",
                simulatedSizeBytes = 5_190_000L,
                colorStart = 0xFF090D16.toInt(),
                colorEnd = 0xFF059669.toInt(),
                accentColor = 0xFF10B981.toInt(),
                paletteIdx = 2
            ),
            PhotoSpec(
                id = "photo_golden_portrait",
                fileName = "Weekend_Getaway_Album.jpg",
                subtitle = "Live Photo • 3024 × 3024",
                simulatedSizeBytes = 4_350_000L,
                colorStart = 0xFF1F2937.toInt(),
                colorEnd = 0xFFD97706.toInt(),
                accentColor = 0xFFF59E0B.toInt(),
                paletteIdx = 3
            )
        )

        val nowSec = System.currentTimeMillis() / 1000L
        return specs.mapIndexed { idx, spec ->
            val outFile = File(showcaseMediaDir, spec.fileName)
            if (!outFile.exists() || outFile.length() == 0L) {
                generateScenicPhotoFile(
                    targetFile = outFile,
                    title = spec.fileName.substringBeforeLast('.').replace('_', ' '),
                    colorStart = spec.colorStart,
                    colorEnd = spec.colorEnd,
                    accentColor = spec.accentColor
                )
            }
            DeviceFileEntry(
                id = spec.id,
                displayName = spec.fileName,
                category = PickerMediaCategory.PHOTOS,
                uri = Uri.fromFile(outFile),
                localPath = outFile.absolutePath,
                sizeBytes = spec.simulatedSizeBytes,
                mimeType = "image/jpeg",
                dateModifiedSeconds = nowSec - idx * 3600L,
                subtitleLabel = spec.subtitle,
                previewPaletteIndex = spec.paletteIdx,
                isSamplePreview = true
            )
        }
    }

    private fun buildRealisticVideoPreviews(): List<DeviceFileEntry> {
        data class VideoSpec(
            val id: String,
            val fileName: String,
            val subtitle: String,
            val durationSec: Int,
            val simulatedSizeBytes: Long,
            val paletteIdx: Int
        )

        val specs = listOf(
            VideoSpec(
                id = "video_coastline_drone",
                fileName = "Coastline_Drone_Flight_4K.mp4",
                subtitle = "0:48 • 4K 60fps • Dolby Vision",
                durationSec = 48,
                simulatedSizeBytes = 148_500_000L,
                paletteIdx = 0
            ),
            VideoSpec(
                id = "video_city_timelapse",
                fileName = "City_Skyline_Hyperlapse.mp4",
                subtitle = "1:15 • 1080p 120fps • Slow-Mo",
                durationSec = 75,
                simulatedSizeBytes = 215_800_000L,
                paletteIdx = 1
            ),
            VideoSpec(
                id = "video_concert_stage",
                fileName = "Live_Concert_Encore_HD.mp4",
                subtitle = "2:04 • 4K UHD • Stereo Audio",
                durationSec = 124,
                simulatedSizeBytes = 312_400_000L,
                paletteIdx = 2
            ),
            VideoSpec(
                id = "video_mountain_roadtrip",
                fileName = "Roadtrip_Highlights_Reel.mp4",
                subtitle = "0:36 • 1080p 60fps • HDR10",
                durationSec = 36,
                simulatedSizeBytes = 94_200_000L,
                paletteIdx = 3
            )
        )

        val nowSec = System.currentTimeMillis() / 1000L
        return specs.mapIndexed { idx, spec ->
            val outFile = File(showcaseMediaDir, spec.fileName)
            if (!outFile.exists()) {
                runCatching {
                    outFile.writeBytes("VAYUDROP_VIDEO_STREAM_${spec.fileName}_${spec.durationSec}".toByteArray())
                }
            }
            DeviceFileEntry(
                id = spec.id,
                displayName = spec.fileName,
                category = PickerMediaCategory.VIDEOS,
                uri = Uri.fromFile(outFile),
                localPath = outFile.absolutePath,
                sizeBytes = spec.simulatedSizeBytes,
                mimeType = "video/mp4",
                dateModifiedSeconds = nowSec - idx * 7200L,
                subtitleLabel = spec.subtitle,
                durationSeconds = spec.durationSec,
                previewPaletteIndex = spec.paletteIdx,
                isSamplePreview = true
            )
        }
    }

    private fun buildRealisticMusicPreviews(): List<DeviceFileEntry> {
        data class MusicSpec(
            val id: String,
            val fileName: String,
            val subtitle: String,
            val durationSec: Int,
            val simulatedSizeBytes: Long,
            val freqHz: Double,
            val paletteIdx: Int
        )

        val specs = listOf(
            MusicSpec(
                id = "music_midnight_horizon",
                fileName = "Midnight_Horizon_Synth.wav",
                subtitle = "3:28 • Studio Lossless • 24-bit",
                durationSec = 208,
                simulatedSizeBytes = 34_600_000L,
                freqHz = 440.0,
                paletteIdx = 0
            ),
            MusicSpec(
                id = "music_acoustic_breeze",
                fileName = "Acoustic_Morning_Sessions.wav",
                subtitle = "2:54 • Unplugged Live • Stereo",
                durationSec = 174,
                simulatedSizeBytes = 28_900_000L,
                freqHz = 528.0,
                paletteIdx = 1
            ),
            MusicSpec(
                id = "music_deep_focus_ambient",
                fileName = "Deep_Focus_Ambient_Flow.wav",
                subtitle = "4:12 • Spatial Audio • Chill",
                durationSec = 252,
                simulatedSizeBytes = 41_200_000L,
                freqHz = 396.0,
                paletteIdx = 2
            ),
            MusicSpec(
                id = "music_sunset_drive",
                fileName = "Pacific_Coast_Drive_Beat.wav",
                subtitle = "3:05 • High-Res Audio • 320kbps",
                durationSec = 185,
                simulatedSizeBytes = 19_400_000L,
                freqHz = 493.88,
                paletteIdx = 3
            )
        )

        val nowSec = System.currentTimeMillis() / 1000L
        return specs.mapIndexed { idx, spec ->
            val outFile = File(showcaseMediaDir, spec.fileName)
            if (!outFile.exists() || outFile.length() == 0L) {
                generatePlayableWavFile(outFile, spec.freqHz, durationSeconds = 3)
            }
            DeviceFileEntry(
                id = spec.id,
                displayName = spec.fileName,
                category = PickerMediaCategory.MUSIC,
                uri = Uri.fromFile(outFile),
                localPath = outFile.absolutePath,
                sizeBytes = spec.simulatedSizeBytes,
                mimeType = "audio/wav",
                dateModifiedSeconds = nowSec - idx * 5400L,
                subtitleLabel = spec.subtitle,
                durationSeconds = spec.durationSec,
                previewPaletteIndex = spec.paletteIdx,
                isSamplePreview = true
            )
        }
    }

    private fun buildRealisticDocumentPreviews(): List<DeviceFileEntry> {
        data class DocSpec(
            val id: String,
            val fileName: String,
            val subtitle: String,
            val simulatedSizeBytes: Long,
            val mime: String,
            val paletteIdx: Int
        )

        val specs = listOf(
            DocSpec(
                id = "doc_product_keynote",
                fileName = "Q4_Product_Keynote_Deck.pdf",
                subtitle = "PDF Presentation • 28 Slides",
                simulatedSizeBytes = 18_450_000L,
                mime = "application/pdf",
                paletteIdx = 0
            ),
            DocSpec(
                id = "doc_travel_itinerary",
                fileName = "Europe_Trip_Passes_2026.pdf",
                subtitle = "PDF Document • Boarding & Hotel",
                simulatedSizeBytes = 4_120_000L,
                mime = "application/pdf",
                paletteIdx = 1
            ),
            DocSpec(
                id = "doc_design_assets",
                fileName = "Brand_UI_Kit_Vectors.zip",
                subtitle = "ZIP Archive • 64 Assets",
                simulatedSizeBytes = 86_300_000L,
                mime = "application/zip",
                paletteIdx = 2
            ),
            DocSpec(
                id = "doc_financial_summary",
                fileName = "Annual_Budget_Planner.xlsx",
                subtitle = "Spreadsheet • Updated Today",
                simulatedSizeBytes = 2_780_000L,
                mime = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
                paletteIdx = 3
            )
        )

        val nowSec = System.currentTimeMillis() / 1000L
        return specs.mapIndexed { idx, spec ->
            val outFile = File(showcaseMediaDir, spec.fileName)
            if (!outFile.exists()) {
                runCatching {
                    outFile.writeBytes("VAYUDROP_DOCUMENT_${spec.fileName}".toByteArray())
                }
            }
            DeviceFileEntry(
                id = spec.id,
                displayName = spec.fileName,
                category = PickerMediaCategory.DOCUMENTS,
                uri = Uri.fromFile(outFile),
                localPath = outFile.absolutePath,
                sizeBytes = spec.simulatedSizeBytes,
                mimeType = spec.mime,
                dateModifiedSeconds = nowSec - idx * 4800L,
                subtitleLabel = spec.subtitle,
                previewPaletteIndex = spec.paletteIdx,
                isSamplePreview = true
            )
        }
    }

    private fun generateScenicPhotoFile(
        targetFile: File,
        title: String,
        colorStart: Int,
        colorEnd: Int,
        accentColor: Int
    ) {
        runCatching {
            val width = 640
            val height = 420
            val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)

            val bgPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                shader = LinearGradient(
                    0f, 0f, width.toFloat(), height.toFloat(),
                    colorStart, colorEnd, Shader.TileMode.CLAMP
                )
            }
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), bgPaint)

            // Draw glowing sun / moon orb
            val orbPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = accentColor
                alpha = 95
            }
            canvas.drawCircle(width * 0.72f, height * 0.34f, 88f, orbPaint)
            orbPaint.alpha = 190
            canvas.drawCircle(width * 0.72f, height * 0.34f, 48f, orbPaint)

            // Draw stylized horizon waves
            val wavePaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0x44000000
                style = Paint.Style.FILL
            }
            canvas.drawRect(0f, height * 0.68f, width.toFloat(), height.toFloat(), wavePaint)

            // Subtle title watermark
            val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
                color = 0xEEFFFFFF.toInt()
                textSize = 26f
                isFakeBoldText = true
            }
            canvas.drawText(title, 32f, height - 38f, textPaint)

            FileOutputStream(targetFile).use { fos ->
                bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fos)
            }
        }
    }

    private fun generatePlayableWavFile(
        targetFile: File,
        freqHz: Double,
        durationSeconds: Int
    ) {
        runCatching {
            val sampleRate = 8000
            val numSamples = sampleRate * durationSeconds
            val pcmData = ByteArray(numSamples * 2)
            for (i in 0 until numSamples) {
                val t = i.toDouble() / sampleRate.toDouble()
                val envelope = if (i < 800) i / 800.0 else if (i > numSamples - 800) (numSamples - i) / 800.0 else 1.0
                val sampleVal = (sin(2.0 * Math.PI * freqHz * t) * 0.35 * envelope * Short.MAX_VALUE).toInt().toShort()
                pcmData[i * 2] = (sampleVal.toInt() and 0xFF).toByte()
                pcmData[i * 2 + 1] = ((sampleVal.toInt() shr 8) and 0xFF).toByte()
            }

            val totalDataLen = pcmData.size + 36
            val header = ByteBuffer.allocate(44).order(ByteOrder.LITTLE_ENDIAN).apply {
                put("RIFF".toByteArray())
                putInt(totalDataLen)
                put("WAVE".toByteArray())
                put("fmt ".toByteArray())
                putInt(16)
                putShort(1) // PCM
                putShort(1) // Mono
                putInt(sampleRate)
                putInt(sampleRate * 2)
                putShort(2)
                putShort(16)
                put("data".toByteArray())
                putInt(pcmData.size)
            }.array()

            FileOutputStream(targetFile).use { fos ->
                fos.write(header)
                fos.write(pcmData)
            }
        }
    }

    private fun queryMediaStoreCollection(
        category: PickerMediaCategory,
        collectionUri: Uri,
        defaultMime: String
    ): List<DeviceFileEntry> {
        val entries = mutableListOf<DeviceFileEntry>()
        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATA
        )
        val sortOrder = "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"

        runCatching {
            context.contentResolver.query(
                collectionUri,
                projection,
                null,
                null,
                sortOrder
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

                var count = 0
                while (cursor.moveToNext() && count < 100) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: "media_$id"
                    val size = cursor.getLong(sizeCol)
                    if (size <= 0L) continue
                    val mime = cursor.getString(mimeCol) ?: defaultMime
                    val dateSec = cursor.getLong(dateCol)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) else null
                    val contentUri = ContentUris.withAppendedId(collectionUri, id)

                    entries.add(
                        DeviceFileEntry(
                            id = "${category.name.lowercase()}_$id",
                            displayName = name,
                            category = category,
                            uri = contentUri,
                            localPath = path,
                            sizeBytes = size,
                            mimeType = mime,
                            dateModifiedSeconds = dateSec,
                            subtitleLabel = category.label,
                            previewPaletteIndex = count % 4
                        )
                    )
                    count++
                }
            }
        }
        return entries
    }

    private fun queryDocumentsCollection(): List<DeviceFileEntry> {
        val entries = mutableListOf<DeviceFileEntry>()

        val filesUri = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            MediaStore.Downloads.EXTERNAL_CONTENT_URI
        } else {
            MediaStore.Files.getContentUri("external")
        }

        val projection = arrayOf(
            MediaStore.MediaColumns._ID,
            MediaStore.MediaColumns.DISPLAY_NAME,
            MediaStore.MediaColumns.SIZE,
            MediaStore.MediaColumns.MIME_TYPE,
            MediaStore.MediaColumns.DATE_MODIFIED,
            MediaStore.MediaColumns.DATA
        )

        runCatching {
            context.contentResolver.query(
                filesUri,
                projection,
                null,
                null,
                "${MediaStore.MediaColumns.DATE_MODIFIED} DESC"
            )?.use { cursor ->
                val idCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns._ID)
                val nameCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DISPLAY_NAME)
                val sizeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.SIZE)
                val mimeCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.MIME_TYPE)
                val dateCol = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATE_MODIFIED)
                val dataCol = cursor.getColumnIndex(MediaStore.MediaColumns.DATA)

                var count = 0
                while (cursor.moveToNext() && count < 60) {
                    val id = cursor.getLong(idCol)
                    val name = cursor.getString(nameCol) ?: continue
                    val size = cursor.getLong(sizeCol)
                    if (size <= 0L) continue
                    val mime = cursor.getString(mimeCol) ?: "application/octet-stream"
                    val dateSec = cursor.getLong(dateCol)
                    val path = if (dataCol >= 0) cursor.getString(dataCol) else null
                    val contentUri = ContentUris.withAppendedId(filesUri, id)

                    entries.add(
                        DeviceFileEntry(
                            id = "doc_$id",
                            displayName = name,
                            category = PickerMediaCategory.DOCUMENTS,
                            uri = contentUri,
                            localPath = path,
                            sizeBytes = size,
                            mimeType = mime,
                            dateModifiedSeconds = dateSec,
                            subtitleLabel = "Document",
                            previewPaletteIndex = count % 4
                        )
                    )
                    count++
                }
            }
        }

        val publicDirs = listOfNotNull(
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS),
            Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS)
        )
        for (dir in publicDirs) {
            runCatching {
                dir.walkTopDown()
                    .maxDepth(2)
                    .filter { it.isFile && it.length() > 0L && !it.name.endsWith("-wal") && !it.name.endsWith("-shm") }
                    .forEach { file ->
                        if (entries.none { it.localPath == file.absolutePath }) {
                            entries.add(
                                DeviceFileEntry(
                                    id = "file_${file.absolutePath.hashCode()}",
                                    displayName = file.name,
                                    category = PickerMediaCategory.DOCUMENTS,
                                    uri = Uri.fromFile(file),
                                    localPath = file.absolutePath,
                                    sizeBytes = file.length(),
                                    mimeType = guessMimeType(file.name),
                                    dateModifiedSeconds = file.lastModified() / 1000L,
                                    subtitleLabel = "Saved File"
                                )
                            )
                        }
                    }
            }
        }

        return entries.sortedByDescending { it.dateModifiedSeconds }
    }

    /**
     * Converts user-picked `ContentResolver` URIs (from Photo Picker or Document Picker) into
     * `DeviceFileEntry` records with real metadata.
     */
    suspend fun resolvePickedUris(
        uris: List<Uri>,
        targetCategory: PickerMediaCategory
    ): List<DeviceFileEntry> = withContext(Dispatchers.IO) {
        val resolver = context.contentResolver
        uris.mapNotNull { uri ->
            runCatching {
                var displayName = uri.lastPathSegment ?: "selected_file"
                var byteSize = 0L
                resolver.query(uri, null, null, null, null)?.use { cursor ->
                    val nameIdx = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    val sizeIdx = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (cursor.moveToFirst()) {
                        if (nameIdx >= 0) displayName = cursor.getString(nameIdx) ?: displayName
                        if (sizeIdx >= 0) byteSize = cursor.getLong(sizeIdx)
                    }
                }
                if (byteSize <= 0L) {
                    resolver.openAssetFileDescriptor(uri, "r")?.use { afd ->
                        if (afd.length > 0) byteSize = afd.length
                    }
                }
                val mime = resolver.getType(uri) ?: guessMimeType(displayName)
                DeviceFileEntry(
                    id = "picked_${uri.toString().hashCode()}",
                    displayName = displayName,
                    category = targetCategory,
                    uri = uri,
                    localPath = null,
                    sizeBytes = byteSize.coerceAtLeast(1024L),
                    mimeType = mime,
                    dateModifiedSeconds = System.currentTimeMillis() / 1000L,
                    subtitleLabel = "Selected from device"
                )
            }.getOrNull()
        }
    }

    /**
     * Reads partition storage metrics via `StatFs`.
     */
    fun readStoragePartitionMetrics(): StoragePartitionMetrics {
        val dataPath = Environment.getDataDirectory()
        val statFs = runCatching { StatFs(dataPath.path) }.getOrNull()
        val rawTotal = statFs?.totalBytes ?: 0L
        val rawAvail = statFs?.availableBytes ?: 0L

        val fallbackFile = context.filesDir
        val total = if (rawTotal > 0L) rawTotal else fallbackFile.totalSpace.coerceAtLeast(64L * 1024L * 1024L)
        val avail = if (rawTotal > 0L) rawAvail else fallbackFile.usableSpace.coerceAtLeast(32L * 1024L * 1024L)
        val used = (total - avail).coerceAtLeast(0L)
        val fsType = detectFilesystemType(dataPath)
        val isFat32 = fsType.equals("vfat", ignoreCase = true) ||
            fsType.equals("fat32", ignoreCase = true) ||
            fsType.equals("msdos", ignoreCase = true)

        return StoragePartitionMetrics(
            dataTotalBytes = total,
            dataUsedBytes = used,
            dataAvailableBytes = avail,
            filesystemType = fsType,
            isFat32Limited = isFat32
        )
    }

    private fun detectFilesystemType(path: File): String {
        return runCatching {
            val canonical = path.canonicalPath
            var bestMount = ""
            var bestFs = "Standard"
            BufferedReader(FileReader("/proc/mounts")).use { reader ->
                var line = reader.readLine()
                while (line != null) {
                    val parts = line.split(" ")
                    if (parts.size >= 3) {
                        val mp = parts[1]
                        val fs = parts[2]
                        if (canonical.startsWith(mp) && mp.length >= bestMount.length) {
                            bestMount = mp
                            bestFs = fs
                        }
                    }
                    line = reader.readLine()
                }
            }
            bestFs
        }.getOrDefault("Standard")
    }

    private fun guessMimeType(name: String): String {
        val lower = name.lowercase()
        return when {
            lower.endsWith(".jpg") || lower.endsWith(".jpeg") -> "image/jpeg"
            lower.endsWith(".png") -> "image/png"
            lower.endsWith(".mp4") || lower.endsWith(".mkv") -> "video/mp4"
            lower.endsWith(".mp3") || lower.endsWith(".wav") -> "audio/mpeg"
            lower.endsWith(".pdf") -> "application/pdf"
            lower.endsWith(".apk") -> "application/vnd.android.package-archive"
            lower.endsWith(".zip") -> "application/zip"
            else -> "application/octet-stream"
        }
    }
}
