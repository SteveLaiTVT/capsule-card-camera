package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
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
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.capsulecardcamera.theme.CapsuleBlack
import com.example.capsulecardcamera.theme.CapsuleGreen
import com.example.capsulecardcamera.theme.CapsuleOrange
import com.example.capsulecardcamera.theme.CapsuleRed
import com.example.capsulecardcamera.theme.CapsuleSurfaceLight
import com.example.capsulecardcamera.theme.CapsuleWarmWhite
import kotlin.math.max
import kotlin.math.roundToInt

internal val FrameWarmWhite = CapsuleWarmWhite
internal val FrameCream = CapsuleSurfaceLight
internal val FrameBlack = CapsuleBlack
internal val FrameGreen = CapsuleGreen
internal val FrameRed = CapsuleRed
internal val FrameOrange = CapsuleOrange

internal data class CapturedPhoto(
  val id: Int,
  val bitmap: Bitmap,
  val frameStyle: PhotoFrameStyle,
  val captureStyle: PhotoCaptureStyle = PhotoCaptureStyle.Clean,
  val aiState: PhotoAiState = PhotoAiState.Idle,
  val selectedAiTags: Set<String> = emptySet(),
  val generatedFrameSpec: GeneratedFrameSpec? = null,
  val aiEnhancementApplied: Boolean = false,
  val frameGenerationState: FrameGenerationState = FrameGenerationState.Idle,
  val frameConversation: List<FrameConversationMessage> = emptyList(),
)

internal enum class PhotoFrameStyle(val label: String) {
  Stamp("Stamp"),
  Polaroid("Polaroid"),
  Film("Film"),
  ColorPop("Color"),
}

@Composable
internal fun FramedPhoto(
  photo: CapturedPhoto,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  PhotoFrame(
    bitmap = photo.bitmap,
    frameStyle = photo.frameStyle,
    generatedFrameSpec = photo.generatedFrameSpec,
    modifier = modifier,
    onClick = onClick,
  )
}

@Composable
internal fun FrameSettingsScreen(
  photo: CapturedPhoto?,
  selectedFrameStyle: PhotoFrameStyle,
  copy: CameraCopy,
  customFrameSpecs: List<GeneratedFrameSpec> = emptyList(),
  selectedGeneratedFrameSpec: GeneratedFrameSpec? = photo?.generatedFrameSpec?.takeIf { it.baseStyle == selectedFrameStyle },
  onFrameSelected: (PhotoFrameStyle) -> Unit,
  onGeneratedFrameSelected: (GeneratedFrameSpec) -> Unit = {},
  onApplyAiFrame: (PhotoFrameStyle) -> Unit = onFrameSelected,
  onAiTagToggled: (String) -> Unit = {},
  onGenerateAiFrame: (String) -> Unit = {},
  onClose: () -> Unit,
  onSave: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val activeGeneratedFrameSpec = selectedGeneratedFrameSpec?.takeIf { it.baseStyle == selectedFrameStyle }

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(FrameRed)
        .testTag("frame-settings-screen"),
  ) {
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

    Column(
      modifier =
        Modifier
          .fillMaxSize()
          .verticalScroll(rememberScrollState())
          .padding(start = 24.dp, top = topPadding + 18.dp, end = 24.dp)
          .padding(bottom = bottomPadding + 96.dp),
    ) {
      FrameSettingsTopBar(
        title = if (photo == null) copy.defaultFrameTitle else copy.photoFrameTitle,
        onClose = onClose,
      )

      Box(
        modifier =
          Modifier
            .fillMaxWidth()
            .height(360.dp)
            .padding(top = 18.dp),
        contentAlignment = Alignment.Center,
      ) {
        if (photo == null) {
          FramePreviewCard(
            frameStyle = selectedFrameStyle,
            generatedFrameSpec = activeGeneratedFrameSpec,
            modifier =
              Modifier
                .width(238.dp)
                .aspectRatio(0.78f),
          )
        } else {
          FramedPhoto(
            photo = photo.copy(frameStyle = selectedFrameStyle, generatedFrameSpec = activeGeneratedFrameSpec),
            modifier =
              Modifier
                .width(258.dp)
                .aspectRatio(0.78f),
          )
        }
      }

      Text(
        text = copy.frameStyleTitle,
        color = FrameWarmWhite,
        fontSize = 16.sp,
        lineHeight = 20.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp),
      )

      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
      ) {
        PhotoFrameStyle.entries.forEach { frameStyle ->
          FrameOptionCard(
            frameStyle = frameStyle,
            label = copy.frameStyleLabel(frameStyle),
            selected = activeGeneratedFrameSpec == null && frameStyle == selectedFrameStyle,
            onClick = { onFrameSelected(frameStyle) },
            modifier = Modifier.weight(1f),
          )
        }
      }

      GeneratedFrameLibrarySection(
        customFrameSpecs = customFrameSpecs,
        selectedGeneratedFrameSpec = activeGeneratedFrameSpec,
        copy = copy,
        onGeneratedFrameSelected = onGeneratedFrameSelected,
        modifier =
          Modifier
            .fillMaxWidth()
            .padding(top = 16.dp),
      )

      if (photo != null) {
        PhotoAiInsightPanel(
          aiState = photo.aiState,
          selectedTags = photo.selectedAiTags,
          selectedFrameStyle = selectedFrameStyle,
          copy = copy,
          onApplyAiFrame = onApplyAiFrame,
          onAiTagToggled = onAiTagToggled,
          frameGenerationState = photo.frameGenerationState,
          frameConversation = photo.frameConversation,
          onGenerateAiFrame = onGenerateAiFrame,
          modifier =
            Modifier
              .fillMaxWidth()
              .padding(top = 14.dp),
        )
      }

      Spacer(modifier = Modifier.height(18.dp))
    }

    SaveFramedPhotoButton(
      enabled = photo != null,
      enabledLabel = copy.savePhotoWithFrame,
      disabledLabel = copy.selectPhotoToSave,
      onClick = onSave,
      modifier =
        Modifier
          .align(Alignment.BottomCenter)
          .fillMaxWidth()
          .padding(start = 24.dp, end = 24.dp, bottom = bottomPadding + 18.dp),
    )
  }
}

@Composable
internal fun FrameSettingsTopBar(
  title: String,
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Row(
    modifier = modifier.fillMaxWidth(),
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Box(
      modifier =
        Modifier
          .size(44.dp)
          .clip(CircleShape)
          .background(Color.Black.copy(alpha = 0.18f))
          .border(1.dp, Color.White.copy(alpha = 0.14f), CircleShape)
          .clickable(onClick = onClose),
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.size(22.dp)) {
        drawLine(
          color = FrameWarmWhite,
          start = Offset(size.width * 0.65f, size.height * 0.2f),
          end = Offset(size.width * 0.35f, size.height * 0.5f),
          strokeWidth = 2.4.dp.toPx(),
          cap = StrokeCap.Round,
        )
        drawLine(
          color = FrameWarmWhite,
          start = Offset(size.width * 0.35f, size.height * 0.5f),
          end = Offset(size.width * 0.65f, size.height * 0.8f),
          strokeWidth = 2.4.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }

    Text(
      text = title,
      color = FrameWarmWhite,
      fontSize = 22.sp,
      lineHeight = 26.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      modifier = Modifier.weight(1f),
    )

    Spacer(modifier = Modifier.size(44.dp))
  }
}

@Composable
private fun FrameOptionCard(
  frameStyle: PhotoFrameStyle,
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) Color.White.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.1f))
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) FrameWarmWhite else Color.White.copy(alpha = 0.14f),
          shape = RoundedCornerShape(10.dp),
        )
        .clickable(onClick = onClick)
        .padding(horizontal = 7.dp, vertical = 8.dp),
    horizontalAlignment = Alignment.CenterHorizontally,
  ) {
    FramePreviewCard(
      frameStyle = frameStyle,
      modifier =
        Modifier
          .fillMaxWidth()
          .aspectRatio(0.78f),
    )
    Text(
      text = label,
      color = FrameWarmWhite,
      fontSize = 11.sp,
      lineHeight = 14.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      textAlign = TextAlign.Center,
      modifier = Modifier.padding(top = 7.dp),
    )
  }
}

@Composable
private fun SaveFramedPhotoButton(
  enabled: Boolean,
  enabledLabel: String,
  disabledLabel: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .height(56.dp)
        .clip(RoundedCornerShape(18.dp))
        .background(if (enabled) FrameBlack else FrameBlack.copy(alpha = 0.36f))
        .border(1.dp, Color.White.copy(alpha = if (enabled) 0.14f else 0.08f), RoundedCornerShape(18.dp))
        .clickable(enabled = enabled, onClick = onClick)
        .testTag("save-framed-photo-button"),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = if (enabled) enabledLabel else disabledLabel,
      color = if (enabled) FrameWarmWhite else FrameWarmWhite.copy(alpha = 0.52f),
      fontSize = 16.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
    )
  }
}

@Composable
internal fun FramePreviewCard(
  frameStyle: PhotoFrameStyle,
  generatedFrameSpec: GeneratedFrameSpec? = null,
  modifier: Modifier = Modifier,
) {
  PhotoFrame(
    bitmap = null,
    frameStyle = frameStyle,
    generatedFrameSpec = generatedFrameSpec,
    modifier = modifier,
    onClick = null,
  )
}

@Composable
private fun PhotoFrame(
  bitmap: Bitmap?,
  frameStyle: PhotoFrameStyle,
  generatedFrameSpec: GeneratedFrameSpec? = null,
  modifier: Modifier = Modifier,
  onClick: (() -> Unit)? = null,
) {
  val effectiveFrameStyle = generatedFrameSpec?.baseStyle ?: frameStyle
  val shape = remember(effectiveFrameStyle) { effectiveFrameStyle.shape() }
  val frameColor = generatedFrameSpec?.backgroundColor?.toGeneratedFrameColor(effectiveFrameStyle.frameColor()) ?: effectiveFrameStyle.frameColor()
  val borderColor = generatedFrameSpec?.inkColor?.toGeneratedFrameColor(effectiveFrameStyle.borderColor())?.copy(alpha = 0.28f) ?: effectiveFrameStyle.borderColor()
  val contentPadding = effectiveFrameStyle.contentPadding(generatedFrameSpec)
  val imageShape = effectiveFrameStyle.imageShape(generatedFrameSpec)
  val clickableModifier =
    if (onClick == null) {
      Modifier
    } else {
      Modifier.clickable(onClick = onClick)
    }

  Box(
    modifier =
      modifier
        .clip(shape)
        .background(frameColor)
        .border(1.dp, borderColor, shape)
        .then(clickableModifier),
  ) {
    FrameBackgroundDecoration(
      frameStyle = effectiveFrameStyle,
      generatedFrameSpec = generatedFrameSpec,
      modifier = Modifier.matchParentSize(),
    )

    if (bitmap == null) {
      FrameImageSlot(
        bitmap = null,
        generatedFrameSpec = generatedFrameSpec,
        contentPadding = contentPadding,
        imageShape = imageShape,
        modifier = Modifier.matchParentSize(),
      )
    } else {
      FrameImageSlot(
        bitmap = bitmap,
        generatedFrameSpec = generatedFrameSpec,
        contentPadding = contentPadding,
        imageShape = imageShape,
        modifier = Modifier.matchParentSize(),
      )
    }

    FrameForegroundDecoration(
      frameStyle = effectiveFrameStyle,
      generatedFrameSpec = generatedFrameSpec,
      modifier = Modifier.matchParentSize(),
    )
  }
}

@Composable
private fun FrameImageSlot(
  bitmap: Bitmap?,
  generatedFrameSpec: GeneratedFrameSpec?,
  contentPadding: PaddingValues,
  imageShape: Shape,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .padding(contentPadding)
        .clip(imageShape)
        .border(
          width = if (generatedFrameSpec == null) 1.dp else 1.4.dp,
          color =
            generatedFrameSpec
              ?.accentColor
              ?.toGeneratedFrameColor(FrameGreen)
              ?.copy(alpha = 0.26f)
              ?: Color.Black.copy(alpha = 0.08f),
          shape = imageShape,
        ),
  ) {
    if (bitmap == null) {
      FramePlaceholder(modifier = Modifier.matchParentSize())
    } else {
      Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = null,
        contentScale = ContentScale.Crop,
        modifier = Modifier.matchParentSize(),
      )
    }

    if (generatedFrameSpec != null) {
      GeneratedPhotoTreatmentOverlay(
        spec = generatedFrameSpec,
        modifier = Modifier.matchParentSize(),
      )
      GeneratedThemeImageOverlay(
        spec = generatedFrameSpec,
        modifier = Modifier.matchParentSize(),
      )
    }
  }
}

@Composable
private fun FramePlaceholder(modifier: Modifier = Modifier) {
  Box(
    modifier =
      modifier.background(
        Brush.linearGradient(
          colors =
            listOf(
              Color.White.copy(alpha = 0.95f),
              FrameGreen.copy(alpha = 0.46f),
              FrameOrange.copy(alpha = 0.58f),
            ),
        ),
      ),
  ) {
    Canvas(modifier = Modifier.fillMaxSize()) {
      drawCircle(
        color = Color.White.copy(alpha = 0.52f),
        radius = size.minDimension * 0.22f,
        center = Offset(size.width * 0.28f, size.height * 0.26f),
      )
      drawRoundRect(
        color = Color.Black.copy(alpha = 0.12f),
        topLeft = Offset(size.width * 0.24f, size.height * 0.52f),
        size = Size(size.width * 0.52f, size.height * 0.13f),
        cornerRadius = CornerRadius(18.dp.toPx()),
      )
    }
  }
}

@Composable
private fun FrameBackgroundDecoration(
  frameStyle: PhotoFrameStyle,
  generatedFrameSpec: GeneratedFrameSpec?,
  modifier: Modifier = Modifier,
) {
  if (frameStyle != PhotoFrameStyle.ColorPop && generatedFrameSpec == null) return

  Canvas(modifier = modifier) {
    if (frameStyle == PhotoFrameStyle.ColorPop) {
      drawCircle(
        color = FrameRed.copy(alpha = 0.5f),
        radius = size.minDimension * 0.4f,
        center = Offset(size.width * 0.08f, size.height * 0.1f),
      )
      drawCircle(
        color = FrameGreen.copy(alpha = 0.62f),
        radius = size.minDimension * 0.34f,
        center = Offset(size.width * 0.95f, size.height * 0.88f),
      )
    }
    if (generatedFrameSpec != null) {
      drawGeneratedFrameComposition(generatedFrameSpec)
      drawGeneratedFrameMotif(
        motif = generatedFrameSpec.motif,
        accentColor = generatedFrameSpec.accentColor.toGeneratedFrameColor(FrameGreen),
        inkColor = generatedFrameSpec.inkColor.toGeneratedFrameColor(FrameBlack),
      )
    }
  }
}

@Composable
private fun FrameForegroundDecoration(
  frameStyle: PhotoFrameStyle,
  generatedFrameSpec: GeneratedFrameSpec?,
  modifier: Modifier = Modifier,
) {
  Box(modifier = modifier) {
    when (frameStyle) {
      PhotoFrameStyle.Film -> {
        Canvas(modifier = Modifier.matchParentSize()) {
        val holeWidth = 4.dp.toPx()
        val holeHeight = 8.dp.toPx()
        val sideInset = 5.dp.toPx()
        val topInset = 12.dp.toPx()
        val availableHeight = size.height - topInset * 2f
        val count = max(4, (availableHeight / 28.dp.toPx()).roundToInt())
        val step = availableHeight / count

        repeat(count) { index ->
          val y = topInset + step * index + (step - holeHeight) / 2f
          drawRoundRect(
            color = FrameWarmWhite.copy(alpha = 0.82f),
            topLeft = Offset(sideInset, y),
            size = Size(holeWidth, holeHeight),
            cornerRadius = CornerRadius(2.dp.toPx()),
          )
          drawRoundRect(
            color = FrameWarmWhite.copy(alpha = 0.82f),
            topLeft = Offset(size.width - sideInset - holeWidth, y),
            size = Size(holeWidth, holeHeight),
            cornerRadius = CornerRadius(2.dp.toPx()),
          )
        }
      }
    }
      PhotoFrameStyle.Polaroid -> {
        Canvas(modifier = Modifier.matchParentSize()) {
        drawLine(
          color = Color.Black.copy(alpha = 0.07f),
          start = Offset(size.width * 0.28f, size.height - 15.dp.toPx()),
          end = Offset(size.width * 0.72f, size.height - 15.dp.toPx()),
          strokeWidth = 1.4.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }
      PhotoFrameStyle.ColorPop -> {
        Canvas(modifier = Modifier.matchParentSize()) {
        val stripeHeight = 9.dp.toPx()
        drawRoundRect(
          color = FrameOrange.copy(alpha = 0.82f),
          topLeft = Offset(0f, size.height - stripeHeight * 2.1f),
          size = Size(size.width, stripeHeight),
          cornerRadius = CornerRadius(stripeHeight, stripeHeight),
        )
        drawRoundRect(
          color = FrameGreen.copy(alpha = 0.88f),
          topLeft = Offset(size.width * 0.18f, size.height - stripeHeight * 3.2f),
          size = Size(size.width * 0.64f, stripeHeight),
          cornerRadius = CornerRadius(stripeHeight, stripeHeight),
        )
      }
    }
      PhotoFrameStyle.Stamp -> Unit
    }

    if (generatedFrameSpec != null) {
      Canvas(modifier = Modifier.matchParentSize()) {
        drawGeneratedEmbeddedFrameBridge(generatedFrameSpec)
      }
    }

    if (generatedFrameSpec != null && generatedFrameSpec.caption.isNotBlank()) {
      GeneratedFrameCaption(
        spec = generatedFrameSpec,
        frameStyle = frameStyle,
        modifier = Modifier.matchParentSize(),
      )
    }
  }
}

@Composable
private fun GeneratedPhotoTreatmentOverlay(
  spec: GeneratedFrameSpec,
  modifier: Modifier = Modifier,
) {
  if (spec.photoTreatment == GeneratedPhotoTreatment.Natural) return
  Canvas(modifier = modifier) {
    drawGeneratedPhotoTreatment(spec)
  }
}

@Composable
private fun GeneratedThemeImageOverlay(
  spec: GeneratedFrameSpec,
  modifier: Modifier = Modifier,
) {
  if (spec.themeOverlay == GeneratedFrameThemeOverlay.None) return
  Canvas(modifier = modifier) {
    drawGeneratedThemeOverlay(spec)
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGeneratedFrameComposition(spec: GeneratedFrameSpec) {
  val accentColor = spec.accentColor.toGeneratedFrameColor(FrameGreen)
  val inkColor = spec.inkColor.toGeneratedFrameColor(FrameBlack)
  when (spec.composition) {
    GeneratedFrameComposition.Classic -> Unit
    GeneratedFrameComposition.Offset -> {
      drawRoundRect(
        color = accentColor.copy(alpha = 0.22f),
        topLeft = Offset(size.width * 0.08f, size.height * 0.08f),
        size = Size(size.width * 0.42f, size.height * 0.1f),
        cornerRadius = CornerRadius(size.minDimension * 0.04f),
      )
      drawRoundRect(
        color = inkColor.copy(alpha = 0.12f),
        topLeft = Offset(size.width * 0.5f, size.height * 0.76f),
        size = Size(size.width * 0.38f, size.height * 0.08f),
        cornerRadius = CornerRadius(size.minDimension * 0.04f),
      )
    }
    GeneratedFrameComposition.Poster -> {
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(accentColor.copy(alpha = 0.38f), Color.Transparent),
            start = Offset(size.width, 0f),
            end = Offset(size.width * 0.2f, size.height * 0.55f),
          ),
      )
      drawRoundRect(
        color = inkColor.copy(alpha = 0.18f),
        topLeft = Offset(size.width * 0.12f, size.height * 0.86f),
        size = Size(size.width * 0.76f, size.height * 0.028f),
        cornerRadius = CornerRadius(size.minDimension * 0.02f),
      )
    }
    GeneratedFrameComposition.Portal -> {
      drawCircle(
        color = accentColor.copy(alpha = 0.24f),
        radius = size.minDimension * 0.42f,
        center = Offset(size.width * 0.5f, size.height * 0.5f),
        style = Stroke(width = size.minDimension * 0.035f),
      )
      drawCircle(
        color = Color.White.copy(alpha = 0.12f),
        radius = size.minDimension * 0.34f,
        center = Offset(size.width * 0.5f, size.height * 0.5f),
        style = Stroke(width = size.minDimension * 0.018f),
      )
    }
    GeneratedFrameComposition.Scrapbook -> {
      drawRoundRect(
        color = Color.White.copy(alpha = 0.32f),
        topLeft = Offset(size.width * 0.1f, size.height * 0.02f),
        size = Size(size.width * 0.26f, size.height * 0.07f),
        cornerRadius = CornerRadius(size.minDimension * 0.015f),
      )
      drawRoundRect(
        color = accentColor.copy(alpha = 0.36f),
        topLeft = Offset(size.width * 0.64f, size.height * 0.91f),
        size = Size(size.width * 0.26f, size.height * 0.06f),
        cornerRadius = CornerRadius(size.minDimension * 0.015f),
      )
    }
    GeneratedFrameComposition.Split -> {
      drawRect(
        color = inkColor.copy(alpha = 0.13f),
        topLeft = Offset(0f, 0f),
        size = Size(size.width * 0.18f, size.height),
      )
      drawRect(
        color = accentColor.copy(alpha = 0.22f),
        topLeft = Offset(size.width * 0.84f, 0f),
        size = Size(size.width * 0.16f, size.height),
      )
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGeneratedPhotoTreatment(spec: GeneratedFrameSpec) {
  val accentColor = spec.accentColor.toGeneratedFrameColor(FrameGreen)
  val backgroundColor = spec.backgroundColor.toGeneratedFrameColor(FrameCream)
  when (spec.photoTreatment) {
    GeneratedPhotoTreatment.Natural -> Unit
    GeneratedPhotoTreatment.WarmGlow ->
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(FrameOrange.copy(alpha = 0.2f), Color.Transparent, backgroundColor.copy(alpha = 0.14f)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
          ),
      )
    GeneratedPhotoTreatment.CoolFade ->
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(Color(0xFF2B6FD6).copy(alpha = 0.2f), Color.Transparent, Color.Black.copy(alpha = 0.14f)),
            start = Offset(size.width, 0f),
            end = Offset(0f, size.height),
          ),
      )
    GeneratedPhotoTreatment.Noir -> {
      drawRect(color = Color.Black.copy(alpha = 0.26f))
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(Color.White.copy(alpha = 0.1f), Color.Transparent, Color.Black.copy(alpha = 0.24f)),
            start = Offset(0f, 0f),
            end = Offset(size.width, size.height),
          ),
      )
    }
    GeneratedPhotoTreatment.PopTint -> {
      drawRect(color = accentColor.copy(alpha = 0.18f))
      drawCircle(
        color = Color.White.copy(alpha = 0.16f),
        radius = size.minDimension * 0.36f,
        center = Offset(size.width * 0.18f, size.height * 0.18f),
      )
    }
    GeneratedPhotoTreatment.DreamWash -> {
      drawRect(color = Color.White.copy(alpha = 0.14f))
      drawCircle(
        color = backgroundColor.copy(alpha = 0.24f),
        radius = size.minDimension * 0.5f,
        center = Offset(size.width * 0.82f, size.height * 0.16f),
      )
      drawCircle(
        color = accentColor.copy(alpha = 0.14f),
        radius = size.minDimension * 0.32f,
        center = Offset(size.width * 0.12f, size.height * 0.88f),
      )
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGeneratedThemeOverlay(spec: GeneratedFrameSpec) {
  val accentColor = spec.accentColor.toGeneratedFrameColor(FrameGreen)
  val inkColor = spec.inkColor.toGeneratedFrameColor(FrameBlack)
  when (spec.themeOverlay) {
    GeneratedFrameThemeOverlay.None -> Unit
    GeneratedFrameThemeOverlay.Ribbon -> {
      drawLine(
        color = accentColor.copy(alpha = 0.62f),
        start = Offset(-size.width * 0.08f, size.height * 0.72f),
        end = Offset(size.width * 1.08f, size.height * 0.55f),
        strokeWidth = size.minDimension * 0.08f,
        cap = StrokeCap.Round,
      )
      drawLine(
        color = inkColor.copy(alpha = 0.2f),
        start = Offset(size.width * 0.12f, size.height * 0.76f),
        end = Offset(size.width * 0.78f, size.height * 0.64f),
        strokeWidth = size.minDimension * 0.012f,
        cap = StrokeCap.Round,
      )
    }
    GeneratedFrameThemeOverlay.CornerBloom -> {
      repeat(4) { index ->
        drawCircle(
          color = accentColor.copy(alpha = 0.42f - index * 0.05f),
          radius = size.minDimension * (0.08f + index * 0.025f),
          center = Offset(size.width * (0.04f + index * 0.06f), size.height * (0.08f + index * 0.035f)),
        )
      }
      drawLine(
        color = inkColor.copy(alpha = 0.22f),
        start = Offset(0f, size.height * 0.24f),
        end = Offset(size.width * 0.32f, 0f),
        strokeWidth = size.minDimension * 0.012f,
        cap = StrokeCap.Round,
      )
    }
    GeneratedFrameThemeOverlay.LightLeak ->
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(FrameOrange.copy(alpha = 0.46f), accentColor.copy(alpha = 0.12f), Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.78f, size.height * 0.65f),
          ),
      )
    GeneratedFrameThemeOverlay.FilmBurn -> {
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(FrameRed.copy(alpha = 0.46f), FrameOrange.copy(alpha = 0.28f), Color.Transparent),
            start = Offset(size.width, 0f),
            end = Offset(size.width * 0.25f, size.height),
          ),
      )
    }
    GeneratedFrameThemeOverlay.StickerTrail -> {
      repeat(6) { index ->
        val center =
          Offset(
            x = size.width * (0.1f + index * 0.15f),
            y = size.height * (0.18f + (index % 3) * 0.12f),
          )
        drawCircle(color = accentColor.copy(alpha = 0.42f), radius = size.minDimension * 0.035f, center = center)
        drawLine(
          color = Color.White.copy(alpha = 0.48f),
          start = Offset(center.x - size.minDimension * 0.025f, center.y),
          end = Offset(center.x + size.minDimension * 0.025f, center.y),
          strokeWidth = size.minDimension * 0.006f,
          cap = StrokeCap.Round,
        )
      }
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGeneratedEmbeddedFrameBridge(spec: GeneratedFrameSpec) {
  val accentColor = spec.accentColor.toGeneratedFrameColor(FrameGreen)
  val inkColor = spec.inkColor.toGeneratedFrameColor(FrameBlack)
  when (spec.themeOverlay) {
    GeneratedFrameThemeOverlay.None -> Unit
    GeneratedFrameThemeOverlay.Ribbon -> {
      drawLine(
        color = accentColor.copy(alpha = 0.46f),
        start = Offset(-size.width * 0.08f, size.height * 0.63f),
        end = Offset(size.width * 1.08f, size.height * 0.5f),
        strokeWidth = size.minDimension * 0.035f,
        cap = StrokeCap.Round,
      )
    }
    GeneratedFrameThemeOverlay.CornerBloom -> {
      drawCircle(
        color = accentColor.copy(alpha = 0.26f),
        radius = size.minDimension * 0.18f,
        center = Offset(size.width * 0.13f, size.height * 0.13f),
      )
      drawCircle(
        color = FrameWarmWhite.copy(alpha = 0.18f),
        radius = size.minDimension * 0.12f,
        center = Offset(size.width * 0.2f, size.height * 0.18f),
      )
    }
    GeneratedFrameThemeOverlay.LightLeak -> {
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(accentColor.copy(alpha = 0.32f), Color.Transparent),
            start = Offset(0f, 0f),
            end = Offset(size.width * 0.62f, size.height * 0.48f),
          ),
      )
    }
    GeneratedFrameThemeOverlay.FilmBurn -> {
      drawRect(
        brush =
          Brush.linearGradient(
            colors = listOf(FrameOrange.copy(alpha = 0.3f), FrameRed.copy(alpha = 0.18f), Color.Transparent),
            start = Offset(size.width, 0f),
            end = Offset(size.width * 0.28f, size.height),
          ),
      )
    }
    GeneratedFrameThemeOverlay.StickerTrail -> {
      repeat(4) { index ->
        val center =
          Offset(
            x = size.width * (0.18f + index * 0.19f),
            y = size.height * (0.11f + (index % 2) * 0.08f),
          )
        drawCircle(color = accentColor.copy(alpha = 0.34f), radius = size.minDimension * 0.028f, center = center)
        drawCircle(color = inkColor.copy(alpha = 0.08f), radius = size.minDimension * 0.038f, center = center, style = Stroke(width = 1.dp.toPx()))
      }
    }
  }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawGeneratedFrameMotif(
  motif: GeneratedFrameMotif,
  accentColor: Color,
  inkColor: Color,
) {
  when (motif) {
    GeneratedFrameMotif.None -> Unit
    GeneratedFrameMotif.Dots -> {
      val dotRadius = size.minDimension * 0.018f
      repeat(8) { index ->
        val x = size.width * (0.1f + (index % 4) * 0.26f)
        val y = if (index < 4) size.height * 0.08f else size.height * 0.91f
        drawCircle(color = accentColor.copy(alpha = 0.62f), radius = dotRadius, center = Offset(x, y))
      }
    }
    GeneratedFrameMotif.Sparkles -> {
      repeat(5) { index ->
        val center =
          Offset(
            x = size.width * (0.14f + (index % 3) * 0.34f),
            y = if (index % 2 == 0) size.height * 0.12f else size.height * 0.88f,
          )
        val radius = size.minDimension * 0.026f
        drawLine(
          color = accentColor.copy(alpha = 0.78f),
          start = Offset(center.x - radius, center.y),
          end = Offset(center.x + radius, center.y),
          strokeWidth = 1.6.dp.toPx(),
          cap = StrokeCap.Round,
        )
        drawLine(
          color = accentColor.copy(alpha = 0.78f),
          start = Offset(center.x, center.y - radius),
          end = Offset(center.x, center.y + radius),
          strokeWidth = 1.6.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }
    GeneratedFrameMotif.Waves -> {
      repeat(3) { index ->
        val y = size.height * (0.1f + index * 0.035f)
        drawLine(
          color = accentColor.copy(alpha = 0.46f),
          start = Offset(size.width * 0.08f, y),
          end = Offset(size.width * 0.92f, y + size.height * 0.025f),
          strokeWidth = 2.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }
    GeneratedFrameMotif.Leaves -> {
      repeat(5) { index ->
        val center =
          Offset(
            x = if (index % 2 == 0) size.width * 0.12f else size.width * 0.88f,
            y = size.height * (0.14f + index * 0.16f),
          )
        drawOval(
          color = accentColor.copy(alpha = 0.5f),
          topLeft = Offset(center.x - size.width * 0.025f, center.y - size.height * 0.012f),
          size = Size(size.width * 0.05f, size.height * 0.024f),
        )
      }
    }
    GeneratedFrameMotif.Lines -> {
      repeat(5) { index ->
        val x = size.width * (0.12f + index * 0.19f)
        drawLine(
          color = inkColor.copy(alpha = 0.2f),
          start = Offset(x, size.height * 0.04f),
          end = Offset(x + size.width * 0.06f, size.height * 0.15f),
          strokeWidth = 1.4.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }
  }
}

@Composable
private fun GeneratedFrameCaption(
  spec: GeneratedFrameSpec,
  frameStyle: PhotoFrameStyle,
  modifier: Modifier = Modifier,
) {
  val bottomPadding =
    when (frameStyle) {
      PhotoFrameStyle.Film -> 6.dp
      PhotoFrameStyle.Polaroid -> 11.dp
      PhotoFrameStyle.ColorPop -> 8.dp
      PhotoFrameStyle.Stamp -> 7.dp
    }
  Box(
    modifier = modifier.padding(horizontal = 14.dp, vertical = bottomPadding),
    contentAlignment = Alignment.BottomCenter,
  ) {
    Text(
      text = spec.caption,
      color = spec.inkColor.toGeneratedFrameColor(if (frameStyle == PhotoFrameStyle.Film) FrameWarmWhite else FrameBlack),
      fontSize = if (frameStyle == PhotoFrameStyle.Film) 8.sp else 10.sp,
      lineHeight = if (frameStyle == PhotoFrameStyle.Film) 10.sp else 12.sp,
      fontWeight = FontWeight.Bold,
      textAlign = TextAlign.Center,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      modifier =
        Modifier
          .clip(RoundedCornerShape(6.dp))
          .background(spec.backgroundColor.toGeneratedFrameColor(Color.Transparent).copy(alpha = if (frameStyle == PhotoFrameStyle.Film) 0.18f else 0f))
          .padding(horizontal = 6.dp, vertical = 2.dp),
    )
  }
}

private fun PhotoFrameStyle.shape(): Shape =
  when (this) {
    PhotoFrameStyle.Stamp -> StampStickerShape()
    PhotoFrameStyle.Polaroid -> RoundedCornerShape(10.dp)
    PhotoFrameStyle.Film -> RoundedCornerShape(8.dp)
    PhotoFrameStyle.ColorPop -> RoundedCornerShape(12.dp)
  }

private fun PhotoFrameStyle.imageShape(generatedFrameSpec: GeneratedFrameSpec?): Shape =
  when (generatedFrameSpec?.composition) {
    GeneratedFrameComposition.Portal -> RoundedCornerShape(24.dp)
    GeneratedFrameComposition.Scrapbook -> RoundedCornerShape(topStart = 15.dp, topEnd = 6.dp, bottomEnd = 18.dp, bottomStart = 8.dp)
    GeneratedFrameComposition.Split -> RoundedCornerShape(topStart = 2.dp, topEnd = 16.dp, bottomEnd = 8.dp, bottomStart = 2.dp)
    else ->
      when (this) {
        PhotoFrameStyle.Stamp -> RoundedCornerShape(4.dp)
        PhotoFrameStyle.Polaroid -> RoundedCornerShape(5.dp)
        PhotoFrameStyle.Film -> RoundedCornerShape(3.dp)
        PhotoFrameStyle.ColorPop -> RoundedCornerShape(5.dp)
      }
  }

private fun PhotoFrameStyle.contentPadding(generatedFrameSpec: GeneratedFrameSpec?): PaddingValues {
  val composition = generatedFrameSpec?.composition ?: GeneratedFrameComposition.Classic
  return when (composition) {
    GeneratedFrameComposition.Classic ->
      when (this) {
        PhotoFrameStyle.Stamp -> PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 18.dp)
        PhotoFrameStyle.Polaroid -> PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 30.dp)
        PhotoFrameStyle.Film -> PaddingValues(start = 18.dp, top = 10.dp, end = 18.dp, bottom = 10.dp)
        PhotoFrameStyle.ColorPop -> PaddingValues(start = 8.dp, top = 8.dp, end = 8.dp, bottom = 22.dp)
      }
    GeneratedFrameComposition.Offset -> PaddingValues(start = 11.dp, top = 7.dp, end = 7.dp, bottom = 24.dp)
    GeneratedFrameComposition.Poster -> PaddingValues(start = 8.dp, top = 6.dp, end = 8.dp, bottom = 30.dp)
    GeneratedFrameComposition.Portal -> PaddingValues(start = 10.dp, top = 10.dp, end = 10.dp, bottom = 16.dp)
    GeneratedFrameComposition.Scrapbook -> PaddingValues(start = 14.dp, top = 10.dp, end = 7.dp, bottom = 26.dp)
    GeneratedFrameComposition.Split -> PaddingValues(start = 18.dp, top = 8.dp, end = 8.dp, bottom = 18.dp)
  }
}

internal fun PhotoFrameStyle.frameColor(): Color =
  when (this) {
    PhotoFrameStyle.Stamp -> FrameWarmWhite
    PhotoFrameStyle.Polaroid -> FrameCream
    PhotoFrameStyle.Film -> FrameBlack
    PhotoFrameStyle.ColorPop -> FrameOrange
  }

private fun PhotoFrameStyle.borderColor(): Color =
  when (this) {
    PhotoFrameStyle.Stamp -> Color.Black.copy(alpha = 0.1f)
    PhotoFrameStyle.Polaroid -> Color.Black.copy(alpha = 0.1f)
    PhotoFrameStyle.Film -> Color.White.copy(alpha = 0.16f)
    PhotoFrameStyle.ColorPop -> Color.Black.copy(alpha = 0.1f)
  }

internal fun String.toGeneratedFrameColor(fallback: Color): Color =
  when (trim().lowercase()) {
    "warmwhite" -> FrameWarmWhite
    "cream" -> FrameCream
    "black" -> FrameBlack
    "green" -> FrameGreen
    "red" -> FrameRed
    "orange" -> FrameOrange
    "blue" -> Color(0xFF2876D9)
    "pink" -> Color(0xFFF04D86)
    "purple" -> Color(0xFF7B55D9)
    "teal" -> Color(0xFF1F9E99)
    "yellow" -> Color(0xFFF2CC38)
    "white" -> Color.White
    "gray", "grey" -> Color(0xFF777777)
    else -> fallback
  }

private class StampStickerShape(
  private val toothSize: Dp = 8.dp,
  private val toothDepth: Dp = 3.5.dp,
) : Shape {
  override fun createOutline(
    size: Size,
    layoutDirection: LayoutDirection,
    density: Density,
  ): Outline {
    if (size.width <= 0f || size.height <= 0f) {
      return Outline.Generic(Path())
    }

    val depthPx =
      with(density) { toothDepth.toPx() }
        .coerceAtLeast(1f)
        .coerceAtMost(size.minDimension / 5f)
    val toothPx = with(density) { toothSize.toPx() }.coerceAtLeast(depthPx * 2f)
    val horizontalTeeth = max(4, (size.width / toothPx).roundToInt())
    val verticalTeeth = max(4, (size.height / toothPx).roundToInt())

    return Outline.Generic(
      Path().apply {
        moveTo(0f, 0f)
        addStampHorizontalEdge(
          startX = 0f,
          endX = size.width,
          outerY = 0f,
          innerY = depthPx,
          teeth = horizontalTeeth,
        )
        addStampVerticalEdge(
          startY = 0f,
          endY = size.height,
          outerX = size.width,
          innerX = size.width - depthPx,
          teeth = verticalTeeth,
        )
        addStampHorizontalEdge(
          startX = size.width,
          endX = 0f,
          outerY = size.height,
          innerY = size.height - depthPx,
          teeth = horizontalTeeth,
        )
        addStampVerticalEdge(
          startY = size.height,
          endY = 0f,
          outerX = 0f,
          innerX = depthPx,
          teeth = verticalTeeth,
        )
        close()
      },
    )
  }
}

private fun Path.addStampHorizontalEdge(
  startX: Float,
  endX: Float,
  outerY: Float,
  innerY: Float,
  teeth: Int,
) {
  val step = (endX - startX) / teeth
  for (index in 0 until teeth) {
    val segmentStart = startX + step * index
    val segmentEnd = startX + step * (index + 1)
    lineTo((segmentStart + segmentEnd) / 2f, innerY)
    lineTo(segmentEnd, outerY)
  }
}

private fun Path.addStampVerticalEdge(
  startY: Float,
  endY: Float,
  outerX: Float,
  innerX: Float,
  teeth: Int,
) {
  val step = (endY - startY) / teeth
  for (index in 0 until teeth) {
    val segmentStart = startY + step * index
    val segmentEnd = startY + step * (index + 1)
    lineTo(innerX, (segmentStart + segmentEnd) / 2f)
    lineTo(outerX, segmentEnd)
  }
}
