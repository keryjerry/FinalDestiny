package com.devil.finaldestiny.ui.components

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.layout.Box
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.zIndex
import kotlinx.coroutines.launch

/**
 * Reusable Native iOS-style Pinch-to-Zoom container component with Elastic Snap-Back.
 * Supports multi-touch 2-finger zoom & pan, high z-index overlay during zoom to avoid clipping,
 * scroll interception disallowing, and smooth elastic spring animation back to 1.0f on touch release.
 */
@Composable
fun PinchZoomContainer(
    modifier: Modifier = Modifier,
    clipToBounds: Boolean = false,
    minScale: Float = 1f,
    maxScale: Float = 5f,
    onZoomStateChanged: ((Boolean) -> Unit)? = null,
    content: @Composable () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val scale = remember { Animatable(1f) }
    val offsetX = remember { Animatable(0f) }
    val offsetY = remember { Animatable(0f) }
    var isPinching by remember { mutableStateOf(false) }

    val isZoomed = scale.value > 1.01f || isPinching

    LaunchedEffect(isZoomed) {
        onZoomStateChanged?.invoke(isZoomed)
    }

    Box(
        modifier = modifier
            .zIndex(if (isZoomed) 999f else 0f)
            .graphicsLayer {
                scaleX = scale.value
                scaleY = scale.value
                translationX = offsetX.value
                translationY = offsetY.value
                clip = clipToBounds && !isZoomed
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    do {
                        val event = awaitPointerEvent()
                        val pointerCount = event.changes.count { it.pressed }

                        if (pointerCount >= 2) {
                            isPinching = true
                            val zoomChange = event.calculateZoom()
                            val panChange = event.calculatePan()

                            val newScale = (scale.value * zoomChange).coerceIn(minScale, maxScale)
                            val currentScale = scale.value

                            coroutineScope.launch {
                                scale.snapTo(newScale)
                                if (newScale > 1f) {
                                    offsetX.snapTo(offsetX.value + panChange.x * currentScale)
                                    offsetY.snapTo(offsetY.value + panChange.y * currentScale)
                                }
                            }
                            event.changes.forEach { it.consume() }
                        } else if (pointerCount == 1 && scale.value > 1.01f) {
                            val panChange = event.calculatePan()
                            coroutineScope.launch {
                                offsetX.snapTo(offsetX.value + panChange.x * scale.value)
                                offsetY.snapTo(offsetY.value + panChange.y * scale.value)
                            }
                            event.changes.forEach { it.consume() }
                        }
                    } while (event.changes.any { it.pressed })

                    // Elastic Snap-Back on Touch Release (ACTION_UP / ACTION_CANCEL)
                    isPinching = false
                    coroutineScope.launch {
                        val snapSpring = spring<Float>(
                            dampingRatio = Spring.DampingRatioMediumBouncy,
                            stiffness = Spring.StiffnessMediumLow
                        )
                        launch { scale.animateTo(1f, snapSpring) }
                        launch { offsetX.animateTo(0f, snapSpring) }
                        launch { offsetY.animateTo(0f, snapSpring) }
                    }
                }
            }
    ) {
        content()
    }
}
