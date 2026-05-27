package com.example.capsulecardcamera.ui.main

import androidx.camera.core.ImageCapture
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
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
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.onClick
import androidx.compose.ui.semantics.onLongClick
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val StageCameraBackground = Color(0xFFECE5F1)
private val StageCameraPanel = Color(0xFFF8EEF2)
private val StageCameraInk = Color(0xFF121111)
internal val StageCameraCornerButtonSize = 52.dp
internal val StageCameraCornerButtonPadding = 34.dp
internal val StageCameraAlbumThumbnailInset = 7.dp

@Composable
internal fun StageCamera(
  navigationBarHeight: Dp,
  maxWidth: Dp,
  maxHeight: Dp,
  dynamicIslandMetrics: DynamicIslandMetrics,
  hasCameraPermission: Boolean,
  cameraLens: CameraLens,
  copy: CameraCopy,
  onImageCaptureReady: (ImageCapture?) -> Unit,
  albumOpen: Boolean,
  albumFlipProgress: Float,
  latestAlbumPhoto: CapturedPhoto?,
  captureCurtainProgress: Float,
  onSceneTuningChanged: (CameraSceneTuning) -> Unit = {},
  onAlbumClick: () -> Unit,
  onAlbumLongClick: () -> Unit,
  onSettingsClick: () -> Unit,
  onShutterClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val previewWidth = stageCameraPreviewWidth(maxWidth)
  val previewHeight = stageCameraPreviewHeight(maxHeight = maxHeight, previewWidth = previewWidth)
  val logoTop = stageCameraLogoTop(dynamicIslandMetrics = dynamicIslandMetrics, maxHeight = maxHeight)
  val previewTop = stageCameraPreviewTop(dynamicIslandMetrics = dynamicIslandMetrics, maxHeight = maxHeight)
  val shutterTop = stageCameraShutterTop(dynamicIslandMetrics = dynamicIslandMetrics, maxHeight = maxHeight, maxWidth = maxWidth)
  val density = LocalDensity.current

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
      metrics = dynamicIslandMetrics,
      modifier =
        Modifier
          .align(Alignment.TopStart)
          .padding(start = dynamicIslandMetrics.left, top = dynamicIslandMetrics.top),
    )

    if (!albumOpen || albumFlipProgress < 0.58f) {
      val cameraDeckFlipProgress = stageCameraFrontCardProgress(albumFlipProgress)
      Box(
        modifier =
          Modifier
            .matchParentSize()
            .graphicsLayer {
              rotationY = 90f * cameraDeckFlipProgress
              alpha = stageCameraFrontCardAlpha(albumFlipProgress)
              scaleX = 1f - 0.03f * cameraDeckFlipProgress
              cameraDistance = 18f * density.density
            }
            .testTag("stage-camera-deck"),
      ) {
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
              .padding(top = logoTop)
              .testTag("stage-camera-logo"),
        )

        StagePreviewCard(
          hasCameraPermission = hasCameraPermission,
          cameraLens = cameraLens,
          curtainProgress = captureCurtainProgress,
          onImageCaptureReady = onImageCaptureReady,
          onSceneTuningChanged = onSceneTuningChanged,
          modifier =
            Modifier
              .align(Alignment.TopCenter)
              .padding(top = previewTop)
              .size(width = previewWidth, height = previewHeight),
        )

        StageShutterControl(
          contentDescription = copy.shutterContentDescription,
          onShutterClick = onShutterClick,
          modifier =
            Modifier
              .align(Alignment.TopCenter)
              .padding(top = shutterTop),
        )
      }
    }

    StageAlbumButton(
      contentDescription = copy.framedAlbumLabel,
      latestPhoto = latestAlbumPhoto,
      onClick = onAlbumClick,
      onLongClick = onAlbumLongClick,
      modifier =
        Modifier
          .align(Alignment.BottomStart)
          .padding(start = StageCameraCornerButtonPadding, bottom = navigationBarHeight + StageCameraCornerButtonPadding)
          .testTag("stage-album-button"),
    )

    StageCornerButton(
      contentDescription = copy.settingsButtonContentDescription,
      icon = ControlIcon.Settings,
      onClick = onSettingsClick,
      modifier =
        Modifier
          .align(Alignment.BottomEnd)
          .padding(end = StageCameraCornerButtonPadding, bottom = navigationBarHeight + StageCameraCornerButtonPadding)
          .testTag("stage-settings-button"),
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
private fun StageDynamicIsland(
  metrics: DynamicIslandMetrics,
  modifier: Modifier = Modifier,
) {
  DynamicIslandPill(
    metrics = metrics,
    color = StageCameraInk,
    testTag = "stage-dynamic-island",
    modifier = modifier,
  )
}

@Composable
private fun StagePreviewCard(
  hasCameraPermission: Boolean,
  cameraLens: CameraLens,
  curtainProgress: Float,
  onImageCaptureReady: (ImageCapture?) -> Unit,
  onSceneTuningChanged: (CameraSceneTuning) -> Unit,
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
        onSceneTuningChanged = onSceneTuningChanged,
        modifier =
          Modifier
            .matchParentSize()
            .clip(RoundedCornerShape(24.dp)),
      )
    }
    StageShutterCurtain(progress = curtainProgress, modifier = Modifier.matchParentSize())
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
private fun StageShutterCurtain(
  progress: Float,
  modifier: Modifier = Modifier,
) {
  if (progress <= 0f) return
  Canvas(
    modifier =
      modifier
        .testTag("stage-shutter-curtain"),
  ) {
    val coverHeight = size.height * progress.coerceIn(0f, 1f)
    drawRect(
      color = StageCameraInk.copy(alpha = 0.96f),
      topLeft = Offset.Zero,
      size = Size(size.width, coverHeight),
    )
    drawRoundRect(
      brush =
        Brush.verticalGradient(
          colors =
            listOf(
              Color.Transparent,
              Color.Black.copy(alpha = 0.26f),
            ),
          startY = (coverHeight - 44.dp.toPx()).coerceAtLeast(0f),
          endY = coverHeight + 10.dp.toPx(),
        ),
      topLeft = Offset(0f, (coverHeight - 44.dp.toPx()).coerceAtLeast(0f)),
      size = Size(size.width, 54.dp.toPx()),
      cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx()),
    )
    drawRoundRect(
      color = FrameWarmWhite.copy(alpha = 0.42f),
      topLeft = Offset(size.width * 0.38f, (coverHeight - 12.dp.toPx()).coerceAtLeast(0f)),
      size = Size(size.width * 0.24f, 3.dp.toPx()),
      cornerRadius = CornerRadius(3.dp.toPx(), 3.dp.toPx()),
    )
  }
}

@Composable
private fun StageCornerButton(
  contentDescription: String,
  icon: ControlIcon,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  onLongClick: (() -> Unit)? = null,
) {
  RoundControlButton(
    color = StageCameraInk,
    contentDescription = contentDescription,
    onClick = onClick,
    icon = icon,
    modifier = modifier.size(StageCameraCornerButtonSize),
    onLongClick = onLongClick,
  )
}

@Composable
private fun StageAlbumButton(
  contentDescription: String,
  latestPhoto: CapturedPhoto?,
  onClick: () -> Unit,
  onLongClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  if (latestPhoto == null) {
    StageCornerButton(
      contentDescription = contentDescription,
      icon = ControlIcon.Gallery,
      onClick = onClick,
      onLongClick = onLongClick,
      modifier = modifier,
    )
    return
  }

  Box(
    modifier =
      modifier
        .size(StageCameraCornerButtonSize)
        .clip(CircleShape)
        .background(StageCameraInk)
        .border(1.4.dp, Color.Black.copy(alpha = 0.16f), CircleShape)
        .pointerInput(onClick, onLongClick) {
          detectTapGestures(
            onTap = { onClick() },
            onLongPress = { onLongClick() },
          )
        }
        .semantics {
          this.contentDescription = contentDescription
          onClick {
            onClick()
            true
          }
          onLongClick {
            onLongClick()
            true
          }
        },
    contentAlignment = Alignment.Center,
  ) {
    Image(
      bitmap = latestPhoto.bitmap.asImageBitmap(),
      contentDescription = null,
      contentScale = ContentScale.Crop,
      modifier =
        Modifier
          .size(StageCameraCornerButtonSize - StageCameraAlbumThumbnailInset * 2f)
          .clip(RoundedCornerShape(11.dp))
          .border(1.dp, FrameWarmWhite.copy(alpha = 0.72f), RoundedCornerShape(11.dp))
          .testTag("stage-album-thumbnail"),
    )
  }
}

@Composable
private fun StageShutterControl(
  contentDescription: String,
  onShutterClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .testTag("stage-capture-deck"),
    contentAlignment = Alignment.Center,
  ) {
    ShutterButton(
      contentDescription = contentDescription,
      onClick = onShutterClick,
      modifier = Modifier.size(72.dp),
      testTag = "stage-shutter-button",
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

private fun stageCameraFrontCardProgress(progress: Float): Float {
  val normalized = (progress / 0.52f).coerceIn(0f, 1f)
  return normalized * normalized * (3f - 2f * normalized)
}

private fun stageCameraFrontCardAlpha(progress: Float): Float =
  (1f - ((progress - 0.42f) / 0.14f).coerceIn(0f, 1f)).coerceIn(0f, 1f)

internal fun stageCameraLogoTop(dynamicIslandMetrics: DynamicIslandMetrics, maxHeight: Dp): Dp =
  dynamicIslandMetrics.bottom + if (maxHeight < 760.dp) 26.dp else 42.dp

internal fun stageCameraPreviewTop(dynamicIslandMetrics: DynamicIslandMetrics, maxHeight: Dp): Dp =
  stageCameraLogoTop(dynamicIslandMetrics = dynamicIslandMetrics, maxHeight = maxHeight) +
    if (maxHeight < 760.dp) 78.dp else 92.dp

internal fun stageCameraPreviewWidth(maxWidth: Dp): Dp {
  val candidate = maxWidth * 0.68f
  return if (candidate < 286.dp) candidate else 286.dp
}

internal fun stageCameraPreviewHeight(maxHeight: Dp, previewWidth: Dp): Dp {
  val ideal = previewWidth * 1.34f
  val cap = if (maxHeight < 760.dp) maxHeight * 0.42f else 384.dp
  return if (ideal < cap) ideal else cap
}

internal fun stageCameraShutterTop(
  dynamicIslandMetrics: DynamicIslandMetrics,
  maxHeight: Dp,
  maxWidth: Dp,
): Dp {
  val previewWidth = stageCameraPreviewWidth(maxWidth)
  val previewHeight = stageCameraPreviewHeight(maxHeight = maxHeight, previewWidth = previewWidth)
  return stageCameraPreviewTop(dynamicIslandMetrics = dynamicIslandMetrics, maxHeight = maxHeight) +
    previewHeight +
    if (maxHeight < 760.dp) 18.dp else 28.dp
}

internal fun stageCameraPhotoWallTop(
  dynamicIslandMetrics: DynamicIslandMetrics,
  navigationBarHeight: Dp,
  maxHeight: Dp,
  maxWidth: Dp,
): Dp {
  val preferred =
    stageCameraShutterTop(dynamicIslandMetrics = dynamicIslandMetrics, maxHeight = maxHeight, maxWidth = maxWidth) +
      98.dp
  val maxAllowed = maxHeight - navigationBarHeight - 180.dp
  val capped = if (preferred < maxAllowed) preferred else maxAllowed
  val minimum = dynamicIslandMetrics.bottom + if (maxHeight < 760.dp) 400.dp else 528.dp
  return when {
    maxAllowed < minimum -> maxAllowed
    capped > minimum -> capped
    else -> minimum
  }
}

internal fun stageCameraAlbumTop(dynamicIslandMetrics: DynamicIslandMetrics, maxHeight: Dp): Dp =
  dynamicIslandMetrics.bottom + if (maxHeight < 760.dp) 70.dp else 92.dp

internal fun stageCameraAlbumButtonTop(maxHeight: Dp, navigationBarHeight: Dp): Dp =
  maxHeight - navigationBarHeight - StageCameraCornerButtonPadding - StageCameraCornerButtonSize
