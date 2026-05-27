package com.example.capsulecardcamera.ui.main

import android.Manifest
import android.os.Build
import android.graphics.Bitmap
import android.content.pm.PackageManager
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.core.animateFloatAsState
import androidx.camera.core.ImageCapture
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.AnimationSpec
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.draggable
import androidx.compose.foundation.gestures.rememberDraggableState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview as ComposePreview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.example.capsulecardcamera.theme.CapsuleCardCameraTheme
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

private const val CaptureStartProgress = 0.995f
private const val CaptureCancelProgress = 0.9f
private const val CaptureCancelVelocity = -700f
private const val CountdownStepMillis = 850L
private const val FullScreenHintStartProgress = 1.04f
private const val FullScreenCommitProgress = 1.24f
private const val FullScreenEndProgress = 1.66f
private const val StageCapturePrintDurationMillis = 2600
private const val StageAlbumFlipDurationMillis = 560

@Composable
fun MainScreen(
  modifier: Modifier = Modifier,
) {
  PullDownIslandCameraDemo(modifier = modifier)
}

@Composable
internal fun MainScreenWithDependencies(
  modifier: Modifier = Modifier,
  photoAiAnalyzer: PhotoAiAnalyzer? = null,
  photoFrameGenerator: PhotoFrameGenerator? = null,
) {
  PullDownIslandCameraDemo(modifier = modifier, photoAiAnalyzer = photoAiAnalyzer, photoFrameGenerator = photoFrameGenerator)
}

@Composable
private fun PullDownIslandCameraDemo(
  modifier: Modifier = Modifier,
  photoAiAnalyzer: PhotoAiAnalyzer? = null,
  photoFrameGenerator: PhotoFrameGenerator? = null,
) {
  val expansion = remember { Animatable(0f) }
  val scope = rememberCoroutineScope()
  val density = LocalDensity.current
  val context = LocalContext.current
  val hapticFeedback = LocalHapticFeedback.current
  val maxPullPx = with(density) { 174.dp.toPx() }
  val statusBarHeight = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val navigationBarHeight = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val rawProgress = expansion.value.coerceIn(0f, FullScreenEndProgress)
  val progress = rawProgress.coerceIn(0f, 1f)
  val overPullProgress = smoothStep(cameraOverPullProgress(rawProgress))
  val fullScreenProgress = smoothStep(cameraFullScreenProgress(rawProgress))
  val shutterSoundPlayer = rememberShutterSoundEffectPlayer()
  var cameraPreferences by remember { mutableStateOf(loadCameraPreferences(context)) }
  val analyzer =
    remember(photoAiAnalyzer) {
      photoAiAnalyzer ?: createDefaultPhotoAiAnalyzer()
    }
  val frameGenerator =
    remember(photoFrameGenerator) {
      photoFrameGenerator ?: createDefaultPhotoFrameGenerator()
    }
  var imageCapture by remember { mutableStateOf<ImageCapture?>(null) }
  val copy = cameraPreferences.copyText()
  val isStageCamera = cameraPreferences.cameraDisplayStyle == CameraDisplayStyle.StageList
  var countdownValue by remember { mutableIntStateOf(0) }
  var captureLockedForFullPull by remember { mutableStateOf(false) }
  var isCaptureInProgress by remember { mutableStateOf(false) }
  var suppressCountdownAtExpanded by remember { mutableStateOf(false) }
  val captureFlashAlpha = remember { Animatable(0f) }
  val thumbnailFlightProgress = remember { Animatable(1f) }
  val stageCaptureCurtainProgress = remember { Animatable(0f) }
  var cameraSceneTuning by remember { mutableStateOf(CameraSceneTuning.Neutral) }
  var flyingThumbnail by remember { mutableStateOf<CapturedPhoto?>(null) }
  var captureFeedbackMode by remember { mutableStateOf(CaptureFeedbackMode.PullList) }
  var nextPhotoId by remember { mutableIntStateOf(0) }
  var capturedPhotos by remember { mutableStateOf<List<CapturedPhoto>>(emptyList()) }
  var selectedPhotoIds by remember { mutableStateOf<Set<Int>>(emptySet()) }
  var isStageAlbumOpen by remember { mutableStateOf(false) }
  val stageAlbumFlipProgress by
    animateFloatAsState(
      targetValue = if (isStageCamera && isStageAlbumOpen) 1f else 0f,
      animationSpec = tween(durationMillis = StageAlbumFlipDurationMillis, easing = FastOutSlowInEasing),
      label = "stageAlbumFlipProgress",
    )
  var defaultFrameStyle by remember { mutableStateOf(PhotoFrameStyle.Polaroid) }
  var defaultGeneratedFrameSpec by remember { mutableStateOf<GeneratedFrameSpec?>(null) }
  var customFrameSpecs by remember { mutableStateOf<List<GeneratedFrameSpec>>(emptyList()) }
  var overlayRoute by remember { mutableStateOf<CameraOverlayRoute?>(null) }
  val frameSettingsRoute = overlayRoute as? CameraOverlayRoute.FrameSettings
  val frameSettingsPhotoId = frameSettingsRoute?.photoId
  val isFrameSettingsOpen = frameSettingsRoute != null
  val isSettingsOpen = overlayRoute == CameraOverlayRoute.Settings
  val isFrameManagerOpen = overlayRoute == CameraOverlayRoute.FrameManager
  var draftFrameStyle by remember { mutableStateOf(defaultFrameStyle) }
  var frameManagerGenerationState by remember { mutableStateOf<FrameGenerationState>(FrameGenerationState.Idle) }
  var frameManagerConversation by remember { mutableStateOf<List<FrameConversationMessage>>(emptyList()) }
  var pendingSaveRequests by remember { mutableStateOf<List<FrameSaveRequest>>(emptyList()) }
  var hasCameraPermission by remember {
    mutableStateOf(ContextCompat.checkSelfPermission(context, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED)
  }
  var permissionRequested by remember { mutableStateOf(false) }

  val permissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      hasCameraPermission = granted
      permissionRequested = true
    }

  fun saveFrameRequests(requests: List<FrameSaveRequest>) {
    if (requests.isEmpty()) return
    scope.launch {
      val savedCount =
        withContext(Dispatchers.IO) {
          requests.count { request ->
            saveFramedPhoto(
              context = context.applicationContext,
              bitmap = request.bitmap,
              frameStyle = request.frameStyle,
              defaultAlbum = request.defaultAlbum,
              metadata = request.metadata,
              generatedFrameSpec = request.generatedFrameSpec,
            )
          }
        }
      val albumName = requests.first().defaultAlbum.displayName(copy)
      Toast
        .makeText(
          context,
          if (savedCount == requests.size) {
            if (requests.size == 1) {
              "${copy.savedToAlbumPrefix} $albumName"
            } else {
              copy.savedPhotosTemplate.format(savedCount, albumName)
            }
          } else if (savedCount > 0) {
            copy.savedPhotosTemplate.format(savedCount, albumName)
          } else {
            copy.saveFailed
          },
          Toast.LENGTH_SHORT,
        )
        .show()
    }
  }

  val storagePermissionLauncher =
    rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
      val requests = pendingSaveRequests
      pendingSaveRequests = emptyList()
      if (granted && requests.isNotEmpty()) {
        saveFrameRequests(requests)
      } else {
        Toast.makeText(context, copy.storagePermissionDenied, Toast.LENGTH_SHORT).show()
      }
  }

  fun requestSaveFrameRequests(requests: List<FrameSaveRequest>) {
    if (requests.isEmpty()) return
    if (
      Build.VERSION.SDK_INT < Build.VERSION_CODES.Q &&
        ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
    ) {
      pendingSaveRequests = requests
      storagePermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
    } else {
      saveFrameRequests(requests)
    }
  }

  fun requestSaveFramedPhotos(photos: List<CapturedPhoto>) {
    requestSaveFrameRequests(photos.toFrameSaveRequests(cameraPreferences.defaultAlbum))
  }

  fun requestSaveFramedPhoto(photo: CapturedPhoto, frameStyle: PhotoFrameStyle) {
    requestSaveFrameRequests(listOf(photo.toFrameSaveRequest(defaultAlbum = cameraPreferences.defaultAlbum, frameStyle = frameStyle)))
  }

  fun selectFrameStyle(frameStyle: PhotoFrameStyle) {
    draftFrameStyle = frameStyle
    if (frameSettingsPhotoId == null) {
      defaultFrameStyle = frameStyle
      defaultGeneratedFrameSpec = null
    } else {
      capturedPhotos = updateCapturedPhotoFrameStyle(capturedPhotos, frameSettingsPhotoId, frameStyle)
    }
  }

  fun selectGeneratedFrameSpec(frameSpec: GeneratedFrameSpec) {
    draftFrameStyle = frameSpec.baseStyle
    if (frameSettingsPhotoId == null) {
      defaultFrameStyle = frameSpec.baseStyle
      defaultGeneratedFrameSpec = frameSpec
    } else {
      capturedPhotos = updateCapturedPhotoGeneratedFrameSpec(capturedPhotos, frameSettingsPhotoId, frameSpec)
    }
  }

  fun setDefaultGeneratedFrameSpec(frameSpec: GeneratedFrameSpec) {
    defaultFrameStyle = frameSpec.baseStyle
    defaultGeneratedFrameSpec = frameSpec
    customFrameSpecs = addGeneratedFrameSpecToLibrary(customFrameSpecs, frameSpec)
    frameManagerGenerationState = FrameGenerationState.Ready(frameSpec)
  }

  fun updatePhotoAiState(photoId: Int, aiState: PhotoAiState) {
    capturedPhotos = updateCapturedPhotoAiState(capturedPhotos, photoId, aiState)
  }

  fun togglePhotoAiTag(photoId: Int, tag: String) {
    capturedPhotos = toggleCapturedPhotoAiTag(capturedPhotos, photoId, tag)
  }

  fun updatePhotoFrameGenerationState(photoId: Int, frameGenerationState: FrameGenerationState) {
    capturedPhotos = updateCapturedPhotoFrameGenerationState(capturedPhotos, photoId, frameGenerationState)
    if (frameGenerationState is FrameGenerationState.Ready) {
      customFrameSpecs = addGeneratedFrameSpecToLibrary(customFrameSpecs, frameGenerationState.spec)
    }
    if (frameSettingsPhotoId == photoId && frameGenerationState is FrameGenerationState.Ready) {
      draftFrameStyle = frameGenerationState.spec.baseStyle
    }
  }

  fun generateFrameForPhoto(photoId: Int, instruction: String) {
    val photo = capturedPhotos.firstOrNull { it.id == photoId } ?: return
    val insight = (photo.aiState as? PhotoAiState.Ready)?.insight ?: return
    capturedPhotos = beginCapturedPhotoFrameGeneration(capturedPhotos, photoId, instruction)
    scope.launch {
      val generationId = PhotoAiDiagnostics.nextAnalysisId("frame", photoId)
      frameGenerator
        .generateFrame(
          bitmap = photo.bitmap,
          insight = insight,
          selectedTags = photo.selectedAiTags,
          currentFrameStyle = photo.frameStyle,
          previousFrameSpec = photo.generatedFrameSpec,
          conversation = photo.frameConversation,
          instruction = instruction,
          generationId = generationId,
        )
        .collect { frameGenerationState ->
          updatePhotoFrameGenerationState(photoId, frameGenerationState)
        }
    }
  }

  fun generateManagedFrame(instruction: String) {
    val request = instruction.trim().ifBlank { "Generate a custom frame from my idea" }
    frameManagerGenerationState = FrameGenerationState.Generating
    frameManagerConversation = frameManagerConversation + FrameConversationMessage(FrameConversationRole.User, request)
    scope.launch {
      val generationId = PhotoAiDiagnostics.nextAnalysisId("frame_manager")
      val promptInsight =
        PhotoInsight(
          title = request.take(36),
          tags = request.split(Regex("\\s+")).map { it.trim(',', '.', ';') }.filter { it.isNotBlank() }.take(6),
          subject = "custom frame",
          scene = "frame studio",
          colors = emptyList(),
          confidence = PhotoInsightConfidence.High,
          suggestedFrameStyle = defaultGeneratedFrameSpec?.baseStyle ?: defaultFrameStyle,
        )
      frameGenerator
        .generateFrame(
          bitmap = null,
          insight = promptInsight,
          selectedTags = emptySet(),
          currentFrameStyle = defaultGeneratedFrameSpec?.baseStyle ?: defaultFrameStyle,
          previousFrameSpec = customFrameSpecs.firstOrNull(),
          conversation = frameManagerConversation,
          instruction = request,
          generationId = generationId,
        )
        .collect { generationState ->
          when (generationState) {
            is FrameGenerationState.Ready -> {
              val frameSpec = generationState.spec
              customFrameSpecs = addGeneratedFrameSpecToLibrary(customFrameSpecs, frameSpec)
              defaultFrameStyle = frameSpec.baseStyle
              defaultGeneratedFrameSpec = frameSpec
              frameManagerGenerationState = generationState
              frameManagerConversation =
                frameManagerConversation +
                  FrameConversationMessage(
                    role = FrameConversationRole.Assistant,
                    text = frameSpec.reason.ifBlank { frameSpec.title },
                  )
            }
            else -> frameManagerGenerationState = generationState
          }
        }
    }
  }

  fun addCapturedPhoto(bitmap: Bitmap): CapturedPhoto {
    val photoId = nextPhotoId
    val captureStyle =
      if (defaultGeneratedFrameSpec != null) {
        PhotoCaptureStyle.AiImmersive
      } else {
        PhotoCaptureStyle.Clean
      }
    val photo = createCapturedPhoto(photoId, bitmap, defaultFrameStyle, defaultGeneratedFrameSpec, captureStyle)
    nextPhotoId += 1
    capturedPhotos = prependCapturedPhoto(capturedPhotos, photo)
    return photo
  }

  fun maybeGeneratePhotoAwareFrame(photoId: Int) {
    val photo = capturedPhotos.firstOrNull { it.id == photoId } ?: return
    if (!photo.captureStyle.shouldGeneratePhotoAwareFrame()) return
    if (photo.frameGenerationState == FrameGenerationState.Generating) return
    if (photo.aiState !is PhotoAiState.Ready) return
    generateFrameForPhoto(photoId, photo.captureStyle.photoAwareFrameInstruction(photo.generatedFrameSpec))
  }

  suspend fun applyPhotoAiEnhancement(
    photoId: Int,
    insight: PhotoInsight,
  ) {
    val photo = capturedPhotos.firstOrNull { it.id == photoId } ?: return
    if (photo.aiEnhancementApplied) return
    val profile = PhotoEnhancementProfile.fromInsight(insight)
    val enhancedBitmap = withContext(Dispatchers.Default) { enhanceBitmap(photo.bitmap, profile) }
    capturedPhotos =
      updateCapturedPhotoBitmap(
        photos = capturedPhotos,
        photoId = photoId,
        bitmap = enhancedBitmap,
        aiEnhancementApplied = true,
      )
  }

  fun analyzeCapturedPhoto(photoId: Int, bitmap: Bitmap) {
    scope.launch {
      val analysisId = PhotoAiDiagnostics.nextAnalysisId("gemini_nano", photoId)
      val startedAt = PhotoAiDiagnostics.now()
      PhotoAiDiagnostics.info(
        "analysis=$analysisId ui start photoId=$photoId provider=gemini_nano bitmap=${PhotoAiDiagnostics.bitmapLabel(bitmap)}",
      )
      try {
        analyzer.analyze(bitmap, analysisId).collect { aiState ->
          PhotoAiDiagnostics.info(
            "analysis=$analysisId ui state=${aiState.diagnosticName()} elapsedMs=${PhotoAiDiagnostics.elapsedMs(startedAt)}${aiState.diagnosticDetail()}",
          )
          updatePhotoAiState(photoId, aiState)
          if (aiState is PhotoAiState.Ready) {
            applyPhotoAiEnhancement(photoId = photoId, insight = aiState.insight)
            maybeGeneratePhotoAwareFrame(photoId)
          }
        }
      } finally {
        PhotoAiDiagnostics.info("analysis=$analysisId ui complete elapsedMs=${PhotoAiDiagnostics.elapsedMs(startedAt)}")
      }
    }
  }

  fun clearPhotoSelection() {
    selectedPhotoIds = emptySet()
  }

  fun enterPhotoSelection(photo: CapturedPhoto) {
    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    countdownValue = 0
    selectedPhotoIds = setOf(photo.id)
  }

  fun togglePhotoSelection(photo: CapturedPhoto) {
    selectedPhotoIds = toggleCapturedPhotoSelection(selectedPhotoIds, photo.id)
  }

  fun applyFrameToSelectedPhotos(frameStyle: PhotoFrameStyle) {
    capturedPhotos = applyFrameStyleToCapturedPhotos(capturedPhotos, selectedPhotoIds, frameStyle)
  }

  fun saveSelectedPhotos() {
    requestSaveFramedPhotos(capturedPhotos.filter { it.id in selectedPhotoIds })
  }

  fun deleteSelectedPhotos() {
    if (selectedPhotoIds.isEmpty()) return
    capturedPhotos = deleteCapturedPhotos(capturedPhotos, selectedPhotoIds)
    clearPhotoSelection()
  }

  fun handleCapturedPhoto(bitmap: Bitmap): CapturedPhoto {
    val photo = addCapturedPhoto(bitmap)
    analyzeCapturedPhoto(photo.id, bitmap)
    return photo
  }

  fun playCapturePrintEffect(photo: CapturedPhoto, feedbackMode: CaptureFeedbackMode) {
    scope.launch {
      captureFlashAlpha.stop()
      captureFlashAlpha.snapTo(0.42f)
      launch { captureFlashAlpha.animateTo(0f, animationSpec = tween(durationMillis = 380)) }

      thumbnailFlightProgress.stop()
      captureFeedbackMode = feedbackMode
      flyingThumbnail = photo
      thumbnailFlightProgress.snapTo(0f)
      thumbnailFlightProgress.animateTo(
        targetValue = 1f,
        animationSpec =
          tween(
            durationMillis = if (feedbackMode == CaptureFeedbackMode.StageCamera) StageCapturePrintDurationMillis else 560,
            easing = if (feedbackMode == CaptureFeedbackMode.StageCamera) LinearEasing else FastOutSlowInEasing,
          ),
      )
      delay(80L)
      if (flyingThumbnail?.id == photo.id) {
        flyingThumbnail = null
      }
    }
  }

  fun playStageCaptureCurtain() {
    scope.launch {
      stageCaptureCurtainProgress.stop()
      stageCaptureCurtainProgress.snapTo(0f)
      stageCaptureCurtainProgress.animateTo(
        targetValue = 1f,
        animationSpec = tween(durationMillis = 180, easing = FastOutSlowInEasing),
      )
      delay(110L)
      stageCaptureCurtainProgress.animateTo(
        targetValue = 0f,
        animationSpec = tween(durationMillis = 340, easing = FastOutSlowInEasing),
      )
    }
  }

  fun collapseCameraIsland() {
    suppressCountdownAtExpanded = false
    scope.launch {
      expansion.animateTo(
        targetValue = 0f,
        animationSpec = spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.9f),
      )
    }
  }

  fun finishCapturedBitmap(
    bitmap: Bitmap,
    feedbackMode: CaptureFeedbackMode,
    collapseAfterCapture: Boolean,
  ) {
    scope.launch {
      val enhancedBitmap = withContext(Dispatchers.Default) { enhanceBitmap(bitmap, cameraSceneTuning.enhancementProfile) }
      isCaptureInProgress = false
      val photo = handleCapturedPhoto(enhancedBitmap)
      playCapturePrintEffect(photo = photo, feedbackMode = feedbackMode)
      if (collapseAfterCapture) {
        collapseCameraIsland()
      } else {
        captureLockedForFullPull = false
      }
    }
  }

  fun closeFrameSettings() {
    if (overlayRoute is CameraOverlayRoute.FrameSettings) {
      overlayRoute = null
    }
    captureLockedForFullPull = false
  }

  fun closeSettings() {
    if (overlayRoute == CameraOverlayRoute.Settings) {
      overlayRoute = null
    }
    captureLockedForFullPull = false
  }

  fun closeFrameManager() {
    if (overlayRoute == CameraOverlayRoute.FrameManager) {
      overlayRoute = null
    }
    captureLockedForFullPull = false
  }

  fun openFrameSettings(photoId: Int?) {
    clearPhotoSelection()
    countdownValue = 0
    captureLockedForFullPull = true
    draftFrameStyle =
      if (photoId == null) {
        defaultFrameStyle
      } else {
        capturedPhotos.firstOrNull { it.id == photoId }?.frameStyle ?: defaultFrameStyle
      }
    overlayRoute = CameraOverlayRoute.FrameSettings(photoId)
  }

  fun openSettings() {
    clearPhotoSelection()
    isStageAlbumOpen = false
    countdownValue = 0
    captureLockedForFullPull = true
    overlayRoute = CameraOverlayRoute.Settings
  }

  fun openFrameManager() {
    clearPhotoSelection()
    isStageAlbumOpen = false
    countdownValue = 0
    captureLockedForFullPull = true
    overlayRoute = CameraOverlayRoute.FrameManager
  }

  fun captureImmediately(collapseAfterCapture: Boolean) {
    val capture = imageCapture ?: return
    if (!hasCameraPermission || isFrameSettingsOpen || isSettingsOpen || isFrameManagerOpen || isCaptureInProgress) return

    countdownValue = 0
    captureLockedForFullPull = true
    isCaptureInProgress = true
    if (!collapseAfterCapture) {
      isStageAlbumOpen = false
      playStageCaptureCurtain()
    }
    shutterSoundPlayer.play(cameraPreferences)
    capturePhoto(
      imageCapture = capture,
      executor = ContextCompat.getMainExecutor(context),
      onCaptured = { bitmap ->
        finishCapturedBitmap(
          bitmap = bitmap,
          feedbackMode = if (collapseAfterCapture) CaptureFeedbackMode.PullList else CaptureFeedbackMode.StageCamera,
          collapseAfterCapture = collapseAfterCapture,
        )
      },
      onError = {
        isCaptureInProgress = false
        captureLockedForFullPull = false
      },
    )
  }

  fun updateCameraPreferences(nextPreferences: CameraPreferences) {
    val lensChanged = nextPreferences.cameraLens != cameraPreferences.cameraLens
    val displayStyleChanged = nextPreferences.cameraDisplayStyle != cameraPreferences.cameraDisplayStyle
    cameraPreferences = nextPreferences
    saveCameraPreferences(context.applicationContext, nextPreferences)
    if (lensChanged || displayStyleChanged) {
      imageCapture = null
      countdownValue = 0
      captureLockedForFullPull = true
    }
    if (displayStyleChanged) {
      isStageAlbumOpen = false
      suppressCountdownAtExpanded = false
      scope.launch { expansion.snapTo(0f) }
    }
  }

  fun toggleCameraLens() {
    val nextLens =
      when (cameraPreferences.cameraLens) {
        CameraLens.Front -> CameraLens.Back
        CameraLens.Back -> CameraLens.Front
      }
    updateCameraPreferences(cameraPreferences.copy(cameraLens = nextLens))
  }

  fun toggleStageAlbum() {
    if (!isStageCamera) return
    clearPhotoSelection()
    isStageAlbumOpen = !isStageAlbumOpen
  }

  fun flipCameraAndOpenStageAlbum() {
    if (!isStageCamera) return
    hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
    clearPhotoSelection()
    toggleCameraLens()
    isStageAlbumOpen = true
  }

  LaunchedEffect(isStageCamera, progress, hasCameraPermission, permissionRequested) {
    if ((isStageCamera || progress > 0.01f) && !hasCameraPermission && !permissionRequested) {
      permissionRequested = true
      permissionLauncher.launch(Manifest.permission.CAMERA)
    }
  }

  LaunchedEffect(isStageCamera, rawProgress, countdownValue, isCaptureInProgress, isFrameSettingsOpen, isSettingsOpen, isFrameManagerOpen) {
    if (isStageCamera || isCaptureInProgress || isFrameSettingsOpen || isSettingsOpen || isFrameManagerOpen) {
      return@LaunchedEffect
    }
    if (rawProgress < 0.82f) {
      suppressCountdownAtExpanded = false
      captureLockedForFullPull = false
    } else if (
      !suppressCountdownAtExpanded &&
        rawProgress >= CaptureStartProgress &&
        rawProgress < FullScreenCommitProgress &&
        countdownValue == 0
    ) {
      captureLockedForFullPull = false
    }
  }

  LaunchedEffect(isStageCamera, fullScreenProgress) {
    if (!isStageCamera && fullScreenProgress > 0f) {
      countdownValue = 0
      captureLockedForFullPull = true
    }
  }

  LaunchedEffect(capturedPhotos) {
    val availableIds = capturedPhotos.map { it.id }.toSet()
    selectedPhotoIds = selectedPhotoIds.intersect(availableIds)
  }

  LaunchedEffect(imageCapture, hasCameraPermission, captureLockedForFullPull, isCaptureInProgress, isStageCamera) {
    val capture = imageCapture
    if (isStageCamera || capture == null || !hasCameraPermission || captureLockedForFullPull || isCaptureInProgress) {
      countdownValue = 0
      return@LaunchedEffect
    }

    snapshotFlow { expansion.value }.first { it >= CaptureStartProgress }

    try {
      for (value in 3 downTo 1) {
        countdownValue = value
        delay(CountdownStepMillis)
        if (expansion.value < CaptureCancelProgress) {
          countdownValue = 0
          captureLockedForFullPull = true
          return@LaunchedEffect
        }
        if (expansion.value >= FullScreenCommitProgress) {
          countdownValue = 0
          captureLockedForFullPull = true
          return@LaunchedEffect
        }
      }
      countdownValue = 0
      captureLockedForFullPull = true
      isCaptureInProgress = true
      shutterSoundPlayer.play(cameraPreferences)
      capturePhoto(
        imageCapture = capture,
        executor = ContextCompat.getMainExecutor(context),
        onCaptured = { bitmap ->
          finishCapturedBitmap(
            bitmap = bitmap,
            feedbackMode = CaptureFeedbackMode.PullList,
            collapseAfterCapture = true,
          )
        },
        onError = {
          isCaptureInProgress = false
        },
      )
    } finally {
      countdownValue = 0
    }
  }

  val dragState =
    rememberDraggableState { delta ->
      val previous = expansion.value
      val maxProgress =
        if (countdownValue != 0 || expansion.value > 1f) {
          FullScreenEndProgress
        } else {
          1f
        }
      val dragProgress = delta / maxPullPx
      val resistedDragProgress =
        if (delta > 0f && expansion.value + dragProgress > FullScreenCommitProgress) {
          dragProgress * 0.52f
        } else {
          dragProgress
        }
      val next = (expansion.value + resistedDragProgress).coerceIn(0f, maxProgress)
      if (countdownValue != 0 && next < CaptureCancelProgress) {
        countdownValue = 0
        captureLockedForFullPull = true
      }
      if (previous < FullScreenCommitProgress && next >= FullScreenCommitProgress) {
        hapticFeedback.performHapticFeedback(HapticFeedbackType.LongPress)
      }
      if (countdownValue != 0 && next >= FullScreenCommitProgress) {
        countdownValue = 0
        captureLockedForFullPull = true
      }
      scope.launch { expansion.snapTo(next) }
    }

  val dragModifier =
    Modifier.draggable(
      state = dragState,
      orientation = Orientation.Vertical,
      onDragStarted = {
        suppressCountdownAtExpanded = false
        scope.launch { expansion.stop() }
      },
      onDragStopped = { velocity ->
        if (velocity < CaptureCancelVelocity && countdownValue != 0) {
          countdownValue = 0
          captureLockedForFullPull = true
        }
        val target =
          when {
            expansion.value >= FullScreenCommitProgress -> FullScreenEndProgress
            velocity > 900f -> 1f
            velocity < -900f -> 0f
            expansion.value > 1f -> 1f
            expansion.value > 0.38f -> 1f
            else -> 0f
          }
        scope.launch {
          val settleSpec: AnimationSpec<Float> =
            if (target >= FullScreenCommitProgress) {
              spring(stiffness = Spring.StiffnessLow, dampingRatio = 0.92f)
            } else {
              spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.86f)
            }
          expansion.animateTo(
            targetValue = target,
            animationSpec = settleSpec,
          )
        }
      },
    )
  val isPhotoSelectionMode = selectedPhotoIds.isNotEmpty()
  val dragDisabled = isStageCamera || isFrameSettingsOpen || isSettingsOpen || isFrameManagerOpen || isPhotoSelectionMode
  val activeDragModifier = if (dragDisabled) Modifier else dragModifier
  val activeScreenDragModifier = if (dragDisabled) Modifier else dragModifier
  val backHandlerEnabled =
    isPhotoSelectionMode ||
      isStageAlbumOpen ||
      isFrameSettingsOpen ||
      isSettingsOpen ||
      isFrameManagerOpen ||
      isCaptureInProgress ||
      (!isStageCamera && (countdownValue != 0 || rawProgress > 0.01f))

  BackHandler(enabled = backHandlerEnabled) {
    when {
      isCaptureInProgress -> Unit
      isPhotoSelectionMode -> clearPhotoSelection()
      isStageAlbumOpen -> isStageAlbumOpen = false
      isFrameSettingsOpen -> closeFrameSettings()
      isSettingsOpen -> closeSettings()
      isFrameManagerOpen -> closeFrameManager()
      rawProgress >= FullScreenCommitProgress || fullScreenProgress > 0f -> {
        countdownValue = 0
        suppressCountdownAtExpanded = true
        captureLockedForFullPull = true
        scope.launch {
          expansion.animateTo(
            targetValue = 1f,
            animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.9f),
          )
        }
      }
      rawProgress > 0.01f || countdownValue != 0 -> {
        countdownValue = 0
        captureLockedForFullPull = false
        collapseCameraIsland()
      }
    }
  }

  BoxWithConstraints(
    modifier =
      modifier
        .fillMaxSize()
        .background(if (isStageCamera) FrameWarmWhite else FrameRed)
        .then(activeScreenDragModifier)
        .semantics { testTagsAsResourceId = true }
        .testTag("pull-island-camera-demo"),
  ) {
    val dynamicIslandMetrics =
      rememberDynamicIslandMetrics(
        maxWidth = maxWidth,
        statusBarHeight = statusBarHeight,
        coverMode = cameraPreferences.dynamicIslandCoverMode,
      )
    val islandTop = dynamicIslandMetrics.top
    val logoTop = lerpDp(84.dp, 224.dp, progress)
    val compactControlsTop = lerpDp(132.dp, 286.dp, progress)
    val fullScreenControlsTop = maxHeight - navigationBarHeight - 112.dp
    val controlsTop = lerpDp(compactControlsTop, fullScreenControlsTop, fullScreenProgress)
    val pullPhotoWallTop = controlsTop + lerpDp(104.dp, 116.dp, progress)
    val stagePhotoWallTop =
      stageCameraPhotoWallTop(
        dynamicIslandMetrics = dynamicIslandMetrics,
        navigationBarHeight = navigationBarHeight,
        maxHeight = maxHeight,
        maxWidth = maxWidth,
      )
    val stageAlbumTop = stageCameraAlbumTop(dynamicIslandMetrics = dynamicIslandMetrics, maxHeight = maxHeight)
    val stageAlbumSurfaceVisible = isStageCamera && (isStageAlbumOpen || stageAlbumFlipProgress > 0.01f)
    val photoWallTop =
      when {
        stageAlbumSurfaceVisible -> stageAlbumTop
        isStageCamera -> stagePhotoWallTop
        else -> pullPhotoWallTop
      }
    val availablePhotoWallHeight =
      maxHeight -
        photoWallTop -
        if (isStageCamera) {
          navigationBarHeight + if (stageAlbumSurfaceVisible) 112.dp else 14.dp
        } else {
          24.dp
        }
    val minPhotoWallHeight = if (stageAlbumSurfaceVisible) 360.dp else if (isStageCamera) 150.dp else 220.dp
    val maxPhotoWallHeight = if (stageAlbumSurfaceVisible) 720.dp else if (isStageCamera) 330.dp else 430.dp
    val photoWallHeight =
      when {
        availablePhotoWallHeight < minPhotoWallHeight -> minPhotoWallHeight
        availablePhotoWallHeight > maxPhotoWallHeight -> maxPhotoWallHeight
        else -> availablePhotoWallHeight
      }
    val captureFeedbackStartTop =
      if (captureFeedbackMode == CaptureFeedbackMode.StageCamera) {
        dynamicIslandMetrics.bottom - 4.dp
      } else {
        islandTop + 16.dp
      }
    val captureFeedbackStartSize =
      if (captureFeedbackMode == CaptureFeedbackMode.StageCamera) {
        88.dp
      } else {
        104.dp
      }
    val stageAlbumButtonTop = stageCameraAlbumButtonTop(maxHeight = maxHeight, navigationBarHeight = navigationBarHeight)
    val selectedPhotos = capturedPhotos.filter { it.id in selectedPhotoIds }
    val selectedFrameStyle = selectedPhotos.map { it.frameStyle }.distinct().singleOrNull()

    if (isStageCamera) {
      StageCamera(
        navigationBarHeight = navigationBarHeight,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        dynamicIslandMetrics = dynamicIslandMetrics,
        hasCameraPermission = hasCameraPermission,
        cameraLens = cameraPreferences.cameraLens,
        copy = copy,
        onImageCaptureReady = { imageCapture = it },
        albumOpen = isStageAlbumOpen,
        albumFlipProgress = stageAlbumFlipProgress,
        latestAlbumPhoto = capturedPhotos.firstOrNull(),
        captureCurtainProgress = stageCaptureCurtainProgress.value,
        onSceneTuningChanged = { cameraSceneTuning = it },
        onAlbumClick = { toggleStageAlbum() },
        onAlbumLongClick = { flipCameraAndOpenStageAlbum() },
        onSettingsClick = { openSettings() },
        onShutterClick = { captureImmediately(collapseAfterCapture = false) },
      )
    } else {
      CameraHomeBackdrop()

      Text(
        text = "Pico\nCam",
        color = FrameWarmWhite,
        textAlign = TextAlign.Center,
        fontSize = 32.sp,
        lineHeight = 29.sp,
        fontWeight = FontWeight.Bold,
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = logoTop)
            .graphicsLayer {
              alpha = lerpFloat(1f, 0.92f, progress) * (1f - fullScreenProgress)
              scaleX = lerpFloat(1f, 0.92f, progress)
              scaleY = lerpFloat(1f, 0.92f, progress)
            }
            .testTag("pico-logo"),
      )

      SettingsFloatingButton(
        contentDescription = copy.settingsButtonContentDescription,
        onClick = { openSettings() },
        modifier =
          Modifier
            .align(Alignment.TopEnd)
            .padding(top = statusBarHeight + 18.dp, end = 22.dp)
            .graphicsLayer { alpha = 1f - fullScreenProgress },
      )

      HomeCaptureHint(
        copy = copy,
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = logoTop + 86.dp, start = 42.dp, end = 42.dp)
            .graphicsLayer {
              alpha = (1f - progress * 1.35f).coerceIn(0f, 1f)
              translationY = lerpFloat(0f, -18f, progress)
            },
      )
    }

    val photoWallPhotos = if (isStageCamera && !stageAlbumSurfaceVisible) emptyList() else capturedPhotos
    if (!isStageCamera || stageAlbumSurfaceVisible) {
      val stageGalleryFlipProgress = if (stageAlbumSurfaceVisible) stageAlbumBackCardProgress(stageAlbumFlipProgress) else 1f
      PhotoWall(
        photos = photoWallPhotos,
        gridHeight = photoWallHeight,
        columns = if (isStageCamera && stageAlbumSurfaceVisible) 2 else 3,
        horizontalPadding = if (isStageCamera) 38.dp else 28.dp,
        itemAspectRatio = if (isStageCamera && stageAlbumSurfaceVisible) 0.72f else 0.78f,
        selectedPhotoIds = selectedPhotoIds,
        selectionMode = isPhotoSelectionMode,
        onPhotoClick = { photo ->
          if (isPhotoSelectionMode) {
            togglePhotoSelection(photo)
          } else {
            openFrameSettings(photoId = photo.id)
          }
        },
        onPhotoLongClick = { photo -> enterPhotoSelection(photo) },
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = photoWallTop)
            .graphicsLayer {
              alpha =
                (1f - fullScreenProgress) *
                if (isStageCamera && stageAlbumSurfaceVisible) stageAlbumBackCardAlpha(stageAlbumFlipProgress) else 1f
              rotationY = if (isStageCamera && stageAlbumSurfaceVisible) lerpFloat(-88f, 0f, stageGalleryFlipProgress) else 0f
              scaleX = if (isStageCamera && stageAlbumSurfaceVisible) lerpFloat(0.96f, 1f, stageGalleryFlipProgress) else 1f
              scaleY = if (isStageCamera && stageAlbumSurfaceVisible) lerpFloat(0.98f, 1f, stageGalleryFlipProgress) else 1f
              cameraDistance = 18f * density.density
            },
      )
    }

    if (!isStageCamera) {
      PullIsland(
        progress = progress,
        overPullProgress = overPullProgress,
        fullScreenProgress = fullScreenProgress,
        maxWidth = maxWidth,
        maxHeight = maxHeight,
        statusBarHeight = statusBarHeight,
        dynamicIslandMetrics = dynamicIslandMetrics,
        hasCameraPermission = hasCameraPermission,
        countdownValue = countdownValue,
        fullScreenHint = copy.pullForFullScreenHint,
        showFullScreenHint = countdownValue != 0 && fullScreenProgress == 0f,
        cameraLens = cameraPreferences.cameraLens,
        cameraDisplayStyle = CameraDisplayStyle.PullList,
        onImageCaptureReady = { imageCapture = it },
        onSceneTuningChanged = { cameraSceneTuning = it },
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = islandTop)
            .then(activeDragModifier)
            .then(
              if (progress < 0.5f) {
                Modifier.clickable {
                  suppressCountdownAtExpanded = false
                  scope.launch {
                    expansion.animateTo(
                      targetValue = 1f,
                      animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.88f),
                    )
                  }
                }
              } else {
                Modifier
              },
            ),
      )

      CameraControls(
        progress = progress,
        fullScreenProgress = fullScreenProgress,
        copy = copy,
        onFrameSettingsClick = { openFrameSettings(photoId = null) },
        onShutterClick = { captureImmediately(collapseAfterCapture = true) },
        onClose = {
          scope.launch {
            expansion.animateTo(
              targetValue = 0f,
              animationSpec = spring(stiffness = Spring.StiffnessMediumLow, dampingRatio = 0.9f),
            )
          }
        },
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = controlsTop),
      )

      PullHint(
        progress = progress,
        modifier =
          Modifier
            .align(Alignment.TopCenter)
            .padding(top = dynamicIslandMetrics.bottom + 10.dp),
      )

      HomeFrameStudioPanel(
        copy = copy,
        defaultFrameStyle = defaultFrameStyle,
        defaultGeneratedFrameSpec = defaultGeneratedFrameSpec,
        customFrameCount = customFrameSpecs.size,
        onFrameSettingsClick = { openFrameSettings(photoId = null) },
        onFrameManagerClick = { openFrameManager() },
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 24.dp, end = 24.dp, bottom = navigationBarHeight + 34.dp)
            .graphicsLayer {
              alpha = (1f - progress).coerceIn(0f, 1f)
              translationY = lerpFloat(0f, 52f, progress)
            },
      )
    }

    CapturePrintFeedback(
      thumbnail = flyingThumbnail,
      thumbnailProgress = thumbnailFlightProgress.value,
      flashAlpha = captureFlashAlpha.value,
      feedbackMode = captureFeedbackMode,
      startTop = captureFeedbackStartTop,
      startSize = captureFeedbackStartSize,
      photoWallTop = photoWallTop,
      targetLeft = if (captureFeedbackMode == CaptureFeedbackMode.StageCamera) StageCameraCornerButtonPadding + StageCameraAlbumThumbnailInset else 28.dp,
      targetTop = if (captureFeedbackMode == CaptureFeedbackMode.StageCamera) stageAlbumButtonTop + StageCameraAlbumThumbnailInset else photoWallTop + 6.dp,
      maxWidth = maxWidth,
      maxHeight = maxHeight,
    )

    if (isPhotoSelectionMode) {
      PhotoSelectionToolbar(
        selectedCount = selectedPhotoIds.size,
        selectedFrameStyle = selectedFrameStyle,
        copy = copy,
        onClearSelection = { clearPhotoSelection() },
        onSaveSelected = { saveSelectedPhotos() },
        onDeleteSelected = { deleteSelectedPhotos() },
        onFrameSelected = { frameStyle -> applyFrameToSelectedPhotos(frameStyle) },
        modifier =
          Modifier
            .align(Alignment.BottomCenter)
            .padding(start = 16.dp, end = 16.dp, bottom = 22.dp),
      )
    }

    if (isFrameSettingsOpen) {
      val selectedPhoto = frameSettingsPhotoId?.let { id -> capturedPhotos.firstOrNull { it.id == id } }
      val selectedGeneratedFrameSpec =
        if (frameSettingsPhotoId == null) {
          defaultGeneratedFrameSpec?.takeIf { it.baseStyle == draftFrameStyle }
        } else {
          selectedPhoto?.generatedFrameSpec?.takeIf { it.baseStyle == draftFrameStyle }
        }
      FrameSettingsScreen(
        photo = selectedPhoto,
        selectedFrameStyle = draftFrameStyle,
        copy = copy,
        customFrameSpecs = customFrameSpecs,
        selectedGeneratedFrameSpec = selectedGeneratedFrameSpec,
        onFrameSelected = { frameStyle -> selectFrameStyle(frameStyle) },
        onGeneratedFrameSelected = { frameSpec -> selectGeneratedFrameSpec(frameSpec) },
        onApplyAiFrame = { frameStyle -> selectFrameStyle(frameStyle) },
        onAiTagToggled = { tag ->
          val selected = selectedPhoto
          if (selected != null) {
            togglePhotoAiTag(selected.id, tag)
          }
        },
        onGenerateAiFrame = { instruction ->
          val selected = selectedPhoto
          if (selected != null) {
            generateFrameForPhoto(selected.id, instruction)
          }
        },
        onClose = { closeFrameSettings() },
        onSave = {
          val selected = selectedPhoto
          if (selected != null) {
            requestSaveFramedPhoto(selected, draftFrameStyle)
          }
        },
      )
    }

    if (isSettingsOpen) {
      CameraSettingsScreen(
        preferences = cameraPreferences,
        onPreferencesChanged = ::updateCameraPreferences,
        onPreviewSound = { previewPreferences -> shutterSoundPlayer.play(previewPreferences) },
        onFrameManagerClick = { openFrameManager() },
        onClose = { closeSettings() },
      )
    }

    if (isFrameManagerOpen) {
      FrameManagementScreen(
        customFrameSpecs = customFrameSpecs,
        selectedGeneratedFrameSpec = defaultGeneratedFrameSpec?.takeIf { it.baseStyle == defaultFrameStyle },
        frameGenerationState = frameManagerGenerationState,
        copy = copy,
        onGenerateFrame = { instruction -> generateManagedFrame(instruction) },
        onFrameSelected = { frameSpec -> setDefaultGeneratedFrameSpec(frameSpec) },
        onSetDefaultFrame = { frameSpec -> setDefaultGeneratedFrameSpec(frameSpec) },
        onClose = { closeFrameManager() },
      )
    }
  }
}

private fun cameraOverPullProgress(progress: Float): Float =
  ((progress - FullScreenHintStartProgress) / (FullScreenCommitProgress - FullScreenHintStartProgress)).coerceIn(0f, 1f)

private fun cameraFullScreenProgress(progress: Float): Float =
  ((progress - FullScreenCommitProgress) / (FullScreenEndProgress - FullScreenCommitProgress)).coerceIn(0f, 1f)

private fun smoothStep(progress: Float): Float = progress * progress * (3f - 2f * progress)

private fun stageAlbumBackCardProgress(progress: Float): Float = smoothStep(((progress - 0.42f) / 0.58f).coerceIn(0f, 1f))

private fun stageAlbumBackCardAlpha(progress: Float): Float = ((progress - 0.38f) / 0.22f).coerceIn(0f, 1f)

private fun lerpDp(start: Dp, end: Dp, progress: Float): Dp = start + (end - start) * progress

private fun lerpFloat(start: Float, end: Float, progress: Float): Float = start + (end - start) * progress

@ComposePreview(showBackground = true, widthDp = 390, heightDp = 844)
@Composable
fun MainScreenPreview() {
  CapsuleCardCameraTheme { MainScreen(modifier = Modifier.fillMaxSize()) }
}

@ComposePreview(showBackground = true, widthDp = 360, heightDp = 780)
@Composable
fun MainScreenPortraitPreview() {
  CapsuleCardCameraTheme { MainScreen(modifier = Modifier.fillMaxSize()) }
}

private sealed interface CameraOverlayRoute {
  data class FrameSettings(val photoId: Int?) : CameraOverlayRoute

  data object Settings : CameraOverlayRoute

  data object FrameManager : CameraOverlayRoute
}
