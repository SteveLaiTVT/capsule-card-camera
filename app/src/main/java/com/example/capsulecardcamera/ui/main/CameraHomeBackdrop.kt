package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

private val CameraHomeRed = Color(0xFFE9342A)
private val CameraHomeDeepRed = Color(0xFFB91F1B)
private val CameraHomeDesk = Color(0xFFFFE7CE)
private val CameraHomeInk = Color(0xFF241312)
private val CameraHomeShadow = Color(0xFF130B0B)

@Composable
internal fun CameraHomeBackdrop(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier.fillMaxSize()) {
    drawRect(
      brush =
        Brush.verticalGradient(
          colors =
            listOf(
              CameraHomeRed,
              Color(0xFFF34D3F),
              CameraHomeDesk,
              Color(0xFFE9D693),
              CameraHomeInk,
            ),
          startY = 0f,
          endY = size.height,
        ),
    )

    drawPath(
      path =
        Path().apply {
          moveTo(0f, 0f)
          lineTo(size.width, 0f)
          lineTo(size.width, size.height * 0.32f)
          lineTo(0f, size.height * 0.43f)
          close()
        },
      brush =
        Brush.linearGradient(
          colors = listOf(Color(0xFFF45142), CameraHomeRed, CameraHomeDeepRed),
          start = Offset(0f, 0f),
          end = Offset(size.width, size.height * 0.42f),
        ),
    )

    drawPath(
      path =
        Path().apply {
          moveTo(0f, size.height * 0.43f)
          lineTo(size.width, size.height * 0.32f)
          lineTo(size.width, size.height * 0.67f)
          lineTo(0f, size.height * 0.72f)
          close()
        },
      brush =
        Brush.linearGradient(
          colors = listOf(CameraHomeDesk, FrameCream, Color(0xFFE7D982)),
          start = Offset(size.width * 0.12f, size.height * 0.36f),
          end = Offset(size.width, size.height * 0.72f),
        ),
    )

    drawRoundRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(Color(0xFF3A1715), CameraHomeShadow),
          startY = size.height * 0.70f,
          endY = size.height,
        ),
      topLeft = Offset(-size.width * 0.06f, size.height * 0.70f),
      size = Size(size.width * 1.12f, size.height * 0.35f),
      cornerRadius = CornerRadius(28.dp.toPx(), 28.dp.toPx()),
    )

    drawPath(
      path =
        Path().apply {
          moveTo(size.width * 0.72f, size.height * 0.73f)
          lineTo(size.width, size.height * 0.70f)
          lineTo(size.width, size.height)
          lineTo(size.width * 0.58f, size.height)
          close()
        },
      color = FrameBlack.copy(alpha = 0.42f),
    )

    drawRoundRect(
      color = FrameWarmWhite.copy(alpha = 0.24f),
      topLeft = Offset(size.width * 0.1f, size.height * 0.13f),
      size = Size(24.dp.toPx(), 24.dp.toPx()),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
      style = Stroke(width = 1.6.dp.toPx()),
    )
    drawRoundRect(
      color = FrameWarmWhite.copy(alpha = 0.20f),
      topLeft = Offset(size.width * 0.82f, size.height * 0.13f),
      size = Size(24.dp.toPx(), 32.dp.toPx()),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
      style = Stroke(width = 1.6.dp.toPx()),
    )
    repeat(5) { index ->
      val x = size.width * (0.12f + index * 0.17f)
      drawRoundRect(
        color = FrameBlack.copy(alpha = 0.12f),
        topLeft = Offset(x, size.height * 0.56f),
        size = Size(size.width * 0.08f, 6.dp.toPx()),
        cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
      )
    }
  }
}
