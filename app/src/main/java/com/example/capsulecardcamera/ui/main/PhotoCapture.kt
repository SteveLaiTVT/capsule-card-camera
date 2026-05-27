package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.Matrix
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.ImageProxy
import kotlin.math.max

internal fun capturePhoto(
  imageCapture: ImageCapture,
  executor: java.util.concurrent.Executor,
  onCaptured: (Bitmap) -> Unit,
  onError: (ImageCaptureException) -> Unit = {},
) {
  imageCapture.takePicture(
    executor,
    object : ImageCapture.OnImageCapturedCallback() {
      override fun onCaptureSuccess(image: ImageProxy) {
        try {
          image.toBitmapOrNull()?.let(onCaptured)
        } finally {
          image.close()
        }
      }

      override fun onError(exception: ImageCaptureException) {
        onError(exception)
      }
    },
  )
}

private fun ImageProxy.toBitmapOrNull(): Bitmap? {
  val buffer = planes.firstOrNull()?.buffer ?: return null
  buffer.rewind()
  val bytes = ByteArray(buffer.remaining())
  buffer.get(bytes)
  val decoded = BitmapFactory.decodeByteArray(bytes, 0, bytes.size) ?: return null
  val rotated = decoded.rotate(imageInfo.rotationDegrees)
  return rotated.scaleLongestSide(maxSide = 900)
}

private fun Bitmap.rotate(degrees: Int): Bitmap {
  if (degrees == 0) return this
  val matrix = Matrix().apply { postRotate(degrees.toFloat()) }
  return Bitmap.createBitmap(this, 0, 0, width, height, matrix, true)
}

private fun Bitmap.scaleLongestSide(maxSide: Int): Bitmap {
  val longest = max(width, height)
  if (longest <= maxSide) return this
  val scale = maxSide.toFloat() / longest.toFloat()
  return Bitmap.createScaledBitmap(this, (width * scale).toInt(), (height * scale).toInt(), true)
}
