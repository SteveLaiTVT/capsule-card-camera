package com.example.capsulecardcamera.ui.main

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class CameraIslandTest {

  @Test
  fun exposureMeterTrack_usesPreferredHeightWhenEnoughSpace() {
    val track =
      exposureMeterTrack(
        preferredCenterY = 100f,
        canvasHeight = 240f,
        verticalPadding = 12f,
        preferredHeight = 84f,
      )

    assertEquals(58f, track?.top ?: 0f, 0.001f)
    assertEquals(84f, track?.height ?: 0f, 0.001f)
  }

  @Test
  fun exposureMeterTrack_shrinksForShortCanvas() {
    val track =
      exposureMeterTrack(
        preferredCenterY = 20f,
        canvasHeight = 25f,
        verticalPadding = 12f,
        preferredHeight = 84f,
      )

    assertEquals(12f, track?.top ?: 0f, 0.001f)
    assertEquals(1f, track?.height ?: 0f, 0.001f)
  }

  @Test
  fun exposureMeterTrack_returnsNullWhenNoDrawableSpace() {
    assertNull(
      exposureMeterTrack(
        preferredCenterY = 20f,
        canvasHeight = 20f,
        verticalPadding = 12f,
        preferredHeight = 84f,
      ),
    )
  }
}
