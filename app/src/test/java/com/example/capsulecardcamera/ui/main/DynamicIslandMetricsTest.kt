package com.example.capsulecardcamera.ui.main

import androidx.compose.ui.unit.dp
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class DynamicIslandMetricsTest {

  @Test
  fun fallbackDynamicIslandMetrics_usesComfortCoverByDefault() {
    val metrics = fallbackDynamicIslandMetrics(maxWidth = 390.dp)

    assertEquals(0.dp, metrics.top)
    assertEquals(111.dp, metrics.left)
    assertEquals(168.dp, metrics.width)
    assertEquals(46.dp, metrics.height)
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
        coverMode = DynamicIslandCoverMode.Maximum,
      )

    assertEquals(14.dp, metrics.top)
    assertEquals(102.dp, metrics.left)
    assertEquals(196.dp, metrics.width)
    assertEquals(60.dp, metrics.height)
    assertTrue(metrics.hasCutout)
  }
}
