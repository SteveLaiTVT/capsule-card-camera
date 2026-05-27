package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
internal fun CapturePrintFeedback(
  thumbnail: Bitmap?,
  thumbnailProgress: Float,
  flashAlpha: Float,
  startTop: Dp,
  startSize: Dp,
  photoWallTop: Dp,
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

  val bitmap = thumbnail ?: return
  val progress = captureFeedbackSmoothStep(thumbnailProgress.coerceIn(0f, 1f))
  val endSize = 42.dp
  val size = captureFeedbackLerpDp(startSize, endSize, progress)
  val startX = (maxWidth - startSize) * 0.5f
  val startY = startTop
  val endX = 28.dp
  val wallTargetY = photoWallTop + 6.dp
  val endY =
    when {
      wallTargetY < startTop + 52.dp -> startTop + 52.dp
      wallTargetY > maxHeight - 124.dp -> maxHeight - 124.dp
      else -> wallTargetY
    }
  val x = captureFeedbackLerpDp(startX, endX, progress)
  val y = captureFeedbackLerpDp(startY, endY, progress)
  val corner = captureFeedbackLerpDp(28.dp, 11.dp, progress)

  Image(
    bitmap = bitmap.asImageBitmap(),
    contentDescription = null,
    contentScale = ContentScale.Crop,
    modifier =
      Modifier
        .padding(start = x, top = y)
        .size(size)
        .clip(RoundedCornerShape(corner))
        .border(1.dp, FrameWarmWhite.copy(alpha = 0.82f), RoundedCornerShape(corner))
        .graphicsLayer {
          alpha = captureFeedbackLerpFloat(0.94f, 1f, progress)
          shadowElevation = captureFeedbackLerpFloat(18f, 5f, progress)
        }
        .testTag("capture-print-thumbnail-flight"),
  )
}

private fun captureFeedbackSmoothStep(progress: Float): Float = progress * progress * (3f - 2f * progress)

private fun captureFeedbackLerpDp(start: Dp, end: Dp, progress: Float): Dp = start + (end - start) * progress

private fun captureFeedbackLerpFloat(start: Float, end: Float, progress: Float): Float = start + (end - start) * progress
