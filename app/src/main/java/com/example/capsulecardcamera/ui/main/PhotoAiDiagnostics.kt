package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import java.util.concurrent.atomic.AtomicLong

internal object PhotoAiDiagnostics {
  private const val Tag = "PhotoAiAnalysis"
  private val sequence = AtomicLong(0L)

  fun nextAnalysisId(
    prefix: String,
    photoId: Int? = null,
  ): String {
    val suffix = sequence.incrementAndGet()
    return if (photoId == null) {
      "$prefix-$suffix"
    } else {
      "$prefix-photo-$photoId-$suffix"
    }
  }

  fun now(): Long = SystemClock.elapsedRealtime()

  fun elapsedMs(startMillis: Long): Long = now() - startMillis

  fun bitmapLabel(bitmap: Bitmap): String {
    val width = runCatching { bitmap.width }.getOrDefault(0)
    val height = runCatching { bitmap.height }.getOrDefault(0)
    val bytes = runCatching { bitmap.allocationByteCount }.getOrDefault(0)
    return "${width}x$height bytes=$bytes"
  }

  fun info(message: String) {
    Log.i(Tag, message)
  }

  fun warn(
    message: String,
    error: Throwable? = null,
  ) {
    if (error == null) {
      Log.w(Tag, message)
    } else {
      Log.w(Tag, message, error)
    }
  }
}

internal fun PhotoAiState.diagnosticName(): String =
  when (this) {
    PhotoAiState.Idle -> "Idle"
    PhotoAiState.Preparing -> "Preparing"
    PhotoAiState.Analyzing -> "Analyzing"
    is PhotoAiState.Ready -> "Ready"
    PhotoAiState.Unavailable -> "Unavailable"
    is PhotoAiState.Failed -> "Failed"
  }

internal fun PhotoAiState.diagnosticDetail(): String =
  when (this) {
    is PhotoAiState.Ready ->
      " title=${insight.title.diagnosticPreview()} tags=${insight.tags.joinToString()}"
    is PhotoAiState.Failed -> " message=${message.orEmpty().diagnosticPreview()}"
    else -> ""
  }

private fun String.diagnosticPreview(maxLength: Int = 120): String =
  replace(Regex("\\s+"), " ")
    .trim()
    .take(maxLength)
