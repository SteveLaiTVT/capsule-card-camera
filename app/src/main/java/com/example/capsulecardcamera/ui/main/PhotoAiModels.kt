package com.example.capsulecardcamera.ui.main

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

internal data class PhotoInsight(
  val title: String,
  val tags: List<String>,
  val subject: String,
  val scene: String,
  val colors: List<String>,
  val confidence: PhotoInsightConfidence,
  val suggestedFrameStyle: PhotoFrameStyle? = null,
  val frameReason: String = "",
)

internal enum class PhotoInsightConfidence(val label: String) {
  Low("low"),
  Medium("medium"),
  High("high"),
}

internal sealed interface PhotoAiState {
  object Idle : PhotoAiState

  object Preparing : PhotoAiState

  object Analyzing : PhotoAiState

  data class Ready(val insight: PhotoInsight) : PhotoAiState

  object Unavailable : PhotoAiState

  data class Failed(val message: String? = null) : PhotoAiState
}

internal data class GeneratedFrameSpec(
  val title: String,
  val baseStyle: PhotoFrameStyle,
  val backgroundColor: String,
  val accentColor: String,
  val inkColor: String,
  val motif: GeneratedFrameMotif,
  val composition: GeneratedFrameComposition = GeneratedFrameComposition.Classic,
  val photoTreatment: GeneratedPhotoTreatment = GeneratedPhotoTreatment.Natural,
  val themeOverlay: GeneratedFrameThemeOverlay = GeneratedFrameThemeOverlay.None,
  val caption: String,
  val reason: String,
)

internal enum class GeneratedFrameMotif(val label: String) {
  None("none"),
  Dots("dots"),
  Sparkles("sparkles"),
  Waves("waves"),
  Leaves("leaves"),
  Lines("lines"),
}

internal enum class GeneratedFrameComposition(val label: String) {
  Classic("classic"),
  Offset("offset"),
  Poster("poster"),
  Portal("portal"),
  Scrapbook("scrapbook"),
  Split("split"),
}

internal enum class GeneratedPhotoTreatment(val label: String) {
  Natural("natural"),
  WarmGlow("warmGlow"),
  CoolFade("coolFade"),
  Noir("noir"),
  PopTint("popTint"),
  DreamWash("dreamWash"),
}

internal enum class GeneratedFrameThemeOverlay(val label: String) {
  None("none"),
  Ribbon("ribbon"),
  CornerBloom("cornerBloom"),
  LightLeak("lightLeak"),
  FilmBurn("filmBurn"),
  StickerTrail("stickerTrail"),
}

internal sealed interface FrameGenerationState {
  object Idle : FrameGenerationState

  object Generating : FrameGenerationState

  data class Ready(val spec: GeneratedFrameSpec) : FrameGenerationState

  object Unavailable : FrameGenerationState

  data class Failed(val message: String? = null) : FrameGenerationState
}

internal data class FrameConversationMessage(
  val role: FrameConversationRole,
  val text: String,
)

internal enum class FrameConversationRole {
  User,
  Assistant,
}

internal fun parsePhotoInsight(raw: String): PhotoInsight? {
  val jsonText = raw.extractJsonObject() ?: return null
  return runCatching {
    val payload = PhotoInsightJson.decodeFromString<PhotoInsightPayload>(jsonText)
    val tags = payload.tags.cleanList(maxItems = 6)
    val colors = payload.colors.cleanList(maxItems = 4)
    val title = payload.title.cleanText(maxLength = 48).ifBlank { tags.firstOrNull().orEmpty() }
    val subject = payload.subject.cleanText(maxLength = 40)
    val scene = payload.scene.cleanText(maxLength = 40)
    val suggestedFrameStyle =
      payload.frameStyle.toSuggestedPhotoFrameStyle()
        ?: suggestPhotoFrameStyle(tags = tags, subject = subject, scene = scene, colors = colors)
    PhotoInsight(
      title = title,
      tags = tags,
      subject = subject,
      scene = scene,
      colors = colors,
      confidence = payload.confidence.toPhotoInsightConfidence(),
      suggestedFrameStyle = suggestedFrameStyle,
      frameReason = payload.frameReason.cleanText(maxLength = 72),
    )
  }.getOrNull()
}

internal fun parseGeneratedFrameSpec(
  raw: String,
  fallbackStyle: PhotoFrameStyle,
): GeneratedFrameSpec? {
  val jsonText = raw.extractJsonObject() ?: return null
  return runCatching {
    val payload = PhotoInsightJson.decodeFromString<GeneratedFramePayload>(jsonText)
    val baseStyle = payload.creativeBaseStyle(fallbackStyle)
    GeneratedFrameSpec(
      title = payload.title.cleanText(maxLength = 36).ifBlank { "AI frame" },
      baseStyle = baseStyle,
      backgroundColor = payload.backgroundColor.cleanFrameColorKey(defaultColor = baseStyle.defaultGeneratedBackgroundColor()),
      accentColor = payload.accentColor.cleanFrameColorKey(defaultColor = baseStyle.defaultGeneratedAccentColor()),
      inkColor = payload.inkColor.cleanFrameColorKey(defaultColor = baseStyle.defaultGeneratedInkColor()),
      motif = payload.motif.toGeneratedFrameMotif(),
      composition = payload.composition.toGeneratedFrameComposition(default = GeneratedFrameComposition.Portal),
      photoTreatment = payload.photoTreatment.toGeneratedPhotoTreatment(default = GeneratedPhotoTreatment.WarmGlow),
      themeOverlay = payload.themeOverlay.toGeneratedFrameThemeOverlay(default = GeneratedFrameThemeOverlay.LightLeak),
      caption = payload.caption.cleanText(maxLength = 28),
      reason = payload.reason.cleanText(maxLength = 80),
    )
  }.getOrNull()
}

internal fun buildGeneratedFramePrompt(
  insight: PhotoInsight,
  selectedTags: Set<String>,
  currentFrameStyle: PhotoFrameStyle,
  previousFrameSpec: GeneratedFrameSpec?,
  conversation: List<FrameConversationMessage>,
  instruction: String,
): String {
  val selectedTagsText = selectedTags.joinToString(", ").ifBlank { insight.tags.joinToString(", ") }
  val previousSpecText =
    previousFrameSpec?.let { spec ->
      "Previous frame: title=${spec.title}, style=${spec.baseStyle.label}, composition=${spec.composition.label}, treatment=${spec.photoTreatment.label}, overlay=${spec.themeOverlay.label}, motif=${spec.motif.label}, colors=${spec.backgroundColor}/${spec.accentColor}/${spec.inkColor}, caption=${spec.caption}."
    }.orEmpty()
  val conversationText =
    conversation
      .takeLast(4)
      .joinToString("\n") { message -> "${message.role.name.lowercase()}: ${message.text.cleanText(maxLength = 120)}" }
  val userInstruction = instruction.cleanText(maxLength = 160).ifBlank { "Generate a frame from the photo theme." }

  return """
Return only compact JSON for a drawable photo frame spec.
Use the attached photo when available plus the photo theme, not a bitmap generator. Do not mention private attributes or identify people.
The AI frame is a capture style language, not an album page or plain decorative mat.
Make a comfortable embedded immersive frame: preserve the photo as the hero, use the subject placement, scene, and colors, and blend the border into the image with a non-classic composition, a photoTreatment, and a themeOverlay that crosses from the border into the image. Avoid plain postage/stamp unless the user explicitly asks for stamps or postage.
Photo title: ${insight.title}
Subject: ${insight.subject}
Scene: ${insight.scene}
Colors: ${insight.colors.joinToString(", ")}
Selected tags: $selectedTagsText
Current base style: ${currentFrameStyle.label}
$previousSpecText
Conversation:
$conversationText
User request: $userInstruction
Allowed baseStyle: polaroid, film, color. Output stamp only if the user request explicitly contains stamp or postage.
Prefer color, film, or polaroid for new creative frames. If the current base style is stamp, move away from stamp.
Allowed color keys: warmWhite, cream, black, green, red, orange, blue, pink, purple, teal, yellow, white, gray.
Allowed motif: none, dots, sparkles, waves, leaves, lines.
Allowed composition: offset, poster, portal, scrapbook, split. Avoid classic unless the user asks for a simple frame.
Allowed photoTreatment: natural, warmGlow, coolFade, noir, popTint, dreamWash. Avoid natural unless the user asks for no edit.
Allowed themeOverlay: ribbon, cornerBloom, lightLeak, filmBurn, stickerTrail. Avoid none unless the user asks for no overlay.
JSON schema:
{"title":"...","baseStyle":"polaroid|film|color","backgroundColor":"...","accentColor":"...","inkColor":"...","motif":"...","composition":"offset|poster|portal|scrapbook|split","photoTreatment":"warmGlow|coolFade|noir|popTint|dreamWash","themeOverlay":"ribbon|cornerBloom|lightLeak|filmBurn|stickerTrail","caption":"...","reason":"..."}
Keep title under 5 words, caption under 4 words, reason under 12 words.
If there is a previous frame, make the next frame visually distinct by changing baseStyle, composition, photoTreatment, themeOverlay, motif, or colors. Do not repeat the same style/composition/treatment/overlay unless the user explicitly asks.
  """.trimIndent()
}

internal fun updateCapturedPhotoAiState(
  photos: List<CapturedPhoto>,
  photoId: Int,
  aiState: PhotoAiState,
): List<CapturedPhoto> =
  photos.map { photo ->
    if (photo.id == photoId) {
      val selectedTags =
        if (aiState is PhotoAiState.Ready && photo.aiState !is PhotoAiState.Ready && photo.selectedAiTags.isEmpty()) {
          aiState.insight.tags.toSet()
        } else {
          photo.selectedAiTags
        }
      photo.copy(aiState = aiState, selectedAiTags = selectedTags)
    } else {
      photo
    }
  }

internal fun toggleCapturedPhotoAiTag(
  photos: List<CapturedPhoto>,
  photoId: Int,
  tag: String,
): List<CapturedPhoto> {
  val normalizedTag = tag.trim()
  if (normalizedTag.isBlank()) return photos
  return photos.map { photo ->
    if (photo.id == photoId) {
      val selectedTags =
        if (normalizedTag in photo.selectedAiTags) {
          photo.selectedAiTags - normalizedTag
        } else {
          photo.selectedAiTags + normalizedTag
        }
      photo.copy(selectedAiTags = selectedTags)
    } else {
      photo
    }
  }
}

internal fun beginCapturedPhotoFrameGeneration(
  photos: List<CapturedPhoto>,
  photoId: Int,
  instruction: String,
): List<CapturedPhoto> {
  val message = instruction.cleanText(maxLength = 120).ifBlank { "Generate from photo theme" }
  return photos.map { photo ->
    if (photo.id == photoId) {
      photo.copy(
        frameGenerationState = FrameGenerationState.Generating,
        frameConversation = photo.frameConversation + FrameConversationMessage(FrameConversationRole.User, message),
      )
    } else {
      photo
    }
  }
}

internal fun updateCapturedPhotoFrameGenerationState(
  photos: List<CapturedPhoto>,
  photoId: Int,
  frameGenerationState: FrameGenerationState,
): List<CapturedPhoto> =
  photos.map { photo ->
    if (photo.id == photoId) {
      when (frameGenerationState) {
        is FrameGenerationState.Ready ->
          photo.copy(
            frameStyle = frameGenerationState.spec.baseStyle,
            generatedFrameSpec = frameGenerationState.spec,
            frameGenerationState = frameGenerationState,
            frameConversation =
              photo.frameConversation +
                FrameConversationMessage(
                  role = FrameConversationRole.Assistant,
                  text = frameGenerationState.spec.reason.ifBlank { frameGenerationState.spec.title },
                ),
          )
        else -> photo.copy(frameGenerationState = frameGenerationState)
      }
    } else {
      photo
    }
  }

internal fun GeneratedFrameSpec.ensureDistinctFrom(previous: GeneratedFrameSpec?): GeneratedFrameSpec {
  previous ?: return this
  if (!isVisuallySameAs(previous)) return this
  val nextStyle = baseStyle.nextGeneratedFrameStyle()
  val nextMotif = motif.nextGeneratedFrameMotif()
  return copy(
    baseStyle = nextStyle,
    backgroundColor = nextStyle.defaultGeneratedBackgroundColor(),
    accentColor = accentColor.nextGeneratedAccentColor(),
    inkColor = nextStyle.defaultGeneratedInkColor(),
    motif = nextMotif,
    composition = composition.nextGeneratedFrameComposition(),
    photoTreatment = photoTreatment.nextGeneratedPhotoTreatment(),
    themeOverlay = themeOverlay.nextGeneratedFrameThemeOverlay(),
    reason = reason.ifBlank { "Adjusted to a distinct frame variation" },
  )
}

internal fun addGeneratedFrameSpecToLibrary(
  library: List<GeneratedFrameSpec>,
  spec: GeneratedFrameSpec,
  maxItems: Int = 8,
): List<GeneratedFrameSpec> =
  (listOf(spec) + library.filterNot { it.isSameLibraryFrameAs(spec) }).take(maxItems)

@Serializable
private data class PhotoInsightPayload(
  val title: String = "",
  val tags: List<String> = emptyList(),
  val subject: String = "",
  val scene: String = "",
  val colors: List<String> = emptyList(),
  @SerialName("confidence") val confidence: String = "",
  val frameStyle: String = "",
  val frameReason: String = "",
)

@Serializable
private data class GeneratedFramePayload(
  val title: String = "",
  val baseStyle: String = "",
  val backgroundColor: String = "",
  val accentColor: String = "",
  val inkColor: String = "",
  val motif: String = "",
  val composition: String = "",
  val photoTreatment: String = "",
  val themeOverlay: String = "",
  val caption: String = "",
  val reason: String = "",
)

private val PhotoInsightJson =
  Json {
    ignoreUnknownKeys = true
    isLenient = true
  }

private fun String.extractJsonObject(): String? {
  val start = indexOf('{')
  val end = lastIndexOf('}')
  if (start < 0 || end <= start) return null
  return substring(start, end + 1)
}

private fun List<String>.cleanList(maxItems: Int): List<String> =
  map { it.cleanText(maxLength = 28) }
    .filter { it.isNotBlank() }
    .distinct()
    .take(maxItems)

private fun String.cleanText(maxLength: Int): String =
  trim()
    .replace(Regex("\\s+"), " ")
    .take(maxLength)

private fun String.cleanFrameColorKey(defaultColor: String): String {
  val key =
    trim()
      .replace(Regex("[^A-Za-z]"), "")
      .replaceFirstChar { it.lowercase() }
  return when (key.lowercase()) {
    "warmwhite" -> "warmWhite"
    "cream" -> "cream"
    "black" -> "black"
    "green" -> "green"
    "red" -> "red"
    "orange" -> "orange"
    "blue" -> "blue"
    "pink" -> "pink"
    "purple" -> "purple"
    "teal" -> "teal"
    "yellow" -> "yellow"
    "white" -> "white"
    "gray", "grey" -> "gray"
    else -> defaultColor
  }
}

private fun String.toPhotoInsightConfidence(): PhotoInsightConfidence =
  when (trim().lowercase()) {
    "high" -> PhotoInsightConfidence.High
    "medium" -> PhotoInsightConfidence.Medium
    else -> PhotoInsightConfidence.Low
  }

private fun String.toSuggestedPhotoFrameStyle(): PhotoFrameStyle? =
  when (trim().lowercase()) {
    "stamp", "sticker" -> PhotoFrameStyle.Stamp
    "polaroid", "instant" -> PhotoFrameStyle.Polaroid
    "film", "cinematic" -> PhotoFrameStyle.Film
    "color", "colorpop", "color_pop", "pop" -> PhotoFrameStyle.ColorPop
    else -> null
  }

private fun String.toGeneratedFrameMotif(): GeneratedFrameMotif =
  when (trim().lowercase()) {
    "dots", "dot" -> GeneratedFrameMotif.Dots
    "sparkles", "sparkle", "stars", "star" -> GeneratedFrameMotif.Sparkles
    "waves", "wave" -> GeneratedFrameMotif.Waves
    "leaves", "leaf" -> GeneratedFrameMotif.Leaves
    "lines", "line", "stripes", "stripe" -> GeneratedFrameMotif.Lines
    else -> GeneratedFrameMotif.None
  }

private fun GeneratedFramePayload.creativeBaseStyle(fallbackStyle: PhotoFrameStyle): PhotoFrameStyle {
  val requestedStyle = baseStyle.toSuggestedPhotoFrameStyle() ?: fallbackStyle
  if (requestedStyle != PhotoFrameStyle.Stamp || mentionsStampIntent()) return requestedStyle
  return when (fallbackStyle) {
    PhotoFrameStyle.Stamp -> PhotoFrameStyle.ColorPop
    PhotoFrameStyle.Polaroid -> PhotoFrameStyle.Film
    PhotoFrameStyle.Film -> PhotoFrameStyle.ColorPop
    PhotoFrameStyle.ColorPop -> PhotoFrameStyle.Polaroid
  }
}

private fun GeneratedFramePayload.mentionsStampIntent(): Boolean =
  listOf(title, caption, reason)
    .joinToString(" ")
    .lowercase()
    .let { text -> "postage" in text || "postal" in text }

private fun String.toGeneratedFrameComposition(default: GeneratedFrameComposition): GeneratedFrameComposition {
  val normalized = trim().lowercase()
  if (normalized.isBlank()) return default
  return when (normalized) {
    "classic", "simple" -> GeneratedFrameComposition.Classic
    "offset", "asymmetric", "offcenter", "off_center" -> GeneratedFrameComposition.Offset
    "poster", "magazine", "cover" -> GeneratedFrameComposition.Poster
    "portal", "window", "layered" -> GeneratedFrameComposition.Portal
    "scrapbook", "collage", "tape" -> GeneratedFrameComposition.Scrapbook
    "split", "diptych", "panel" -> GeneratedFrameComposition.Split
    else -> default
  }
}

private fun String.toGeneratedPhotoTreatment(default: GeneratedPhotoTreatment): GeneratedPhotoTreatment {
  val normalized = trim().lowercase()
  if (normalized.isBlank()) return default
  return when (normalized) {
    "natural", "none", "clean" -> GeneratedPhotoTreatment.Natural
    "warmglow", "warm", "glow", "golden" -> GeneratedPhotoTreatment.WarmGlow
    "coolfade", "cool", "bluefade", "cyan" -> GeneratedPhotoTreatment.CoolFade
    "noir", "blackwhite", "bw", "monochrome" -> GeneratedPhotoTreatment.Noir
    "poptint", "pop", "tint", "colorpop" -> GeneratedPhotoTreatment.PopTint
    "dreamwash", "dream", "soft", "wash" -> GeneratedPhotoTreatment.DreamWash
    else -> default
  }
}

private fun String.toGeneratedFrameThemeOverlay(default: GeneratedFrameThemeOverlay): GeneratedFrameThemeOverlay {
  val normalized = trim().lowercase()
  if (normalized.isBlank()) return default
  return when (normalized) {
    "none", "clean" -> GeneratedFrameThemeOverlay.None
    "ribbon", "band", "stripe" -> GeneratedFrameThemeOverlay.Ribbon
    "cornerbloom", "bloom", "corner", "leafcorner" -> GeneratedFrameThemeOverlay.CornerBloom
    "lightleak", "flare", "leak" -> GeneratedFrameThemeOverlay.LightLeak
    "filmburn", "burn", "lightburn" -> GeneratedFrameThemeOverlay.FilmBurn
    "stickertrail", "stickers", "trail", "confetti" -> GeneratedFrameThemeOverlay.StickerTrail
    else -> default
  }
}

private fun GeneratedFrameSpec.isVisuallySameAs(other: GeneratedFrameSpec): Boolean =
  baseStyle == other.baseStyle &&
    motif == other.motif &&
    composition == other.composition &&
    photoTreatment == other.photoTreatment &&
    themeOverlay == other.themeOverlay

private fun GeneratedFrameSpec.isSameLibraryFrameAs(other: GeneratedFrameSpec): Boolean =
  title.equals(other.title, ignoreCase = true) &&
    baseStyle == other.baseStyle &&
    motif == other.motif &&
    composition == other.composition &&
    photoTreatment == other.photoTreatment &&
    themeOverlay == other.themeOverlay &&
    backgroundColor == other.backgroundColor &&
    accentColor == other.accentColor &&
    caption.equals(other.caption, ignoreCase = true)

private fun PhotoFrameStyle.nextGeneratedFrameStyle(): PhotoFrameStyle =
  when (this) {
    PhotoFrameStyle.Stamp -> PhotoFrameStyle.Polaroid
    PhotoFrameStyle.Polaroid -> PhotoFrameStyle.Film
    PhotoFrameStyle.Film -> PhotoFrameStyle.ColorPop
    PhotoFrameStyle.ColorPop -> PhotoFrameStyle.Stamp
  }

private fun GeneratedFrameMotif.nextGeneratedFrameMotif(): GeneratedFrameMotif =
  when (this) {
    GeneratedFrameMotif.None -> GeneratedFrameMotif.Dots
    GeneratedFrameMotif.Dots -> GeneratedFrameMotif.Sparkles
    GeneratedFrameMotif.Sparkles -> GeneratedFrameMotif.Waves
    GeneratedFrameMotif.Waves -> GeneratedFrameMotif.Leaves
    GeneratedFrameMotif.Leaves -> GeneratedFrameMotif.Lines
    GeneratedFrameMotif.Lines -> GeneratedFrameMotif.Dots
  }

private fun GeneratedFrameComposition.nextGeneratedFrameComposition(): GeneratedFrameComposition =
  when (this) {
    GeneratedFrameComposition.Classic -> GeneratedFrameComposition.Offset
    GeneratedFrameComposition.Offset -> GeneratedFrameComposition.Poster
    GeneratedFrameComposition.Poster -> GeneratedFrameComposition.Portal
    GeneratedFrameComposition.Portal -> GeneratedFrameComposition.Scrapbook
    GeneratedFrameComposition.Scrapbook -> GeneratedFrameComposition.Split
    GeneratedFrameComposition.Split -> GeneratedFrameComposition.Offset
  }

private fun GeneratedPhotoTreatment.nextGeneratedPhotoTreatment(): GeneratedPhotoTreatment =
  when (this) {
    GeneratedPhotoTreatment.Natural -> GeneratedPhotoTreatment.WarmGlow
    GeneratedPhotoTreatment.WarmGlow -> GeneratedPhotoTreatment.CoolFade
    GeneratedPhotoTreatment.CoolFade -> GeneratedPhotoTreatment.Noir
    GeneratedPhotoTreatment.Noir -> GeneratedPhotoTreatment.PopTint
    GeneratedPhotoTreatment.PopTint -> GeneratedPhotoTreatment.DreamWash
    GeneratedPhotoTreatment.DreamWash -> GeneratedPhotoTreatment.WarmGlow
  }

private fun GeneratedFrameThemeOverlay.nextGeneratedFrameThemeOverlay(): GeneratedFrameThemeOverlay =
  when (this) {
    GeneratedFrameThemeOverlay.None -> GeneratedFrameThemeOverlay.Ribbon
    GeneratedFrameThemeOverlay.Ribbon -> GeneratedFrameThemeOverlay.CornerBloom
    GeneratedFrameThemeOverlay.CornerBloom -> GeneratedFrameThemeOverlay.LightLeak
    GeneratedFrameThemeOverlay.LightLeak -> GeneratedFrameThemeOverlay.FilmBurn
    GeneratedFrameThemeOverlay.FilmBurn -> GeneratedFrameThemeOverlay.StickerTrail
    GeneratedFrameThemeOverlay.StickerTrail -> GeneratedFrameThemeOverlay.Ribbon
  }

private fun String.nextGeneratedAccentColor(): String =
  when (trim().lowercase()) {
    "red" -> "blue"
    "blue" -> "yellow"
    "yellow" -> "teal"
    "teal" -> "pink"
    "pink" -> "green"
    "green" -> "purple"
    "purple" -> "orange"
    "orange" -> "red"
    else -> "green"
  }

private fun PhotoFrameStyle.defaultGeneratedBackgroundColor(): String =
  when (this) {
    PhotoFrameStyle.Stamp -> "warmWhite"
    PhotoFrameStyle.Polaroid -> "cream"
    PhotoFrameStyle.Film -> "black"
    PhotoFrameStyle.ColorPop -> "orange"
  }

private fun PhotoFrameStyle.defaultGeneratedAccentColor(): String =
  when (this) {
    PhotoFrameStyle.Stamp -> "red"
    PhotoFrameStyle.Polaroid -> "green"
    PhotoFrameStyle.Film -> "yellow"
    PhotoFrameStyle.ColorPop -> "green"
  }

private fun PhotoFrameStyle.defaultGeneratedInkColor(): String =
  when (this) {
    PhotoFrameStyle.Film -> "white"
    else -> "black"
  }

private fun suggestPhotoFrameStyle(
  tags: List<String>,
  subject: String,
  scene: String,
  colors: List<String>,
): PhotoFrameStyle {
  val text = (tags + subject + scene + colors).joinToString(" ").lowercase()
  return when {
    listOf("night", "street", "cinematic", "urban", "shadow", "film").any { it in text } -> PhotoFrameStyle.Film
    listOf("portrait", "person", "selfie", "warm", "home", "family").any { it in text } -> PhotoFrameStyle.Polaroid
    listOf("food", "flower", "toy", "art", "bright", "red", "orange", "green").any { it in text } -> PhotoFrameStyle.ColorPop
    else -> PhotoFrameStyle.Stamp
  }
}
