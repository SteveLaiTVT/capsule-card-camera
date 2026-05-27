package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val PhotoTagChipColor = Color(0xFFFFF4E8).copy(alpha = 0.9f)

@Composable
internal fun PhotoWall(
  photos: List<CapturedPhoto>,
  gridHeight: Dp,
  columns: Int = 3,
  horizontalPadding: Dp = 28.dp,
  itemAspectRatio: Float = 0.78f,
  selectedPhotoIds: Set<Int>,
  selectionMode: Boolean,
  onPhotoClick: (CapturedPhoto) -> Unit,
  onPhotoLongClick: (CapturedPhoto) -> Unit,
  modifier: Modifier = Modifier,
) {
  if (photos.isEmpty()) return

  LazyVerticalGrid(
    columns = GridCells.Fixed(columns),
    modifier =
      modifier
        .fillMaxWidth()
        .height(gridHeight)
        .testTag("photo-wall"),
    contentPadding = PaddingValues(horizontal = horizontalPadding, vertical = 4.dp),
    horizontalArrangement = Arrangement.spacedBy(12.dp),
    verticalArrangement = Arrangement.spacedBy(12.dp),
    userScrollEnabled = photos.size > 3,
  ) {
    items(items = photos, key = { it.id }) { photo ->
      val selected = photo.id in selectedPhotoIds
      Box(
        modifier =
          Modifier
            .animateItem()
            .fillMaxWidth()
            .aspectRatio(itemAspectRatio)
            .pointerInput(photo.id, selectionMode, selected) {
              detectTapGestures(
                onTap = { onPhotoClick(photo) },
                onLongPress = { onPhotoLongClick(photo) },
              )
            },
      ) {
        FramedPhoto(
          photo = photo,
          modifier = Modifier.matchParentSize(),
        )
        if (selectionMode) {
          PhotoSelectionScrim(selected = selected, modifier = Modifier.matchParentSize())
        }
        PhotoAiTagChips(
          aiState = photo.aiState,
          selectedTags = photo.selectedAiTags,
          modifier =
            Modifier
              .align(Alignment.BottomStart)
              .padding(7.dp),
        )
      }
    }
  }
}

@Composable
private fun PhotoSelectionScrim(
  selected: Boolean,
  modifier: Modifier = Modifier,
) {
  Box(
    modifier =
      modifier
        .clip(RoundedCornerShape(8.dp))
        .background(Color.Black.copy(alpha = if (selected) 0.2f else 0.42f))
        .border(
          width = if (selected) 2.dp else 1.dp,
          color = if (selected) FrameGreen else FrameWarmWhite.copy(alpha = 0.28f),
          shape = RoundedCornerShape(8.dp),
        )
        .testTag(if (selected) "photo-selection-selected" else "photo-selection-unselected"),
    contentAlignment = Alignment.TopEnd,
  ) {
    Box(
      modifier =
        Modifier
          .padding(7.dp)
          .size(22.dp)
          .clip(CircleShape)
          .background(if (selected) FrameGreen else FrameBlack.copy(alpha = 0.66f))
          .border(1.dp, FrameWarmWhite.copy(alpha = 0.74f), CircleShape),
      contentAlignment = Alignment.Center,
    ) {
      if (selected) {
        Text(
          text = "✓",
          color = FrameBlack,
          fontSize = 14.sp,
          lineHeight = 14.sp,
          fontWeight = FontWeight.Bold,
        )
      }
    }
  }
}

@Composable
internal fun PhotoSelectionToolbar(
  selectedCount: Int,
  selectedFrameStyle: PhotoFrameStyle?,
  copy: CameraCopy,
  onClearSelection: () -> Unit,
  onSaveSelected: () -> Unit,
  onDeleteSelected: () -> Unit,
  onFrameSelected: (PhotoFrameStyle) -> Unit,
  modifier: Modifier = Modifier,
) {
  Column(
    modifier =
      modifier
        .fillMaxWidth()
        .clip(RoundedCornerShape(18.dp))
        .background(FrameBlack.copy(alpha = 0.94f))
        .border(1.dp, FrameWarmWhite.copy(alpha = 0.16f), RoundedCornerShape(18.dp))
        .padding(12.dp)
        .testTag("photo-selection-toolbar"),
    verticalArrangement = Arrangement.spacedBy(10.dp),
  ) {
    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(10.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      Text(
        text = copy.selectedPhotosTemplate.format(selectedCount),
        color = FrameWarmWhite,
        fontSize = 14.sp,
        lineHeight = 18.sp,
        fontWeight = FontWeight.Bold,
        modifier = Modifier.weight(1f),
      )
      PhotoSelectionActionChip(label = copy.saveSelectedPhotos, onClick = onSaveSelected, testTag = "save-selected-photos")
      PhotoSelectionActionChip(label = copy.deleteSelectedPhotos, onClick = onDeleteSelected, destructive = true, testTag = "delete-selected-photos")
      PhotoSelectionActionChip(label = copy.clearSelectionLabel, onClick = onClearSelection, testTag = "clear-photo-selection")
    }

    Row(
      modifier = Modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically,
    ) {
      PhotoFrameStyle.entries.forEach { frameStyle ->
        PhotoFrameSelectionChip(
          label = copy.frameStyleLabel(frameStyle),
          selected = selectedFrameStyle == frameStyle,
          onClick = { onFrameSelected(frameStyle) },
          modifier = Modifier.weight(1f),
        )
      }
    }
  }
}

@Composable
private fun PhotoSelectionActionChip(
  label: String,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  destructive: Boolean = false,
  testTag: String? = null,
) {
  Text(
    text = label,
    color = if (destructive) FrameWarmWhite else FrameBlack,
    fontSize = 12.sp,
    lineHeight = 14.sp,
    fontWeight = FontWeight.Bold,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    textAlign = TextAlign.Center,
    modifier =
      modifier
        .clip(RoundedCornerShape(12.dp))
        .background(if (destructive) FrameRed else FrameGreen)
        .clickable(onClick = onClick)
        .then(if (testTag == null) Modifier else Modifier.testTag(testTag))
        .padding(horizontal = 12.dp, vertical = 8.dp),
  )
}

@Composable
private fun PhotoFrameSelectionChip(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
) {
  Text(
    text = label,
    color = if (selected) FrameBlack else FrameWarmWhite,
    fontSize = 11.sp,
    lineHeight = 13.sp,
    fontWeight = FontWeight.Bold,
    maxLines = 1,
    overflow = TextOverflow.Ellipsis,
    textAlign = TextAlign.Center,
    modifier =
      modifier
        .clip(RoundedCornerShape(10.dp))
        .background(if (selected) FrameWarmWhite else Color.White.copy(alpha = 0.08f))
        .border(1.dp, if (selected) FrameGreen else FrameWarmWhite.copy(alpha = 0.14f), RoundedCornerShape(10.dp))
        .clickable(onClick = onClick)
        .padding(horizontal = 6.dp, vertical = 8.dp),
  )
}

@Composable
internal fun PhotoAiTagChips(
  aiState: PhotoAiState,
  selectedTags: Set<String>? = null,
  modifier: Modifier = Modifier,
) {
  val insight = (aiState as? PhotoAiState.Ready)?.insight ?: return
  val tags = (selectedTags?.toList() ?: insight.tags).take(2)
  if (tags.isEmpty()) return

  Row(
    modifier = modifier.testTag("photo-ai-tag-chips"),
    horizontalArrangement = Arrangement.spacedBy(4.dp),
  ) {
    tags.forEach { tag ->
      Text(
        text = tag,
        color = FrameBlack,
        fontSize = 9.sp,
        lineHeight = 11.sp,
        fontWeight = FontWeight.Bold,
        maxLines = 1,
        overflow = TextOverflow.Ellipsis,
        modifier =
          Modifier
            .weight(1f, fill = false)
            .clip(RoundedCornerShape(8.dp))
            .background(PhotoTagChipColor)
            .padding(horizontal = 6.dp, vertical = 3.dp)
            .testTag("photo-ai-tag-chip"),
      )
    }
  }
}
