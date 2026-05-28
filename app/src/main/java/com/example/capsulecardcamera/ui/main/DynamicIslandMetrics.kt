package com.example.capsulecardcamera.ui.main

import android.graphics.Rect
import android.view.ViewTreeObserver
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.core.view.ViewCompat

private data class DynamicIslandCutoutSnapshot(
  val bounds: Rect?,
  val safeInsetTopPx: Int,
)

internal data class DynamicIslandMetrics(
  val top: Dp,
  val left: Dp,
  val width: Dp,
  val height: Dp,
  val hasCutout: Boolean,
) {
  val bottom: Dp = top + height
  val centerX: Dp = left + width / 2f
}

@Composable
internal fun rememberDynamicIslandMetrics(
  maxWidth: Dp,
  statusBarHeight: Dp,
  coverMode: DynamicIslandCoverMode,
): DynamicIslandMetrics {
  val density = LocalDensity.current
  val view = LocalView.current
  var cutoutSnapshot by remember { mutableStateOf<DynamicIslandCutoutSnapshot?>(null) }

  DisposableEffect(view) {
    fun readCutoutSnapshot() {
      val displayCutout = ViewCompat.getRootWindowInsets(view)?.displayCutout
      val candidateBounds =
        displayCutout
          ?.boundingRects
          .orEmpty()
          .filter { it.width() > 0 && it.height() > 0 }
          .minWithOrNull(compareBy<Rect> { it.top }.thenBy { kotlin.math.abs(it.centerX() - view.width / 2) })

      cutoutSnapshot =
        displayCutout?.let {
          DynamicIslandCutoutSnapshot(
            bounds = candidateBounds?.let(::Rect),
            safeInsetTopPx = it.safeInsetTop,
          )
        }
    }

    val listener = ViewTreeObserver.OnGlobalLayoutListener { readCutoutSnapshot() }
    view.viewTreeObserver.addOnGlobalLayoutListener(listener)
    view.post { readCutoutSnapshot() }

    onDispose {
      if (view.viewTreeObserver.isAlive) {
        view.viewTreeObserver.removeOnGlobalLayoutListener(listener)
      }
    }
  }

  return remember(cutoutSnapshot, density, maxWidth, statusBarHeight, coverMode) {
    val snapshot = cutoutSnapshot
    if (snapshot == null) {
      fallbackDynamicIslandMetrics(maxWidth = maxWidth, coverMode = coverMode)
    } else {
      val bounds = snapshot.bounds
      val safeInsetTop = with(density) { snapshot.safeInsetTopPx.toDp() }
      val cutoutTop = bounds?.let { with(density) { it.top.toDp() }.coerceAtLeast(0.dp) } ?: 0.dp
      val cutoutWidth = bounds?.let { with(density) { it.width().toDp() } } ?: 0.dp
      val cutoutHeight = bounds?.let { with(density) { it.height().toDp() } } ?: safeInsetTop
      val cutoutCenter =
        bounds?.let {
          with(density) {
            it.centerX().toDp()
          }
        } ?: maxWidth / 2f

      cutoutDynamicIslandMetrics(
        maxWidth = maxWidth,
        statusBarHeight = statusBarHeight,
        cutoutTop = cutoutTop,
        cutoutCenter = cutoutCenter,
        cutoutWidth = cutoutWidth,
        cutoutHeight = cutoutHeight,
        safeInsetTop = safeInsetTop,
        coverMode = coverMode,
      )
    }
  }
}

internal fun cutoutDynamicIslandMetrics(
  maxWidth: Dp,
  statusBarHeight: Dp,
  cutoutTop: Dp,
  cutoutCenter: Dp,
  cutoutWidth: Dp,
  cutoutHeight: Dp,
  safeInsetTop: Dp,
  coverMode: DynamicIslandCoverMode,
): DynamicIslandMetrics {
  val profile = coverMode.coverProfile()
  val islandWidth =
    (cutoutWidth + profile.horizontalOverscan)
      .coerceIn(profile.minWidth, maxWidth * profile.maxWidthFraction)
  val islandTop = (cutoutTop - profile.topOverscan).coerceAtLeast(profile.topClearance)
  val cutoutBottom = cutoutTop + cutoutHeight
  val requiredBottom =
    maxOf(
      cutoutBottom + profile.bottomOverscan,
      safeInsetTop + profile.safeInsetBottomOverscan,
      statusBarHeight + profile.statusBarBottomOverscan,
      islandTop + profile.minHeight,
    )
  val islandHeight = (requiredBottom - islandTop).coerceIn(profile.minHeight, profile.maxHeight)
  val islandLeft = (cutoutCenter - islandWidth / 2f).coerceIn(0.dp, maxWidth - islandWidth)

  return DynamicIslandMetrics(
    top = islandTop,
    left = islandLeft,
    width = islandWidth,
    height = islandHeight,
    hasCutout = true,
  )
}

internal fun fallbackDynamicIslandMetrics(
  maxWidth: Dp,
  coverMode: DynamicIslandCoverMode = DynamicIslandCoverMode.Comfort,
): DynamicIslandMetrics {
  val profile = coverMode.coverProfile()
  val width = profile.minWidth.coerceAtMost(maxWidth * profile.maxWidthFraction)
  return DynamicIslandMetrics(
    top = profile.topClearance,
    left = (maxWidth - width) / 2f,
    width = width,
    height = profile.minHeight,
    hasCutout = false,
  )
}

private data class DynamicIslandCoverProfile(
  val minWidth: Dp,
  val minHeight: Dp,
  val maxHeight: Dp,
  val horizontalOverscan: Dp,
  val topClearance: Dp,
  val topOverscan: Dp,
  val bottomOverscan: Dp,
  val safeInsetBottomOverscan: Dp,
  val statusBarBottomOverscan: Dp,
  val maxWidthFraction: Float,
)

private fun DynamicIslandCoverMode.coverProfile(): DynamicIslandCoverProfile =
  when (this) {
    DynamicIslandCoverMode.Precise ->
      DynamicIslandCoverProfile(
        minWidth = 128.dp,
        minHeight = 44.dp,
        maxHeight = 56.dp,
        horizontalOverscan = 86.dp,
        topClearance = 6.dp,
        topOverscan = 0.dp,
        bottomOverscan = 10.dp,
        safeInsetBottomOverscan = 4.dp,
        statusBarBottomOverscan = 0.dp,
        maxWidthFraction = 0.56f,
      )
    DynamicIslandCoverMode.Comfort ->
      DynamicIslandCoverProfile(
        minWidth = 168.dp,
        minHeight = 46.dp,
        maxHeight = 58.dp,
        horizontalOverscan = 134.dp,
        topClearance = 8.dp,
        topOverscan = 4.dp,
        bottomOverscan = 16.dp,
        safeInsetBottomOverscan = 8.dp,
        statusBarBottomOverscan = 4.dp,
        maxWidthFraction = 0.66f,
      )
    DynamicIslandCoverMode.Maximum ->
      DynamicIslandCoverProfile(
        minWidth = 196.dp,
        minHeight = 60.dp,
        maxHeight = 74.dp,
        horizontalOverscan = 172.dp,
        topClearance = 10.dp,
        topOverscan = 10.dp,
        bottomOverscan = 26.dp,
        safeInsetBottomOverscan = 14.dp,
        statusBarBottomOverscan = 8.dp,
        maxWidthFraction = 0.78f,
      )
  }

@Composable
internal fun DynamicIslandPill(
  metrics: DynamicIslandMetrics,
  color: Color,
  modifier: Modifier = Modifier,
  testTag: String = "dynamic-island-pill",
  content: @Composable BoxScope.() -> Unit = {},
) {
  val shape = dynamicIslandShape(top = metrics.top, height = metrics.height)
  Box(
    modifier =
      modifier
        .size(width = metrics.width, height = metrics.height)
        .shadow(elevation = 8.dp, shape = shape, clip = false)
        .clip(shape)
        .background(color)
        .semantics { contentDescription = "Front camera dynamic island" }
        .testTag(testTag),
  ) {
    content()
  }
}

internal fun dynamicIslandShape(
  top: Dp,
  height: Dp,
): RoundedCornerShape {
  val sideRadius = height / 2f
  val topRadius =
    if (top <= 0.dp) {
      height * 0.34f
    } else {
      sideRadius
    }
  return RoundedCornerShape(
    topStart = topRadius,
    topEnd = topRadius,
    bottomStart = sideRadius,
    bottomEnd = sideRadius,
  )
}
