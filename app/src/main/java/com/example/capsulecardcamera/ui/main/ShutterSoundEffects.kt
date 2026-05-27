package com.example.capsulecardcamera.ui.main

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.platform.LocalContext
import com.example.capsulecardcamera.R

@Composable
internal fun rememberShutterSoundEffectPlayer(): ShutterSoundEffectPlayer {
  val applicationContext = LocalContext.current.applicationContext
  val player = remember(applicationContext) { ShutterSoundEffectPlayer(applicationContext) }
  DisposableEffect(player) {
    onDispose { player.release() }
  }
  return player
}

internal class ShutterSoundEffectPlayer(context: Context) {
  private val soundPool =
    SoundPool
      .Builder()
      .setMaxStreams(1)
      .setAudioAttributes(
        AudioAttributes
          .Builder()
          .setUsage(AudioAttributes.USAGE_ASSISTANCE_SONIFICATION)
          .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
          .build(),
      )
      .build()
  private var loaded = false
  private val soundId = soundPool.load(context, R.raw.shutter_classic_cc0, 1)

  init {
    soundPool.setOnLoadCompleteListener { _, loadedSoundId, status ->
      if (loadedSoundId == soundId && status == 0) {
        loaded = true
      }
    }
  }

  fun play(preferences: CameraPreferences) {
    if (!preferences.soundEffectsEnabled || !loaded) return

    val volume = (preferences.soundEffectVolume.gain * preferences.shutterSoundStyle.gain).coerceIn(0f, 1f)
    soundPool.play(soundId, volume, volume, 1, 0, preferences.shutterSoundStyle.playbackRate)
  }

  fun release() {
    soundPool.release()
  }
}
