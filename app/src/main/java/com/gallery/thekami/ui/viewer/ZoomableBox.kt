package com.gallery.thekami.ui.viewer

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.spring
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.calculateCentroid
import androidx.compose.foundation.gestures.calculatePan
import androidx.compose.foundation.gestures.calculateZoom
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.input.pointer.positionChanged
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.unit.IntSize
import kotlin.math.abs
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

private const val MAX_SCALE = 5f
private const val MIN_SCALE = 0.8f
private const val DOUBLE_TAP_SCALE = 2.5f

/**
 * Photo gestures:
 * - pinch zoom around the fingers (1x-5x; pinching below 1x springs back),
 * - double-tap zooms in/out with a spring animation,
 * - while zoomed, one finger pans inside the picture's real edges; at an edge a horizontal
 *   swipe is left to the pager so you can move to the next photo,
 * - at 1x, a vertical drag moves the photo and fades the background; release past a threshold
 *   to close, otherwise it springs back.
 * [contentAspect] (width/height of the decoded picture) makes pan limits match the picture
 * instead of the whole screen.
 */
@Composable
fun ZoomableBox(
    isCurrent: Boolean,
    contentAspect: Float?,
    onTap: () -> Unit,
    onDismissDrag: (progress: Float) -> Unit,
    onDismiss: () -> Unit,
    content: @Composable () -> Unit,
) {
    var scale by remember { mutableFloatStateOf(1f) }
    var offset by remember { mutableStateOf(Offset.Zero) }
    var dragY by remember { mutableFloatStateOf(0f) }
    var boxSize by remember { mutableStateOf(IntSize.Zero) }
    var animJob by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val currentOnTap by rememberUpdatedState(onTap)
    val currentOnDrag by rememberUpdatedState(onDismissDrag)
    val currentOnDismiss by rememberUpdatedState(onDismiss)
    val currentAspect by rememberUpdatedState(contentAspect)

    fun center() = Offset(boxSize.width / 2f, boxSize.height / 2f)

    /** How far the picture may be moved from the centre at scale [s]. */
    fun limit(s: Float): Offset {
        val bw = boxSize.width.toFloat()
        val bh = boxSize.height.toFloat()
        val aspect = currentAspect
        val fitW: Float
        val fitH: Float
        if (aspect == null || aspect <= 0f || bw <= 0f || bh <= 0f) {
            fitW = bw
            fitH = bh
        } else if (aspect > bw / bh) {
            fitW = bw
            fitH = bw / aspect
        } else {
            fitH = bh
            fitW = bh * aspect
        }
        return Offset(maxOf(0f, (fitW * s - bw) / 2f), maxOf(0f, (fitH * s - bh) / 2f))
    }

    fun clampOffset(value: Offset, s: Float): Offset {
        val l = limit(maxOf(s, 1f))
        return Offset(value.x.coerceIn(-l.x, l.x), value.y.coerceIn(-l.y, l.y))
    }

    fun progressOf(y: Float): Float =
        if (boxSize.height > 0) (abs(y) / (boxSize.height * 0.45f)).coerceIn(0f, 1f) else 0f

    fun animateTo(targetScale: Float, targetOffset: Offset) {
        animJob?.cancel()
        val startScale = scale
        val startOffset = offset
        animJob = scope.launch {
            animate(0f, 1f, animationSpec = spring(dampingRatio = 0.85f, stiffness = 380f)) { t, _ ->
                scale = startScale + (targetScale - startScale) * t
                offset = startOffset + (targetOffset - startOffset) * t
            }
        }
    }

    LaunchedEffect(isCurrent) {
        if (!isCurrent) {
            animJob?.cancel()
            scale = 1f
            offset = Offset.Zero
            dragY = 0f
            currentOnDrag(0f)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .onSizeChanged { boxSize = it }
            .pointerInput(Unit) {
                detectTapGestures(
                    onTap = { currentOnTap() },
                    onDoubleTap = { tap ->
                        if (scale > 1.05f) {
                            animateTo(1f, Offset.Zero)
                        } else {
                            val target = clampOffset((tap - center()) * (1f - DOUBLE_TAP_SCALE), DOUBLE_TAP_SCALE)
                            animateTo(DOUBLE_TAP_SCALE, target)
                        }
                    },
                )
            }
            .pointerInput(Unit) {
                awaitEachGesture {
                    awaitFirstDown(requireUnconsumed = false)
                    animJob?.cancel()
                    val slop = viewConfiguration.touchSlop
                    var total = Offset.Zero
                    var decided = false
                    var dismissing = false
                    do {
                        val event = awaitPointerEvent()
                        if (event.changes.size > 1) {
                            dismissing = false
                            val zoom = event.calculateZoom()
                            val pan = event.calculatePan()
                            val centroid = event.calculateCentroid() - center()
                            val newScale = (scale * zoom).coerceIn(MIN_SCALE, MAX_SCALE)
                            val ratio = newScale / scale
                            offset = clampOffset(centroid * (1f - ratio) + offset * ratio + pan, newScale)
                            scale = newScale
                            event.changes.forEach { if (it.positionChanged()) it.consume() }
                        } else {
                            val pan = event.calculatePan()
                            if (scale > 1.01f) {
                                val next = clampOffset(offset + pan, scale)
                                val horizontal = abs(pan.x) > abs(pan.y)
                                val blockedHorizontally = next.x == offset.x
                                if (!(horizontal && blockedHorizontally)) {
                                    offset = next
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            } else {
                                total += pan
                                if (!decided && total.getDistance() > slop) {
                                    decided = true
                                    dismissing = abs(total.y) > abs(total.x) * 1.2f
                                }
                                if (dismissing) {
                                    dragY += pan.y
                                    currentOnDrag(progressOf(dragY))
                                    event.changes.forEach { if (it.positionChanged()) it.consume() }
                                }
                            }
                        }
                    } while (event.changes.any { it.pressed })

                    if (dismissing || dragY != 0f) {
                        if (abs(dragY) > boxSize.height * 0.18f) {
                            currentOnDismiss()
                        } else {
                            scope.launch {
                                animate(dragY, 0f, animationSpec = spring(stiffness = 400f)) { value, _ ->
                                    dragY = value
                                    currentOnDrag(progressOf(value))
                                }
                            }
                        }
                    }
                    if (scale < 1f) animateTo(1f, Offset.Zero)
                }
            }
            .graphicsLayer {
                val shrink = 1f - 0.25f * progressOf(dragY)
                scaleX = scale * shrink
                scaleY = scale * shrink
                translationX = offset.x
                translationY = offset.y + dragY
            },
    ) { content() }
}
