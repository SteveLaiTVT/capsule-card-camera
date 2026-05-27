package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun HomeFrameStudioPanel(
  copy: CameraCopy,
  defaultFrameStyle: PhotoFrameStyle,
  defaultGeneratedFrameSpec: GeneratedFrameSpec?,
  customFrameCount: Int,
  onFrameSettingsClick: () -> Unit,
  onFrameManagerClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(22.dp))
        .background(FrameBlack.copy(alpha = 0.84f))
        .border(1.dp, FrameWarmWhite.copy(alpha = 0.12f), RoundedCornerShape(22.dp))
        .padding(12.dp)
        .testTag("home-frame-studio-panel"),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    FramePreviewCard(
      frameStyle = defaultGeneratedFrameSpec?.baseStyle ?: defaultFrameStyle,
      generatedFrameSpec = defaultGeneratedFrameSpec,
      modifier =
        Modifier
          .width(58.dp)
          .aspectRatio(0.78f)
          .clickable(onClick = onFrameSettingsClick),
    )

    Column(modifier = Modifier.weight(1f)) {
      Text(
        text = copy.myFramesTitle,
        color = FrameWarmWhite,
        fontSize = 15.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
      Text(
        text = homeFrameStudioSummary(copy, defaultFrameStyle, defaultGeneratedFrameSpec, customFrameCount),
        color = FrameWarmWhite.copy(alpha = 0.66f),
        fontSize = 11.sp,
        lineHeight = 14.sp,
        maxLines = 2,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 3.dp),
      )
    }

    Box(
      modifier =
        Modifier
          .height(42.dp)
          .clip(RoundedCornerShape(14.dp))
          .background(FrameGreen)
          .clickable(onClick = onFrameManagerClick)
          .padding(horizontal = 14.dp)
          .testTag("home-frame-manager-button"),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = copy.frameManagerGenerateLabel,
        color = Color.Black,
        fontSize = 12.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}

private fun homeFrameStudioSummary(
  copy: CameraCopy,
  defaultFrameStyle: PhotoFrameStyle,
  defaultGeneratedFrameSpec: GeneratedFrameSpec?,
  customFrameCount: Int,
): String {
  val defaultFrameLabel = defaultGeneratedFrameSpec?.title?.takeIf { it.isNotBlank() } ?: copy.frameStyleLabel(defaultFrameStyle)
  return if (customFrameCount > 0) {
    "$defaultFrameLabel · $customFrameCount"
  } else {
    copy.frameManagerDescription
  }
}
