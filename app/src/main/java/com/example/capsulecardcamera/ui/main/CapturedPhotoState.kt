package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap

internal fun createCapturedPhoto(
  id: Int,
  bitmap: Bitmap,
  defaultFrameStyle: PhotoFrameStyle,
  defaultGeneratedFrameSpec: GeneratedFrameSpec?,
  captureStyle: PhotoCaptureStyle = defaultFrameStyle.defaultCaptureStyle(),
): CapturedPhoto =
  CapturedPhoto(
    id = id,
    bitmap = bitmap,
    frameStyle = defaultFrameStyle,
    captureStyle = captureStyle,
    generatedFrameSpec = defaultGeneratedFrameSpec?.takeIf { it.baseStyle == defaultFrameStyle },
  )

internal fun prependCapturedPhoto(
  photos: List<CapturedPhoto>,
  photo: CapturedPhoto,
  maxPhotos: Int = 9,
): List<CapturedPhoto> = (listOf(photo) + photos).take(maxPhotos)

internal fun updateCapturedPhotoFrameStyle(
  photos: List<CapturedPhoto>,
  photoId: Int,
  frameStyle: PhotoFrameStyle,
): List<CapturedPhoto> =
  photos.map { photo ->
    if (photo.id == photoId) {
      photo.copy(
        frameStyle = frameStyle,
        generatedFrameSpec = null,
        frameGenerationState = FrameGenerationState.Idle,
      )
    } else {
      photo
    }
  }

internal fun updateCapturedPhotoGeneratedFrameSpec(
  photos: List<CapturedPhoto>,
  photoId: Int,
  frameSpec: GeneratedFrameSpec,
): List<CapturedPhoto> =
  photos.map { photo ->
    if (photo.id == photoId) {
      photo.copy(
        frameStyle = frameSpec.baseStyle,
        generatedFrameSpec = frameSpec,
        frameGenerationState = FrameGenerationState.Ready(frameSpec),
      )
    } else {
      photo
    }
  }

internal fun applyFrameStyleToCapturedPhotos(
  photos: List<CapturedPhoto>,
  photoIds: Set<Int>,
  frameStyle: PhotoFrameStyle,
): List<CapturedPhoto> {
  if (photoIds.isEmpty()) return photos
  return photos.map { photo ->
    if (photo.id in photoIds) {
      photo.copy(
        frameStyle = frameStyle,
        generatedFrameSpec = null,
        frameGenerationState = FrameGenerationState.Idle,
      )
    } else {
      photo
    }
  }
}

internal fun deleteCapturedPhotos(
  photos: List<CapturedPhoto>,
  photoIds: Set<Int>,
): List<CapturedPhoto> {
  if (photoIds.isEmpty()) return photos
  return photos.filterNot { it.id in photoIds }
}

internal fun updateCapturedPhotoBitmap(
  photos: List<CapturedPhoto>,
  photoId: Int,
  bitmap: Bitmap,
  aiEnhancementApplied: Boolean = false,
): List<CapturedPhoto> =
  photos.map { photo ->
    if (photo.id == photoId) {
      photo.copy(bitmap = bitmap, aiEnhancementApplied = photo.aiEnhancementApplied || aiEnhancementApplied)
    } else {
      photo
    }
  }

internal fun toggleCapturedPhotoSelection(
  selectedPhotoIds: Set<Int>,
  photoId: Int,
): Set<Int> =
  if (photoId in selectedPhotoIds) {
    selectedPhotoIds - photoId
  } else {
    selectedPhotoIds + photoId
  }

internal fun CapturedPhoto.toFrameSaveRequest(
  defaultAlbum: DefaultAlbum,
  frameStyle: PhotoFrameStyle = this.frameStyle,
): FrameSaveRequest {
  val frameSpec = generatedFrameSpec?.takeIf { it.baseStyle == frameStyle }
  return FrameSaveRequest(
    bitmap = bitmap,
    frameStyle = frameStyle,
    defaultAlbum = defaultAlbum,
    metadata = copy(frameStyle = frameStyle, generatedFrameSpec = frameSpec).saveMetadata(),
    generatedFrameSpec = frameSpec,
  )
}

internal fun List<CapturedPhoto>.toFrameSaveRequests(defaultAlbum: DefaultAlbum): List<FrameSaveRequest> =
  map { photo -> photo.toFrameSaveRequest(defaultAlbum = defaultAlbum) }

internal data class FrameSaveRequest(
  val bitmap: Bitmap,
  val frameStyle: PhotoFrameStyle,
  val defaultAlbum: DefaultAlbum,
  val metadata: PhotoSaveMetadata,
  val generatedFrameSpec: GeneratedFrameSpec?,
)
