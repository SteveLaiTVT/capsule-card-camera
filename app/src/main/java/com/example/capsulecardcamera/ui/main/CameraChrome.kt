package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

private val CameraChromeBlack = Color(0xFF060606)

@Composable
internal fun CameraControls(
  progress: Float,
  fullScreenProgress: Float,
  copy: CameraCopy,
  onFrameSettingsClick: () -> Unit,
  onShutterClick: () -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .width(cameraChromeLerpDp(214.dp, 238.dp, fullScreenProgress))
        .graphicsLayer {
          alpha = cameraChromeLerpFloat(0f, 1f, ((progress - 0.28f) / 0.55f).coerceIn(0f, 1f))
          translationY = cameraChromeLerpFloat(-16f, 0f, progress)
        },
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RoundControlButton(
      color = FrameGreen,
      contentDescription = copy.frameSettingsContentDescription,
      onClick = onFrameSettingsClick,
      icon = ControlIcon.Frame,
      modifier = Modifier.size(42.dp),
    )
    ShutterButton(
      contentDescription = copy.shutterContentDescription,
      onClick = onShutterClick,
      modifier = Modifier.size(62.dp),
    )
    RoundControlButton(
      color = FrameRed,
      contentDescription = copy.closePreviewContentDescription,
      onClick = onClose,
      icon = ControlIcon.Close,
      modifier = Modifier.size(42.dp),
    )
  }
}

@Composable
internal fun ShutterButton(
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  testTag: String = "shutter-button",
) {
  Box(
    modifier =
      modifier
        .clip(CircleShape)
        .background(
          Brush.radialGradient(
            colors = listOf(Color(0xFFFF4238), Color(0xFFD01716), Color(0xFF260707)),
          ),
        )
        .border(2.dp, Color.Black.copy(alpha = 0.5f), CircleShape)
        .clickable(onClick = onClick)
        .semantics { this.contentDescription = contentDescription }
        .testTag(testTag),
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawCircle(Color.White.copy(alpha = 0.18f), radius = size.minDimension * 0.42f)
      drawCircle(Color.Black.copy(alpha = 0.2f), radius = size.minDimension * 0.3f)
    }
  }
}

@Composable
internal fun SettingsFloatingButton(
  contentDescription: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  RoundControlButton(
    color = CameraChromeBlack,
    contentDescription = contentDescription,
    onClick = onClick,
    icon = ControlIcon.Settings,
    modifier =
      modifier
        .size(44.dp)
        .testTag("settings-button"),
  )
}

@Composable
internal fun RoundControlButton(
  color: Color,
  contentDescription: String,
  onClick: () -> Unit,
  icon: ControlIcon,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .clip(CircleShape)
        .background(color)
        .border(1.4.dp, Color.Black.copy(alpha = 0.16f), CircleShape)
        .clickable(onClick = onClick)
        .semantics { this.contentDescription = contentDescription },
    contentAlignment = Alignment.Center,
  ) {
    Canvas(modifier = Modifier.size(23.dp)) {
      when (icon) {
        ControlIcon.Settings -> drawSettingsIcon()
        ControlIcon.Frame -> drawFrameIcon()
        ControlIcon.Close -> drawCloseIcon()
        ControlIcon.Flash -> drawFlashIcon()
        ControlIcon.Lens -> drawLensIcon()
        ControlIcon.Gallery -> drawGalleryIcon()
      }
    }
  }
}

@Composable
internal fun PullHint(progress: Float, modifier: Modifier = Modifier) {
  if (progress > 0.05f) return

  Canvas(modifier = modifier.size(width = 34.dp, height = 20.dp).graphicsLayer { alpha = 0.34f }) {
    drawLine(
      color = FrameWarmWhite,
      start = Offset(size.width * 0.5f, 0f),
      end = Offset(size.width * 0.5f, size.height * 0.58f),
      strokeWidth = 1.5.dp.toPx(),
      cap = StrokeCap.Round,
    )
    drawLine(
      color = FrameWarmWhite,
      start = Offset(size.width * 0.35f, size.height * 0.42f),
      end = Offset(size.width * 0.5f, size.height * 0.58f),
      strokeWidth = 1.5.dp.toPx(),
      cap = StrokeCap.Round,
    )
    drawLine(
      color = FrameWarmWhite,
      start = Offset(size.width * 0.65f, size.height * 0.42f),
      end = Offset(size.width * 0.5f, size.height * 0.58f),
      strokeWidth = 1.5.dp.toPx(),
      cap = StrokeCap.Round,
    )
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawSettingsIcon() {
  drawCircle(
    color = FrameWarmWhite,
    radius = size.minDimension * 0.18f,
    center = Offset(size.width * 0.5f, size.height * 0.5f),
    style = Stroke(width = 2.1.dp.toPx()),
  )
  repeat(8) { index ->
    val angle = Math.toRadians((index * 45).toDouble())
    val innerRadius = size.minDimension * 0.31f
    val outerRadius = size.minDimension * 0.43f
    val start =
      Offset(
        x = size.width * 0.5f + kotlin.math.cos(angle).toFloat() * innerRadius,
        y = size.height * 0.5f + kotlin.math.sin(angle).toFloat() * innerRadius,
      )
    val end =
      Offset(
        x = size.width * 0.5f + kotlin.math.cos(angle).toFloat() * outerRadius,
        y = size.height * 0.5f + kotlin.math.sin(angle).toFloat() * outerRadius,
      )
    drawLine(color = FrameWarmWhite, start = start, end = end, strokeWidth = 1.7.dp.toPx(), cap = StrokeCap.Round)
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFrameIcon() {
  drawRoundRect(
    color = FrameWarmWhite,
    topLeft = Offset(size.width * 0.16f, size.height * 0.2f),
    size = Size(size.width * 0.68f, size.height * 0.6f),
    cornerRadius = CornerRadius(2.dp.toPx()),
    style = Stroke(width = 2.2.dp.toPx()),
  )
  drawLine(
    color = FrameWarmWhite.copy(alpha = 0.82f),
    start = Offset(size.width * 0.3f, size.height * 0.58f),
    end = Offset(size.width * 0.46f, size.height * 0.43f),
    strokeWidth = 1.9.dp.toPx(),
    cap = StrokeCap.Round,
  )
  drawLine(
    color = FrameWarmWhite.copy(alpha = 0.82f),
    start = Offset(size.width * 0.46f, size.height * 0.43f),
    end = Offset(size.width * 0.72f, size.height * 0.66f),
    strokeWidth = 1.9.dp.toPx(),
    cap = StrokeCap.Round,
  )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawCloseIcon() {
  drawLine(
    color = FrameWarmWhite,
    start = Offset(size.width * 0.25f, size.height * 0.25f),
    end = Offset(size.width * 0.75f, size.height * 0.75f),
    strokeWidth = 2.3.dp.toPx(),
    cap = StrokeCap.Round,
  )
  drawLine(
    color = FrameWarmWhite,
    start = Offset(size.width * 0.75f, size.height * 0.25f),
    end = Offset(size.width * 0.25f, size.height * 0.75f),
    strokeWidth = 2.3.dp.toPx(),
    cap = StrokeCap.Round,
  )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawFlashIcon() {
  val bolt =
    androidx.compose.ui.graphics.Path().apply {
      moveTo(size.width * 0.56f, size.height * 0.08f)
      lineTo(size.width * 0.28f, size.height * 0.53f)
      lineTo(size.width * 0.49f, size.height * 0.53f)
      lineTo(size.width * 0.38f, size.height * 0.92f)
      lineTo(size.width * 0.73f, size.height * 0.42f)
      lineTo(size.width * 0.52f, size.height * 0.42f)
      close()
    }
  drawPath(color = FrameWarmWhite, path = bolt)
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLensIcon() {
  drawCircle(
    color = FrameWarmWhite,
    radius = size.minDimension * 0.32f,
    center = Offset(size.width * 0.5f, size.height * 0.5f),
    style = Stroke(width = 2.1.dp.toPx()),
  )
  drawCircle(
    color = FrameWarmWhite.copy(alpha = 0.85f),
    radius = size.minDimension * 0.11f,
    center = Offset(size.width * 0.5f, size.height * 0.5f),
  )
  drawLine(
    color = FrameWarmWhite.copy(alpha = 0.74f),
    start = Offset(size.width * 0.2f, size.height * 0.5f),
    end = Offset(size.width * 0.8f, size.height * 0.5f),
    strokeWidth = 1.6.dp.toPx(),
    cap = StrokeCap.Round,
  )
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGalleryIcon() {
  drawRoundRect(
    color = FrameWarmWhite,
    topLeft = Offset(size.width * 0.18f, size.height * 0.22f),
    size = Size(size.width * 0.64f, size.height * 0.56f),
    cornerRadius = CornerRadius(4.dp.toPx()),
    style = Stroke(width = 2.dp.toPx()),
  )
  drawCircle(
    color = FrameWarmWhite.copy(alpha = 0.88f),
    radius = size.minDimension * 0.08f,
    center = Offset(size.width * 0.38f, size.height * 0.4f),
  )
  drawLine(
    color = FrameWarmWhite.copy(alpha = 0.82f),
    start = Offset(size.width * 0.25f, size.height * 0.68f),
    end = Offset(size.width * 0.44f, size.height * 0.52f),
    strokeWidth = 1.7.dp.toPx(),
    cap = StrokeCap.Round,
  )
  drawLine(
    color = FrameWarmWhite.copy(alpha = 0.82f),
    start = Offset(size.width * 0.44f, size.height * 0.52f),
    end = Offset(size.width * 0.76f, size.height * 0.7f),
    strokeWidth = 1.7.dp.toPx(),
    cap = StrokeCap.Round,
  )
}

private fun cameraChromeLerpDp(start: Dp, end: Dp, progress: Float): Dp = start + (end - start) * progress

private fun cameraChromeLerpFloat(start: Float, end: Float, progress: Float): Float = start + (end - start) * progress

internal enum class ControlIcon {
  Settings,
  Frame,
  Close,
  Flash,
  Lens,
  Gallery,
}
