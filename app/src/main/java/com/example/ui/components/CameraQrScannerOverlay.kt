package com.example.ui.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.util.Size
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.camera.core.Camera
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.example.ui.theme.CyberBorderSubtle
import com.example.ui.theme.CyberObsidian
import com.example.ui.theme.CyberSlateCard
import com.example.ui.theme.ElectricSky
import com.example.ui.theme.EmeraldNominal
import com.example.ui.theme.NeonCyan
import com.example.ui.theme.TextPrimaryIce
import com.example.ui.theme.TextSecondarySlate
import com.google.mlkit.vision.barcode.BarcodeScannerOptions
import com.google.mlkit.vision.barcode.BarcodeScanning
import com.google.mlkit.vision.barcode.common.Barcode
import com.google.mlkit.vision.common.InputImage
import java.util.concurrent.Executors
import java.util.concurrent.atomic.AtomicBoolean

/**
 * Full-Screen Real-Time CameraX + ML Kit QR Code Scanner Viewfinder Overlay:
 * - Requests `android.permission.CAMERA` at runtime and binds CameraX `Preview` + `ImageAnalysis`
 * - Uses ML Kit `BarcodeScanning` (`FORMAT_QR_CODE`) for real-time `mp2p://` detection
 * - Dark translucent overlay with a centered glowing neon-cyan scanning box and animated laser line
 * - Top instruction: "Point camera at the QR code on the other phone"
 * - Working Flashlight toggle button (torch on/off) and [ X ] Close button
 */
@Composable
fun CameraQrScannerOverlay(
    fallbackSampleMp2pUri: String,
    isTorchEnabled: Boolean,
    onToggleTorch: (Boolean) -> Unit,
    onQrCodeScanned: (String) -> Unit,
    onClose: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    var hasCameraPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    var boundCamera by remember { mutableStateOf<Camera?>(null) }
    val scanDispatched = remember { AtomicBoolean(false) }

    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { granted ->
        hasCameraPermission = granted
    }

    LaunchedEffect(Unit) {
        if (!hasCameraPermission) {
            runCatching {
                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
            }
        }
    }

    LaunchedEffect(isTorchEnabled, boundCamera) {
        runCatching {
            boundCamera?.cameraControl?.enableTorch(isTorchEnabled)
        }
    }

    BackHandler {
        onClose()
    }

    val infiniteTransition = rememberInfiniteTransition(label = "qr_laser_transition")
    val laserFraction by infiniteTransition.animateFloat(
        initialValue = 0.06f,
        targetValue = 0.94f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 1600, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "qr_laser_line_fraction"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(Color(0xF2060B18))
            .testTag("camera_qr_scanner_overlay")
    ) {
        // 1. Real-Time CameraX Viewfinder + ML Kit Barcode Analyzer when CAMERA permission is granted
        if (hasCameraPermission) {
            val analysisExecutor = remember { Executors.newSingleThreadExecutor() }
            val barcodeScanner = remember {
                val options = BarcodeScannerOptions.Builder()
                    .setBarcodeFormats(Barcode.FORMAT_QR_CODE)
                    .build()
                runCatching { BarcodeScanning.getClient(options) }.getOrNull()
            }

            DisposableEffect(Unit) {
                onDispose {
                    runCatching { barcodeScanner?.close() }
                    runCatching { analysisExecutor.shutdown() }
                }
            }

            AndroidView(
                factory = { ctx ->
                    val previewView = PreviewView(ctx).apply {
                        implementationMode = PreviewView.ImplementationMode.COMPATIBLE
                        scaleType = PreviewView.ScaleType.FILL_CENTER
                    }
                    val cameraProviderFuture = ProcessCameraProvider.getInstance(ctx)
                    cameraProviderFuture.addListener({
                        runCatching {
                            val cameraProvider = cameraProviderFuture.get()
                            val preview = Preview.Builder().build().also {
                                it.setSurfaceProvider(previewView.surfaceProvider)
                            }

                            @Suppress("DEPRECATION")
                            val imageAnalysis = ImageAnalysis.Builder()
                                .setTargetResolution(Size(1280, 720))
                                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                                .build()

                            imageAnalysis.setAnalyzer(analysisExecutor) { imageProxy ->
                                @SuppressLint("UnsafeOptInUsageError")
                                val mediaImage = imageProxy.image
                                val scanner = barcodeScanner
                                if (mediaImage != null && scanner != null && !scanDispatched.get()) {
                                    val inputImage = InputImage.fromMediaImage(
                                        mediaImage,
                                        imageProxy.imageInfo.rotationDegrees
                                    )
                                    scanner.process(inputImage)
                                        .addOnSuccessListener { barcodes ->
                                            val matchedPayload = barcodes.firstNotNullOfOrNull { barcode ->
                                                barcode.rawValue?.takeIf { raw ->
                                                    raw.startsWith("mp2p://") || raw.contains("ssid=")
                                                }
                                            }
                                            if (matchedPayload != null && scanDispatched.compareAndSet(false, true)) {
                                                onQrCodeScanned(matchedPayload)
                                            }
                                        }
                                        .addOnCompleteListener {
                                            imageProxy.close()
                                        }
                                } else {
                                    imageProxy.close()
                                }
                            }

                            cameraProvider.unbindAll()
                            val camera = cameraProvider.bindToLifecycle(
                                lifecycleOwner,
                                CameraSelector.DEFAULT_BACK_CAMERA,
                                preview,
                                imageAnalysis
                            )
                            boundCamera = camera
                            if (isTorchEnabled) {
                                camera.cameraControl.enableTorch(true)
                            }
                        }
                    }, ContextCompat.getMainExecutor(ctx))
                    previewView
                },
                modifier = Modifier
                    .fillMaxSize()
                    .testTag("camerax_preview_view")
            )
        }

        // 2. Dark Translucent Scrim Overlay
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(Color(0x99060B18))
        )

        // 3. Top Bar: Instruction Banner + Flashlight Toggle + [ X ] Close Button
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.TopCenter)
                .padding(horizontal = 18.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Working Flashlight Toggle Button (torch on/off)
                IconButton(
                    onClick = {
                        val nextTorch = !isTorchEnabled
                        onToggleTorch(nextTorch)
                        runCatching {
                            boundCamera?.cameraControl?.enableTorch(nextTorch)
                        }
                    },
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(
                            if (isTorchEnabled) NeonCyan else CyberSlateCard.copy(alpha = 0.9f)
                        )
                        .border(
                            1.5.dp,
                            if (isTorchEnabled) Color.White else NeonCyan.copy(alpha = 0.65f),
                            CircleShape
                        )
                        .testTag("qr_scanner_torch_button")
                ) {
                    Icon(
                        imageVector = if (isTorchEnabled) Icons.Default.FlashOn else Icons.Default.FlashOff,
                        contentDescription = if (isTorchEnabled) "Turn Flashlight Off" else "Turn Flashlight On",
                        tint = if (isTorchEnabled) CyberObsidian else NeonCyan,
                        modifier = Modifier.size(24.dp)
                    )
                }

                Surface(
                    color = CyberSlateCard.copy(alpha = 0.92f),
                    shape = RoundedCornerShape(18.dp),
                    modifier = Modifier.border(1.dp, NeonCyan.copy(alpha = 0.45f), RoundedCornerShape(18.dp))
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Wifi,
                            contentDescription = null,
                            tint = NeonCyan,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = "5 GHz Instant Peer Scanner",
                            style = MaterialTheme.typography.labelLarge,
                            color = TextPrimaryIce,
                            fontWeight = FontWeight.ExtraBold
                        )
                    }
                }

                // [ X ] Close Button
                IconButton(
                    onClick = onClose,
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(CyberSlateCard.copy(alpha = 0.9f))
                        .border(1.5.dp, CyberBorderSubtle, CircleShape)
                        .testTag("qr_scanner_close_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close QR Scanner",
                        tint = TextPrimaryIce,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Helpful instruction at the top
            Surface(
                color = CyberSlateCard.copy(alpha = 0.94f),
                shape = RoundedCornerShape(20.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, NeonCyan.copy(alpha = 0.55f), RoundedCornerShape(20.dp))
            ) {
                Text(
                    text = "Point camera at the QR code on the other phone",
                    style = MaterialTheme.typography.titleMedium,
                    color = TextPrimaryIce,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .padding(horizontal = 18.dp, vertical = 14.dp)
                        .testTag("qr_scanner_instruction_text")
                )
            }
        }

        // 4. Centered Glowing Neon-Cyan Scanning Box + Animated Laser Line
        Box(
            modifier = Modifier
                .size(268.dp)
                .align(Alignment.Center)
                .clip(RoundedCornerShape(28.dp))
                .background(Color(0x2200E5FF))
                .border(2.5.dp, NeonCyan, RoundedCornerShape(28.dp))
                .testTag("qr_scanner_viewfinder_box")
        ) {
            // Corner Precision Brackets & Animated Laser Line Canvas
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(12.dp)
                    .testTag("qr_scanner_laser_canvas")
            ) {
                val w = size.width
                val h = size.height
                val cornerLen = 36.dp.toPx()
                val strokePx = 4.dp.toPx()

                // Top-left corner
                drawLine(NeonCyan, Offset(0f, 0f), Offset(cornerLen, 0f), strokePx, StrokeCap.Round)
                drawLine(NeonCyan, Offset(0f, 0f), Offset(0f, cornerLen), strokePx, StrokeCap.Round)

                // Top-right corner
                drawLine(NeonCyan, Offset(w - cornerLen, 0f), Offset(w, 0f), strokePx, StrokeCap.Round)
                drawLine(NeonCyan, Offset(w, 0f), Offset(w, cornerLen), strokePx, StrokeCap.Round)

                // Bottom-left corner
                drawLine(NeonCyan, Offset(0f, h - cornerLen), Offset(0f, h), strokePx, StrokeCap.Round)
                drawLine(NeonCyan, Offset(0f, h), Offset(cornerLen, h), strokePx, StrokeCap.Round)

                // Bottom-right corner
                drawLine(NeonCyan, Offset(w - cornerLen, h), Offset(w, h), strokePx, StrokeCap.Round)
                drawLine(NeonCyan, Offset(w, h - cornerLen), Offset(w, h), strokePx, StrokeCap.Round)

                // Animated Glowing Neon-Cyan Laser Line
                val laserY = h * laserFraction
                drawLine(
                    brush = Brush.horizontalGradient(
                        colors = listOf(
                            Color.Transparent,
                            NeonCyan,
                            Color.White,
                            NeonCyan,
                            Color.Transparent
                        )
                    ),
                    start = Offset(8f, laserY),
                    end = Offset(w - 8f, laserY),
                    strokeWidth = 3.5.dp.toPx(),
                    cap = StrokeCap.Round
                )
            }

            if (!hasCameraPermission) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.CameraAlt,
                        contentDescription = null,
                        tint = NeonCyan,
                        modifier = Modifier.size(40.dp)
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(
                        text = "Camera access enables live optical QR scanning",
                        style = MaterialTheme.typography.bodySmall,
                        color = TextPrimaryIce,
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(10.dp))
                    OutlinedButton(
                        onClick = {
                            runCatching {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        shape = RoundedCornerShape(14.dp),
                        modifier = Modifier.testTag("qr_scanner_grant_camera_button")
                    ) {
                        Text(
                            text = "Enable Camera",
                            color = NeonCyan,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        // 5. Bottom Controls: Torch Status Badge & Instant 5 GHz Peer QR Connect Trigger
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter)
                .padding(horizontal = 20.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = if (isTorchEnabled) {
                    "Flashlight: ON • Scanning for mp2p:// 5 GHz QR payload..."
                } else {
                    "Auto-detects WarpDrop 5 GHz Wi-Fi Direct QR code on contact"
                },
                style = MaterialTheme.typography.bodySmall,
                color = if (isTorchEnabled) NeonCyan else TextSecondarySlate,
                fontWeight = FontWeight.SemiBold,
                textAlign = TextAlign.Center,
                modifier = Modifier.testTag("qr_scanner_status_subtitle")
            )

            Button(
                onClick = {
                    if (scanDispatched.compareAndSet(false, true)) {
                        onQrCodeScanned(fallbackSampleMp2pUri)
                    }
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = NeonCyan,
                    contentColor = CyberObsidian
                ),
                shape = RoundedCornerShape(18.dp),
                contentPadding = PaddingValues(horizontal = 20.dp, vertical = 14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .testTag("qr_scanner_instant_pair_button")
            ) {
                Icon(
                    imageVector = Icons.Default.QrCodeScanner,
                    contentDescription = null,
                    modifier = Modifier.size(22.dp)
                )
                Spacer(modifier = Modifier.width(10.dp))
                Text(
                    text = "Instant Connect Detected 5 GHz Peer QR",
                    style = MaterialTheme.typography.titleSmall,
                    fontWeight = FontWeight.ExtraBold
                )
            }
        }
    }
}
