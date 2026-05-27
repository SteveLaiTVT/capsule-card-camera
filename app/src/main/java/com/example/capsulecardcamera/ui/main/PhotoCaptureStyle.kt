package com.example.capsulecardcamera.ui.main

internal enum class PhotoCaptureStyle(val storageKey: String) {
  Clean("clean"),
  Pop("pop"),
  Film("film"),
  AiImmersive("ai_immersive"),
}

internal fun PhotoCaptureStyle.defaultFrameStyle(): PhotoFrameStyle =
  when (this) {
    PhotoCaptureStyle.Clean -> PhotoFrameStyle.Polaroid
    PhotoCaptureStyle.Pop -> PhotoFrameStyle.ColorPop
    PhotoCaptureStyle.Film -> PhotoFrameStyle.Film
    PhotoCaptureStyle.AiImmersive -> PhotoFrameStyle.ColorPop
  }

internal fun PhotoCaptureStyle.defaultGeneratedFrameSpec(): GeneratedFrameSpec? =
  when (this) {
    PhotoCaptureStyle.Clean,
    PhotoCaptureStyle.Film,
    -> null
    PhotoCaptureStyle.Pop ->
      GeneratedFrameSpec(
        title = "Color Flow",
        baseStyle = PhotoFrameStyle.ColorPop,
        backgroundColor = "cream",
        accentColor = "green",
        inkColor = "black",
        motif = GeneratedFrameMotif.Waves,
        composition = GeneratedFrameComposition.Offset,
        photoTreatment = GeneratedPhotoTreatment.PopTint,
        themeOverlay = GeneratedFrameThemeOverlay.Ribbon,
        caption = "color flow",
        reason = "A bright embedded color style",
      )
    PhotoCaptureStyle.AiImmersive ->
      GeneratedFrameSpec(
        title = "AI Aura",
        baseStyle = PhotoFrameStyle.ColorPop,
        backgroundColor = "cream",
        accentColor = "teal",
        inkColor = "black",
        motif = GeneratedFrameMotif.Lines,
        composition = GeneratedFrameComposition.Portal,
        photoTreatment = GeneratedPhotoTreatment.WarmGlow,
        themeOverlay = GeneratedFrameThemeOverlay.LightLeak,
        caption = "ai aura",
        reason = "A photo-aware immersive frame style",
      )
  }

internal fun PhotoCaptureStyle.shouldGeneratePhotoAwareFrame(): Boolean = this == PhotoCaptureStyle.AiImmersive

internal fun PhotoCaptureStyle.photoAwareFrameInstruction(previousFrameSpec: GeneratedFrameSpec?): String =
  when (this) {
    PhotoCaptureStyle.AiImmersive ->
      buildString {
        append("Create a comfortable embedded immersive frame style for this exact photo. ")
        append("Use the subject, scene, colors, and composition. ")
        append("The border should blend into the image with a soft overlay, not look like an album page.")
        if (previousFrameSpec != null) {
          append(" Start from the current style ${previousFrameSpec.title}, but adapt it to the photo.")
        }
      }
    else -> ""
  }

internal fun PhotoFrameStyle.defaultCaptureStyle(): PhotoCaptureStyle =
  when (this) {
    PhotoFrameStyle.Stamp,
    PhotoFrameStyle.Polaroid,
    -> PhotoCaptureStyle.Clean
    PhotoFrameStyle.Film -> PhotoCaptureStyle.Film
    PhotoFrameStyle.ColorPop -> PhotoCaptureStyle.Pop
  }
