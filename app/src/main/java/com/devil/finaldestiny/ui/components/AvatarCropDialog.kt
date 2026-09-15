package com.devil.finaldestiny.ui.components

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.*
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.devil.finaldestiny.ui.screens.saveBitmapToCacheUri
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.InputStream
import kotlin.math.max
import kotlin.math.min

@Composable
fun AvatarCropDialog(
    rawImageUri: Uri,
    onDismiss: () -> Unit,
    onCropSuccess: (Bitmap, Uri) -> Unit
) {
    val context = LocalContext.current
    var sourceBitmap by remember { mutableStateOf<Bitmap?>(null) }
    var isLoading by remember { mutableStateOf(true) }

    LaunchedEffect(rawImageUri) {
        withContext(Dispatchers.IO) {
            try {
                val inputStream: InputStream? = context.contentResolver.openInputStream(rawImageUri)
                val loaded = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                sourceBitmap = loaded
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isLoading = false
            }
        }
    }

    if (sourceBitmap == null && !isLoading) {
        onDismiss()
        return
    }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = Color.Black
        ) {
            if (isLoading || sourceBitmap == null) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    CircularProgressIndicator(color = Color.White)
                }
            } else {
                val bmp = sourceBitmap!!
                var scale by remember { mutableFloatStateOf(1f) }
                var offset by remember { mutableStateOf(Offset.Zero) }

                BoxWithConstraints(
                    modifier = Modifier.fillMaxSize()
                ) {
                    val containerWidth = constraints.maxWidth.toFloat()
                    val containerHeight = constraints.maxHeight.toFloat()
                    val cropSizePx = min(containerWidth, containerHeight) * 0.82f
                    val cropRadiusPx = cropSizePx / 2f
                    val cropCenter = Offset(containerWidth / 2f, containerHeight / 2f)

                    val bmpWidth = bmp.width.toFloat()
                    val bmpHeight = bmp.height.toFloat()
                    val baseScale = max(cropSizePx / bmpWidth, cropSizePx / bmpHeight)
                    val currentScale = baseScale * scale

                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .pointerInput(Unit) {
                                detectTransformGestures { _, pan, zoom, _ ->
                                    scale = (scale * zoom).coerceIn(0.5f, 5f)
                                    offset += pan
                                }
                            }
                    ) {
                        Canvas(modifier = Modifier.fillMaxSize()) {
                            val dispW = bmpWidth * currentScale
                            val dispH = bmpHeight * currentScale
                            val left = cropCenter.x - (dispW / 2f) + offset.x
                            val top = cropCenter.y - (dispH / 2f) + offset.y

                            drawImage(
                                image = bmp.asImageBitmap(),
                                dstOffset = androidx.compose.ui.unit.IntOffset(left.toInt(), top.toInt()),
                                dstSize = androidx.compose.ui.unit.IntSize(dispW.toInt(), dispH.toInt())
                            )

                            // Semi-transparent overlay with 1:1 circular viewport cutout
                            val outerPath = Path().apply {
                                addRect(Rect(0f, 0f, size.width, size.height))
                            }
                            val circlePath = Path().apply {
                                addOval(Rect(cropCenter, cropRadiusPx))
                            }
                            val overlayPath = Path.combine(PathOperation.Difference, outerPath, circlePath)
                            drawPath(overlayPath, color = Color.Black.copy(alpha = 0.75f))

                            // Viewport ring
                            drawCircle(
                                color = Color.White,
                                radius = cropRadiusPx,
                                center = cropCenter,
                                style = androidx.compose.ui.graphics.drawscope.Stroke(width = 3f)
                            )
                        }
                    }

                    // Top Action Bar
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .statusBarsPadding()
                            .padding(16.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        IconButton(onClick = onDismiss) {
                            Icon(Icons.Default.Close, contentDescription = "Cancel", tint = Color.White)
                        }

                        Text("Crop Profile Photo", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 16.sp)

                        IconButton(
                            onClick = {
                                val dispW = bmpWidth * currentScale
                                val dispH = bmpHeight * currentScale
                                val imgLeftInVp = cropCenter.x - (dispW / 2f) + offset.x
                                val imgTopInVp = cropCenter.y - (dispH / 2f) + offset.y

                                val cropLeftInVp = cropCenter.x - cropRadiusPx
                                val cropTopInVp = cropCenter.y - cropRadiusPx

                                val cropXInImg = ((cropLeftInVp - imgLeftInVp) / currentScale).coerceIn(0f, bmpWidth - 1f)
                                val cropYInImg = ((cropTopInVp - imgTopInVp) / currentScale).coerceIn(0f, bmpHeight - 1f)
                                val cropSizeInImg = (cropSizePx / currentScale).coerceIn(1f, min(bmpWidth - cropXInImg, bmpHeight - cropYInImg))

                                val cropped = try {
                                    Bitmap.createBitmap(
                                        bmp,
                                        cropXInImg.toInt(),
                                        cropYInImg.toInt(),
                                        cropSizeInImg.toInt(),
                                        cropSizeInImg.toInt()
                                    )
                                } catch (e: Exception) {
                                    bmp
                                }
                                val scaledCropped = Bitmap.createScaledBitmap(cropped, 512, 512, true)
                                val savedUri = saveBitmapToCacheUri(context, scaledCropped)
                                if (savedUri != null) {
                                    onCropSuccess(scaledCropped, savedUri)
                                } else {
                                    onDismiss()
                                }
                            }
                        ) {
                            Icon(Icons.Default.Check, contentDescription = "Done", tint = Color(0xFF3897F0), modifier = Modifier.size(28.dp))
                        }
                    }

                    // Bottom instruction pill
                    Box(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .navigationBarsPadding()
                            .padding(bottom = 24.dp)
                            .clip(RoundedCornerShape(20.dp))
                            .background(Color.White.copy(alpha = 0.2f))
                            .padding(horizontal = 16.dp, vertical = 8.dp)
                    ) {
                        Text("Pinch to zoom, drag to position", color = Color.White, fontSize = 13.sp)
                    }
                }
            }
        }
    }
}
