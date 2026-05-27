package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun PhotoAiInsightPanel(
  aiState: PhotoAiState,
  selectedTags: Set<String>,
  selectedFrameStyle: PhotoFrameStyle,
  copy: CameraCopy,
  onApplyAiFrame: (PhotoFrameStyle) -> Unit,
  onAiTagToggled: (String) -> Unit,
  frameGenerationState: FrameGenerationState,
  frameConversation: List<FrameConversationMessage>,
  onGenerateAiFrame: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(14.dp))
        .background(Color.Black.copy(alpha = 0.14f))
        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
        .padding(horizontal = 12.dp, vertical = 10.dp)
        .testTag("photo-ai-insight"),
  ) {
    Text(
      text = copy.aiInsightTitle,
      color = FrameWarmWhite,
      fontSize = 12.sp,
      lineHeight = 15.sp,
      fontWeight = FontWeight.Bold,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
    )

    when (aiState) {
      PhotoAiState.Idle -> PhotoAiStatusText(copy.aiIdle)
      PhotoAiState.Preparing -> PhotoAiStatusText(copy.aiPreparing)
      PhotoAiState.Analyzing -> PhotoAiStatusText(copy.aiAnalyzing)
      PhotoAiState.Unavailable -> PhotoAiStatusText(copy.aiUnavailable)
      is PhotoAiState.Failed -> PhotoAiStatusText(copy.aiFailed)
      is PhotoAiState.Ready ->
        PhotoAiInsightContent(
          insight = aiState.insight,
          selectedTags = selectedTags,
          selectedFrameStyle = selectedFrameStyle,
          copy = copy,
          onApplyAiFrame = onApplyAiFrame,
          onAiTagToggled = onAiTagToggled,
          frameGenerationState = frameGenerationState,
          frameConversation = frameConversation,
          onGenerateAiFrame = onGenerateAiFrame,
        )
    }
  }
}

@Composable
private fun PhotoAiStatusText(text: String) {
  Text(
    text = text,
    color = FrameWarmWhite.copy(alpha = 0.72f),
    fontSize = 11.sp,
    lineHeight = 14.sp,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.padding(top = 5.dp),
  )
}

@Composable
private fun PhotoAiInsightContent(
  insight: PhotoInsight,
  selectedTags: Set<String>,
  selectedFrameStyle: PhotoFrameStyle,
  copy: CameraCopy,
  onApplyAiFrame: (PhotoFrameStyle) -> Unit,
  onAiTagToggled: (String) -> Unit,
  frameGenerationState: FrameGenerationState,
  frameConversation: List<FrameConversationMessage>,
  onGenerateAiFrame: (String) -> Unit,
) {
  Text(
    text = insight.title,
    color = FrameWarmWhite,
    fontSize = 13.sp,
    lineHeight = 16.sp,
    fontWeight = FontWeight.Bold,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.padding(top = 5.dp),
  )
  if (insight.tags.isNotEmpty()) {
    PhotoAiTagSelectionRows(
      tags = insight.tags,
      selectedTags = selectedTags,
      onTagToggled = onAiTagToggled,
      modifier = Modifier.padding(top = 4.dp),
    )
  }
  Text(
    text = "${copy.aiSubjectLabel}: ${insight.subject.ifBlank { "-" }}  ${copy.aiSceneLabel}: ${insight.scene.ifBlank { "-" }}",
    color = FrameWarmWhite.copy(alpha = 0.74f),
    fontSize = 10.sp,
    lineHeight = 13.sp,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.padding(top = 4.dp),
  )
  Text(
    text =
      "${copy.aiColorsLabel}: ${insight.colors.joinToString(", ").ifBlank { "-" }}  " +
        "${copy.aiConfidenceLabel}: ${copy.confidenceLabel(insight.confidence)}",
    color = FrameWarmWhite.copy(alpha = 0.68f),
    fontSize = 10.sp,
    lineHeight = 13.sp,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    modifier = Modifier.padding(top = 2.dp),
  )

  val suggestedFrameStyle = insight.suggestedFrameStyle
  if (suggestedFrameStyle != null) {
    val applied = suggestedFrameStyle == selectedFrameStyle
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
      Column(modifier = Modifier.weight(1f)) {
        Text(
          text = "${copy.aiFrameSuggestionLabel}: ${copy.frameStyleLabel(suggestedFrameStyle)}",
          color = FrameWarmWhite,
          fontSize = 11.sp,
          lineHeight = 14.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
        if (insight.frameReason.isNotBlank()) {
          Text(
            text = insight.frameReason,
            color = FrameWarmWhite.copy(alpha = 0.62f),
            fontSize = 10.sp,
            lineHeight = 13.sp,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.padding(top = 2.dp),
          )
        }
      }
      Box(
        modifier =
          Modifier
            .height(34.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (applied) FrameGreen.copy(alpha = 0.5f) else FrameWarmWhite)
            .clickable(enabled = !applied) { onApplyAiFrame(suggestedFrameStyle) }
            .padding(horizontal = 12.dp)
            .testTag("ai-frame-suggestion-button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = if (applied) copy.aiFrameAppliedLabel else copy.aiFrameApplyLabel,
          color = if (applied) FrameWarmWhite else FrameBlack,
          fontSize = 11.sp,
          lineHeight = 14.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
        )
      }
    }
  }

  GeneratedFrameConversationPanel(
    frameGenerationState = frameGenerationState,
    frameConversation = frameConversation,
    copy = copy,
    onGenerateAiFrame = onGenerateAiFrame,
    modifier = Modifier.padding(top = 10.dp),
  )
}

@Composable
private fun GeneratedFrameConversationPanel(
  frameGenerationState: FrameGenerationState,
  frameConversation: List<FrameConversationMessage>,
  copy: CameraCopy,
  onGenerateAiFrame: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  var promptText by remember(frameConversation.size) { mutableStateOf("") }
  val isGenerating = frameGenerationState == FrameGenerationState.Generating
  val buttonLabel =
    when {
      isGenerating -> copy.aiFrameGenerating
      frameGenerationState is FrameGenerationState.Ready -> copy.aiFrameRefineLabel
      else -> copy.aiFrameGenerateLabel
    }

  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Color.Black.copy(alpha = 0.12f))
        .border(1.dp, Color.White.copy(alpha = 0.1f), RoundedCornerShape(12.dp))
        .padding(10.dp)
        .testTag("ai-frame-generator"),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = copy.aiFrameGeneratorTitle,
        color = FrameWarmWhite,
        fontSize = 11.sp,
        lineHeight = 14.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.weight(1f),
      )
      if (frameGenerationState is FrameGenerationState.Ready) {
        Text(
          text = frameGenerationState.spec.title,
          color = FrameGreen,
          fontSize = 10.sp,
          lineHeight = 13.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }

    when (frameGenerationState) {
      FrameGenerationState.Idle -> Unit
      FrameGenerationState.Generating -> PhotoAiStatusText(copy.aiFrameGenerating)
      FrameGenerationState.Unavailable -> PhotoAiStatusText(copy.aiFrameUnavailable)
      is FrameGenerationState.Failed -> PhotoAiStatusText(copy.aiFrameGenerationFailed)
      is FrameGenerationState.Ready -> {
        val spec = frameGenerationState.spec
        Text(
          text = spec.reason.ifBlank { copy.aiFrameGeneratedLabel },
          color = FrameWarmWhite.copy(alpha = 0.7f),
          fontSize = 10.sp,
          lineHeight = 13.sp,
          maxLines = 2,
          overflow = TextOverflow.Ellipsis,
          modifier = Modifier.padding(top = 5.dp),
        )
      }
    }

    frameConversation.takeLast(2).forEach { message ->
      Text(
        text = message.text,
        color = FrameWarmWhite.copy(alpha = if (message.role == FrameConversationRole.User) 0.78f else 0.62f),
        fontSize = 10.sp,
        lineHeight = 13.sp,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier = Modifier.padding(top = 4.dp),
      )
    }

    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 8.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicTextField(
        value = promptText,
        onValueChange = { promptText = it.take(120) },
        enabled = !isGenerating,
        singleLine = true,
        textStyle =
          TextStyle(
            color = FrameWarmWhite,
            fontSize = 11.sp,
            lineHeight = 14.sp,
          ),
        decorationBox = { innerTextField ->
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(34.dp)
                .clip(RoundedCornerShape(11.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(11.dp))
                .padding(horizontal = 10.dp),
            contentAlignment = Alignment.CenterStart,
          ) {
            if (promptText.isBlank()) {
              Text(
                text = copy.aiFramePromptPlaceholder,
                color = FrameWarmWhite.copy(alpha = 0.46f),
                fontSize = 11.sp,
                lineHeight = 14.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
              )
            }
            innerTextField()
          }
        },
        modifier =
          Modifier
            .weight(1f)
            .testTag("ai-frame-prompt-input"),
      )

      Box(
        modifier =
          Modifier
            .height(34.dp)
            .testTag("ai-frame-generate-button")
            .clip(RoundedCornerShape(11.dp))
            .background(if (isGenerating) FrameWarmWhite.copy(alpha = 0.38f) else FrameWarmWhite)
            .clickable(enabled = !isGenerating) {
              onGenerateAiFrame(promptText.trim())
              promptText = ""
            }
            .padding(horizontal = 12.dp),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = buttonLabel,
          color = FrameBlack,
          fontSize = 11.sp,
          lineHeight = 14.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun PhotoAiTagSelectionRows(
  tags: List<String>,
  selectedTags: Set<String>,
  onTagToggled: (String) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier.testTag("photo-ai-selectable-tags"),
    verticalArrangement = Arrangement.spacedBy(5.dp),
  ) {
    tags.chunked(3).forEach { rowTags ->
      Row(
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalAlignment = Alignment.CenterVertically,
      ) {
        rowTags.forEach { tag ->
          val selected = tag in selectedTags
          Text(
            text = tag,
            color = if (selected) FrameBlack else FrameWarmWhite,
            fontSize = 10.sp,
            lineHeight = 12.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier =
              Modifier
                .clip(RoundedCornerShape(9.dp))
                .background(if (selected) FrameGreen else Color.White.copy(alpha = 0.12f))
                .border(1.dp, if (selected) FrameGreen else Color.White.copy(alpha = 0.14f), RoundedCornerShape(9.dp))
                .clickable { onTagToggled(tag) }
                .padding(horizontal = 7.dp, vertical = 4.dp)
                .testTag("photo-ai-selectable-tag"),
          )
        }
      }
    }
  }
}
