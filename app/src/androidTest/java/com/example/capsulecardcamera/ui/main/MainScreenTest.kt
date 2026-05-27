package com.example.capsulecardcamera.ui.main

import android.content.Context
import androidx.activity.ComponentActivity
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertCountEquals
import androidx.compose.ui.test.onAllNodesWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.unit.dp
import androidx.test.core.app.ApplicationProvider
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test

/** UI tests for [com.example.capsulecardcamera.ui.main.MainScreen]. */
class MainScreenTest {

  @get:Rule val composeTestRule = createAndroidComposeRule<ComponentActivity>()

  @Before
  fun clearCameraPreferences() {
    ApplicationProvider
      .getApplicationContext<Context>()
      .getSharedPreferences("capsule_card_camera_settings", Context.MODE_PRIVATE)
      .edit()
      .clear()
      .commit()
  }

  private fun setMainContent() {
    composeTestRule.setContent { MainScreen() }
  }

  @Test
  fun pullIslandCameraDemo_exists() {
    setMainContent()

    composeTestRule.onNodeWithTag("pull-island-camera-demo").assertExists()
    composeTestRule.onNodeWithTag("dynamic-island").assertExists()
    composeTestRule.onNodeWithTag("home-capture-hint").assertExists()
    composeTestRule.onAllNodesWithTag("capture-style-dock").assertCountEquals(0)
    composeTestRule.onNodeWithTag("home-frame-studio-panel").assertExists()
    composeTestRule.onNodeWithText("Pico\nCam").assertExists()
  }

  @Test
  fun settingsButton_opensSettingsScreen() {
    setMainContent()

    composeTestRule.onNodeWithTag("settings-button").assertExists().performClick()
    composeTestRule.onNodeWithTag("camera-settings-screen").assertExists()
    composeTestRule.onNodeWithTag("sound-effects-setting").assertExists()
  }

  @Test
  fun settingsScreen_opensFrameManagementScreen() {
    setMainContent()

    composeTestRule.onNodeWithTag("settings-button").assertExists().performClick()
    composeTestRule.onNodeWithTag("open-frame-manager").performScrollTo().performClick()

    composeTestRule.onNodeWithTag("frame-management-screen").assertExists()
    composeTestRule.onNodeWithTag("frame-manager-generate-button").assertExists()
  }

  @Test
  fun homeFrameStudioPanel_opensFrameManagementScreen() {
    setMainContent()

    composeTestRule.onNodeWithTag("home-frame-manager-button").assertExists().performClick()

    composeTestRule.onNodeWithTag("frame-management-screen").assertExists()
    composeTestRule.onNodeWithTag("frame-manager-generate-button").assertExists()
  }

  @Test
  fun shutterButton_acceptsClick() {
    setMainContent()

    composeTestRule.onNodeWithTag("shutter-button").assertExists().performClick()
  }

  @Test
  fun fullscreenCameraControls_useMainCaptureControls() {
    composeTestRule.setContent {
      CameraControls(
        progress = 1f,
        fullScreenProgress = 1f,
        copy = CameraPreferences(language = CameraLanguage.English).copyText(),
        onFrameSettingsClick = {},
        onShutterClick = {},
        onClose = {},
      )
    }

    composeTestRule.onAllNodesWithTag("fullscreen-camera-controls").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("capture-style-dock").assertCountEquals(0)
    composeTestRule.onNodeWithTag("shutter-button").assertExists()
  }

  @Test
  fun stageCamera_keepsShutterAndCornerControls() {
    composeTestRule.setContent {
      StageCamera(
        navigationBarHeight = 16.dp,
        maxWidth = 390.dp,
        maxHeight = 840.dp,
        dynamicIslandMetrics = fallbackDynamicIslandMetrics(390.dp),
        hasCameraPermission = false,
        cameraLens = CameraLens.Front,
        copy = CameraPreferences(language = CameraLanguage.English).copyText(),
        onImageCaptureReady = {},
        albumOpen = false,
        albumFlipProgress = 0f,
        latestAlbumPhoto = null,
        captureCurtainProgress = 1f,
        onAlbumClick = {},
        onAlbumLongClick = {},
        onSettingsClick = {},
        onShutterClick = {},
      )
    }

    composeTestRule.onNodeWithTag("stage-camera").assertExists()
    composeTestRule.onNodeWithTag("stage-dynamic-island").assertExists()
    composeTestRule.onNodeWithTag("stage-preview-card").assertExists()
    composeTestRule.onNodeWithTag("stage-shutter-curtain").assertExists()
    composeTestRule.onNodeWithTag("stage-shutter-button").assertExists()
    composeTestRule.onNodeWithTag("stage-album-button").assertExists()
    composeTestRule.onNodeWithTag("stage-settings-button").assertExists()
    composeTestRule.onAllNodesWithTag("stage-tool-row").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("stage-tool-flash").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("stage-tool-frame").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("stage-tool-lens").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("stage-frame-manager-button").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("countdown-value").assertCountEquals(0)
  }

  @Test
  fun photoAiTagChips_showReadyTags() {
    composeTestRule.setContent {
      PhotoAiTagChips(
        aiState =
          PhotoAiState.Ready(
            PhotoInsight(
              title = "Street portrait",
              tags = listOf("portrait", "street", "ignored"),
              subject = "person",
              scene = "urban street",
              colors = listOf("red", "cream"),
              confidence = PhotoInsightConfidence.High,
              suggestedFrameStyle = PhotoFrameStyle.Film,
              frameReason = "Cinematic street mood",
            ),
          ),
      )
    }

    composeTestRule.onNodeWithTag("photo-ai-tag-chips").assertExists()
    composeTestRule.onNodeWithText("portrait").assertExists()
    composeTestRule.onNodeWithText("street").assertExists()
  }

  @Test
  fun soundEffectsOff_hidesToneAndVolumeSettings() {
    composeTestRule.setContent {
      var preferences by remember { mutableStateOf(CameraPreferences()) }
      CameraSettingsScreen(
        preferences = preferences,
        onPreferencesChanged = { preferences = it },
        onClose = {},
      )
    }

    composeTestRule.onNodeWithTag("shutter-sound-style-setting").assertExists()
    composeTestRule.onNodeWithTag("sound-volume-setting").assertExists()

    composeTestRule.onNodeWithTag("sound-effects-off").performScrollTo().performClick()

    composeTestRule.onAllNodesWithTag("shutter-sound-style-setting").assertCountEquals(0)
    composeTestRule.onAllNodesWithTag("sound-volume-setting").assertCountEquals(0)
  }

  @Test
  fun settingsScreen_usesGeminiNanoModelOnly() {
    composeTestRule.setContent {
      var preferences by remember { mutableStateOf(CameraPreferences(language = CameraLanguage.English)) }
      CameraSettingsScreen(
        preferences = preferences,
        onPreferencesChanged = { preferences = it },
        onClose = {},
      )
    }

    composeTestRule.onNodeWithTag("ai-model-setting").assertExists()
    composeTestRule.onNodeWithTag("ai-provider-gemini-nano").assertExists()
    composeTestRule.onNodeWithText("Gemini Nano").assertExists()
    composeTestRule.onAllNodesWithTag("local-vlm-model-panel").assertCountEquals(0)
  }

  @Test
  fun settingsScreen_switchesCameraDisplayStyle() {
    var selectedPreferences = CameraPreferences(language = CameraLanguage.English)
    composeTestRule.setContent {
      var preferences by remember { mutableStateOf(selectedPreferences) }
      CameraSettingsScreen(
        preferences = preferences,
        onPreferencesChanged = {
          preferences = it
          selectedPreferences = it
        },
        onClose = {},
      )
    }

    composeTestRule.onNodeWithTag("camera-display-style-setting").assertExists()
    composeTestRule.onNodeWithTag("camera-display-style-stage_list").performClick()

    composeTestRule.runOnIdle {
      assertEquals(CameraDisplayStyle.StageList, selectedPreferences.cameraDisplayStyle)
    }
  }

  @Test
  fun settingsScreen_switchesDynamicIslandCoverMode() {
    var selectedPreferences = CameraPreferences(language = CameraLanguage.English)
    composeTestRule.setContent {
      var preferences by remember { mutableStateOf(selectedPreferences) }
      CameraSettingsScreen(
        preferences = preferences,
        onPreferencesChanged = {
          preferences = it
          selectedPreferences = it
        },
        onClose = {},
      )
    }

    composeTestRule.onNodeWithTag("dynamic-island-cover-setting").assertExists()
    composeTestRule.onNodeWithTag("dynamic-island-cover-maximum").performScrollTo().performClick()

    composeTestRule.runOnIdle {
      assertEquals(DynamicIslandCoverMode.Maximum, selectedPreferences.dynamicIslandCoverMode)
    }
  }

  @Test
  fun frameSettingsScreen_showsGeneratedFrameConversationControl() {
    composeTestRule.setContent {
      FrameSettingsScreen(
        photo =
          CapturedPhoto(
            id = 1,
            bitmap = BitmapTestFixtures.androidBitmap(),
            frameStyle = PhotoFrameStyle.Stamp,
            aiState =
              PhotoAiState.Ready(
                PhotoInsight(
                  title = "Desk light",
                  tags = listOf("desk", "lamp"),
                  subject = "desk",
                  scene = "workspace",
                  colors = listOf("green", "black"),
                  confidence = PhotoInsightConfidence.High,
                  suggestedFrameStyle = PhotoFrameStyle.Film,
                ),
              ),
          ),
        selectedFrameStyle = PhotoFrameStyle.Stamp,
        copy = CameraPreferences(language = CameraLanguage.English).copyText(),
        customFrameSpecs =
          listOf(
            GeneratedFrameSpec(
              title = "Desk glow",
              baseStyle = PhotoFrameStyle.ColorPop,
              backgroundColor = "teal",
              accentColor = "yellow",
              inkColor = "black",
              motif = GeneratedFrameMotif.Sparkles,
              caption = "desk glow",
              reason = "Bright workspace mood",
            ),
          ),
        onFrameSelected = {},
        onClose = {},
        onSave = {},
      )
    }

    composeTestRule.onNodeWithTag("my-frames-section").performScrollTo().assertExists()
    composeTestRule.onNodeWithTag("my-frame-option").assertExists()
    composeTestRule.onNodeWithTag("ai-frame-generator").assertExists()
    composeTestRule.onNodeWithTag("ai-frame-generate-button").performScrollTo().assertExists()
  }

  @Test
  fun frameManagementScreen_showsCustomFrameLibrary() {
    composeTestRule.setContent {
      FrameManagementScreen(
        customFrameSpecs =
          listOf(
            GeneratedFrameSpec(
              title = "Desk glow",
              baseStyle = PhotoFrameStyle.ColorPop,
              backgroundColor = "teal",
              accentColor = "yellow",
              inkColor = "black",
              motif = GeneratedFrameMotif.Sparkles,
              caption = "desk glow",
              reason = "Bright workspace mood",
            ),
          ),
        selectedGeneratedFrameSpec = null,
        frameGenerationState = FrameGenerationState.Idle,
        copy = CameraPreferences(language = CameraLanguage.English).copyText(),
        onGenerateFrame = {},
        onFrameSelected = {},
        onSetDefaultFrame = {},
        onClose = {},
      )
    }

    composeTestRule.onNodeWithTag("frame-management-screen").assertExists()
    composeTestRule.onNodeWithTag("frame-manager-frame-option").performScrollTo().assertExists()
  }
}

private object BitmapTestFixtures {
  fun androidBitmap(): android.graphics.Bitmap =
    android.graphics.Bitmap.createBitmap(24, 24, android.graphics.Bitmap.Config.ARGB_8888)
}
