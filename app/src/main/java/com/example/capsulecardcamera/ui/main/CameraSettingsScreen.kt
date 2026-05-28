package com.example.capsulecardcamera.ui.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTagsAsResourceId
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val SettingsRed = Color(0xFFE23328)
private val SettingsWarmWhite = Color(0xFFFFF4E8)
private val SettingsBlack = Color(0xFF111111)
private val SettingsOrange = Color(0xFFE59C17)

@Composable
internal fun CameraSettingsScreen(
  preferences: CameraPreferences,
  onPreferencesChanged: (CameraPreferences) -> Unit,
  onPreviewSound: (CameraPreferences) -> Unit = {},
  onFrameManagerClick: () -> Unit = {},
  onClose: () -> Unit,
  modifier: Modifier = Modifier,
) {
  val copy = preferences.copyText()
  val topPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
  val bottomPadding = WindowInsets.navigationBars.asPaddingValues().calculateBottomPadding()
  val topBarTop = topPadding + OverlayTopBarTopInset
  val topBarHeight = 44.dp

  Box(
    modifier =
      modifier
        .fillMaxSize()
        .background(SettingsRed)
        .semantics { testTagsAsResourceId = true }
        .testTag("camera-settings-screen"),
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
          .padding(start = 24.dp, top = topBarTop + topBarHeight, end = 24.dp, bottom = bottomPadding + 18.dp),
    ) {
      SettingGroup(
        title = copy.languageLabel,
        modifier = Modifier.padding(top = 28.dp),
      ) {
        SettingOptionsRow {
          SettingOption(
            label = copy.systemLanguageLabel,
            selected = preferences.language == CameraLanguage.System,
            onClick = { onPreferencesChanged(preferences.copy(language = CameraLanguage.System)) },
            modifier = Modifier.weight(1f),
          )
          SettingOption(
            label = copy.englishLanguageLabel,
            selected = preferences.language == CameraLanguage.English,
            onClick = { onPreferencesChanged(preferences.copy(language = CameraLanguage.English)) },
            modifier = Modifier.weight(1f),
          )
          SettingOption(
            label = copy.chineseLanguageLabel,
            selected = preferences.language == CameraLanguage.ChineseSimplified,
            onClick = { onPreferencesChanged(preferences.copy(language = CameraLanguage.ChineseSimplified)) },
            modifier = Modifier.weight(1f),
          )
        }
      }

      SettingGroup(
        title = copy.albumLabel,
        modifier = Modifier.padding(top = 24.dp),
      ) {
        SettingOptionsRow {
          DefaultAlbum.entries.forEach { album ->
            SettingOption(
              label = album.displayName(copy),
              selected = preferences.defaultAlbum == album,
              onClick = { onPreferencesChanged(preferences.copy(defaultAlbum = album)) },
              modifier = Modifier.weight(1f),
            )
          }
        }
      }

      SettingGroup(
        title = copy.lensLabel,
        modifier = Modifier.padding(top = 24.dp),
      ) {
        SettingOptionsRow {
          SettingOption(
            label = copy.frontLensLabel,
            selected = preferences.cameraLens == CameraLens.Front,
            onClick = { onPreferencesChanged(preferences.copy(cameraLens = CameraLens.Front)) },
            modifier = Modifier.weight(1f),
          )
          SettingOption(
            label = copy.backLensLabel,
            selected = preferences.cameraLens == CameraLens.Back,
            onClick = { onPreferencesChanged(preferences.copy(cameraLens = CameraLens.Back)) },
            modifier = Modifier.weight(1f),
          )
        }
      }

      SettingGroup(
        title = copy.cameraDisplayStyleTitle,
        modifier =
          Modifier
            .padding(top = 24.dp)
            .testTag("camera-display-style-setting"),
      ) {
        SettingOptionsRow {
          CameraDisplayStyle.entries.forEach { displayStyle ->
            SettingOption(
              label = displayStyle.displayName(copy),
              selected = preferences.cameraDisplayStyle == displayStyle,
              onClick = { onPreferencesChanged(preferences.copy(cameraDisplayStyle = displayStyle)) },
              modifier =
                Modifier
                  .weight(1f)
                  .testTag("camera-display-style-${displayStyle.storageKey}"),
            )
          }
        }
      }

      SettingGroup(
        title = copy.dynamicIslandCoverTitle,
        modifier =
          Modifier
            .padding(top = 24.dp)
            .testTag("dynamic-island-cover-setting"),
      ) {
        SettingOptionsRow {
          DynamicIslandCoverMode.entries.forEach { coverMode ->
            SettingOption(
              label = coverMode.displayName(copy),
              selected = preferences.dynamicIslandCoverMode == coverMode,
              onClick = { onPreferencesChanged(preferences.copy(dynamicIslandCoverMode = coverMode)) },
              modifier =
                Modifier
                  .weight(1f)
                  .testTag("dynamic-island-cover-${coverMode.storageKey}"),
            )
          }
        }
      }

      SettingGroup(
        title = copy.aiModelLabel,
        modifier =
          Modifier
            .padding(top = 24.dp)
            .testTag("ai-model-setting"),
      ) {
        SettingOptionsRow {
          SettingOption(
            label = copy.aiProviderGeminiLabel,
            selected = true,
            onClick = {},
            modifier =
              Modifier
                .weight(1f)
                .testTag("ai-provider-gemini-nano"),
          )
        }
        Text(
          text = copy.aiProviderDescription,
          color = SettingsWarmWhite.copy(alpha = 0.72f),
          fontSize = 11.sp,
          lineHeight = 15.sp,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      SettingGroup(
        title = copy.myFramesTitle,
        modifier =
          Modifier
            .padding(top = 24.dp)
            .testTag("frame-manager-setting"),
      ) {
        SettingOptionsRow {
          SettingOption(
            label = copy.frameManagerOpenLabel,
            selected = false,
            onClick = onFrameManagerClick,
            modifier =
              Modifier
                .weight(1f)
                .testTag("open-frame-manager"),
          )
        }
        Text(
          text = copy.frameManagerDescription,
          color = SettingsWarmWhite.copy(alpha = 0.72f),
          fontSize = 11.sp,
          lineHeight = 15.sp,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      SettingGroup(
        title = copy.soundEffectsLabel,
        modifier =
          Modifier
            .padding(top = 24.dp)
            .testTag("sound-effects-setting"),
      ) {
        SettingOptionsRow {
          SettingOption(
            label = copy.soundEffectsOnLabel,
            selected = preferences.soundEffectsEnabled,
            onClick = {
              val nextPreferences = preferences.copy(soundEffectsEnabled = true)
              onPreferencesChanged(nextPreferences)
              onPreviewSound(nextPreferences)
            },
            modifier =
              Modifier
                .weight(1f)
                .testTag("sound-effects-on"),
          )
          SettingOption(
            label = copy.soundEffectsOffLabel,
            selected = !preferences.soundEffectsEnabled,
            onClick = { onPreferencesChanged(preferences.copy(soundEffectsEnabled = false)) },
            modifier =
              Modifier
                .weight(1f)
                .testTag("sound-effects-off"),
          )
        }
        Text(
          text = copy.soundCreditLabel,
          color = SettingsWarmWhite.copy(alpha = 0.68f),
          fontSize = 11.sp,
          lineHeight = 14.sp,
          modifier = Modifier.padding(top = 8.dp),
        )
      }

      if (preferences.soundEffectsEnabled) {
        SettingGroup(
          title = copy.shutterSoundLabel,
          modifier =
            Modifier
              .padding(top = 18.dp)
              .testTag("shutter-sound-style-setting"),
        ) {
          SettingOptionsRow {
            ShutterSoundStyle.entries.forEach { style ->
              SettingOption(
                label = copy.shutterSoundStyleLabel(style),
                selected = preferences.shutterSoundStyle == style,
                onClick = {
                  val nextPreferences = preferences.copy(shutterSoundStyle = style)
                  onPreferencesChanged(nextPreferences)
                  onPreviewSound(nextPreferences)
                },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }

        SettingGroup(
          title = copy.soundVolumeLabel,
          modifier =
            Modifier
              .padding(top = 18.dp)
              .testTag("sound-volume-setting"),
        ) {
          SettingOptionsRow {
            SoundEffectVolume.entries.forEach { volume ->
              SettingOption(
                label = copy.soundEffectVolumeLabel(volume),
                selected = preferences.soundEffectVolume == volume,
                onClick = {
                  val nextPreferences = preferences.copy(soundEffectVolume = volume)
                  onPreferencesChanged(nextPreferences)
                  onPreviewSound(nextPreferences)
                },
                modifier = Modifier.weight(1f),
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(24.dp))
    }

    SettingsTopBar(
      title = copy.settingsTitle,
      contentDescription = copy.closeSettingsContentDescription,
      onClose = onClose,
      modifier =
        Modifier
          .align(Alignment.TopCenter)
          .padding(start = 24.dp, top = topBarTop, end = 24.dp)
          .testTag("settings-top-bar"),
    )
  }
}

@Composable
private fun SettingsTopBar(
  title: String,
  contentDescription: String,
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
          .clickable(onClick = onClose)
          .semantics { this.contentDescription = contentDescription },
      contentAlignment = Alignment.Center,
    ) {
      Canvas(modifier = Modifier.size(22.dp)) {
        drawLine(
          color = SettingsWarmWhite,
          start = Offset(size.width * 0.25f, size.height * 0.25f),
          end = Offset(size.width * 0.75f, size.height * 0.75f),
          strokeWidth = 2.4.dp.toPx(),
          cap = StrokeCap.Round,
        )
        drawLine(
          color = SettingsWarmWhite,
          start = Offset(size.width * 0.75f, size.height * 0.25f),
          end = Offset(size.width * 0.25f, size.height * 0.75f),
          strokeWidth = 2.4.dp.toPx(),
          cap = StrokeCap.Round,
        )
      }
    }

    Text(
      text = title,
      color = SettingsWarmWhite,
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
private fun SettingGroup(
  title: String,
  modifier: Modifier = Modifier,
  content: @Composable () -> Unit,
) {
  Column(modifier = modifier.fillMaxWidth()) {
    Text(
      text = title,
      color = SettingsWarmWhite,
      fontSize = 15.sp,
      lineHeight = 20.sp,
      fontWeight = FontWeight.Bold,
      modifier = Modifier.padding(bottom = 10.dp),
    )
    content()
  }
}

@Composable
private fun SettingOptionsRow(content: @Composable RowScope.() -> Unit) {
  Row(
    modifier = Modifier.fillMaxWidth(),
    horizontalArrangement = Arrangement.spacedBy(10.dp),
    content = content,
  )
}

@Composable
private fun SettingOption(
  label: String,
  selected: Boolean,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  enabled: Boolean = true,
) {
  Box(
    modifier =
      modifier
        .height(54.dp)
        .clip(RoundedCornerShape(14.dp))
        .background(
          when {
            !enabled -> SettingsBlack.copy(alpha = 0.1f)
            selected -> SettingsWarmWhite
            else -> SettingsBlack.copy(alpha = 0.18f)
          },
        )
        .border(
          width = if (selected) 2.dp else 1.dp,
          color =
            when {
              !enabled -> Color.White.copy(alpha = 0.08f)
              selected -> SettingsOrange
              else -> Color.White.copy(alpha = 0.14f)
            },
          shape = RoundedCornerShape(14.dp),
        )
        .clickable(enabled = enabled, onClick = onClick)
        .padding(horizontal = 6.dp),
    contentAlignment = Alignment.Center,
  ) {
    Text(
      text = label,
      color =
        when {
          !enabled -> SettingsWarmWhite.copy(alpha = 0.45f)
          selected -> SettingsBlack
          else -> SettingsWarmWhite
        },
      fontSize = 13.sp,
      lineHeight = 16.sp,
      fontWeight = if (selected) FontWeight.Bold else FontWeight.Medium,
      textAlign = TextAlign.Center,
    )
  }
}
