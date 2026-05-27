package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.ColorMatrix
import android.graphics.ColorMatrixColorFilter
import android.graphics.Paint

internal data class PhotoEnhancementProfile(
  val exposure: Float = 0f,
  val contrast: Float = 1f,
  val saturation: Float = 1f,
  val warmth: Float = 0f,
) {
  companion object {
    val Neutral = PhotoEnhancementProfile()

    fun fromCenterStats(stats: CenterFrameStats): PhotoEnhancementProfile {
      val exposure =
        when {
          stats.luma < 0.30f -> 0.12f
          stats.luma < 0.40f -> 0.07f
          stats.luma > 0.76f -> -0.08f
          stats.luma > 0.68f -> -0.04f
          else -> 0.02f
        }
      val contrast = if (stats.contrast < 0.11f) 1.08f else 1.04f
      return PhotoEnhancementProfile(
        exposure = exposure,
        contrast = contrast,
        saturation = 1.04f,
        warmth = 0.01f,
      )
    }

    fun fromInsight(insight: PhotoInsight): PhotoEnhancementProfile {
      val text =
        buildString {
          append(insight.subject)
          append(' ')
          append(insight.scene)
          append(' ')
          append(insight.title)
          append(' ')
          append(insight.tags.joinToString(" "))
          append(' ')
          append(insight.colors.joinToString(" "))
        }.lowercase()

      return when {
        listOf("person", "portrait", "face", "skin", "eyes", "hair").any { it in text } ->
          PhotoEnhancementProfile(exposure = 0.035f, contrast = 1.035f, saturation = 1.055f, warmth = 0.03f)
        listOf("night", "dark", "low light", "dim", "shadow").any { it in text } ->
          PhotoEnhancementProfile(exposure = 0.08f, contrast = 1.08f, saturation = 1.035f, warmth = 0.015f)
        listOf("document", "screen", "monitor", "text", "paper", "keyboard").any { it in text } ->
          PhotoEnhancementProfile(exposure = 0.025f, contrast = 1.11f, saturation = 0.96f, warmth = 0f)
        listOf("food", "flower", "toy", "product", "bright", "red", "orange", "green").any { it in text } ->
          PhotoEnhancementProfile(exposure = 0.02f, contrast = 1.055f, saturation = 1.11f, warmth = 0.015f)
        else ->
          PhotoEnhancementProfile(exposure = 0.02f, contrast = 1.045f, saturation = 1.04f, warmth = 0.01f)
      }
    }
  }

  fun isNeutral(): Boolean =
    exposure == 0f && contrast == 1f && saturation == 1f && warmth == 0f
}

internal fun enhanceBitmap(
  bitmap: Bitmap,
  profile: PhotoEnhancementProfile,
): Bitmap {
  if (profile.isNeutral()) return bitmap
  val output = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
  val canvas = Canvas(output)
  val saturationMatrix = ColorMatrix().apply { setSaturation(profile.saturation.coerceIn(0.85f, 1.18f)) }
  val contrast = profile.contrast.coerceIn(0.9f, 1.18f)
  val brightness = profile.exposure.coerceIn(-0.16f, 0.18f) * 255f
  val warmth = profile.warmth.coerceIn(-0.08f, 0.08f) * 255f
  val translate = (-0.5f * contrast + 0.5f) * 255f + brightness
  val toneMatrix =
    ColorMatrix(
      floatArrayOf(
        contrast, 0f, 0f, 0f, translate + warmth,
        0f, contrast, 0f, 0f, translate + warmth * 0.34f,
        0f, 0f, contrast, 0f, translate - warmth * 0.42f,
        0f, 0f, 0f, 1f, 0f,
      ),
    )
  saturationMatrix.postConcat(toneMatrix)
  val paint =
    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
      colorFilter = ColorMatrixColorFilter(saturationMatrix)
    }
  canvas.drawBitmap(bitmap, 0f, 0f, paint)
  return output
}
