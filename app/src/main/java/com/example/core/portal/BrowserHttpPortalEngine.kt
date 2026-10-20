package com.example.core.portal

import android.content.Context
import com.example.core.crypto.QrMatrixGenerator
import com.example.domain.model.BrowserHttpPortalState
import com.example.domain.model.TransferPayloadItem
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import java.io.BufferedReader
import java.io.File
import java.io.FileInputStream
import java.io.InputStreamReader
import java.io.OutputStream
import java.net.Inet4Address
import java.net.InetSocketAddress
import java.net.NetworkInterface
import java.net.ServerSocket
import java.net.Socket
import java.nio.charset.StandardCharsets

/**
 * Zero-App Browser Receiver Local HTTP Portal Engine.
 * Hosts a real embedded HTTP/1.1 ServerSocket on the device's 5 GHz Wi-Fi Direct / LAN interface
 * so PCs, Macs, or iPhones without WarpDrop installed can open the URL or scan the QR code to
 * download queued files (including APKs & Split APKs) directly in any web browser.
 */
class BrowserHttpPortalEngine(
    private val context: Context
) {
    private val portalScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private var serverSocket: ServerSocket? = null
    private var acceptJob: Job? = null

    @Volatile
    private var sharedItemsSnapshot: List<TransferPayloadItem> = emptyList()

    private val _portalState = MutableStateFlow(BrowserHttpPortalState())
    val portalState: StateFlow<BrowserHttpPortalState> = _portalState.asStateFlow()

    fun updateSharedQueue(items: List<TransferPayloadItem>) {
        sharedItemsSnapshot = items
        _portalState.update { it.copy(sharedFilesCount = items.size) }
    }

    fun startPortal(preferredPort: Int = 8980) {
        if (_portalState.value.isRunning) return

        portalScope.launch {
            runCatching {
                val socket = ServerSocket()
                socket.reuseAddress = true
                val boundPort = try {
                    socket.bind(InetSocketAddress("0.0.0.0", preferredPort))
                    preferredPort
                } catch (_: Exception) {
                    socket.bind(InetSocketAddress("0.0.0.0", 0))
                    socket.localPort
                }
                serverSocket = socket

                val localIp = resolveBestLocalIpv4Address()
                val url = "http://$localIp:$boundPort"
                val qr = QrMatrixGenerator.encodePayloadToQrMatrix(url)

                _portalState.value = BrowserHttpPortalState(
                    isRunning = true,
                    localIpAddress = localIp,
                    port = boundPort,
                    portalUrl = url,
                    qrMatrix = qr,
                    activeBrowserClients = 0,
                    totalRequestsServed = 0,
                    sharedFilesCount = sharedItemsSnapshot.size
                )

                acceptJob = launch {
                    while (isActive && !socket.isClosed) {
                        val client = runCatching { socket.accept() }.getOrNull() ?: break
                        launch { handleBrowserClient(client) }
                    }
                }
            }
        }
    }

    fun stopPortal() {
        acceptJob?.cancel()
        acceptJob = null
        runCatching { serverSocket?.close() }
        serverSocket = null
        _portalState.update { it.copy(isRunning = false, activeBrowserClients = 0) }
    }

    private fun handleBrowserClient(client: Socket) {
        _portalState.update {
            it.copy(
                activeBrowserClients = it.activeBrowserClients + 1,
                totalRequestsServed = it.totalRequestsServed + 1
            )
        }
        try {
            client.soTimeout = 10_000
            val reader = BufferedReader(InputStreamReader(client.getInputStream(), StandardCharsets.UTF_8))
            val requestLine = reader.readLine() ?: return
            val parts = requestLine.split(" ")
            val path = if (parts.size >= 2) parts[1] else "/"

            val output = client.getOutputStream()
            if (path.startsWith("/download/")) {
                val index = path.removePrefix("/download/").toIntOrNull() ?: -1
                val item = sharedItemsSnapshot.getOrNull(index)
                if (item != null) {
                    streamItemToBrowser(item, output)
                    return
                }
            }

            // Render sleek dark HTML5 Browser Receiver Portal
            val html = buildBrowserPortalHtml(sharedItemsSnapshot)
            val htmlBytes = html.toByteArray(StandardCharsets.UTF_8)
            val header = buildString {
                append("HTTP/1.1 200 OK\r\n")
                append("Content-Type: text/html; charset=utf-8\r\n")
                append("Content-Length: ${htmlBytes.size}\r\n")
                append("Connection: close\r\n\r\n")
            }
            output.write(header.toByteArray(StandardCharsets.UTF_8))
            output.write(htmlBytes)
            output.flush()
        } catch (_: Exception) {
        } finally {
            runCatching { client.close() }
            _portalState.update {
                it.copy(activeBrowserClients = (it.activeBrowserClients - 1).coerceAtLeast(0))
            }
        }
    }

    private fun streamItemToBrowser(item: TransferPayloadItem, output: OutputStream) {
        val localFile = item.localFilePath?.let { File(it) }?.takeIf { it.exists() }
        if (localFile != null) {
            val header = buildString {
                append("HTTP/1.1 200 OK\r\n")
                append("Content-Type: ${item.mimeType}\r\n")
                append("Content-Length: ${localFile.length()}\r\n")
                append("Content-Disposition: attachment; filename=\"${item.displayName}\"\r\n")
                append("Connection: close\r\n\r\n")
            }
            output.write(header.toByteArray(StandardCharsets.UTF_8))
            FileInputStream(localFile).use { fis ->
                val buf = ByteArray(64 * 1024)
                var r = fis.read(buf)
                while (r != -1) {
                    output.write(buf, 0, r)
                    r = fis.read(buf)
                }
            }
            output.flush()
        } else if (item.uri != null) {
            context.contentResolver.openInputStream(item.uri)?.use { input ->
                val header = buildString {
                    append("HTTP/1.1 200 OK\r\n")
                    append("Content-Type: ${item.mimeType}\r\n")
                    append("Content-Disposition: attachment; filename=\"${item.displayName}\"\r\n")
                    append("Connection: close\r\n\r\n")
                }
                output.write(header.toByteArray(StandardCharsets.UTF_8))
                val buf = ByteArray(64 * 1024)
                var r = input.read(buf)
                while (r != -1) {
                    output.write(buf, 0, r)
                    r = input.read(buf)
                }
                output.flush()
            }
        }
    }

    private fun buildBrowserPortalHtml(items: List<TransferPayloadItem>): String {
        val rows = if (items.isEmpty()) {
            "<p style='color:#94A3B8;'>No files queued yet. Select Photos, Videos, Documents, or Installed APKs in the WarpDrop Transfer tab.</p>"
        } else {
            items.mapIndexed { idx, item ->
                """
                <div style="background:#162032;border:1px solid #26354D;border-radius:12px;padding:14px;margin-bottom:10px;display:flex;justify-content:space-between;align-items:center;">
                  <div>
                    <div style="color:#F8FAFC;font-weight:600;">${item.displayName}</div>
                    <div style="color:#94A3B8;font-size:12px;">${item.sizeBytes / 1024} KB • ${item.mimeType}</div>
                  </div>
                  <a href="/download/$idx" style="background:#00F0FF;color:#070B14;padding:8px 16px;border-radius:8px;text-decoration:none;font-weight:700;">Download</a>
                </div>
                """.trimIndent()
            }.joinToString("\n")
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
          <meta charset="utf-8"/>
          <meta name="viewport" content="width=device-width,initial-scale=1"/>
          <title>WarpDrop Zero-App Browser Portal</title>
        </head>
        <body style="background:#070B14;color:#F8FAFC;font-family:system-ui,-apple-system,sans-serif;padding:24px;max-width:680px;margin:0 auto;">
          <h2 style="color:#00F0FF;margin-bottom:4px;">WarpDrop 5 GHz Browser Portal</h2>
          <p style="color:#94A3B8;margin-top:0;">Share with PC, Mac or iPhone via WarpDrop Web</p>
          $rows
        </body>
        </html>
        """.trimIndent()
    }

    private fun resolveBestLocalIpv4Address(): String {
        return runCatching {
            val interfaces = NetworkInterface.getNetworkInterfaces()?.toList().orEmpty()
            // Prefer p2p0 / wlan0 IPv4 addresses
            for (nif in interfaces) {
                if (!nif.isUp || nif.isLoopback) continue
                val addrs = nif.inetAddresses.toList().filterIsInstance<Inet4Address>()
                val match = addrs.firstOrNull { !it.isLoopbackAddress }
                if (match != null) {
                    return@runCatching match.hostAddress ?: "192.168.49.1"
                }
            }
            "192.168.49.1"
        }.getOrDefault("192.168.49.1")
    }
}
