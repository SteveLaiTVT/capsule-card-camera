package com.example.capsulecardcamera.ui.main

import android.graphics.Bitmap
import junit.framework.TestCase.assertEquals
import junit.framework.TestCase.assertNotNull
import junit.framework.TestCase.assertNull
import junit.framework.TestCase.assertTrue
import org.junit.Test

class PhotoAiAnalysisTest {
  @Test
  fun parsePhotoInsight_validJson_returnsNormalizedInsight() {
    val insight =
      parsePhotoInsight(
        """
        ```json
        {
          "title": "  Street portrait  ",
          "tags": ["portrait", "street", "portrait", "warm light", "city", "extra", "ignored"],
          "subject": "person",
          "scene": "urban street",
          "colors": ["red", "cream", "black", "gold", "ignored"],
          "confidence": "high",
          "cameraMode": "portrait",
          "frameStyle": "film",
          "frameReason": "Cinematic street mood"
        }
        ```
        """.trimIndent(),
      )

    assertNotNull(insight)
    assertEquals("Street portrait", insight?.title)
    assertEquals(listOf("portrait", "street", "warm light", "city", "extra", "ignored"), insight?.tags)
    assertEquals(listOf("red", "cream", "black", "gold"), insight?.colors)
    assertEquals(PhotoInsightConfidence.High, insight?.confidence)
    assertEquals(CameraSceneMode.Portrait, insight?.cameraSceneMode)
    assertEquals(PhotoFrameStyle.Film, insight?.suggestedFrameStyle)
    assertEquals("Cinematic street mood", insight?.frameReason)
  }

  @Test
  fun parsePhotoInsight_invalidJson_returnsNull() {
    assertNull(parsePhotoInsight("not json"))
  }

  @Test
  fun parsePhotoInsight_unknownConfidence_defaultsLow() {
    val insight =
      parsePhotoInsight(
        """
        {"title":"Desk","tags":["desk"],"subject":"desk","scene":"office","colors":["white"],"confidence":"certain"}
        """.trimIndent(),
      )

    assertEquals(PhotoInsightConfidence.Low, insight?.confidence)
  }

  @Test
  fun parsePhotoInsight_missingFrameStyle_usesHeuristicSuggestion() {
    val insight =
      parsePhotoInsight(
        """
        {"title":"Cake","tags":["food","bright"],"subject":"cake","scene":"table","colors":["orange"],"confidence":"medium"}
        """.trimIndent(),
      )

    assertEquals(PhotoFrameStyle.ColorPop, insight?.suggestedFrameStyle)
    assertEquals(CameraSceneMode.Food, insight?.cameraSceneMode)
  }

  @Test
  fun parsePhotoInsight_acceptsSceneryCameraModeAliases() {
    val insight =
      parsePhotoInsight(
        """
        {"title":"Mountain","tags":["sky"],"subject":"mountain","scene":"outdoor","colors":["blue"],"confidence":"high","cameraMode":"landscape"}
        """.trimIndent(),
      )

    assertEquals(CameraSceneMode.Scenery, insight?.cameraSceneMode)
  }

  @Test
  fun parseGeneratedFrameSpec_validJson_returnsNormalizedSpec() {
    val spec =
      parseGeneratedFrameSpec(
        raw =
          """
          {
            "title": "  Night Desk  ",
            "baseStyle": "film",
            "backgroundColor": "purple",
            "accentColor": "yellow",
            "inkColor": "white",
            "motif": "sparkles",
            "composition": "portal",
            "photoTreatment": "warmGlow",
            "themeOverlay": "lightLeak",
            "caption": "Late light",
            "reason": "Matches the neon desk mood"
          }
          """.trimIndent(),
        fallbackStyle = PhotoFrameStyle.Stamp,
      )

    assertNotNull(spec)
    assertEquals("Night Desk", spec?.title)
    assertEquals(PhotoFrameStyle.Film, spec?.baseStyle)
    assertEquals("purple", spec?.backgroundColor)
    assertEquals("yellow", spec?.accentColor)
    assertEquals("white", spec?.inkColor)
    assertEquals(GeneratedFrameMotif.Sparkles, spec?.motif)
    assertEquals(GeneratedFrameComposition.Portal, spec?.composition)
    assertEquals(GeneratedPhotoTreatment.WarmGlow, spec?.photoTreatment)
    assertEquals(GeneratedFrameThemeOverlay.LightLeak, spec?.themeOverlay)
    assertEquals("Late light", spec?.caption)
  }

  @Test
  fun parseGeneratedFrameSpec_unknownStyleUsesFallbackAndDefaultColors() {
    val spec =
      parseGeneratedFrameSpec(
        raw = """{"title":"Desk","baseStyle":"unknown","backgroundColor":"unknown","accentColor":"","inkColor":"","motif":"unknown","caption":"","reason":""}""",
        fallbackStyle = PhotoFrameStyle.Polaroid,
      )

    assertEquals(PhotoFrameStyle.Polaroid, spec?.baseStyle)
    assertEquals("cream", spec?.backgroundColor)
    assertEquals("green", spec?.accentColor)
    assertEquals("black", spec?.inkColor)
    assertEquals(GeneratedFrameMotif.None, spec?.motif)
    assertEquals(GeneratedFrameComposition.Portal, spec?.composition)
    assertEquals(GeneratedPhotoTreatment.WarmGlow, spec?.photoTreatment)
    assertEquals(GeneratedFrameThemeOverlay.LightLeak, spec?.themeOverlay)
  }

  @Test
  fun parseGeneratedFrameSpec_stampWithoutStampIntentMovesToCreativeStyle() {
    val spec =
      parseGeneratedFrameSpec(
        raw = """{"title":"Ribbon light","baseStyle":"stamp","backgroundColor":"cream","accentColor":"green","inkColor":"black","motif":"lines","caption":"soft light","reason":"Ribbon crosses the photo"}""",
        fallbackStyle = PhotoFrameStyle.Stamp,
      )

    assertEquals(PhotoFrameStyle.ColorPop, spec?.baseStyle)
    assertEquals(GeneratedFrameComposition.Portal, spec?.composition)
    assertEquals(GeneratedPhotoTreatment.WarmGlow, spec?.photoTreatment)
    assertEquals(GeneratedFrameThemeOverlay.LightLeak, spec?.themeOverlay)
  }

  @Test
  fun ensureDistinctFrom_changesRepeatedStyleAndMotif() {
    val previous =
      GeneratedFrameSpec(
        title = "Earbuds frame",
        baseStyle = PhotoFrameStyle.Stamp,
        backgroundColor = "warmWhite",
        accentColor = "red",
        inkColor = "black",
        motif = GeneratedFrameMotif.Lines,
        caption = "audio",
        reason = "First pass",
      )
    val repeated = previous.copy(reason = "Repeated pass")

    val distinct = repeated.ensureDistinctFrom(previous)

    assertEquals(PhotoFrameStyle.Polaroid, distinct.baseStyle)
    assertEquals(GeneratedFrameMotif.Dots, distinct.motif)
    assertEquals(GeneratedFrameComposition.Offset, distinct.composition)
    assertEquals(GeneratedPhotoTreatment.WarmGlow, distinct.photoTreatment)
    assertEquals(GeneratedFrameThemeOverlay.Ribbon, distinct.themeOverlay)
    assertEquals("cream", distinct.backgroundColor)
  }

  @Test
  fun buildGeneratedFramePrompt_requestsCreativePhotoIntegratedFrame() {
    val prompt =
      buildGeneratedFramePrompt(
        insight =
          PhotoInsight(
            title = "Desk light",
            tags = listOf("desk", "lamp"),
            subject = "desk",
            scene = "workspace",
            colors = listOf("green", "black"),
            confidence = PhotoInsightConfidence.High,
            suggestedFrameStyle = PhotoFrameStyle.Film,
          ),
        selectedTags = setOf("desk"),
        currentFrameStyle = PhotoFrameStyle.Stamp,
        previousFrameSpec = null,
        conversation = emptyList(),
        instruction = "make it more creative",
      )

    assertTrue(prompt.contains("non-classic composition"))
    assertTrue(prompt.contains("attached photo"))
    assertTrue(prompt.contains("capture style language"))
    assertTrue(prompt.contains("themeOverlay that crosses from the border into the image"))
    assertTrue(prompt.contains("\"photoTreatment\""))
    assertTrue(prompt.contains("\"themeOverlay\""))
    assertTrue(prompt.contains("Output stamp only if the user request explicitly contains stamp or postage"))
  }

  @Test
  fun photoInsightPrompt_requestsCameraMode() {
    assertTrue(photoInsightPromptForTests().contains("\"cameraMode\":\"portrait|scenery|food\""))
    assertTrue(photoInsightPromptForTests().contains("Pick one cameraMode"))
  }

  @Test
  fun addGeneratedFrameSpecToLibrary_keepsMostRecentAndDedupes() {
    val first =
      GeneratedFrameSpec(
        title = "Earbuds frame",
        baseStyle = PhotoFrameStyle.Stamp,
        backgroundColor = "warmWhite",
        accentColor = "red",
        inkColor = "black",
        motif = GeneratedFrameMotif.Lines,
        caption = "audio",
        reason = "First pass",
      )
    val second = first.copy(title = "Film audio", baseStyle = PhotoFrameStyle.Film, motif = GeneratedFrameMotif.Sparkles)

    val library = addGeneratedFrameSpecToLibrary(addGeneratedFrameSpecToLibrary(listOf(first), second), first)

    assertEquals(first, library.first())
    assertEquals(2, library.size)
  }

  @Test
  fun photoSaveMetadata_userCommentJson_containsSelectedTags() {
    val json =
      PhotoSaveMetadata(
        title = "Street portrait",
        tags = listOf("portrait", "street"),
        subject = "person",
        scene = "urban street",
        colors = listOf("red", "cream"),
        confidence = "high",
      ).toUserCommentJson()

    assertTrue(json.contains("\"tags\":[\"portrait\",\"street\"]"))
    assertTrue(json.contains("\"title\":\"Street portrait\""))
  }

  @Test
  fun updateCapturedPhotoAiState_readySelectsGeneratedTagsOnce() {
    val photo =
      CapturedPhoto(
        id = 7,
        bitmap = BitmapTestFixtures.bitmap(),
        frameStyle = PhotoFrameStyle.Stamp,
      )
    val ready =
      PhotoAiState.Ready(
        PhotoInsight(
          title = "Desk",
          tags = listOf("desk", "lamp"),
          subject = "desk",
          scene = "office",
          colors = listOf("white"),
          confidence = PhotoInsightConfidence.Medium,
        ),
      )

    val updated = updateCapturedPhotoAiState(listOf(photo), photoId = 7, aiState = ready).single()
    val refreshed = updateCapturedPhotoAiState(listOf(updated.copy(selectedAiTags = setOf("desk"))), photoId = 7, aiState = ready).single()

    assertEquals(setOf("desk", "lamp"), updated.selectedAiTags)
    assertEquals(setOf("desk"), refreshed.selectedAiTags)
  }

  @Test
  fun updateCapturedPhotoFrameGenerationState_readyAppliesGeneratedSpec() {
    val photo =
      CapturedPhoto(
        id = 5,
        bitmap = BitmapTestFixtures.bitmap(),
        frameStyle = PhotoFrameStyle.Stamp,
        frameConversation = listOf(FrameConversationMessage(FrameConversationRole.User, "make it cinematic")),
      )
    val spec =
      GeneratedFrameSpec(
        title = "Night desk",
        baseStyle = PhotoFrameStyle.Film,
        backgroundColor = "black",
        accentColor = "yellow",
        inkColor = "white",
        motif = GeneratedFrameMotif.Lines,
        caption = "late work",
        reason = "Dark film border fits the scene",
      )

    val updated =
      updateCapturedPhotoFrameGenerationState(
        photos = listOf(photo),
        photoId = 5,
        frameGenerationState = FrameGenerationState.Ready(spec),
      ).single()

    assertEquals(PhotoFrameStyle.Film, updated.frameStyle)
    assertEquals(spec, updated.generatedFrameSpec)
    assertEquals(FrameGenerationState.Ready(spec), updated.frameGenerationState)
    assertEquals(FrameConversationRole.Assistant, updated.frameConversation.last().role)
  }

  @Test
  fun toggleCapturedPhotoAiTag_updatesSelectedTags() {
    val photo =
      CapturedPhoto(
        id = 3,
        bitmap = BitmapTestFixtures.bitmap(),
        frameStyle = PhotoFrameStyle.Stamp,
        selectedAiTags = setOf("desk", "lamp"),
      )

    val withoutLamp = toggleCapturedPhotoAiTag(listOf(photo), photoId = 3, tag = "lamp").single()
    val withBook = toggleCapturedPhotoAiTag(listOf(withoutLamp), photoId = 3, tag = "book").single()

    assertEquals(setOf("desk"), withoutLamp.selectedAiTags)
    assertEquals(setOf("desk", "book"), withBook.selectedAiTags)
  }

  @Test
  fun capturedPhotoSaveMetadata_usesUserSelectedTags() {
    val photo =
      CapturedPhoto(
        id = 9,
        bitmap = BitmapTestFixtures.bitmap(),
        frameStyle = PhotoFrameStyle.Film,
        aiState =
          PhotoAiState.Ready(
            PhotoInsight(
              title = "Night desk",
              tags = listOf("desk", "lamp", "screen"),
              subject = "desk",
              scene = "workspace",
              colors = listOf("black", "green"),
              confidence = PhotoInsightConfidence.High,
            ),
          ),
        selectedAiTags = setOf("lamp", "screen"),
      )

    val metadata = photo.saveMetadata()

    assertEquals("Night desk", metadata.title)
    assertEquals(listOf("lamp", "screen"), metadata.tags)
    assertEquals("workspace", metadata.scene)
    assertEquals("high", metadata.confidence)
  }

  @Test
  fun capturedPhotoSaveMetadata_includesGeneratedFrameSpec() {
    val photo =
      CapturedPhoto(
        id = 10,
        bitmap = BitmapTestFixtures.bitmap(),
        frameStyle = PhotoFrameStyle.ColorPop,
        selectedAiTags = setOf("flower"),
        generatedFrameSpec =
          GeneratedFrameSpec(
            title = "Bloom pop",
            baseStyle = PhotoFrameStyle.ColorPop,
            backgroundColor = "pink",
            accentColor = "green",
            inkColor = "black",
            motif = GeneratedFrameMotif.Dots,
            composition = GeneratedFrameComposition.Scrapbook,
            photoTreatment = GeneratedPhotoTreatment.PopTint,
            themeOverlay = GeneratedFrameThemeOverlay.CornerBloom,
            caption = "fresh bloom",
            reason = "Bright colors echo the flower",
          ),
      )

    val metadata = photo.saveMetadata()
    val json = metadata.toUserCommentJson()

    assertEquals("Bloom pop", metadata.generatedFrameTitle)
    assertTrue(json.contains("\"generatedFrameTitle\":\"Bloom pop\""))
    assertTrue(json.contains("\"generatedFrameMotif\":\"dots\""))
    assertTrue(json.contains("\"generatedFrameComposition\":\"scrapbook\""))
    assertTrue(json.contains("\"generatedPhotoTreatment\":\"popTint\""))
    assertTrue(json.contains("\"generatedFrameOverlay\":\"cornerBloom\""))
    assertTrue(json.contains("\"generatedFrameColors\":[\"pink\",\"green\",\"black\"]"))
  }
}

private object BitmapTestFixtures {
  private val unsafe: sun.misc.Unsafe by lazy {
    val field = sun.misc.Unsafe::class.java.getDeclaredField("theUnsafe")
    field.isAccessible = true
    field.get(null) as sun.misc.Unsafe
  }

  fun bitmap(): Bitmap = unsafe.allocateInstance(Bitmap::class.java) as Bitmap
}
