package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
internal fun FrameManagementScreen(
  customFrameSpecs: List<GeneratedFrameSpec>,
  selectedGeneratedFrameSpec: GeneratedFrameSpec?,
  frameGenerationState: FrameGenerationState,
  copy: CameraCopy,
  onGenerateFrame: (String) -> Unit,
  onFrameSelected: (GeneratedFrameSpec) -> Unit,
  onSetDefaultFrame: (GeneratedFrameSpec) -> Unit,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val topBarTop = topPadding + OverlayTopBarTopInset
  val topBarHeight = 44.dp
  var promptText by remember(customFrameSpecs.size) { mutableStateOf("") }
  val previewFrameSpec =
    when (frameGenerationState) {
      is FrameGenerationState.Ready -> frameGenerationState.spec
      else -> selectedGeneratedFrameSpec ?: customFrameSpecs.firstOrNull()
    }
  val isGenerating = frameGenerationState == FrameGenerationState.Generating

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(FrameRed)
        .testTag("frame-management-screen"),
  ) {
    FrameScreenBackdrop()

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(start = 24.dp, top = topBarTop + topBarHeight, end = 24.dp)
          .padding(bottom = bottomPadding + 28.dp),
    ) {
      FrameManagerHeroPreview(
        previewFrameSpec = previewFrameSpec,
        modifier =
          Modifier
            .fillMaxWidth()
            .height(270.dp)
            .padding(top = 18.dp),
      )

      FrameManagerPromptPanel(
        promptText = promptText,
        isGenerating = isGenerating,
        copy = copy,
        onPromptChange = { promptText = it },
        onGenerateFrame = {
          onGenerateFrame(promptText.trim())
          promptText = ""
        },
      )

      Text(
        text = copy.myFramesTitle,
        color = FrameWarmWhite,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 18.dp, bottom = 10.dp),
      )

      if (customFrameSpecs.isEmpty()) {
        FrameManagerEmptyState(copy = copy)
      } else {
        FrameManagerLibraryGrid(
          customFrameSpecs = customFrameSpecs,
          selectedGeneratedFrameSpec = selectedGeneratedFrameSpec,
          copy = copy,
          onFrameSelected = onFrameSelected,
          onSetDefaultFrame = onSetDefaultFrame,
        )
      }
    }

    FrameSettingsTopBar(
      title = copy.myFramesTitle,
      onClose = onClose,
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(start = 24.dp, top = topBarTop, end = 24.dp)
          .testTag("frame-manager-top-bar"),
    )
  }
}

@Composable
internal fun GeneratedFrameLibrarySection(
  customFrameSpecs: List<GeneratedFrameSpec>,
  selectedGeneratedFrameSpec: GeneratedFrameSpec?,
  copy: CameraCopy,
  onGeneratedFrameSelected: (GeneratedFrameSpec) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (customFrameSpecs.isEmpty()) return

  Column(modifier = modifier.testTag("my-frames-section")) {
    Text(
      text = copy.myFramesTitle,
      color = FrameWarmWhite,
      fontSize = 16.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 10.dp),
    )

    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
      customFrameSpecs.take(6).chunked(2).forEach { rowSpecs ->
        Row(
          modifier = Modifier.fillMaxWidth(),
          horizontalArrangement = Arrangement.spacedBy(10.dp),
        ) {
          rowSpecs.forEach { spec ->
            GeneratedFrameOptionCard(
              spec = spec,
              selected = spec == selectedGeneratedFrameSpec,
              copy = copy,
              onClick = { onGeneratedFrameSelected(spec) },
              modifier = Modifier.weight(1f),
            )
          }
          if (rowSpecs.size == 1) {
            Spacer(modifier = Modifier.weight(1f))
          }
        }
      }
    }
  }
}

@Composable
private fun FrameScreenBackdrop() {
  Canvas(modifier = Modifier.fillMaxSize()) {
    drawCircle(
      color = Color.White.copy(alpha = 0.08f),
      radius = size.minDimension * 0.52f,
      center = Offset(-size.width * 0.05f, size.height * 0.18f),
    )
    drawCircle(
      color = Color.Black.copy(alpha = 0.05f),
      radius = size.minDimension * 0.42f,
      center = Offset(size.width * 0.96f, size.height * 0.9f),
    )
  }
}

@Composable
private fun FrameManagerHeroPreview(
  previewFrameSpec: GeneratedFrameSpec?,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier = modifier,
    contentAlignment = Alignment.Center,
  ) {
    if (previewFrameSpec == null) {
      FramePreviewCard(
        frameStyle = PhotoFrameStyle.Stamp,
        modifier =
          Modifier
            .width(190.dp)
            .aspectRatio(0.78f),
      )
    } else {
      FramePreviewCard(
        frameStyle = previewFrameSpec.baseStyle,
        generatedFrameSpec = previewFrameSpec,
        modifier =
          Modifier
            .width(204.dp)
            .aspectRatio(0.78f),
      )
    }
  }
}

@Composable
private fun FrameManagerPromptPanel(
  promptText: String,
  isGenerating: Boolean,
  copy: CameraCopy,
  onPromptChange: (String) -> Unit,
  onGenerateFrame: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(14.dp))
        .background(Color.Black.copy(alpha = 0.14f))
        .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(14.dp))
        .padding(12.dp),
  ) {
    Text(
      text = copy.frameManagerDescription,
      color = FrameWarmWhite.copy(alpha = 0.78f),
      fontSize = 11.sp,
      lineHeight = 15.sp,
    )
    Row(
      modifier =
        Modifier
          .fillMaxWidth()
          .padding(top = 10.dp),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      BasicTextField(
        value = promptText,
        onValueChange = { onPromptChange(it.take(140)) },
        enabled = !isGenerating,
        singleLine = true,
        textStyle =
          TextStyle(
            color = FrameWarmWhite,
            fontSize = 12.sp,
            lineHeight = 15.sp,
          ),
        decorationBox = { innerTextField ->
          Box(
            modifier =
              Modifier
                .fillMaxWidth()
                .height(42.dp)
                .clip(RoundedCornerShape(12.dp))
                .background(Color.White.copy(alpha = 0.1f))
                .border(1.dp, Color.White.copy(alpha = 0.12f), RoundedCornerShape(12.dp))
                .padding(horizontal = 12.dp),
            contentAlignment = Alignment.CenterStart,
          ) {
            if (promptText.isBlank()) {
              Text(
                text = copy.frameManagerPromptPlaceholder,
                color = FrameWarmWhite.copy(alpha = 0.46f),
                fontSize = 12.sp,
                lineHeight = 15.sp,
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
            .testTag("frame-manager-prompt-input"),
      )
      Box(
        modifier =
          Modifier
            .height(42.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(if (isGenerating) FrameWarmWhite.copy(alpha = 0.38f) else FrameWarmWhite)
            .clickable(enabled = !isGenerating, onClick = onGenerateFrame)
            .padding(horizontal = 13.dp)
            .testTag("frame-manager-generate-button"),
        contentAlignment = Alignment.Center,
      ) {
        Text(
          text = if (isGenerating) copy.frameManagerGeneratingLabel else copy.frameManagerGenerateLabel,
          color = FrameBlack,
          fontSize = 12.sp,
          lineHeight = 15.sp,
          fontWeight = FontWeight.Bold,
          maxLines = 1,
          overflow = TextOverflow.Ellipsis,
        )
      }
    }
  }
}

@Composable
private fun FrameManagerEmptyState(
  copy: CameraCopy,
  modifier: Modifier = Modifier,
) {
  Text(
    text = copy.frameManagerEmptyLabel,
    color = FrameWarmWhite.copy(alpha = 0.68f),
    fontSize = 12.sp,
    lineHeight = 16.sp,
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(12.dp))
        .background(Color.Black.copy(alpha = 0.12f))
        .padding(14.dp),
  )
}

@Composable
private fun FrameManagerLibraryGrid(
  customFrameSpecs: List<GeneratedFrameSpec>,
  selectedGeneratedFrameSpec: GeneratedFrameSpec?,
  copy: CameraCopy,
  onFrameSelected: (GeneratedFrameSpec) -> Unit,
  onSetDefaultFrame: (GeneratedFrameSpec) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier = modifier,
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    customFrameSpecs.chunked(2).forEach { rowSpecs ->
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        rowSpecs.forEach { spec ->
          FrameManagerOptionCard(
            spec = spec,
            selected = spec == selectedGeneratedFrameSpec,
            isDefault = spec == selectedGeneratedFrameSpec,
            copy = copy,
            onClick = { onFrameSelected(spec) },
            onSetDefault = { onSetDefaultFrame(spec) },
            modifier = Modifier.weight(1f),
          )
        }
        if (rowSpecs.size == 1) {
          Spacer(modifier = Modifier.weight(1f))
        }
      }
    }
  }
}

@Composable
private fun GeneratedFrameOptionCard(
  spec: GeneratedFrameSpec,
  selected: Boolean,
  copy: CameraCopy,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .testTag("my-frame-option")
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f))
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) FrameGreen else Color.White.copy(alpha = 0.14f),
          shape = RoundedCornerShape(10.dp),
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 8.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FramePreviewCard(
      frameStyle = spec.baseStyle,
      generatedFrameSpec = spec,
      modifier =
        Modifier
          .fillMaxWidth()
          .aspectRatio(0.78f),
    )
    Text(
      text = spec.title,
      color = FrameWarmWhite,
      fontSize = 11.sp,
      lineHeight = 14.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(top = 7.dp),
    )
    Text(
      text = if (selected) copy.myFrameSelectedLabel else copy.frameStyleLabel(spec.baseStyle),
      color = if (selected) FrameGreen else FrameWarmWhite.copy(alpha = 0.65f),
      fontSize = 9.sp,
      lineHeight = 12.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(top = 2.dp),
    )
  }
}

@Composable
private fun FrameManagerOptionCard(
  spec: GeneratedFrameSpec,
  selected: Boolean,
  isDefault: Boolean,
  copy: CameraCopy,
  onClick: () -> Unit,
  onSetDefault: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .testTag("frame-manager-frame-option")
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) Color.White.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.1f))
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) FrameGreen else Color.White.copy(alpha = 0.14f),
          shape = RoundedCornerShape(10.dp),
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 8.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FramePreviewCard(
      frameStyle = spec.baseStyle,
      generatedFrameSpec = spec,
      modifier =
        Modifier
          .fillMaxWidth()
          .aspectRatio(0.78f),
    )
    Text(
      text = spec.title,
      color = FrameWarmWhite,
      fontSize = 11.sp,
      lineHeight = 14.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier = Modifier.padding(top = 7.dp),
    )
    Box(
      modifier =
        Modifier
          .fillMaxWidth()
          .height(30.dp)
          .padding(top = 6.dp)
          .clip(RoundedCornerShape(9.dp))
          .background(if (isDefault) FrameGreen else FrameWarmWhite)
          .clickable(enabled = !isDefault, onClick = onSetDefault),
      contentAlignment = Alignment.Center,
    ) {
      Text(
        text = if (isDefault) copy.frameManagerDefaultLabel else copy.frameManagerUseAsDefaultLabel,
        color = FrameBlack,
        fontSize = 10.sp,
        lineHeight = 12.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
      )
    }
  }
}
