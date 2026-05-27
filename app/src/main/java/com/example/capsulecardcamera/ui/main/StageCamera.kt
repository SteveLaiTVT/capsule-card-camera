package com.example.capsulecardcamera.ui.main

import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val StageCameraBackground = Color(0xFFECE5F1)
private val StageCameraPanel = Color(0xFFF8EEF2)
private val StageCameraAccent = Color(0xFF9CCB5F)
private val StageCameraInk = Color(0xFF121111)

@Composable
internal fun StageCamera(
  statusBarHeight: Dp,
  navigationBarHeight: Dp,
  maxWidth: Dp,
  maxHeight: Dp,
  hasCameraPermission: Boolean,
  cameraLens: CameraLens,
  copy: CameraCopy,
  onImageCaptureReady: (ImageCapture?) -> Unit,
  onFrameSettingsClick: () -> Unit,
  onFrameManagerClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onLensToggleClick: () -> Unit,
  onShutterClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val previewWidth = stageCameraPreviewWidth(maxWidth)
  val previewHeight = stageCameraPreviewHeight(maxHeight = maxHeight, previewWidth = previewWidth)
  val previewTop = stageCameraPreviewTop(statusBarHeight = statusBarHeight, maxHeight = maxHeight)
  val toolRowTop = stageCameraToolRowTop(statusBarHeight = statusBarHeight, maxHeight = maxHeight, maxWidth = maxWidth)
  val shutterTop = stageCameraShutterTop(statusBarHeight = statusBarHeight, maxHeight = maxHeight, maxWidth = maxWidth)

  LaunchedEffect(hasCameraPermission) {
    if (!hasCameraPermission) {
      onImageCaptureReady(null)
    }
  }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(StageCameraBackground)
        .testTag("stage-camera"),
  ) {
    StageCameraBackdrop(modifier = Modifier.matchParentSize())

    StageDynamicIsland(
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(top = statusBarHeight + 8.dp),
    )

    Text(
      text = "Pico\nCam",
      color = StageCameraInk,
      textAlign = TextAlign.Center,
      fontSize = 28.sp,
      lineHeight = 25.sp,
      fontWeight = FontWeight.Bold,
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(top = statusBarHeight + 54.dp)
          .testTag("stage-camera-logo"),
    )

    StagePreviewCard(
      hasCameraPermission = hasCameraPermission,
      cameraLens = cameraLens,
      onImageCaptureReady = onImageCaptureReady,
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(top = previewTop)
          .size(width = previewWidth, height = previewHeight),
    )

    StageToolRow(
      copy = copy,
      onFrameSettingsClick = onFrameSettingsClick,
      onSettingsClick = onSettingsClick,
      onLensToggleClick = onLensToggleClick,
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(top = toolRowTop),
    )

    StageCaptureDeck(
      copy = copy,
      onFrameManagerClick = onFrameManagerClick,
      onSettingsClick = onSettingsClick,
      onShutterClick = onShutterClick,
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(top = shutterTop),
    )

    StageHomeLine(
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .padding(bottom = navigationBarHeight + 8.dp),
    )
  }
}

@Composable
private fun StageCameraBackdrop(modifier: Modifier = Modifier) {
  Canvas(modifier = modifier) {
    drawRect(StageCameraBackground)
    drawRoundRect(
      brush =
        Brush.verticalGradient(
          colors = listOf(StageCameraPanel.copy(alpha = 0.96f), Color(0xFFE3DCEB)),
          startY = size.height * 0.48f,
          endY = size.height,
        ),
      topLeft = Offset(-size.width * 0.06f, size.height * 0.58f),
      size = Size(size.width * 1.12f, size.height * 0.5f),
      cornerRadius = CornerRadius(34.dp.toPx(), 34.dp.toPx()),
    )
    drawRoundRect(
      color = Color.White.copy(alpha = 0.34f),
      topLeft = Offset(size.width * 0.08f, size.height * 0.16f),
      size = Size(size.width * 0.18f, 8.dp.toPx()),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
    )
    drawRoundRect(
      color = StageCameraInk.copy(alpha = 0.08f),
      topLeft = Offset(size.width * 0.76f, size.height * 0.19f),
      size = Size(size.width * 0.16f, 8.dp.toPx()),
      cornerRadius = CornerRadius(8.dp.toPx(), 8.dp.toPx()),
    )
    repeat(4) { index ->
      drawLine(
        color = StageCameraInk.copy(alpha = 0.07f),
        start = Offset(size.width * (0.14f + index * 0.21f), size.height * 0.76f),
        end = Offset(size.width * (0.22f + index * 0.21f), size.height * 0.76f),
        strokeWidth = 2.dp.toPx(),
      )
    }
  }
}

@Composable
private fun StageDynamicIsland(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .size(width = 112.dp, height = 31.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(StageCameraInk)
        .testTag("stage-dynamic-island"),
  ) {
    Box(
      modifier =
        Modifier
          .align(Alignment.CenterEnd)
          .padding(end = 13.dp)
          .size(7.dp)
          .clip(CircleShape)
          .background(StageCameraAccent),
    )
  }
}

@Composable
private fun StagePreviewCard(
  hasCameraPermission: Boolean,
  cameraLens: CameraLens,
  onImageCaptureReady: (ImageCapture?) -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(32.dp))
        .background(StageCameraInk)
        .border(8.dp, StageCameraInk, RoundedCornerShape(32.dp))
        .testTag("stage-preview-card"),
  ) {
    if (hasCameraPermission) {
      CameraSurfacePreview(
        cameraLens = cameraLens,
        meteringOverlayAlpha = 0f,
        onImageCaptureReady = onImageCaptureReady,
        modifier =
          Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(24.dp)),
      )
    }
    Canvas(modifier = Modifier.matchParentSize()) {
      drawRoundRect(
        color = Color.White.copy(alpha = 0.12f),
        cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
        style = Stroke(width = 1.dp.toPx()),
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.72f),
        radius = 1.8.dp.toPx(),
        center = Offset(size.width * 0.5f, 9.dp.toPx()),
      )
    }
  }
}

@Composable
private fun StageToolRow(
  copy: CameraCopy,
  onFrameSettingsClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onLensToggleClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .width(150.dp)
        .testTag("stage-tool-row"),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RoundControlButton(
      color = StageCameraInk,
      contentDescription = copy.settingsButtonContentDescription,
      onClick = onSettingsClick,
      icon = ControlIcon.Flash,
      modifier = Modifier.size(34.dp).testTag("stage-tool-flash"),
    )
    RoundControlButton(
      color = StageCameraInk,
      contentDescription = copy.frameSettingsContentDescription,
      onClick = onFrameSettingsClick,
      icon = ControlIcon.Frame,
      modifier = Modifier.size(34.dp).testTag("stage-tool-frame"),
    )
    RoundControlButton(
      color = StageCameraInk,
      contentDescription = copy.lensLabel,
      onClick = onLensToggleClick,
      icon = ControlIcon.Lens,
      modifier = Modifier.size(34.dp).testTag("stage-tool-lens"),
    )
  }
}

@Composable
private fun StageCaptureDeck(
  copy: CameraCopy,
  onFrameManagerClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onShutterClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .width(258.dp)
        .testTag("stage-capture-deck"),
    horizontalArrangement = Arrangement.SpaceBetween,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    RoundControlButton(
      color = StageCameraInk,
      contentDescription = copy.frameManagerOpenLabel,
      onClick = onFrameManagerClick,
      icon = ControlIcon.Gallery,
      modifier = Modifier.size(44.dp).testTag("stage-frame-manager-button"),
    )
    ShutterButton(
      contentDescription = copy.shutterContentDescription,
      onClick = onShutterClick,
      modifier = Modifier.size(72.dp),
      testTag = "stage-shutter-button",
    )
    RoundControlButton(
      color = StageCameraInk,
      contentDescription = copy.settingsButtonContentDescription,
      onClick = onSettingsClick,
      icon = ControlIcon.Settings,
      modifier = Modifier.size(44.dp).testTag("stage-settings-button"),
    )
  }
}

@Composable
private fun StageHomeLine(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier
        .size(width = 118.dp, height = 4.dp)
        .clip(RoundedCornerShape(4.dp))
        .background(StageCameraInk.copy(alpha = 0.76f)),
  )
}

internal fun stageCameraPreviewTop(statusBarHeight: Dp, maxHeight: Dp): Dp =
  statusBarHeight + if (maxHeight < 760.dp) 74.dp else 112.dp

internal fun stageCameraPreviewWidth(maxWidth: Dp): Dp {
  val candidate = maxWidth * 0.68f
  return if (candidate < 286.dp) candidate else 286.dp
}

internal fun stageCameraPreviewHeight(maxHeight: Dp, previewWidth: Dp): Dp {
  val ideal = previewWidth * 1.34f
  val cap = if (maxHeight < 760.dp) maxHeight * 0.42f else 384.dp
  return if (ideal < cap) ideal else cap
}

internal fun stageCameraToolRowTop(
  statusBarHeight: Dp,
  maxHeight: Dp,
  maxWidth: Dp,
): Dp {
  val previewWidth = stageCameraPreviewWidth(maxWidth)
  val previewHeight = stageCameraPreviewHeight(maxHeight = maxHeight, previewWidth = previewWidth)
  return stageCameraPreviewTop(statusBarHeight = statusBarHeight, maxHeight = maxHeight) +
    previewHeight +
    if (maxHeight < 760.dp) 12.dp else 18.dp
}

internal fun stageCameraShutterTop(
  statusBarHeight: Dp,
  maxHeight: Dp,
  maxWidth: Dp,
): Dp =
  stageCameraToolRowTop(statusBarHeight = statusBarHeight, maxHeight = maxHeight, maxWidth = maxWidth) +
    if (maxHeight < 760.dp) 42.dp else 50.dp

internal fun stageCameraPhotoWallTop(
  statusBarHeight: Dp,
  navigationBarHeight: Dp,
  maxHeight: Dp,
  maxWidth: Dp,
): Dp {
  val preferred =
    stageCameraShutterTop(statusBarHeight = statusBarHeight, maxHeight = maxHeight, maxWidth = maxWidth) +
      128.dp
  val maxAllowed = maxHeight - navigationBarHeight - 180.dp
  val capped = if (preferred < maxAllowed) preferred else maxAllowed
  val minimum = statusBarHeight + if (maxHeight < 760.dp) 430.dp else 560.dp
  return when {
    maxAllowed < minimum -> maxAllowed
    capped > minimum -> capped
    else -> minimum
  }
}
