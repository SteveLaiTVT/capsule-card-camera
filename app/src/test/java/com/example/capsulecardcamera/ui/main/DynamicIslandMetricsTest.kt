package com.example.capsulecardcamera.ui.main

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicIslandMetricsTest {

  @Test
  fun fallbackDynamicIslandMetrics_usesMinimalCoverByDefault() {
    val metrics = fallbackDynamicIslandMetrics(maxWidth = 390.dp)

    assertEquals(6.dp, metrics.top)
    assertEquals(131.dp, metrics.left)
    assertEquals(128.dp, metrics.width)
    assertEquals(44.dp, metrics.height)
    assertFalse(metrics.hasCutout)
  }

  @Test
  fun cutoutDynamicIslandMetrics_expandsAroundReportedCutout() {
    val metrics =
      cutoutDynamicIslandMetrics(
        maxWidth = 390.dp,
        statusBarHeight = 30.dp,
        cutoutTop = 24.dp,
        cutoutCenter = 200.dp,
        cutoutWidth = 20.dp,
        cutoutHeight = 18.dp,
        safeInsetTop = 42.dp,
        coverMode = DynamicIslandCoverMode.Precise,
      )

    assertEquals(24.dp, metrics.top)
    assertEquals(136.dp, metrics.left)
    assertEquals(128.dp, metrics.width)
    assertEquals(44.dp, metrics.height)
    assertTrue(metrics.hasCutout)
  }

  @Test
  fun cutoutDynamicIslandMetrics_keepsMinimumTopClearance() {
    val metrics =
      cutoutDynamicIslandMetrics(
        maxWidth = 390.dp,
        statusBarHeight = 28.dp,
        cutoutTop = 0.dp,
        cutoutCenter = 195.dp,
        cutoutWidth = 22.dp,
        cutoutHeight = 28.dp,
        safeInsetTop = 34.dp,
        coverMode = DynamicIslandCoverMode.Precise,
      )

    assertEquals(6.dp, metrics.top)
    assertEquals(128.dp, metrics.width)
    assertTrue(metrics.height >= 44.dp)
    assertTrue(metrics.hasCutout)
  }
}
