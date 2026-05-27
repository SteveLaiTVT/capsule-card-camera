package com.example.capsulecardcamera.ui.main

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas as AndroidCanvas
import android.graphics.LinearGradient
import android.graphics.Paint
import android.graphics.Path as AndroidPath
import android.graphics.Rect
import android.graphics.RectF
import android.graphics.Shader
import android.graphics.Typeface
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.compose.ui.graphics.toArgb
import androidx.exifinterface.media.ExifInterface
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.io.FileOutputStream
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

internal fun saveFramedPhoto(
  context: Context,
  bitmap: Bitmap,
  frameStyle: PhotoFrameStyle,
  defaultAlbum: DefaultAlbum,
  metadata: PhotoSaveMetadata = PhotoSaveMetadata.Empty,
  generatedFrameSpec: GeneratedFrameSpec? = null,
): Boolean {
  val framedBitmap = renderFramedPhotoBitmap(bitmap = bitmap, frameStyle = frameStyle, generatedFrameSpec = generatedFrameSpec)
  val photoBitmap = framedBitmap.toExifCompatibleBitmap()
  val fileName = "capsule-card-${System.currentTimeMillis()}.jpg"

  return try {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
      saveFramedPhotoToMediaStore(context, photoBitmap, fileName, defaultAlbum, metadata)
    } else {
      saveFramedPhotoToPublicPictures(context, photoBitmap, fileName, defaultAlbum, metadata)
    }
  } finally {
    photoBitmap.recycle()
    framedBitmap.recycle()
  }
}

private fun saveFramedPhotoToMediaStore(
  context: Context,
  bitmap: Bitmap,
  fileName: String,
  defaultAlbum: DefaultAlbum,
  metadata: PhotoSaveMetadata,
): Boolean {
  val resolver = context.contentResolver
  val values =
    ContentValues().apply {
      put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
      put(MediaStore.Images.Media.MIME_TYPE, SavedPhotoMimeType)
      put(MediaStore.Images.Media.RELATIVE_PATH, Environment.DIRECTORY_PICTURES + "/" + defaultAlbum.directoryName)
      put(MediaStore.Images.Media.IS_PENDING, 1)
    }
  val uri = resolver.insert(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, values) ?: return false

  return try {
    val saved =
      resolver.openOutputStream(uri)?.use { output ->
        bitmap.compress(SavedPhotoCompressFormat, SavedPhotoQuality, output)
      } == true

    val metadataSaved = saved && writePhotoMetadataToUri(context, uri, metadata)
    if (metadataSaved) {
      values.clear()
      values.put(MediaStore.Images.Media.IS_PENDING, 0)
      resolver.update(uri, values, null, null)
    } else {
      resolver.delete(uri, null, null)
    }
    metadataSaved
  } catch (_: Exception) {
    resolver.delete(uri, null, null)
    false
  }
}

@Suppress("DEPRECATION")
private fun saveFramedPhotoToPublicPictures(
  context: Context,
  bitmap: Bitmap,
  fileName: String,
  defaultAlbum: DefaultAlbum,
  metadata: PhotoSaveMetadata,
): Boolean {
  val directory = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), defaultAlbum.directoryName)
  if (!directory.exists() && !directory.mkdirs()) return false

  val file = File(directory, fileName)
  return try {
    FileOutputStream(file).use { output ->
      bitmap.compress(SavedPhotoCompressFormat, SavedPhotoQuality, output)
    }
    if (!writePhotoMetadataToFile(file, metadata)) {
      file.delete()
      return false
    }

    context.contentResolver.insert(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      ContentValues().apply {
        put(MediaStore.Images.Media.DISPLAY_NAME, fileName)
        put(MediaStore.Images.Media.MIME_TYPE, SavedPhotoMimeType)
        put(MediaStore.Images.Media.DATA, file.absolutePath)
      },
    )
    true
  } catch (_: Exception) {
    file.delete()
    false
  }
}

private const val SavedPhotoMimeType = "image/jpeg"
private const val SavedPhotoQuality = 95
private val SavedPhotoCompressFormat = Bitmap.CompressFormat.JPEG

private fun Bitmap.toExifCompatibleBitmap(): Bitmap {
  val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
  val canvas = AndroidCanvas(output)
  canvas.drawColor(android.graphics.Color.WHITE)
  canvas.drawBitmap(this, 0f, 0f, null)
  return output
}

internal data class PhotoSaveMetadata(
  val title: String = "",
  val tags: List<String> = emptyList(),
  val subject: String = "",
  val scene: String = "",
  val colors: List<String> = emptyList(),
  val confidence: String = "",
  val generatedFrameTitle: String = "",
  val generatedFrameStyle: String = "",
  val generatedFrameColors: List<String> = emptyList(),
  val generatedFrameMotif: String = "",
  val generatedFrameComposition: String = "",
  val generatedPhotoTreatment: String = "",
  val generatedFrameOverlay: String = "",
  val generatedFrameCaption: String = "",
  val generatedFrameReason: String = "",
) {
  val isEmpty: Boolean
    get() =
      title.isBlank() &&
        tags.isEmpty() &&
        subject.isBlank() &&
        scene.isBlank() &&
        colors.isEmpty() &&
        confidence.isBlank() &&
        generatedFrameTitle.isBlank() &&
        generatedFrameStyle.isBlank() &&
        generatedFrameColors.isEmpty() &&
        generatedFrameMotif.isBlank() &&
        generatedFrameComposition.isBlank() &&
        generatedPhotoTreatment.isBlank() &&
        generatedFrameOverlay.isBlank() &&
        generatedFrameCaption.isBlank() &&
        generatedFrameReason.isBlank()

  companion object {
    val Empty = PhotoSaveMetadata()
  }
}

internal fun CapturedPhoto.saveMetadata(): PhotoSaveMetadata {
  val insight = (aiState as? PhotoAiState.Ready)?.insight
  val frameSpec = generatedFrameSpec
  return PhotoSaveMetadata(
    title = insight?.title.orEmpty(),
    tags = selectedAiTags.toList(),
    subject = insight?.subject.orEmpty(),
    scene = insight?.scene.orEmpty(),
    colors = insight?.colors.orEmpty(),
    confidence = insight?.confidence?.label.orEmpty(),
    generatedFrameTitle = frameSpec?.title.orEmpty(),
    generatedFrameStyle = frameSpec?.baseStyle?.label.orEmpty(),
    generatedFrameColors = frameSpec?.let { listOf(it.backgroundColor, it.accentColor, it.inkColor) }.orEmpty(),
    generatedFrameMotif = frameSpec?.motif?.label.orEmpty(),
    generatedFrameComposition = frameSpec?.composition?.label.orEmpty(),
    generatedPhotoTreatment = frameSpec?.photoTreatment?.label.orEmpty(),
    generatedFrameOverlay = frameSpec?.themeOverlay?.label.orEmpty(),
    generatedFrameCaption = frameSpec?.caption.orEmpty(),
    generatedFrameReason = frameSpec?.reason.orEmpty(),
  )
}

internal fun PhotoSaveMetadata.toUserCommentJson(): String =
  PhotoMetadataJson.encodeToString(
    PhotoMetadataPayload(
      title = title,
      tags = tags,
      subject = subject,
      scene = scene,
      colors = colors,
      confidence = confidence,
      generatedFrameTitle = generatedFrameTitle,
      generatedFrameStyle = generatedFrameStyle,
      generatedFrameColors = generatedFrameColors,
      generatedFrameMotif = generatedFrameMotif,
      generatedFrameComposition = generatedFrameComposition,
      generatedPhotoTreatment = generatedPhotoTreatment,
      generatedFrameOverlay = generatedFrameOverlay,
      generatedFrameCaption = generatedFrameCaption,
      generatedFrameReason = generatedFrameReason,
    ),
  )

private fun writePhotoMetadataToUri(
  context: Context,
  uri: android.net.Uri,
  metadata: PhotoSaveMetadata,
): Boolean {
  if (metadata.isEmpty) return true
  return runCatching {
    context.contentResolver.openFileDescriptor(uri, "rw")?.use { descriptor ->
      ExifInterface(descriptor.fileDescriptor).applyPhotoMetadata(metadata)
    } ?: return@runCatching false
    true
  }.getOrDefault(false)
}

private fun writePhotoMetadataToFile(
  file: File,
  metadata: PhotoSaveMetadata,
): Boolean {
  if (metadata.isEmpty) return true
  return runCatching {
    ExifInterface(file.absolutePath).applyPhotoMetadata(metadata)
    true
  }.getOrDefault(false)
}

private fun ExifInterface.applyPhotoMetadata(metadata: PhotoSaveMetadata) {
  if (metadata.title.isNotBlank()) {
    setAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION, metadata.title)
  }
  setAttribute(ExifInterface.TAG_SOFTWARE, "Pico Cam")
  setAttribute(ExifInterface.TAG_USER_COMMENT, metadata.toUserCommentJson())
  saveAttributes()
}

@Serializable
private data class PhotoMetadataPayload(
  val title: String,
  val tags: List<String>,
  val subject: String,
  val scene: String,
  val colors: List<String>,
  val confidence: String,
  val generatedFrameTitle: String,
  val generatedFrameStyle: String,
  val generatedFrameColors: List<String>,
  val generatedFrameMotif: String,
  val generatedFrameComposition: String,
  val generatedPhotoTreatment: String,
  val generatedFrameOverlay: String,
  val generatedFrameCaption: String,
  val generatedFrameReason: String,
)

private val PhotoMetadataJson =
  Json {
    encodeDefaults = false
  }

private fun renderFramedPhotoBitmap(
  bitmap: Bitmap,
  frameStyle: PhotoFrameStyle,
  generatedFrameSpec: GeneratedFrameSpec? = null,
): Bitmap {
  val effectiveFrameStyle = generatedFrameSpec?.baseStyle ?: frameStyle
  val frameSpec = effectiveFrameStyle.renderSpec(generatedFrameSpec)
  val outputWidth = bitmap.width + frameSpec.left + frameSpec.right
  val outputHeight = bitmap.height + frameSpec.top + frameSpec.bottom
  val output = Bitmap.createBitmap(outputWidth, outputHeight, Bitmap.Config.ARGB_8888)
  val canvas = AndroidCanvas(output)
  val framePaint =
    Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG).apply {
      style = Paint.Style.FILL
    }
  val bitmapPaint = Paint(Paint.ANTI_ALIAS_FLAG or Paint.FILTER_BITMAP_FLAG or Paint.DITHER_FLAG)

  drawAndroidFrameBackground(canvas, outputWidth, outputHeight, effectiveFrameStyle, generatedFrameSpec, framePaint)

  val imageRect =
    RectF(
      frameSpec.left.toFloat(),
      frameSpec.top.toFloat(),
      (outputWidth - frameSpec.right).toFloat(),
      (outputHeight - frameSpec.bottom).toFloat(),
  )
  drawCroppedBitmap(canvas = canvas, bitmap = bitmap, destination = imageRect, cornerRadius = frameSpec.imageCornerRadius, paint = bitmapPaint)
  drawAndroidImageInsetBorder(canvas, imageRect, frameSpec.imageCornerRadius, generatedFrameSpec, framePaint)
  if (generatedFrameSpec != null) {
    drawAndroidGeneratedPhotoTreatment(canvas, imageRect, frameSpec.imageCornerRadius, generatedFrameSpec, framePaint)
    drawAndroidGeneratedThemeOverlay(canvas, imageRect, frameSpec.imageCornerRadius, generatedFrameSpec, framePaint)
    drawAndroidGeneratedEmbeddedFrameBridge(canvas, outputWidth, outputHeight, generatedFrameSpec, framePaint)
  }
  drawAndroidFrameForeground(canvas, outputWidth, outputHeight, effectiveFrameStyle, generatedFrameSpec, framePaint)

  return output
}

private fun drawAndroidImageInsetBorder(
  canvas: AndroidCanvas,
  imageRect: RectF,
  cornerRadius: Float,
  generatedFrameSpec: GeneratedFrameSpec?,
  paint: Paint,
) {
  paint.shader = null
  paint.style = Paint.Style.STROKE
  paint.strokeWidth = if (generatedFrameSpec == null) 3f else 4f
  paint.color = generatedFrameSpec?.accentColor?.toGeneratedFrameColor(FrameGreen)?.toArgb() ?: android.graphics.Color.argb(22, 0, 0, 0)
  paint.alpha = if (generatedFrameSpec == null) 255 else 72
  canvas.drawRoundRect(imageRect, cornerRadius, cornerRadius, paint)
  paint.alpha = 255
}

private fun drawAndroidFrameBackground(
  canvas: AndroidCanvas,
  width: Int,
  height: Int,
  frameStyle: PhotoFrameStyle,
  generatedFrameSpec: GeneratedFrameSpec?,
  paint: Paint,
) {
  val generatedBackground = generatedFrameSpec?.backgroundColor?.toGeneratedFrameColor(frameStyle.frameColor())?.toArgb()
  val generatedAccent = generatedFrameSpec?.accentColor?.toGeneratedFrameColor(FrameGreen)?.toArgb() ?: FrameGreen.toArgb()
  when (frameStyle) {
    PhotoFrameStyle.Stamp -> {
      val path = createAndroidStampPath(width = width.toFloat(), height = height.toFloat(), toothSize = 42f, toothDepth = 25f)
      paint.style = Paint.Style.FILL
      paint.color = generatedBackground ?: FrameWarmWhite.toArgb()
      canvas.drawPath(path, paint)
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 3f
      paint.color = android.graphics.Color.argb(34, 0, 0, 0)
      canvas.drawPath(path, paint)
    }
    PhotoFrameStyle.Polaroid -> {
      val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
      paint.style = Paint.Style.FILL
      paint.color = generatedBackground ?: FrameCream.toArgb()
      canvas.drawRoundRect(rect, 40f, 40f, paint)
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 4f
      paint.color = android.graphics.Color.argb(30, 0, 0, 0)
      canvas.drawRoundRect(rect, 40f, 40f, paint)
    }
    PhotoFrameStyle.Film -> {
      val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
      paint.style = Paint.Style.FILL
      paint.color = generatedBackground ?: FrameBlack.toArgb()
      canvas.drawRoundRect(rect, 28f, 28f, paint)
    }
    PhotoFrameStyle.ColorPop -> {
      val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
      paint.style = Paint.Style.FILL
      paint.color = generatedBackground ?: FrameOrange.toArgb()
      canvas.drawRoundRect(rect, 34f, 34f, paint)
      paint.color = FrameRed.copy(alpha = 0.55f).toArgb()
      canvas.drawCircle(width * 0.08f, height * 0.12f, min(width, height) * 0.35f, paint)
      paint.color = generatedAccent
      paint.alpha = 154
      canvas.drawCircle(width * 0.94f, height * 0.88f, min(width, height) * 0.32f, paint)
      paint.alpha = 255
    }
  }
  if (generatedFrameSpec != null) {
    drawAndroidGeneratedFrameComposition(canvas, width, height, generatedFrameSpec, paint)
    drawAndroidGeneratedFrameMotif(canvas, width, height, generatedFrameSpec, paint)
  }
}

private fun drawAndroidFrameForeground(
  canvas: AndroidCanvas,
  width: Int,
  height: Int,
  frameStyle: PhotoFrameStyle,
  generatedFrameSpec: GeneratedFrameSpec?,
  paint: Paint,
) {
  when (frameStyle) {
    PhotoFrameStyle.Film -> {
      paint.style = Paint.Style.FILL
      paint.color = FrameWarmWhite.toArgb()
      val holeWidth = 18f
      val holeHeight = 38f
      val sideInset = 20f
      val topInset = 48f
      val availableHeight = height - topInset * 2f
      val count = max(4, (availableHeight / 92f).roundToInt())
      val step = availableHeight / count
      repeat(count) { index ->
        val y = topInset + step * index + (step - holeHeight) / 2f
        canvas.drawRoundRect(RectF(sideInset, y, sideInset + holeWidth, y + holeHeight), 8f, 8f, paint)
        canvas.drawRoundRect(RectF(width - sideInset - holeWidth, y, width - sideInset, y + holeHeight), 8f, 8f, paint)
      }
    }
    PhotoFrameStyle.Polaroid -> {
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = 4f
      paint.strokeCap = Paint.Cap.ROUND
      paint.color = android.graphics.Color.argb(22, 0, 0, 0)
      canvas.drawLine(width * 0.3f, height - 58f, width * 0.7f, height - 58f, paint)
    }
    PhotoFrameStyle.ColorPop -> {
      paint.style = Paint.Style.FILL
      paint.color = generatedFrameSpec?.accentColor?.toGeneratedFrameColor(FrameGreen)?.toArgb() ?: FrameGreen.toArgb()
      canvas.drawRoundRect(RectF(width * 0.18f, height - 116f, width * 0.82f, height - 86f), 18f, 18f, paint)
      paint.color = FrameRed.toArgb()
      canvas.drawRoundRect(RectF(0f, height - 74f, width.toFloat(), height - 42f), 18f, 18f, paint)
    }
    PhotoFrameStyle.Stamp -> Unit
  }
  if (generatedFrameSpec != null && generatedFrameSpec.caption.isNotBlank()) {
    drawAndroidGeneratedFrameCaption(canvas, width, height, frameStyle, generatedFrameSpec, paint)
  }
}

private fun drawAndroidGeneratedEmbeddedFrameBridge(
  canvas: AndroidCanvas,
  width: Int,
  height: Int,
  spec: GeneratedFrameSpec,
  paint: Paint,
) {
  paint.shader = null
  paint.style = Paint.Style.STROKE
  paint.strokeCap = Paint.Cap.ROUND
  val accent = spec.accentColor.toGeneratedFrameColor(FrameGreen).toArgb()
  when (spec.themeOverlay) {
    GeneratedFrameThemeOverlay.None -> Unit
    GeneratedFrameThemeOverlay.Ribbon -> {
      paint.strokeWidth = min(width, height) * 0.035f
      paint.color = colorWithAlpha(accent, 116)
      canvas.drawLine(-width * 0.08f, height * 0.63f, width * 1.08f, height * 0.5f, paint)
    }
    GeneratedFrameThemeOverlay.CornerBloom -> {
      paint.style = Paint.Style.FILL
      paint.color = colorWithAlpha(accent, 68)
      canvas.drawCircle(width * 0.13f, height * 0.13f, min(width, height) * 0.18f, paint)
      paint.color = colorWithAlpha(FrameWarmWhite.toArgb(), 46)
      canvas.drawCircle(width * 0.2f, height * 0.18f, min(width, height) * 0.12f, paint)
    }
    GeneratedFrameThemeOverlay.LightLeak -> {
      paint.style = Paint.Style.FILL
      paint.shader =
        LinearGradient(
          0f,
          0f,
          width * 0.62f,
          height * 0.48f,
          intArrayOf(colorWithAlpha(accent, 82), android.graphics.Color.TRANSPARENT),
          null,
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
      paint.shader = null
    }
    GeneratedFrameThemeOverlay.FilmBurn -> {
      paint.style = Paint.Style.FILL
      paint.shader =
        LinearGradient(
          width.toFloat(),
          0f,
          width * 0.28f,
          height.toFloat(),
          intArrayOf(colorWithAlpha(FrameOrange.toArgb(), 76), colorWithAlpha(FrameRed.toArgb(), 46), android.graphics.Color.TRANSPARENT),
          null,
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
      paint.shader = null
    }
    GeneratedFrameThemeOverlay.StickerTrail -> {
      paint.style = Paint.Style.FILL
      repeat(4) { index ->
        paint.color = colorWithAlpha(accent, 86)
        canvas.drawCircle(width * (0.18f + index * 0.19f), height * (0.11f + (index % 2) * 0.08f), min(width, height) * 0.028f, paint)
      }
    }
  }
  paint.alpha = 255
  paint.shader = null
}

private fun drawAndroidGeneratedFrameComposition(
  canvas: AndroidCanvas,
  width: Int,
  height: Int,
  spec: GeneratedFrameSpec,
  paint: Paint,
) {
  paint.shader = null
  paint.style = Paint.Style.FILL
  val accent = spec.accentColor.toGeneratedFrameColor(FrameGreen).toArgb()
  val ink = spec.inkColor.toGeneratedFrameColor(FrameBlack).toArgb()
  when (spec.composition) {
    GeneratedFrameComposition.Classic -> Unit
    GeneratedFrameComposition.Offset -> {
      paint.color = colorWithAlpha(accent, 58)
      canvas.drawRoundRect(RectF(width * 0.08f, height * 0.08f, width * 0.5f, height * 0.18f), 28f, 28f, paint)
      paint.color = colorWithAlpha(ink, 32)
      canvas.drawRoundRect(RectF(width * 0.5f, height * 0.76f, width * 0.88f, height * 0.84f), 24f, 24f, paint)
    }
    GeneratedFrameComposition.Poster -> {
      paint.shader =
        LinearGradient(
          width.toFloat(),
          0f,
          width * 0.2f,
          height * 0.55f,
          colorWithAlpha(accent, 92),
          android.graphics.Color.TRANSPARENT,
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), paint)
      paint.shader = null
      paint.color = colorWithAlpha(ink, 44)
      canvas.drawRoundRect(RectF(width * 0.12f, height * 0.86f, width * 0.88f, height * 0.89f), 12f, 12f, paint)
    }
    GeneratedFrameComposition.Portal -> {
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = min(width, height) * 0.035f
      paint.color = colorWithAlpha(accent, 62)
      canvas.drawCircle(width * 0.5f, height * 0.5f, min(width, height) * 0.42f, paint)
      paint.strokeWidth = min(width, height) * 0.018f
      paint.color = android.graphics.Color.argb(34, 255, 255, 255)
      canvas.drawCircle(width * 0.5f, height * 0.5f, min(width, height) * 0.34f, paint)
      paint.style = Paint.Style.FILL
    }
    GeneratedFrameComposition.Scrapbook -> {
      paint.color = android.graphics.Color.argb(82, 255, 255, 255)
      canvas.drawRoundRect(RectF(width * 0.1f, height * 0.02f, width * 0.36f, height * 0.09f), 12f, 12f, paint)
      paint.color = colorWithAlpha(accent, 92)
      canvas.drawRoundRect(RectF(width * 0.64f, height * 0.91f, width * 0.9f, height * 0.97f), 12f, 12f, paint)
    }
    GeneratedFrameComposition.Split -> {
      paint.color = colorWithAlpha(ink, 34)
      canvas.drawRect(0f, 0f, width * 0.18f, height.toFloat(), paint)
      paint.color = colorWithAlpha(accent, 56)
      canvas.drawRect(width * 0.84f, 0f, width.toFloat(), height.toFloat(), paint)
    }
  }
  paint.shader = null
  paint.alpha = 255
}

private fun drawAndroidGeneratedFrameMotif(
  canvas: AndroidCanvas,
  width: Int,
  height: Int,
  spec: GeneratedFrameSpec,
  paint: Paint,
) {
  paint.style = Paint.Style.FILL
  val accent = spec.accentColor.toGeneratedFrameColor(FrameGreen).toArgb()
  val ink = spec.inkColor.toGeneratedFrameColor(FrameBlack).toArgb()
  when (spec.motif) {
    GeneratedFrameMotif.None -> Unit
    GeneratedFrameMotif.Dots -> {
      paint.color = accent
      paint.alpha = 148
      val radius = min(width, height) * 0.018f
      repeat(8) { index ->
        val x = width * (0.1f + (index % 4) * 0.26f)
        val y = if (index < 4) height * 0.08f else height * 0.91f
        canvas.drawCircle(x, y, radius, paint)
      }
    }
    GeneratedFrameMotif.Sparkles -> {
      paint.color = accent
      paint.alpha = 188
      paint.strokeWidth = 6f
      paint.strokeCap = Paint.Cap.ROUND
      repeat(5) { index ->
        val cx = width * (0.14f + (index % 3) * 0.34f)
        val cy = if (index % 2 == 0) height * 0.12f else height * 0.88f
        val radius = min(width, height) * 0.026f
        canvas.drawLine(cx - radius, cy, cx + radius, cy, paint)
        canvas.drawLine(cx, cy - radius, cx, cy + radius, paint)
      }
    }
    GeneratedFrameMotif.Waves -> {
      paint.color = accent
      paint.alpha = 120
      paint.strokeWidth = 8f
      paint.strokeCap = Paint.Cap.ROUND
      repeat(3) { index ->
        val y = height * (0.1f + index * 0.035f)
        canvas.drawLine(width * 0.08f, y, width * 0.92f, y + height * 0.025f, paint)
      }
    }
    GeneratedFrameMotif.Leaves -> {
      paint.color = accent
      paint.alpha = 128
      repeat(5) { index ->
        val cx = if (index % 2 == 0) width * 0.12f else width * 0.88f
        val cy = height * (0.14f + index * 0.16f)
        canvas.drawOval(
          RectF(cx - width * 0.025f, cy - height * 0.012f, cx + width * 0.025f, cy + height * 0.012f),
          paint,
        )
      }
    }
    GeneratedFrameMotif.Lines -> {
      paint.color = ink
      paint.alpha = 54
      paint.strokeWidth = 5f
      paint.strokeCap = Paint.Cap.ROUND
      repeat(5) { index ->
        val x = width * (0.12f + index * 0.19f)
        canvas.drawLine(x, height * 0.04f, x + width * 0.06f, height * 0.15f, paint)
      }
    }
  }
  paint.alpha = 255
}

private fun drawAndroidGeneratedPhotoTreatment(
  canvas: AndroidCanvas,
  imageRect: RectF,
  cornerRadius: Float,
  spec: GeneratedFrameSpec,
  paint: Paint,
) {
  if (spec.photoTreatment == GeneratedPhotoTreatment.Natural) return
  val saveCount = canvas.save()
  canvas.clipPath(
    AndroidPath().apply {
      addRoundRect(imageRect, cornerRadius, cornerRadius, AndroidPath.Direction.CW)
    },
  )
  paint.shader = null
  paint.style = Paint.Style.FILL
  val accent = spec.accentColor.toGeneratedFrameColor(FrameGreen).toArgb()
  val background = spec.backgroundColor.toGeneratedFrameColor(FrameCream).toArgb()
  when (spec.photoTreatment) {
    GeneratedPhotoTreatment.Natural -> Unit
    GeneratedPhotoTreatment.WarmGlow -> {
      paint.shader =
        LinearGradient(
          imageRect.left,
          imageRect.top,
          imageRect.right,
          imageRect.bottom,
          intArrayOf(colorWithAlpha(FrameOrange.toArgb(), 52), android.graphics.Color.TRANSPARENT, colorWithAlpha(background, 36)),
          floatArrayOf(0f, 0.52f, 1f),
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(imageRect, paint)
    }
    GeneratedPhotoTreatment.CoolFade -> {
      paint.shader =
        LinearGradient(
          imageRect.right,
          imageRect.top,
          imageRect.left,
          imageRect.bottom,
          intArrayOf(android.graphics.Color.argb(52, 43, 111, 214), android.graphics.Color.TRANSPARENT, android.graphics.Color.argb(36, 0, 0, 0)),
          null,
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(imageRect, paint)
    }
    GeneratedPhotoTreatment.Noir -> {
      paint.color = android.graphics.Color.argb(66, 0, 0, 0)
      canvas.drawRect(imageRect, paint)
      paint.shader =
        LinearGradient(
          imageRect.left,
          imageRect.top,
          imageRect.right,
          imageRect.bottom,
          intArrayOf(android.graphics.Color.argb(28, 255, 255, 255), android.graphics.Color.TRANSPARENT, android.graphics.Color.argb(58, 0, 0, 0)),
          null,
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(imageRect, paint)
    }
    GeneratedPhotoTreatment.PopTint -> {
      paint.color = colorWithAlpha(accent, 46)
      canvas.drawRect(imageRect, paint)
      paint.color = android.graphics.Color.argb(42, 255, 255, 255)
      canvas.drawCircle(imageRect.left + imageRect.width() * 0.18f, imageRect.top + imageRect.height() * 0.18f, min(imageRect.width(), imageRect.height()) * 0.36f, paint)
    }
    GeneratedPhotoTreatment.DreamWash -> {
      paint.color = android.graphics.Color.argb(36, 255, 255, 255)
      canvas.drawRect(imageRect, paint)
      paint.color = colorWithAlpha(background, 62)
      canvas.drawCircle(imageRect.left + imageRect.width() * 0.82f, imageRect.top + imageRect.height() * 0.16f, min(imageRect.width(), imageRect.height()) * 0.5f, paint)
      paint.color = colorWithAlpha(accent, 36)
      canvas.drawCircle(imageRect.left + imageRect.width() * 0.12f, imageRect.top + imageRect.height() * 0.88f, min(imageRect.width(), imageRect.height()) * 0.32f, paint)
    }
  }
  paint.shader = null
  paint.alpha = 255
  canvas.restoreToCount(saveCount)
}

private fun drawAndroidGeneratedThemeOverlay(
  canvas: AndroidCanvas,
  imageRect: RectF,
  cornerRadius: Float,
  spec: GeneratedFrameSpec,
  paint: Paint,
) {
  if (spec.themeOverlay == GeneratedFrameThemeOverlay.None) return
  val saveCount = canvas.save()
  canvas.clipPath(
    AndroidPath().apply {
      addRoundRect(imageRect, cornerRadius, cornerRadius, AndroidPath.Direction.CW)
    },
  )
  paint.shader = null
  val accent = spec.accentColor.toGeneratedFrameColor(FrameGreen).toArgb()
  val ink = spec.inkColor.toGeneratedFrameColor(FrameBlack).toArgb()
  val left = imageRect.left
  val top = imageRect.top
  val width = imageRect.width()
  val height = imageRect.height()
  val minSize = min(width, height)
  when (spec.themeOverlay) {
    GeneratedFrameThemeOverlay.None -> Unit
    GeneratedFrameThemeOverlay.Ribbon -> {
      paint.style = Paint.Style.STROKE
      paint.strokeCap = Paint.Cap.ROUND
      paint.strokeWidth = minSize * 0.08f
      paint.color = colorWithAlpha(accent, 158)
      canvas.drawLine(left - width * 0.08f, top + height * 0.72f, left + width * 1.08f, top + height * 0.55f, paint)
      paint.strokeWidth = minSize * 0.012f
      paint.color = colorWithAlpha(ink, 52)
      canvas.drawLine(left + width * 0.12f, top + height * 0.76f, left + width * 0.78f, top + height * 0.64f, paint)
    }
    GeneratedFrameThemeOverlay.CornerBloom -> {
      paint.style = Paint.Style.FILL
      repeat(4) { index ->
        paint.color = colorWithAlpha(accent, 108 - index * 12)
        canvas.drawCircle(left + width * (0.04f + index * 0.06f), top + height * (0.08f + index * 0.035f), minSize * (0.08f + index * 0.025f), paint)
      }
      paint.style = Paint.Style.STROKE
      paint.strokeWidth = minSize * 0.012f
      paint.strokeCap = Paint.Cap.ROUND
      paint.color = colorWithAlpha(ink, 56)
      canvas.drawLine(left, top + height * 0.24f, left + width * 0.32f, top, paint)
    }
    GeneratedFrameThemeOverlay.LightLeak -> {
      paint.style = Paint.Style.FILL
      paint.shader =
        LinearGradient(
          left,
          top,
          left + width * 0.78f,
          top + height * 0.65f,
          intArrayOf(colorWithAlpha(FrameOrange.toArgb(), 118), colorWithAlpha(accent, 32), android.graphics.Color.TRANSPARENT),
          null,
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(imageRect, paint)
    }
    GeneratedFrameThemeOverlay.FilmBurn -> {
      paint.style = Paint.Style.FILL
      paint.shader =
        LinearGradient(
          imageRect.right,
          top,
          left + width * 0.25f,
          imageRect.bottom,
          intArrayOf(colorWithAlpha(FrameRed.toArgb(), 118), colorWithAlpha(FrameOrange.toArgb(), 72), android.graphics.Color.TRANSPARENT),
          null,
          Shader.TileMode.CLAMP,
        )
      canvas.drawRect(imageRect, paint)
    }
    GeneratedFrameThemeOverlay.StickerTrail -> {
      paint.style = Paint.Style.FILL
      repeat(6) { index ->
        val cx = left + width * (0.1f + index * 0.15f)
        val cy = top + height * (0.18f + (index % 3) * 0.12f)
        paint.color = colorWithAlpha(accent, 108)
        canvas.drawCircle(cx, cy, minSize * 0.035f, paint)
        paint.style = Paint.Style.STROKE
        paint.strokeWidth = minSize * 0.006f
        paint.strokeCap = Paint.Cap.ROUND
        paint.color = android.graphics.Color.argb(122, 255, 255, 255)
        canvas.drawLine(cx - minSize * 0.025f, cy, cx + minSize * 0.025f, cy, paint)
        paint.style = Paint.Style.FILL
      }
    }
  }
  paint.shader = null
  paint.alpha = 255
  canvas.restoreToCount(saveCount)
}

private fun drawAndroidGeneratedFrameCaption(
  canvas: AndroidCanvas,
  width: Int,
  height: Int,
  frameStyle: PhotoFrameStyle,
  spec: GeneratedFrameSpec,
  paint: Paint,
) {
  paint.style = Paint.Style.FILL
  paint.color = spec.inkColor.toGeneratedFrameColor(if (frameStyle == PhotoFrameStyle.Film) FrameWarmWhite else FrameBlack).toArgb()
  paint.alpha = 230
  paint.textAlign = Paint.Align.CENTER
  paint.typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
  paint.textSize = if (frameStyle == PhotoFrameStyle.Film) 24f else 31f
  val y =
    when (frameStyle) {
      PhotoFrameStyle.Film -> height - 18f
      PhotoFrameStyle.Polaroid -> height - 58f
      PhotoFrameStyle.ColorPop -> height - 44f
      PhotoFrameStyle.Stamp -> height - 44f
    }
  val caption = spec.caption.fitAndroidText(paint, width * 0.72f)
  canvas.drawText(caption, width / 2f, y, paint)
  paint.alpha = 255
  paint.typeface = Typeface.DEFAULT
}

private fun String.fitAndroidText(
  paint: Paint,
  maxWidth: Float,
): String {
  if (paint.measureText(this) <= maxWidth) return this
  val suffix = "..."
  var end = length
  while (end > 0 && paint.measureText(substring(0, end) + suffix) > maxWidth) {
    end -= 1
  }
  return if (end <= 0) suffix else substring(0, end).trimEnd() + suffix
}

private fun drawCroppedBitmap(
  canvas: AndroidCanvas,
  bitmap: Bitmap,
  destination: RectF,
  cornerRadius: Float,
  paint: Paint,
) {
  val source = centerCropSourceRect(bitmap = bitmap, destination = destination)
  val clipPath =
    AndroidPath().apply {
      addRoundRect(destination, cornerRadius, cornerRadius, AndroidPath.Direction.CW)
    }
  val saveCount = canvas.save()
  canvas.clipPath(clipPath)
  canvas.drawBitmap(bitmap, source, destination, paint)
  canvas.restoreToCount(saveCount)
}

private fun colorWithAlpha(
  color: Int,
  alpha: Int,
): Int = (alpha.coerceIn(0, 255) shl 24) or (color and 0x00FFFFFF)

private fun centerCropSourceRect(
  bitmap: Bitmap,
  destination: RectF,
): Rect {
  val sourceRatio = bitmap.width.toFloat() / bitmap.height.toFloat()
  val destinationRatio = destination.width() / destination.height()

  return if (sourceRatio > destinationRatio) {
    val cropWidth = (bitmap.height * destinationRatio).roundToInt().coerceAtLeast(1)
    val left = (bitmap.width - cropWidth) / 2
    Rect(left, 0, left + cropWidth, bitmap.height)
  } else {
    val cropHeight = (bitmap.width / destinationRatio).roundToInt().coerceAtLeast(1)
    val top = (bitmap.height - cropHeight) / 2
    Rect(0, top, bitmap.width, top + cropHeight)
  }
}

private fun createAndroidStampPath(
  width: Float,
  height: Float,
  toothSize: Float,
  toothDepth: Float,
): AndroidPath {
  val horizontalTeeth = max(4, (width / toothSize).roundToInt())
  val verticalTeeth = max(4, (height / toothSize).roundToInt())

  return AndroidPath().apply {
    moveTo(0f, 0f)
    addStampHorizontalEdge(0f, width, 0f, toothDepth, horizontalTeeth)
    addStampVerticalEdge(0f, height, width, width - toothDepth, verticalTeeth)
    addStampHorizontalEdge(width, 0f, height, height - toothDepth, horizontalTeeth)
    addStampVerticalEdge(height, 0f, 0f, toothDepth, verticalTeeth)
    close()
  }
}

private fun AndroidPath.addStampHorizontalEdge(
  startX: Float,
  endX: Float,
  outerY: Float,
  innerY: Float,
  teeth: Int,
) {
  val step = (endX - startX) / teeth
  for (index in 0 until teeth) {
    val segmentStart = startX + step * index
    val segmentEnd = startX + step * (index + 1)
    lineTo((segmentStart + segmentEnd) / 2f, innerY)
    lineTo(segmentEnd, outerY)
  }
}

private fun AndroidPath.addStampVerticalEdge(
  startY: Float,
  endY: Float,
  outerX: Float,
  innerX: Float,
  teeth: Int,
) {
  val step = (endY - startY) / teeth
  for (index in 0 until teeth) {
    val segmentStart = startY + step * index
    val segmentEnd = startY + step * (index + 1)
    lineTo(innerX, (segmentStart + segmentEnd) / 2f)
    lineTo(outerX, segmentEnd)
  }
}

private fun PhotoFrameStyle.renderSpec(generatedFrameSpec: GeneratedFrameSpec? = null): FrameRenderSpec {
  val base =
    when (this) {
    PhotoFrameStyle.Stamp -> FrameRenderSpec(left = 64, top = 64, right = 64, bottom = 132, imageCornerRadius = 18f)
    PhotoFrameStyle.Polaroid -> FrameRenderSpec(left = 58, top = 58, right = 58, bottom = 190, imageCornerRadius = 22f)
    PhotoFrameStyle.Film -> FrameRenderSpec(left = 124, top = 70, right = 124, bottom = 70, imageCornerRadius = 16f)
    PhotoFrameStyle.ColorPop -> FrameRenderSpec(left = 60, top = 60, right = 60, bottom = 154, imageCornerRadius = 22f)
  }
  return base.forComposition(generatedFrameSpec?.composition ?: GeneratedFrameComposition.Classic)
}

private fun FrameRenderSpec.forComposition(composition: GeneratedFrameComposition): FrameRenderSpec =
  when (composition) {
    GeneratedFrameComposition.Classic -> this
    GeneratedFrameComposition.Offset -> copy(left = left + 38, top = max(34, top - 22), right = max(34, right - 18), bottom = bottom + 54, imageCornerRadius = imageCornerRadius + 8f)
    GeneratedFrameComposition.Poster -> copy(top = max(36, top - 24), bottom = bottom + 86, imageCornerRadius = imageCornerRadius + 4f)
    GeneratedFrameComposition.Portal -> copy(left = left + 22, top = top + 22, right = right + 22, bottom = bottom + 16, imageCornerRadius = imageCornerRadius + 20f)
    GeneratedFrameComposition.Scrapbook -> copy(left = left + 48, top = top + 18, right = max(32, right - 16), bottom = bottom + 48, imageCornerRadius = imageCornerRadius + 12f)
    GeneratedFrameComposition.Split -> copy(left = left + 76, top = top, right = right + 12, bottom = bottom + 14, imageCornerRadius = imageCornerRadius + 8f)
  }

private data class FrameRenderSpec(
  val left: Int,
  val top: Int,
  val right: Int,
  val bottom: Int,
  val imageCornerRadius: Float,
)
