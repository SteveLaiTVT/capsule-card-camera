package com.example.capsulecardcamera.ui.main

import android.graphics.Rect
import androidx.camera.core.Camera
import androidx.camera.core.FocusMeteringAction
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageProxy
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.displayCutout
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
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
import androidx.compose.ui.graphics.lerp as lerpColor
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.compose.LocalLifecycleOwner
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetector
import com.google.mlkit.vision.face.FaceDetectorOptions
import java.util.concurrent.Executors
import java.util.concurrent.TimeUnit
import kotlinx.coroutines.delay

private val CameraIslandBlack = Color(0xFF060606)
private val CameraStageBackground = Color(0xFFECE5F1)
private const val CameraIslandFaceFocusMinIntervalMillis = 1200L

@Composable
internal fun PullIsland(
  progress: Float,
  overPullProgress: Float,
  fullScreenProgress: Float,
  maxWidth: Dp,
  maxHeight: Dp,
  statusBarHeight: Dp,
  hasCameraPermission: Boolean,
  countdownValue: Int,
  fullScreenHint: String,
  showFullScreenHint: Boolean,
  cameraLens: CameraLens,
  cameraDisplayStyle: CameraDisplayStyle,
  onImageCaptureReady: (ImageCapture?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val compactWidth = cameraIslandLerpDp(112.dp, 168.dp, progress)
  val frontCameraTopPadding = rememberFrontCameraTopPadding()
  val expandedTopPadding =
    if (frontCameraTopPadding > statusBarHeight) {
      frontCameraTopPadding
    } else {
      statusBarHeight
    }
  val stageProgress =
    if (cameraDisplayStyle == CameraDisplayStyle.StageList) {
      progress * (1f - fullScreenProgress)
    } else {
      0f
    }
  val stageHeight =
    if (maxHeight * 0.46f < 430.dp) {
      maxHeight * 0.46f
    } else {
      430.dp
    }
  val expandedHeight = cameraIslandLerpDp(expandedTopPadding + 172.dp, stageHeight, stageProgress)
  val compactHeight = cameraIslandLerpDp(32.dp, expandedHeight, progress)
  val transitionWidth = cameraIslandLerpDp(compactWidth, maxWidth * 0.82f, overPullProgress)
  val transitionHeight = cameraIslandLerpDp(compactHeight, compactHeight + 92.dp, overPullProgress)
  val transitionRadius = cameraIslandLerpDp(cameraIslandLerpDp(18.dp, 36.dp, progress), 30.dp, overPullProgress)
  val stageWidth = cameraIslandLerpDp(transitionWidth, maxWidth, stageProgress)
  val width = cameraIslandLerpDp(stageWidth, maxWidth, fullScreenProgress)
  val height = cameraIslandLerpDp(transitionHeight, maxHeight, fullScreenProgress)
  val radius = cameraIslandLerpDp(transitionRadius, 0.dp, fullScreenProgress)
  val previewAlpha = (progress / 0.42f).coerceIn(0f, 1f)
  val closedAlpha = (1f - progress * 1.7f).coerceIn(0f, 1f)
  val innerPadding = cameraIslandLerpDp(cameraIslandLerpDp(0.dp, 12.dp, progress), 0.dp, fullScreenProgress)
  val compactTopPadding =
    cameraIslandLerpDp(
      start = 0.dp,
      end =
        if (expandedTopPadding > innerPadding) {
          expandedTopPadding
        } else {
          innerPadding
        },
      progress = progress,
    )
  val topPadding = cameraIslandLerpDp(cameraIslandLerpDp(compactTopPadding, 0.dp, stageProgress), 0.dp, fullScreenProgress)
  val previewRadius = cameraIslandLerpDp(cameraIslandLerpDp(15.dp, 26.dp, progress), 0.dp, fullScreenProgress)
  val stagedPreviewRadius =
    cameraIslandLerpDp(
      cameraIslandLerpDp(previewRadius, 32.dp, stageProgress),
      0.dp,
      fullScreenProgress,
    )
  val stagePreviewWidth = maxWidth * 0.58f
  val stagePreviewHeight =
    if (maxHeight * 0.34f < 320.dp) {
      maxHeight * 0.34f
    } else {
      320.dp
    }
  val nonFullScreenPreviewWidth = cameraIslandLerpDp(width - innerPadding * 2f, stagePreviewWidth, stageProgress)
  val nonFullScreenPreviewHeight = cameraIslandLerpDp(height - topPadding - innerPadding, stagePreviewHeight, stageProgress)
  val previewWidth =
    cameraIslandLerpDp(
      start = nonFullScreenPreviewWidth,
      end = maxWidth,
      progress = fullScreenProgress,
    )
  val previewHeight =
    cameraIslandLerpDp(
      start = nonFullScreenPreviewHeight,
      end = maxHeight,
      progress = fullScreenProgress,
    )
  val thresholdHighlightAlpha = overPullProgress * (1f - fullScreenProgress)
  val previewAssistAlpha = (fullScreenProgress + stageProgress * 0.55f).coerceIn(0f, 1f)

  LaunchedEffect(hasCameraPermission, previewAlpha) {
    if (!hasCameraPermission || previewAlpha <= 0f) {
      onImageCaptureReady(null)
    }
  }

  Box(
    modifier =
      modifier
        .size(width = width, height = height)
        .clip(RoundedCornerShape(radius))
        .background(lerpColor(CameraIslandBlack, CameraStageBackground, stageProgress))
        .border(
          1.dp,
          Color.White.copy(alpha = (0.08f + previewAlpha * 0.08f + thresholdHighlightAlpha * 0.22f) * (1f - fullScreenProgress)),
          RoundedCornerShape(radius),
        )
        .padding(
          PaddingValues(
            start = innerPadding,
            top = topPadding,
            end = innerPadding,
            bottom = innerPadding,
          ),
        )
        .semantics { contentDescription = "Pull down camera island" }
        .testTag("dynamic-island"),
    contentAlignment = Alignment.Center,
  ) {
    if (hasCameraPermission && previewAlpha > 0f) {
      Box(
        modifier =
          Modifier
            .size(width = previewWidth, height = previewHeight)
            .clip(RoundedCornerShape(stagedPreviewRadius))
            .background(CameraIslandBlack)
            .border(
              width = cameraIslandLerpDp(0.dp, 8.dp, stageProgress * (1f - fullScreenProgress)),
              color = CameraIslandBlack.copy(alpha = stageProgress * (1f - fullScreenProgress)),
              shape = RoundedCornerShape(stagedPreviewRadius),
            )
            .graphicsLayer {
              alpha = previewAlpha
              scaleX = cameraIslandLerpFloat(0.72f, 1f, progress)
              scaleY = cameraIslandLerpFloat(0.68f, 1f, progress)
            },
      ) {
        CameraSurfacePreview(
          cameraLens = cameraLens,
          meteringOverlayAlpha = previewAssistAlpha,
          onImageCaptureReady = onImageCaptureReady,
          modifier = Modifier.fillMaxSize(),
        )
        CameraPreviewFrame(
          alpha = previewAlpha,
          radius = stagedPreviewRadius,
          modifier = Modifier.fillMaxSize(),
        )
      }
    }

    CameraPreviewFrame(alpha = previewAlpha * (1f - fullScreenProgress), radius = radius)
    CountdownOverlay(
      value = countdownValue,
      showFullScreenHint = showFullScreenHint,
      fullScreenHint = fullScreenHint,
      thresholdProgress = overPullProgress,
    )
    ClosedIslandMarks(alpha = closedAlpha)
  }
}

@Composable
private fun rememberFrontCameraTopPadding(extraBelowCamera: Dp = 6.dp): Dp {
  val cutoutHeight = WindowInsets.displayCutout.asPaddingValues().calculateTopPadding()
  return if (cutoutHeight > 0.dp) {
    cutoutHeight + extraBelowCamera
  } else {
    12.dp
  }
}

@Composable
internal fun CameraSurfacePreview(
  cameraLens: CameraLens,
  meteringOverlayAlpha: Float,
  onImageCaptureReady: (ImageCapture?) -> Unit,
  modifier: Modifier = Modifier,
) {
  val context = LocalContext.current
  val lifecycleOwner = LocalLifecycleOwner.current
  var previewView by remember { mutableStateOf<PreviewView?>(null) }
  var camera by remember { mutableStateOf<Camera?>(null) }
  var focusPoint by remember { mutableStateOf<Offset?>(null) }
  var focusNonce by remember { mutableIntStateOf(0) }

  LaunchedEffect(focusNonce) {
    if (focusPoint != null) {
      delay(1250L)
      focusPoint = null
    }
  }

  Box(modifier = modifier) {
    AndroidView(
      factory = { viewContext ->
        PreviewView(viewContext).apply {
          implementationMode = PreviewView.ImplementationMode.COMPATIBLE
          scaleType = PreviewView.ScaleType.FILL_CENTER
          previewView = this
        }
      },
      update = { previewView = it },
      modifier =
        Modifier
          .matchParentSize()
          .pointerInput(previewView, camera) {
            detectTapGestures { offset ->
              focusPoint = offset
              focusNonce += 1
              focusCameraAt(previewView = previewView, camera = camera, offset = offset)
            }
          }
          .testTag("camera-preview-surface"),
    )
    CameraMeteringOverlay(
      focusPoint = focusPoint,
      alpha = meteringOverlayAlpha,
      modifier = Modifier.matchParentSize(),
    )
  }

  BindCameraPreview(
    previewView = previewView,
    lifecycleOwner = lifecycleOwner,
    cameraLens = cameraLens,
    onImageCaptureReady = onImageCaptureReady,
    onCameraReady = { camera = it },
    onFaceFocusPoint = { offset ->
      focusCameraAt(previewView = previewView, camera = camera, offset = offset)
    },
    contextExecutorProvider = { ContextCompat.getMainExecutor(context) },
  )
}

@Composable
private fun CameraMeteringOverlay(
  focusPoint: Offset?,
  alpha: Float,
  modifier: Modifier = Modifier,
) {
  Canvas(
    modifier =
      modifier
        .graphicsLayer { this.alpha = alpha.coerceIn(0f, 1f) }
        .testTag("camera-metering-overlay"),
  ) {
    val gridColor = Color.White.copy(alpha = 0.22f)
    val gridStroke = 0.8.dp.toPx()
    drawLine(gridColor, Offset(size.width / 3f, 0f), Offset(size.width / 3f, size.height), gridStroke)
    drawLine(gridColor, Offset(size.width * 2f / 3f, 0f), Offset(size.width * 2f / 3f, size.height), gridStroke)
    drawLine(gridColor, Offset(0f, size.height / 3f), Offset(size.width, size.height / 3f), gridStroke)
    drawLine(gridColor, Offset(0f, size.height * 2f / 3f), Offset(size.width, size.height * 2f / 3f), gridStroke)

    val center = Offset(size.width * 0.5f, size.height * 0.5f)
    val reticleColor = Color.White.copy(alpha = 0.42f)
    drawCircle(reticleColor, radius = 3.dp.toPx(), center = center, style = Stroke(width = 1.dp.toPx()))
    drawLine(reticleColor, Offset(center.x - 18.dp.toPx(), center.y), Offset(center.x - 7.dp.toPx(), center.y), 1.dp.toPx())
    drawLine(reticleColor, Offset(center.x + 7.dp.toPx(), center.y), Offset(center.x + 18.dp.toPx(), center.y), 1.dp.toPx())
    drawLine(reticleColor, Offset(center.x, center.y - 18.dp.toPx()), Offset(center.x, center.y - 7.dp.toPx()), 1.dp.toPx())
    drawLine(reticleColor, Offset(center.x, center.y + 7.dp.toPx()), Offset(center.x, center.y + 18.dp.toPx()), 1.dp.toPx())

    val point = focusPoint ?: return@Canvas
    val ringColor = FrameGreen.copy(alpha = 0.9f)
    val ringRadius = 34.dp.toPx()
    drawCircle(ringColor, radius = ringRadius, center = point, style = Stroke(width = 2.dp.toPx()))
    drawLine(ringColor, Offset(point.x - ringRadius, point.y), Offset(point.x - ringRadius + 10.dp.toPx(), point.y), 2.dp.toPx())
    drawLine(ringColor, Offset(point.x + ringRadius - 10.dp.toPx(), point.y), Offset(point.x + ringRadius, point.y), 2.dp.toPx())
    drawLine(ringColor, Offset(point.x, point.y - ringRadius), Offset(point.x, point.y - ringRadius + 10.dp.toPx()), 2.dp.toPx())
    drawLine(ringColor, Offset(point.x, point.y + ringRadius - 10.dp.toPx()), Offset(point.x, point.y + ringRadius), 2.dp.toPx())

    val exposureX = (point.x + ringRadius + 14.dp.toPx()).coerceAtMost(size.width - 18.dp.toPx())
    val exposureTop = (point.y - 42.dp.toPx()).coerceIn(12.dp.toPx(), size.height - 96.dp.toPx())
    drawLine(
      color = Color.White.copy(alpha = 0.78f),
      start = Offset(exposureX, exposureTop),
      end = Offset(exposureX, exposureTop + 84.dp.toPx()),
      strokeWidth = 1.3.dp.toPx(),
      cap = StrokeCap.Round,
    )
    drawCircle(
      color = FrameWarmWhite.copy(alpha = 0.92f),
      radius = 5.dp.toPx(),
      center = Offset(exposureX, exposureTop + 42.dp.toPx()),
    )
  }
}

@Composable
private fun BindCameraPreview(
  previewView: PreviewView?,
  lifecycleOwner: LifecycleOwner,
  cameraLens: CameraLens,
  onImageCaptureReady: (ImageCapture?) -> Unit,
  onCameraReady: (Camera?) -> Unit,
  onFaceFocusPoint: (Offset) -> Unit,
  contextExecutorProvider: () -> java.util.concurrent.Executor,
) {
  val context = LocalContext.current

  DisposableEffect(previewView, lifecycleOwner, cameraLens) {
    if (previewView == null) {
      return@DisposableEffect onDispose {}
    }

    val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
    val executor = contextExecutorProvider()
    val analysisExecutor = Executors.newSingleThreadExecutor()
    val faceDetector =
      FaceDetection.getClient(
        FaceDetectorOptions
          .Builder()
          .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
          .setMinFaceSize(0.12f)
          .build(),
      )
    var lastFaceFocusAt = 0L
    val listener =
      Runnable {
        runCatching {
          val cameraProvider = cameraProviderFuture.get()
          val preview =
            Preview.Builder().build().apply {
              setSurfaceProvider(previewView.surfaceProvider)
            }
          val imageCapture =
            ImageCapture.Builder()
              .setCaptureMode(ImageCapture.CAPTURE_MODE_MINIMIZE_LATENCY)
              .build()
          val imageAnalysis =
            ImageAnalysis.Builder()
              .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
              .build()
              .apply {
                setAnalyzer(analysisExecutor) { imageProxy ->
                  analyzeFacesForFocus(
                    imageProxy = imageProxy,
                    previewView = previewView,
                    cameraLens = cameraLens,
                    detector = faceDetector,
                    onFaceFocusPoint = { offset ->
                      val now = System.currentTimeMillis()
                      if (now - lastFaceFocusAt >= CameraIslandFaceFocusMinIntervalMillis) {
                        lastFaceFocusAt = now
                        onFaceFocusPoint(offset)
                      }
                    },
                  )
                }
              }

          cameraProvider.unbindAll()
          val camera = cameraProvider.bindToLifecycle(lifecycleOwner, cameraLens.cameraSelector(), preview, imageCapture, imageAnalysis)
          onCameraReady(camera)
          onImageCaptureReady(imageCapture)
        }.onFailure {
          onCameraReady(null)
          onImageCaptureReady(null)
        }
      }

    cameraProviderFuture.addListener(listener, executor)

    onDispose {
      runCatching {
        if (cameraProviderFuture.isDone) {
          cameraProviderFuture.get().unbindAll()
        }
      }
      onCameraReady(null)
      onImageCaptureReady(null)
      faceDetector.close()
      analysisExecutor.shutdown()
    }
  }
}

private fun focusCameraAt(
  previewView: PreviewView?,
  camera: Camera?,
  offset: Offset,
) {
  val meteringPoint = previewView?.meteringPointFactory?.createPoint(offset.x, offset.y) ?: return
  val action =
    FocusMeteringAction
      .Builder(meteringPoint, FocusMeteringAction.FLAG_AF or FocusMeteringAction.FLAG_AE)
      .setAutoCancelDuration(3, TimeUnit.SECONDS)
      .build()
  camera?.cameraControl?.startFocusAndMetering(action)
}

private fun analyzeFacesForFocus(
  imageProxy: ImageProxy,
  previewView: PreviewView,
  cameraLens: CameraLens,
  detector: FaceDetector,
  onFaceFocusPoint: (Offset) -> Unit,
) {
  val mediaImage = imageProxy.image
  if (mediaImage == null) {
    imageProxy.close()
    return
  }

  val rotationDegrees = imageProxy.imageInfo.rotationDegrees
  val image = InputImage.fromMediaImage(mediaImage, rotationDegrees)
  val sourceWidth =
    if (rotationDegrees == 90 || rotationDegrees == 270) {
      imageProxy.height
    } else {
      imageProxy.width
    }
  val sourceHeight =
    if (rotationDegrees == 90 || rotationDegrees == 270) {
      imageProxy.width
    } else {
      imageProxy.height
    }

  detector
    .process(image)
    .addOnSuccessListener { faces ->
      val face = faces.maxByOrNull { it.boundingBox.width() * it.boundingBox.height() } ?: return@addOnSuccessListener
      onFaceFocusPoint(face.boundingBox.toPreviewOffset(sourceWidth = sourceWidth, sourceHeight = sourceHeight, previewView = previewView, cameraLens = cameraLens))
    }
    .addOnCompleteListener {
      imageProxy.close()
    }
}

private fun Rect.toPreviewOffset(
  sourceWidth: Int,
  sourceHeight: Int,
  previewView: PreviewView,
  cameraLens: CameraLens,
): Offset {
  if (sourceWidth <= 0 || sourceHeight <= 0 || previewView.width <= 0 || previewView.height <= 0) {
    return Offset.Zero
  }

  val normalizedX =
    (centerX().toFloat() / sourceWidth.toFloat())
      .coerceIn(0f, 1f)
      .let { x ->
        if (cameraLens == CameraLens.Front) {
          1f - x
        } else {
          x
        }
      }
  val normalizedY = (centerY().toFloat() / sourceHeight.toFloat()).coerceIn(0f, 1f)

  return Offset(
    x = normalizedX * previewView.width,
    y = normalizedY * previewView.height,
  )
}

@Composable
private fun CameraPreviewFrame(
  alpha: Float,
  radius: Dp,
  modifier: Modifier = Modifier,
) {
  Canvas(modifier = modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
    drawRoundRect(
      color = Color.Black.copy(alpha = 0.15f),
      topLeft = Offset.Zero,
      size = size,
      cornerRadius = CornerRadius(radius.toPx(), radius.toPx()),
      style = Stroke(width = 1.dp.toPx()),
    )
    drawCircle(
      color = Color.White.copy(alpha = 0.75f),
      radius = 1.6.dp.toPx(),
      center = Offset(size.width * 0.5f, 6.dp.toPx()),
    )
  }
}

@Composable
private fun ClosedIslandMarks(alpha: Float) {
  Canvas(modifier = Modifier.fillMaxSize().graphicsLayer { this.alpha = alpha }) {
    drawRoundRect(
      brush =
        Brush.verticalGradient(
          listOf(Color.White.copy(alpha = 0.16f), Color.White.copy(alpha = 0.02f)),
        ),
      topLeft = Offset(size.width * 0.19f, 6.dp.toPx()),
      size = Size(size.width * 0.62f, 4.dp.toPx()),
      cornerRadius = CornerRadius(3.dp.toPx()),
    )
    drawCircle(
      color = Color.White.copy(alpha = 0.55f),
      radius = 1.7.dp.toPx(),
      center = Offset(size.width * 0.5f, size.height * 0.52f),
    )
  }
}

@Composable
private fun CountdownOverlay(
  value: Int,
  showFullScreenHint: Boolean,
  fullScreenHint: String,
  thresholdProgress: Float,
) {
  if (value == 0) return

  Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
    Text(
      text = value.toString(),
      color = FrameWarmWhite,
      fontSize = 58.sp,
      lineHeight = 58.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      modifier =
        Modifier
          .graphicsLayer {
            alpha = 0.96f
            shadowElevation = 12f
          }
          .testTag("countdown-value"),
    )

    if (showFullScreenHint) {
      FullScreenThresholdCue(
        progress = thresholdProgress,
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(bottom = 46.dp),
      )

      Text(
        text = fullScreenHint,
        color = FrameWarmWhite.copy(alpha = 0.9f),
        fontSize = 12.sp,
        lineHeight = 15.sp,
        fontWeight = FontWeight.Bold,
        textAlign = TextAlign.Center,
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(horizontal = 16.dp, vertical = 18.dp)
            .graphicsLayer {
              alpha = 0.96f
              shadowElevation = 8f
            }
            .testTag("fullscreen-pull-hint"),
      )
    }
  }
}

@Composable
private fun FullScreenThresholdCue(
  progress: Float,
  modifier: Modifier = Modifier,
) {
  Canvas(
    modifier =
      modifier
        .size(width = 108.dp, height = 6.dp)
        .graphicsLayer {
          alpha = cameraIslandLerpFloat(0.42f, 1f, progress)
          scaleX = cameraIslandLerpFloat(0.88f, 1.08f, progress)
        }
        .testTag("fullscreen-threshold-cue"),
  ) {
    val radius = size.height / 2f
    drawRoundRect(
      color = FrameWarmWhite.copy(alpha = 0.22f),
      size = size,
      cornerRadius = CornerRadius(radius, radius),
    )
    drawRoundRect(
      color = FrameGreen.copy(alpha = 0.86f),
      size = Size(width = size.width * progress.coerceIn(0f, 1f), height = size.height),
      cornerRadius = CornerRadius(radius, radius),
    )
  }
}

private fun cameraIslandLerpDp(start: Dp, end: Dp, progress: Float): Dp = start + (end - start) * progress

private fun cameraIslandLerpFloat(start: Float, end: Float, progress: Float): Float = start + (end - start) * progress
