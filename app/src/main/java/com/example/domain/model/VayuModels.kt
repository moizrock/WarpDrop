package com.example.domain.model

import android.graphics.drawable.Drawable
import android.net.Uri

enum class VayuTab(val route: String, val label: String) {
    TRANSFER("transfer", "Share"),
    CLONE("clone", "Phone Clone"),
    AI_SHIELD("ai_shield", "AI Shield"),
    INBUILT_HUB("inbuilt_hub", "Media Hub")
}

enum class PickerMediaCategory(val label: String) {
    PHOTOS("Photos"),
    VIDEOS("Videos"),
    MUSIC("Music"),
    DOCUMENTS("Files"),
    INSTALLED_APKS("Apps")
}

data class DeviceFileEntry(
    val id: String,
    val displayName: String,
    val category: PickerMediaCategory,
    val uri: Uri?,
    val localPath: String?,
    val sizeBytes: Long,
    val mimeType: String,
    val dateModifiedSeconds: Long,
    val packageName: String? = null,
    val versionName: String? = null,
    val splitApkPaths: List<String> = emptyList(),
    val appIconDrawable: Drawable? = null,
    val subtitleLabel: String? = null,
    val durationSeconds: Int = 0,
    val previewPaletteIndex: Int = 0,
    val isSamplePreview: Boolean = false
) {
    val isSplitApkBundle: Boolean
        get() = splitApkPaths.isNotEmpty()

    val splitCount: Int
        get() = splitApkPaths.size
}

enum class NonDfs5GhzChannel(
    val channelNumber: Int,
    val centerFrequencyMhz: Int,
    val bandLabel: String,
    val maxPhyMbps: Int
) {
    CH_36(36, 5180, "Channel 36", 866),
    CH_40(40, 5200, "Channel 40", 866),
    CH_44(44, 5220, "Channel 44", 866),
    CH_48(48, 5240, "Channel 48", 866),
    CH_149(149, 5745, "Channel 149", 1733),
    CH_153(153, 5765, "Channel 153", 1733),
    CH_157(157, 5785, "Channel 157", 1733),
    CH_161(161, 5805, "Channel 161", 1733);

    companion object {
        fun fromFrequency(freqMhz: Int): NonDfs5GhzChannel? {
            return entries.firstOrNull { it.centerFrequencyMhz == freqMhz }
        }
    }
}

sealed class WifiDirectGroupState {
    data object Idle : WifiDirectGroupState()
    data class Initializing(val phase: String) : WifiDirectGroupState()
    data class GroupFormed(
        val ssid: String,
        val passphrase: String,
        val groupOwnerIp: String,
        val port: Int,
        val frequencyMhz: Int,
        val nonDfsChannel: NonDfs5GhzChannel,
        val isGroupOwner: Boolean,
        val connectedClientsCount: Int
    ) : WifiDirectGroupState()

    data class ConnectedAsClient(
        val groupOwnerIp: String,
        val port: Int,
        val ssid: String,
        val frequencyMhz: Int
    ) : WifiDirectGroupState()

    data class PermissionMissing(
        val requiredPermission: String,
        val isNeverForLocation: Boolean
    ) : WifiDirectGroupState()

    data class Error(val reasonCode: Int, val message: String) : WifiDirectGroupState()
}

data class P2pPeerNode(
    val deviceName: String,
    val deviceAddress: String,
    val statusCode: Int,
    val statusLabel: String,
    val isGroupOwner: Boolean,
    val primaryDeviceType: String
)

data class EphemeralPairingSession(
    val sessionId: String,
    val algorithm: String,
    val ssid: String,
    val passphrase: String,
    val hostIp: String,
    val port: Int,
    val channel: NonDfs5GhzChannel,
    val publicKeyBase64Url: String,
    val sha256FingerprintHex: String,
    val compactFingerprint: String,
    val spake2PinCode: String,
    val spake2CommitmentHex: String,
    val mp2pUriPayload: String,
    val qrMatrix: List<List<Boolean>>,
    val createdAtEpochMs: Long
)

data class ParsedMp2pPayload(
    val version: Int,
    val ssid: String,
    val passphrase: String,
    val hostIp: String,
    val port: Int,
    val channelNumber: Int,
    val frequencyMhz: Int,
    val fingerprint: String,
    val spake2PinHint: String?,
    val peerPublicKeyBase64Url: String,
    val isSignatureValid: Boolean,
    val deviceName: String = "Galaxy S24 Ultra"
)

data class BrowserHttpPortalState(
    val isRunning: Boolean = false,
    val localIpAddress: String = "192.168.49.1",
    val port: Int = 8980,
    val portalUrl: String = "http://192.168.49.1:8980",
    val qrMatrix: List<List<Boolean>> = emptyList(),
    val activeBrowserClients: Int = 0,
    val totalRequestsServed: Int = 0,
    val sharedFilesCount: Int = 0
)

data class TransferPayloadItem(
    val id: String,
    val displayName: String,
    val uri: Uri?,
    val localFilePath: String?,
    val sizeBytes: Long,
    val mimeType: String,
    val sha256Hex: String,
    val progressFraction: Float,
    val speedMBps: Float,
    val etaSeconds: Long,
    val verifiedChunks: Int,
    val totalChunks: Int,
    val resumeChunkOffset: Int,
    val deltaSavedPercent: Float,
    val preflightValidationLabel: String,
    val isSplitApk: Boolean = false,
    val splitCount: Int = 0,
    val status: TransferItemStatus
)

enum class TransferItemStatus(val displayLabel: String) {
    QUEUED("Queued"),
    PREFLIGHT_CHECK("Preparing"),
    READY_5GHZ("Ready to send"),
    STREAMING_NIO("Sending"),
    PAUSED_CHECKPOINT("Paused"),
    VERIFIED_COMPLETE("Completed"),
    FAILED("Unable to send")
}

enum class CloneCategoryType(val title: String, val subtitle: String) {
    WHATSAPP_CHATS("WhatsApp Chats", "Chat history, voice notes & shared media"),
    CONTACTS("Contacts", "Saved phone numbers, emails & contact cards"),
    MEDIA_GALLERY("Photos & Videos", "Camera roll albums, memories & HD videos"),
    INSTALLED_APKS("Installed Apps", "Your apps & app settings")
}

data class CloneCategorySummary(
    val category: CloneCategoryType,
    val itemCount: Int,
    val totalBytes: Long,
    val isSelected: Boolean,
    val sampleItems: List<String>,
    val transferProgress: Float
)

data class DuplicateFileEntry(
    val id: String,
    val fileName: String,
    val filePathOrUri: String,
    val sizeBytes: Long,
    val sha256Hash: String,
    val lastModifiedMs: Long,
    val isOriginalKeep: Boolean
)

data class DuplicateCluster(
    val sha256Hash: String,
    val fileSizeEachBytes: Long,
    val entries: List<DuplicateFileEntry>
) {
    val reclaimableBytes: Long
        get() = if (entries.size > 1) fileSizeEachBytes * (entries.size - 1) else 0L
}

data class VaultedItemRecord(
    val id: String,
    val originalName: String,
    val encryptedFilePath: String,
    val sizeBytes: Long,
    val sha256PlaintextHash: String,
    val cipherAlgorithm: String,
    val ivHex: String,
    val lockedAtEpochMs: Long
)

enum class ThermalStateLevel(val statusInt: Int, val label: String, val throttleAction: String) {
    NONE(0, "Cool & Optimal", "Maximum transfer speed active"),
    LIGHT(1, "Normal Warmth", "High-speed wireless transfer active"),
    MODERATE(2, "Slightly Warm", "Smart speed balancing active"),
    SEVERE(3, "Warm", "Cooling protection enabled"),
    CRITICAL(4, "Hot", "Pausing recommended to cool down"),
    EMERGENCY(5, "Very Hot", "Cooling protection engaged"),
    SHUTDOWN(6, "Overheated", "Transfer paused for device safety")
}

enum class BottleneckKind(val badge: String, val diagnosticAdvice: String) {
    OPTIMAL_5GHZ("Ultra-Fast Ready", "High-speed connection active and device temperature is optimal"),
    WIFI_24GHZ_BAND("Standard Wi-Fi", "Switch to high-speed mode for faster transfers"),
    WEAK_RSSI_SIGNAL("Move Closer", "Bring both phones closer together for faster speeds"),
    THERMAL_HEADROOM_WARN("Cooling Mode", "Device is warm; balancing transfer speed automatically"),
    LOW_BATTERY_SAVER("Low Battery", "Battery is below 20%; plug in charger for best speed")
}

data class TelemetrySamplePoint(
    val timestampMs: Long,
    val transferSpeedMBps: Float,
    val wifiRssiDbm: Int,
    val thermalHeadroom: Float,
    val batteryTempCelsius: Float
)

data class StoragePartitionMetrics(
    val dataTotalBytes: Long,
    val dataUsedBytes: Long,
    val dataAvailableBytes: Long,
    val filesystemType: String,
    val isFat32Limited: Boolean
)

data class LiveTelemetrySnapshot(
    val batteryTempCelsius: Float,
    val batterySoCPercent: Int,
    val batteryVoltageMv: Int,
    val batteryCurrentNowMicroAmps: Long,
    val isCharging: Boolean,
    val thermalLevel: ThermalStateLevel,
    val thermalHeadroomRatio: Float,
    val wifiLinkSpeedMbps: Int,
    val transferSpeedMBps: Float,
    val wifiTxLinkSpeedMbps: Int,
    val wifiRxLinkSpeedMbps: Int,
    val wifiFrequencyMhz: Int,
    val wifiRssiDbm: Int,
    val active5GhzChannelLabel: String,
    val is5GhzBandActive: Boolean,
    val bottleneckKind: BottleneckKind,
    val historySamples: List<TelemetrySamplePoint>
)
