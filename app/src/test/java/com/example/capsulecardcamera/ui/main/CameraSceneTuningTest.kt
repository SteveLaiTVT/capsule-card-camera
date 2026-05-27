package com.example.capsulecardcamera.ui.main

import java.nio.ByteBuffer
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class CameraSceneTuningTest {
  @Test
  fun analyzeCenterLumaPlane_readsCenterCrop() {
    val width = 8
    val height = 8
    val bytes = ByteArray(width * height) { 20 }
    for (y in 2 until 6) {
      for (x in 2 until 6) {
        bytes[y * width + x] = 180.toByte()
      }
    }

    val stats =
      analyzeCenterLumaPlane(
        buffer = ByteBuffer.wrap(bytes),
        width = width,
        height = height,
        rowStride = width,
        pixelStride = 1,
      )

    assertNotNull(stats)
    assertTrue(stats!!.luma > 0.55f)
  }

  @Test
  fun recommendedExposureCompensationIndex_brightensDarkCenter() {
    assertEquals(3, recommendedExposureCompensationIndex(centerLuma = 0.22f, minIndex = -4, maxIndex = 4))
    assertEquals(-3, recommendedExposureCompensationIndex(centerLuma = 0.75f, minIndex = -4, maxIndex = 4))
    assertEquals(0, recommendedExposureCompensationIndex(centerLuma = 0.49f, minIndex = -4, maxIndex = 4))
  }

  @Test
  fun photoEnhancementProfileFromInsight_prefersReadableDocuments() {
    val profile =
      PhotoEnhancementProfile.fromInsight(
        PhotoInsight(
          title = "Laptop screen",
          tags = listOf("text", "keyboard"),
          subject = "document",
          scene = "desk monitor",
          colors = listOf("white"),
          confidence = PhotoInsightConfidence.High,
          suggestedFrameStyle = PhotoFrameStyle.Film,
          frameReason = "clean lines",
        ),
      )

    assertTrue(profile.contrast > 1.08f)
    assertTrue(profile.saturation < 1f)
  }
}
