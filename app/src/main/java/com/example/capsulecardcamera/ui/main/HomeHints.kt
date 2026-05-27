package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HomeCaptureHint(
  copy: CameraCopy,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(18.dp))
        .background(FrameBlack.copy(alpha = 0.22f))
        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(18.dp))
        .padding(horizontal = 14.dp, vertical = 10.dp)
        .testTag("home-capture-hint"),
    horizontalAlignment = Alignment.CenterHorizontally,
    verticalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    Canvas(modifier = Modifier.size(width = 34.dp, height = 16.dp).padding(bottom = 1.dp)) {
      val centerX = size.width * 0.5f
      drawLine(
        color = FrameWarmWhite.copy(alpha = 0.82f),
        start = Offset(centerX, 0f),
        end = Offset(centerX, 11.dp.toPx()),
        strokeWidth = 1.6.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawLine(
        color = FrameWarmWhite.copy(alpha = 0.82f),
        start = Offset(centerX - 5.dp.toPx(), 7.dp.toPx()),
        end = Offset(centerX, 12.dp.toPx()),
        strokeWidth = 1.6.dp.toPx(),
        cap = StrokeCap.Round,
      )
      drawLine(
        color = FrameWarmWhite.copy(alpha = 0.82f),
        start = Offset(centerX + 5.dp.toPx(), 7.dp.toPx()),
        end = Offset(centerX, 12.dp.toPx()),
        strokeWidth = 1.6.dp.toPx(),
        cap = StrokeCap.Round,
      )
    }
    Text(
      text = copy.homePullHintTitle,
      color = FrameWarmWhite,
      fontSize = 13.sp,
      lineHeight = 16.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
    )
    Text(
      text = copy.homePullHintSubtitle,
      color = FrameWarmWhite.copy(alpha = 0.72f),
      fontSize = 10.sp,
      lineHeight = 13.sp,
      textAlign = TextAlign.Center,
    )
  }
}
