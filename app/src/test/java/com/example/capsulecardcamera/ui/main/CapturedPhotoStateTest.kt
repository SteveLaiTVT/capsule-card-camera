package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertSame
import org.junit.Test

class CapturedPhotoStateTest {
  @Test
  fun createCapturedPhoto_appliesMatchingDefaultGeneratedFrameOnly() {
    val bitmap = BitmapFixtures.bitmap()
    val matchingSpec = generatedFrameSpec(baseStyle = PhotoFrameStyle.Film)
    val mismatchedSpec = generatedFrameSpec(baseStyle = PhotoFrameStyle.Polaroid)

    val withMatchingSpec = createCapturedPhoto(1, bitmap, PhotoFrameStyle.Film, matchingSpec)
    val withMismatchedSpec = createCapturedPhoto(2, bitmap, PhotoFrameStyle.Film, mismatchedSpec)

    assertEquals(PhotoFrameStyle.Film, withMatchingSpec.frameStyle)
    assertEquals(matchingSpec, withMatchingSpec.generatedFrameSpec)
    assertNull(withMismatchedSpec.generatedFrameSpec)
  }

  @Test
  fun createCapturedPhoto_keepsCaptureStyle() {
    val bitmap = BitmapFixtures.bitmap()

    val photo = createCapturedPhoto(3, bitmap, PhotoFrameStyle.ColorPop, null, PhotoCaptureStyle.AiImmersive)

    assertEquals(PhotoCaptureStyle.AiImmersive, photo.captureStyle)
  }

  @Test
  fun updateCapturedPhotoFrameStyle_clearsGeneratedFrameState() {
    val spec = generatedFrameSpec(baseStyle = PhotoFrameStyle.ColorPop)
    val photo =
      capturedPhoto(
        id = 7,
        frameStyle = PhotoFrameStyle.ColorPop,
        generatedFrameSpec = spec,
        frameGenerationState = FrameGenerationState.Ready(spec),
      )

    val updated = updateCapturedPhotoFrameStyle(listOf(photo), photoId = 7, frameStyle = PhotoFrameStyle.Film).single()

    assertEquals(PhotoFrameStyle.Film, updated.frameStyle)
    assertNull(updated.generatedFrameSpec)
    assertEquals(FrameGenerationState.Idle, updated.frameGenerationState)
  }

  @Test
  fun updateCapturedPhotoGeneratedFrameSpec_appliesSpecAndReadyState() {
    val photo = capturedPhoto(id = 4, frameStyle = PhotoFrameStyle.Stamp)
    val spec = generatedFrameSpec(baseStyle = PhotoFrameStyle.Polaroid)

    val updated = updateCapturedPhotoGeneratedFrameSpec(listOf(photo), photoId = 4, frameSpec = spec).single()

    assertEquals(PhotoFrameStyle.Polaroid, updated.frameStyle)
    assertEquals(spec, updated.generatedFrameSpec)
    assertEquals(FrameGenerationState.Ready(spec), updated.frameGenerationState)
  }

  @Test
  fun applyFrameStyleToCapturedPhotos_updatesOnlySelectedPhotos() {
    val selected = capturedPhoto(id = 1, frameStyle = PhotoFrameStyle.Stamp, generatedFrameSpec = generatedFrameSpec())
    val unselected = capturedPhoto(id = 2, frameStyle = PhotoFrameStyle.Polaroid)

    val updated = applyFrameStyleToCapturedPhotos(listOf(selected, unselected), photoIds = setOf(1), frameStyle = PhotoFrameStyle.Film)

    assertEquals(PhotoFrameStyle.Film, updated[0].frameStyle)
    assertNull(updated[0].generatedFrameSpec)
    assertEquals(PhotoFrameStyle.Polaroid, updated[1].frameStyle)
  }

  @Test
  fun toggleCapturedPhotoSelection_togglesPhotoId() {
    assertEquals(setOf(3, 5), toggleCapturedPhotoSelection(setOf(3), 5))
    assertEquals(setOf(3), toggleCapturedPhotoSelection(setOf(3, 5), 5))
  }

  @Test
  fun toFrameSaveRequest_keepsOnlyMatchingGeneratedFrameSpec() {
    val bitmap = BitmapFixtures.bitmap()
    val spec = generatedFrameSpec(baseStyle = PhotoFrameStyle.ColorPop, title = "Bloom pop")
    val photo = capturedPhoto(id = 9, bitmap = bitmap, frameStyle = PhotoFrameStyle.ColorPop, generatedFrameSpec = spec)

    val matchingRequest = photo.toFrameSaveRequest(defaultAlbum = DefaultAlbum.Capsule)
    val forcedStyleRequest = photo.toFrameSaveRequest(defaultAlbum = DefaultAlbum.Capsule, frameStyle = PhotoFrameStyle.Film)

    assertSame(bitmap, matchingRequest.bitmap)
    assertEquals(PhotoFrameStyle.ColorPop, matchingRequest.frameStyle)
    assertEquals(spec, matchingRequest.generatedFrameSpec)
    assertEquals("Bloom pop", matchingRequest.metadata.generatedFrameTitle)
    assertEquals(PhotoFrameStyle.Film, forcedStyleRequest.frameStyle)
    assertNull(forcedStyleRequest.generatedFrameSpec)
    assertEquals("", forcedStyleRequest.metadata.generatedFrameTitle)
  }

  private fun capturedPhoto(
    id: Int,
    bitmap: Bitmap = BitmapFixtures.bitmap(),
    frameStyle: PhotoFrameStyle,
    generatedFrameSpec: GeneratedFrameSpec? = null,
    frameGenerationState: FrameGenerationState = FrameGenerationState.Idle,
  ): CapturedPhoto =
    CapturedPhoto(
      id = id,
      bitmap = bitmap,
      frameStyle = frameStyle,
      generatedFrameSpec = generatedFrameSpec,
      frameGenerationState = frameGenerationState,
    )

  private fun generatedFrameSpec(
    title: String = "Custom frame",
    baseStyle: PhotoFrameStyle = PhotoFrameStyle.Stamp,
  ): GeneratedFrameSpec =
    GeneratedFrameSpec(
      title = title,
      baseStyle = baseStyle,
      backgroundColor = "cream",
      accentColor = "green",
      inkColor = "black",
      motif = GeneratedFrameMotif.Dots,
      caption = "custom",
      reason = "Fits the selected scene",
    )
}

private object BitmapFixtures {
  private val unsafe: sun.misc.Unsafe by lazy {
    val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as sun.misc.Unsafe
  }

  fun bitmap(): Bitmap = unsafe.allocateInstance(Bitmap::class.java) as Bitmap
}
