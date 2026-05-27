package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun CapturePrintFeedback(
  thumbnail: CapturedPhoto?,
  thumbnailProgress: Float,
  flashAlpha: Float,
  feedbackMode: CaptureFeedbackMode,
  startTop: Dp,
  startSize: Dp,
  photoWallTop: Dp,
  targetLeft: Dp,
  targetTop: Dp,
  maxWidth: Dp,
  maxHeight: Dp,
) {
  if (flashAlpha > 0f) {
    Canvas(
      modifier =
        Modifier
          .fillMaxSize()
          .testTag("capture-print-flash"),
    ) {
      val alpha = flashAlpha.coerceIn(0f, 0.5f)
      val center = Offset(size.width / 2f, size.height * 0.46f)
      drawRect(Color.Black.copy(alpha = alpha * 0.28f))
      drawCircle(
        brush =
          Brush.radialGradient(
            colors =
              listOf(
                Color.White.copy(alpha = alpha * 0.44f),
                Color.White.copy(alpha = alpha * 0.13f),
                Color.Transparent,
              ),
            center = center,
            radius = size.minDimension * 0.64f,
          ),
        radius = size.minDimension * 0.64f,
        center = center,
      )
      drawCircle(
        color = FrameWarmWhite.copy(alpha = alpha * 0.42f),
        radius = size.minDimension * 0.48f,
        center = center,
        style = Stroke(width = 1.5.dp.toPx()),
      )
    }
  }

  val photo = thumbnail ?: return
  val rawProgress = thumbnailProgress.coerceIn(0f, 1f)
  val progress = captureFeedbackSmoothStep(rawProgress)
  val cardAspectRatio = if (feedbackMode == CaptureFeedbackMode.StageCamera) StageCapturePrintAspectRatio else PullCapturePrintAspectRatio
  val endWidth =
    if (feedbackMode == CaptureFeedbackMode.StageCamera) {
      StageCameraCornerButtonSize - StageCameraAlbumThumbnailInset * 2f
    } else {
      42.dp
    }
  val endHeight = if (feedbackMode == CaptureFeedbackMode.StageCamera) endWidth else endWidth / cardAspectRatio
  val flight =
    when (feedbackMode) {
      CaptureFeedbackMode.PullList -> {
        val width = captureFeedbackLerpDp(startSize, endWidth, progress)
        val startX = (maxWidth - startSize) * 0.5f
        val wallTargetY = photoWallTop + 6.dp
        val endY =
          when {
            wallTargetY < startTop + 52.dp -> startTop + 52.dp
            wallTargetY > maxHeight - 124.dp -> maxHeight - 124.dp
            else -> wallTargetY
          }
        CaptureFeedbackFlight(
          x = captureFeedbackLerpDp(startX, targetLeft, progress),
          y = captureFeedbackLerpDp(startTop, endY, progress),
          width = width,
          height = width / cardAspectRatio,
          alpha = captureFeedbackLerpFloat(0.94f, 1f, progress),
          shadowElevation = captureFeedbackLerpFloat(18f, 5f, progress),
          visibleFraction = 1f,
        )
      }
      CaptureFeedbackMode.StageCamera -> {
        val revealProgress = captureFeedbackSmoothStep((rawProgress / StageCapturePrintRevealEnd).coerceIn(0f, 1f))
        val flyProgress =
          captureFeedbackSmoothStep(
            ((rawProgress - StageCapturePrintHoldEnd) / (1f - StageCapturePrintHoldEnd)).coerceIn(0f, 1f),
          )
        val previewSize = captureFeedbackClampDp(maxWidth * 0.46f, min = startSize + 38.dp, max = 178.dp)
        val previewHeight = previewSize / cardAspectRatio
        val previewX = (maxWidth - previewSize) / 2f
        val previewY = (startTop + 8.dp).coerceAtMost(maxHeight * 0.36f)

        if (rawProgress < StageCapturePrintHoldEnd) {
          CaptureFeedbackFlight(
            x = previewX,
            y = previewY,
            width = previewSize,
            height = previewHeight,
            alpha = captureFeedbackLerpFloat(0f, 1f, revealProgress),
            shadowElevation = captureFeedbackLerpFloat(6f, 24f, revealProgress),
            visibleFraction = revealProgress,
          )
        } else {
          CaptureFeedbackFlight(
            x = captureFeedbackLerpDp(previewX, targetLeft, flyProgress),
            y = captureFeedbackLerpDp(previewY, targetTop, flyProgress),
            width = captureFeedbackLerpDp(previewSize, endWidth, flyProgress),
            height = captureFeedbackLerpDp(previewHeight, endHeight, flyProgress),
            alpha = 1f,
            shadowElevation = captureFeedbackLerpFloat(22f, 5f, flyProgress),
            visibleFraction = 1f,
          )
        }
      }
    }

  Box(
    modifier =
      Modifier
        .padding(start = flight.x, top = flight.y)
        .size(width = flight.width, height = flight.height * flight.visibleFraction.coerceIn(0.04f, 1f))
        .clipToBounds()
        .graphicsLayer {
          alpha = flight.alpha
          shadowElevation = flight.shadowElevation
        }
        .testTag("capture-print-thumbnail-flight"),
  ) {
    FramedPhoto(
      photo = photo,
      modifier = Modifier.size(width = flight.width, height = flight.height),
    )
  }
}

internal enum class CaptureFeedbackMode {
  PullList,
  StageCamera,
}

private data class CaptureFeedbackFlight(
  val x: Dp,
  val y: Dp,
  val width: Dp,
  val height: Dp,
  val alpha: Float,
  val shadowElevation: Float,
  val visibleFraction: Float,
)

private const val StageCapturePrintRevealEnd = 0.77f
private const val StageCapturePrintHoldEnd = 0.86f
private const val StageCapturePrintAspectRatio = 0.72f
private const val PullCapturePrintAspectRatio = 0.78f

private fun captureFeedbackSmoothStep(progress: Float): Float = progress * progress * (3f - 2f * progress)

private fun captureFeedbackClampDp(
  value: Dp,
  min: Dp,
  max: Dp,
): Dp =
  when {
    value < min -> min
    value > max -> max
    else -> value
  }

private fun captureFeedbackLerpDp(start: Dp, end: Dp, progress: Float): Dp = start + (end - start) * progress

private fun captureFeedbackLerpFloat(start: Float, end: Float, progress: Float): Float = start + (end - start) * progress
