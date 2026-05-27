package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import android.os.Build
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

internal interface PhotoAiAnalyzer {
  fun analyze(bitmap: Bitmap): Flow<PhotoAiState> = analyze(bitmap, PhotoAiDiagnostics.nextAnalysisId("photo_ai"))

  fun analyze(
    bitmap: Bitmap,
    analysisId: String,
  ): Flow<PhotoAiState>
}

internal fun createDefaultPhotoAiAnalyzer(): PhotoAiAnalyzer =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    GeminiNanoPhotoAiAnalyzer()
  } else {
    UnsupportedPhotoAiAnalyzer
  }

internal interface PhotoFrameGenerator {
  fun generateFrame(
    bitmap: Bitmap?,
    insight: PhotoInsight,
    selectedTags: Set<String>,
    currentFrameStyle: PhotoFrameStyle,
    previousFrameSpec: GeneratedFrameSpec?,
    conversation: List<FrameConversationMessage>,
    instruction: String,
    generationId: String,
  ): Flow<FrameGenerationState>
}

internal fun createDefaultPhotoFrameGenerator(): PhotoFrameGenerator =
  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
    GeminiNanoPhotoFrameGenerator()
  } else {
    UnsupportedPhotoFrameGenerator
  }

internal class GeminiNanoPhotoAiAnalyzer : PhotoAiAnalyzer {
  override fun analyze(
    bitmap: Bitmap,
    analysisId: String,
  ): Flow<PhotoAiState> {
    val startedAt = PhotoAiDiagnostics.now()
    return flow {
      PhotoAiDiagnostics.info("analysis=$analysisId provider=gemini_nano start bitmap=${PhotoAiDiagnostics.bitmapLabel(bitmap)}")
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        PhotoAiDiagnostics.info("analysis=$analysisId provider=gemini_nano unavailable reason=sdk_${Build.VERSION.SDK_INT}")
        emit(PhotoAiState.Unavailable)
        return@flow
      }

      emit(PhotoAiState.Preparing)
      val generativeModel = Generation.getClient()
      val statusStartedAt = PhotoAiDiagnostics.now()
      val featureStatus = generativeModel.checkStatus()
      PhotoAiDiagnostics.info(
        "analysis=$analysisId provider=gemini_nano feature_status=$featureStatus elapsedMs=${PhotoAiDiagnostics.elapsedMs(statusStartedAt)}",
      )
      when (featureStatus) {
        FeatureStatus.UNAVAILABLE -> {
          PhotoAiDiagnostics.info("analysis=$analysisId provider=gemini_nano unavailable reason=feature_status")
          emit(PhotoAiState.Unavailable)
          return@flow
        }
        FeatureStatus.DOWNLOADABLE,
        FeatureStatus.DOWNLOADING,
        -> {
          var completed = false
          var lastDownloadLogAt = 0L
          generativeModel.download().collect { status ->
            val now = PhotoAiDiagnostics.now()
            if (now - lastDownloadLogAt >= DownloadProgressLogWindowMillis || status !is DownloadStatus.DownloadProgress) {
              PhotoAiDiagnostics.info("analysis=$analysisId provider=gemini_nano download_status=${status.javaClass.simpleName}")
              lastDownloadLogAt = now
            }
            when (status) {
              DownloadStatus.DownloadCompleted -> completed = true
              is DownloadStatus.DownloadFailed -> throw status.e
              is DownloadStatus.DownloadProgress,
              is DownloadStatus.DownloadStarted,
              -> Unit
            }
          }
          if (!completed && generativeModel.checkStatus() != FeatureStatus.AVAILABLE) {
            PhotoAiDiagnostics.info("analysis=$analysisId provider=gemini_nano unavailable reason=download_not_completed")
            emit(PhotoAiState.Unavailable)
            return@flow
          }
        }
        FeatureStatus.AVAILABLE -> Unit
      }

      emit(PhotoAiState.Analyzing)
      val inferenceStartedAt = PhotoAiDiagnostics.now()
      PhotoAiDiagnostics.info("analysis=$analysisId provider=gemini_nano generate start")
      val response =
        generativeModel.generateContent(
          generateContentRequest(ImagePart(bitmap), TextPart(PhotoInsightPrompt)) {
            temperature = 0.2f
            topK = 10
            candidateCount = 1
            maxOutputTokens = 240
          },
        )
      val text = response.candidates.firstOrNull()?.text.orEmpty()
      PhotoAiDiagnostics.info(
        "analysis=$analysisId provider=gemini_nano generate finish elapsedMs=${PhotoAiDiagnostics.elapsedMs(inferenceStartedAt)} outputChars=${text.length}",
      )
      val parseStartedAt = PhotoAiDiagnostics.now()
      val insight = parsePhotoInsight(text)
      PhotoAiDiagnostics.info(
        if (insight == null) {
          "analysis=$analysisId provider=gemini_nano parse failed elapsedMs=${PhotoAiDiagnostics.elapsedMs(parseStartedAt)} outputPreview=${text.logPreview()}"
        } else {
          "analysis=$analysisId provider=gemini_nano ready elapsedMs=${PhotoAiDiagnostics.elapsedMs(startedAt)} tags=${insight.tags.joinToString()} title=${insight.title.logPreview()}"
        },
      )
      emit(if (insight == null) PhotoAiState.Failed("Could not parse model output.") else PhotoAiState.Ready(insight))
    }
      .catch { error ->
        PhotoAiDiagnostics.warn(
          "analysis=$analysisId provider=gemini_nano failed elapsedMs=${PhotoAiDiagnostics.elapsedMs(startedAt)} message=${error.message}",
          error,
        )
        emit(PhotoAiState.Failed(error.message))
      }
      .flowOn(Dispatchers.IO)
  }
}

private object UnsupportedPhotoAiAnalyzer : PhotoAiAnalyzer {
  override fun analyze(
    bitmap: Bitmap,
    analysisId: String,
  ): Flow<PhotoAiState> =
    flow {
      PhotoAiDiagnostics.info("analysis=$analysisId provider=unsupported unavailable")
      emit(PhotoAiState.Unavailable)
    }
}

internal class GeminiNanoPhotoFrameGenerator : PhotoFrameGenerator {
  override fun generateFrame(
    bitmap: Bitmap?,
    insight: PhotoInsight,
    selectedTags: Set<String>,
    currentFrameStyle: PhotoFrameStyle,
    previousFrameSpec: GeneratedFrameSpec?,
    conversation: List<FrameConversationMessage>,
    instruction: String,
    generationId: String,
  ): Flow<FrameGenerationState> {
    val startedAt = PhotoAiDiagnostics.now()
    return flow {
      PhotoAiDiagnostics.info("frame=$generationId provider=gemini_nano start")
      if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
        PhotoAiDiagnostics.info("frame=$generationId provider=gemini_nano unavailable reason=sdk_${Build.VERSION.SDK_INT}")
        emit(FrameGenerationState.Unavailable)
        return@flow
      }

      emit(FrameGenerationState.Generating)
      val generativeModel = Generation.getClient()
      val statusStartedAt = PhotoAiDiagnostics.now()
      val featureStatus = generativeModel.checkStatus()
      PhotoAiDiagnostics.info(
        "frame=$generationId provider=gemini_nano feature_status=$featureStatus elapsedMs=${PhotoAiDiagnostics.elapsedMs(statusStartedAt)}",
      )
      when (featureStatus) {
        FeatureStatus.UNAVAILABLE -> {
          PhotoAiDiagnostics.info("frame=$generationId provider=gemini_nano unavailable reason=feature_status")
          emit(FrameGenerationState.Unavailable)
          return@flow
        }
        FeatureStatus.DOWNLOADABLE,
        FeatureStatus.DOWNLOADING,
        -> {
          var completed = false
          var lastDownloadLogAt = 0L
          generativeModel.download().collect { status ->
            val now = PhotoAiDiagnostics.now()
            if (now - lastDownloadLogAt >= DownloadProgressLogWindowMillis || status !is DownloadStatus.DownloadProgress) {
              PhotoAiDiagnostics.info("frame=$generationId provider=gemini_nano download_status=${status.javaClass.simpleName}")
              lastDownloadLogAt = now
            }
            when (status) {
              DownloadStatus.DownloadCompleted -> completed = true
              is DownloadStatus.DownloadFailed -> throw status.e
              is DownloadStatus.DownloadProgress,
              is DownloadStatus.DownloadStarted,
              -> Unit
            }
          }
          if (!completed && generativeModel.checkStatus() != FeatureStatus.AVAILABLE) {
            PhotoAiDiagnostics.info("frame=$generationId provider=gemini_nano unavailable reason=download_not_completed")
            emit(FrameGenerationState.Unavailable)
            return@flow
          }
        }
        FeatureStatus.AVAILABLE -> Unit
      }

      val inferenceStartedAt = PhotoAiDiagnostics.now()
      val prompt =
        buildGeneratedFramePrompt(
          insight = insight,
          selectedTags = selectedTags,
          currentFrameStyle = currentFrameStyle,
          previousFrameSpec = previousFrameSpec,
          conversation = conversation,
          instruction = instruction,
        )
      PhotoAiDiagnostics.info("frame=$generationId provider=gemini_nano generate start promptChars=${prompt.length}")
      val request =
        if (bitmap == null) {
          generateContentRequest(TextPart(prompt)) {
            temperature = 0.45f
            topK = 24
            candidateCount = 1
            maxOutputTokens = 256
          }
        } else {
          generateContentRequest(ImagePart(bitmap), TextPart(prompt)) {
            temperature = 0.45f
            topK = 24
            candidateCount = 1
            maxOutputTokens = 256
          }
        }
      val response =
        generativeModel.generateContent(request)
      val text = response.candidates.firstOrNull()?.text.orEmpty()
      PhotoAiDiagnostics.info(
        "frame=$generationId provider=gemini_nano generate finish elapsedMs=${PhotoAiDiagnostics.elapsedMs(inferenceStartedAt)} outputChars=${text.length}",
      )
      val spec = parseGeneratedFrameSpec(raw = text, fallbackStyle = currentFrameStyle)?.ensureDistinctFrom(previousFrameSpec)
      PhotoAiDiagnostics.info(
        if (spec == null) {
          "frame=$generationId provider=gemini_nano parse failed outputPreview=${text.logPreview()}"
        } else {
          "frame=$generationId provider=gemini_nano ready elapsedMs=${PhotoAiDiagnostics.elapsedMs(startedAt)} title=${spec.title.logPreview()} style=${spec.baseStyle.label} motif=${spec.motif.label}"
        },
      )
      emit(if (spec == null) FrameGenerationState.Failed("Could not parse frame output.") else FrameGenerationState.Ready(spec))
    }
      .catch { error ->
        PhotoAiDiagnostics.warn(
          "frame=$generationId provider=gemini_nano failed elapsedMs=${PhotoAiDiagnostics.elapsedMs(startedAt)} message=${error.message}",
          error,
        )
        emit(FrameGenerationState.Failed(error.message))
      }
      .flowOn(Dispatchers.IO)
  }
}

private object UnsupportedPhotoFrameGenerator : PhotoFrameGenerator {
  override fun generateFrame(
    bitmap: Bitmap?,
    insight: PhotoInsight,
    selectedTags: Set<String>,
    currentFrameStyle: PhotoFrameStyle,
    previousFrameSpec: GeneratedFrameSpec?,
    conversation: List<FrameConversationMessage>,
    instruction: String,
    generationId: String,
  ): Flow<FrameGenerationState> =
    flow {
      PhotoAiDiagnostics.info("frame=$generationId provider=unsupported unavailable")
      emit(FrameGenerationState.Unavailable)
    }
}

private const val DownloadProgressLogWindowMillis = 1_000L

private const val PhotoInsightPrompt =
  """
Return only compact JSON for this photo:
{"title":"...","tags":["..."],"subject":"...","scene":"...","colors":["..."],"confidence":"low|medium|high","frameStyle":"stamp|polaroid|film|color","frameReason":"..."}
Use at most 6 short English tags and 4 colors. Pick one frameStyle. Do not name people or private attributes. No Markdown.
  """

private fun String.logPreview(maxLength: Int = 160): String =
  replace(Regex("\\s+"), " ")
    .trim()
    .take(maxLength)
