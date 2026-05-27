package com.example.capsulecardcamera.ui.main

import androidx.camera.core.ImageProxy
import java.nio.ByteBuffer
import kotlin.math.max
import kotlin.math.roundToInt
import kotlin.math.sqrt

internal data class CameraSceneTuning(
  val centerLuma: Float,
  val centerContrast: Float,
  val exposureCompensationIndex: Int,
  val enhancementProfile: PhotoEnhancementProfile,
) {
  companion object {
    val Neutral =
      CameraSceneTuning(
        centerLuma = 0.5f,
        centerContrast = 0.2f,
        exposureCompensationIndex = 0,
        enhancementProfile = PhotoEnhancementProfile.Neutral,
      )
  }
}

internal data class CenterFrameStats(
  val luma: Float,
  val contrast: Float,
)

internal fun analyzeCenterFrame(imageProxy: ImageProxy): CenterFrameStats? {
  val plane = imageProxy.planes.firstOrNull() ?: return null
  return analyzeCenterLumaPlane(
    buffer = plane.buffer,
    width = imageProxy.width,
    height = imageProxy.height,
    rowStride = plane.rowStride,
    pixelStride = plane.pixelStride,
  )
}

internal fun analyzeCenterLumaPlane(
  buffer: ByteBuffer,
  width: Int,
  height: Int,
  rowStride: Int,
  pixelStride: Int,
): CenterFrameStats? {
  if (width <= 0 || height <= 0 || rowStride <= 0 || pixelStride <= 0) return null
  val source = buffer.duplicate()
  val cropWidth = max(1, (width * 0.42f).roundToInt())
  val cropHeight = max(1, (height * 0.42f).roundToInt())
  val startX = ((width - cropWidth) / 2).coerceAtLeast(0)
  val startY = ((height - cropHeight) / 2).coerceAtLeast(0)
  val stepX = max(1, cropWidth / 36)
  val stepY = max(1, cropHeight / 36)
  var count = 0
  var sum = 0.0
  var squaredSum = 0.0

  var y = startY
  while (y < startY + cropHeight) {
    var x = startX
    while (x < startX + cropWidth) {
      val index = y * rowStride + x * pixelStride
      if (index >= 0 && index < source.limit()) {
        val luma = (source.get(index).toInt() and 0xFF) / 255.0
        sum += luma
        squaredSum += luma * luma
        count += 1
      }
      x += stepX
    }
    y += stepY
  }

  if (count == 0) return null
  val mean = sum / count
  val variance = (squaredSum / count - mean * mean).coerceAtLeast(0.0)
  return CenterFrameStats(luma = mean.toFloat(), contrast = sqrt(variance).toFloat())
}

internal fun recommendedExposureCompensationIndex(
  centerLuma: Float,
  minIndex: Int,
  maxIndex: Int,
): Int {
  if (minIndex >= maxIndex) return minIndex
  val targetLuma = 0.48f
  val rawIndex = ((targetLuma - centerLuma.coerceIn(0f, 1f)) / 0.085f).roundToInt()
  return rawIndex.coerceIn(minIndex, maxIndex)
}

internal fun cameraSceneTuningFromStats(
  stats: CenterFrameStats,
  exposureCompensationIndex: Int,
): CameraSceneTuning =
  CameraSceneTuning(
    centerLuma = stats.luma,
    centerContrast = stats.contrast,
    exposureCompensationIndex = exposureCompensationIndex,
    enhancementProfile = PhotoEnhancementProfile.fromCenterStats(stats),
  )
