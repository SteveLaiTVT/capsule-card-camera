package com.example.capsulecardcamera.ui.main

import android.content.ContentUris
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.exifinterface.media.ExifInterface
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class PhotoMetadataSaveInstrumentedTest {
  @Test
  fun saveFramedPhoto_writesSelectedTagsToExifMetadata() {
    assumeTrue(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q)

    val context = InstrumentationRegistry.getInstrumentation().targetContext.applicationContext
    val startedAtSeconds = System.currentTimeMillis() / 1000L - 5L
    val bitmap =
      Bitmap.createBitmap(80, 60, Bitmap.Config.ARGB_8888).apply {
        eraseColor(Color.rgb(230, 80, 40))
      }
    val metadata =
      PhotoSaveMetadata(
        title = "Red desk",
        tags = listOf("desk", "red", "selected"),
        subject = "desk",
        scene = "workspace",
        colors = listOf("red"),
        confidence = "high",
      )

    val saved = saveFramedPhoto(context, bitmap, PhotoFrameStyle.Polaroid, DefaultAlbum.Capsule, metadata)
    assertTrue(saved)

    val savedMedia = context.findLatestCapsuleCardPhoto(startedAtSeconds)
    assertNotNull(savedMedia)
    savedMedia ?: return
    try {
      assertTrue(savedMedia.displayName.endsWith(".jpg"))
      assertEquals("image/jpeg", savedMedia.mimeType)

      val exif =
        context.contentResolver.openFileDescriptor(savedMedia.uri, "r")?.use { descriptor ->
          ExifInterface(descriptor.fileDescriptor)
        }
      assertNotNull(exif)
      exif ?: return

      assertEquals("Red desk", exif.getAttribute(ExifInterface.TAG_IMAGE_DESCRIPTION))
      val userComment = exif.getAttribute(ExifInterface.TAG_USER_COMMENT).orEmpty()
      assertTrue(userComment.contains("\"tags\":[\"desk\",\"red\",\"selected\"]"))
      assertTrue(userComment.contains("\"confidence\":\"high\""))
    } finally {
      context.contentResolver.delete(savedMedia.uri, null, null)
      bitmap.recycle()
    }
  }
}

private data class SavedMedia(
  val uri: android.net.Uri,
  val displayName: String,
  val mimeType: String,
)

private fun Context.findLatestCapsuleCardPhoto(startedAtSeconds: Long): SavedMedia? {
  val projection =
    arrayOf(
      MediaStore.Images.Media._ID,
      MediaStore.Images.Media.DISPLAY_NAME,
      MediaStore.Images.Media.MIME_TYPE,
    )
  val selection =
    "${MediaStore.Images.Media.DISPLAY_NAME} LIKE ? AND " +
      "${MediaStore.Images.Media.RELATIVE_PATH} = ? AND " +
      "${MediaStore.Images.Media.DATE_ADDED} >= ?"
  val selectionArgs =
    arrayOf(
      "capsule-card-%.jpg",
      "${Environment.DIRECTORY_PICTURES}/${DefaultAlbum.Capsule.directoryName}/",
      startedAtSeconds.toString(),
    )
  return contentResolver
    .query(
      MediaStore.Images.Media.EXTERNAL_CONTENT_URI,
      projection,
      selection,
      selectionArgs,
      "${MediaStore.Images.Media.DATE_ADDED} DESC",
    )
    ?.use { cursor ->
      if (!cursor.moveToFirst()) return@use null
      val id = cursor.getLong(cursor.getColumnIndexOrThrow(MediaStore.Images.Media._ID))
      val displayName = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DISPLAY_NAME))
      val mimeType = cursor.getString(cursor.getColumnIndexOrThrow(MediaStore.Images.Media.MIME_TYPE))
      SavedMedia(
        uri = ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id),
        displayName = displayName,
        mimeType = mimeType,
      )
    }
}
