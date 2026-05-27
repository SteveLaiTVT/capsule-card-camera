package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.test.core.app.ActivityScenario
import androidx.test.platform.app.InstrumentationRegistry
import com.example.capsulecardcamera.MainActivity
import com.google.mlkit.genai.common.DownloadStatus
import com.google.mlkit.genai.common.FeatureStatus
import com.google.mlkit.genai.prompt.Generation
import com.google.mlkit.genai.prompt.ImagePart
import com.google.mlkit.genai.prompt.TextPart
import com.google.mlkit.genai.prompt.generateContentRequest
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withTimeoutOrNull
import org.junit.Test

class GeminiNanoPromptSmokeTest {
  @Test
  fun checkGeminiNanoPromptStatus() =
    runBlocking {
      val result = StringBuilder()
      fun record(message: String) {
        Log.i(SmokeTestTag, message)
        result.appendLine(message)
      }

      runCatching {
        ActivityScenario.launch(MainActivity::class.java).use { scenario ->
          scenario.moveToState(Lifecycle.State.RESUMED)

          val client = Generation.getClient()
          val initialStatus = client.checkStatus()
          record("initialStatus=$initialStatus")

          if (initialStatus == FeatureStatus.DOWNLOADABLE || initialStatus == FeatureStatus.DOWNLOADING) {
            val downloadResult =
              withTimeoutOrNull(60_000L) {
                var latest = "download-started"
                client.download().collect { status ->
                  latest =
                    when (status) {
                      DownloadStatus.DownloadCompleted -> "download-completed"
                      is DownloadStatus.DownloadFailed -> "download-failed:${status.e.message}"
                      is DownloadStatus.DownloadProgress -> "download-progress"
                      is DownloadStatus.DownloadStarted -> "download-started"
                    }
                  record(latest)
                }
                latest
              }
            record("downloadResult=${downloadResult ?: "download-timeout"}")
          }

          val finalStatus = client.checkStatus()
          record("finalStatus=$finalStatus")
          if (finalStatus == FeatureStatus.AVAILABLE) {
            val bitmap = Bitmap.createBitmap(48, 48, Bitmap.Config.ARGB_8888).apply { eraseColor(Color.rgb(230, 80, 40)) }
            val response =
              withTimeoutOrNull(45_000L) {
                client.generateContent(
                  generateContentRequest(
                    ImagePart(bitmap),
                    TextPart("Return only JSON: {\"title\":\"red square\",\"tags\":[\"red\"],\"subject\":\"shape\",\"scene\":\"test\",\"colors\":[\"red\"],\"confidence\":\"high\"}"),
                  ) {
                    temperature = 0.1f
                    topK = 4
                    candidateCount = 1
                    maxOutputTokens = 80
                  },
                )
              }
            record("inferenceText=${response?.candidates?.firstOrNull()?.text ?: "inference-timeout"}")

            val framePrompt =
              buildGeneratedFramePrompt(
                insight =
                  PhotoInsight(
                    title = "Red square",
                    tags = listOf("red", "shape"),
                    subject = "shape",
                    scene = "test surface",
                    colors = listOf("red"),
                    confidence = PhotoInsightConfidence.High,
                    suggestedFrameStyle = PhotoFrameStyle.ColorPop,
                  ),
                selectedTags = setOf("red"),
                currentFrameStyle = PhotoFrameStyle.Stamp,
                previousFrameSpec = null,
                conversation = emptyList(),
                instruction = "Make it graphic and playful",
              )
            val frameResponse =
              withTimeoutOrNull(45_000L) {
                client.generateContent(
                  generateContentRequest(TextPart(framePrompt)) {
                    temperature = 0.2f
                    topK = 8
                    candidateCount = 1
                    maxOutputTokens = 256
                  },
                )
              }
            val frameText = frameResponse?.candidates?.firstOrNull()?.text.orEmpty()
            val frameSpec = parseGeneratedFrameSpec(frameText, fallbackStyle = PhotoFrameStyle.Stamp)
            record("frameInferenceText=${frameText.ifBlank { "frame-inference-timeout" }}")
            record("frameSpec=${frameSpec?.title ?: "frame-parse-failed"}")
          }
        }
      }.onFailure { error ->
        record("error=${error::class.java.name}:${error.message}")
      }

      InstrumentationRegistry
        .getInstrumentation()
        .sendStatus(0, Bundle().apply { putString("geminiNanoPromptSmoke", result.toString()) })
    }

  private companion object {
    const val SmokeTestTag = "GeminiNanoSmoke"
  }
}
