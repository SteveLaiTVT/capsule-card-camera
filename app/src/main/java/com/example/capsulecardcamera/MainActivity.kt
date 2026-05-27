package com.example.capsulecardcamera

import android.os.Build
import android.os.Bundle
import android.view.Window
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import com.example.capsulecardcamera.theme.CapsuleCardCameraTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)

    enableCameraEdgeToEdge()
    setContent {
      CapsuleCardCameraTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = MaterialTheme.colorScheme.background,
        ) {
          MainNavigation()
        }
      }
    }
  }
}

private fun ComponentActivity.enableCameraEdgeToEdge() {
  enableEdgeToEdge(
    statusBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
    navigationBarStyle = SystemBarStyle.auto(android.graphics.Color.TRANSPARENT, android.graphics.Color.TRANSPARENT),
  )
  window.configureCameraEdgeToEdge()
}

private fun Window.configureCameraEdgeToEdge() {
  WindowCompat.setDecorFitsSystemWindows(this, false)

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
    attributes =
      attributes.apply {
        layoutInDisplayCutoutMode =
          if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_ALWAYS
          } else {
            WindowManager.LayoutParams.LAYOUT_IN_DISPLAY_CUTOUT_MODE_SHORT_EDGES
          }
      }
  }

  if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
    isNavigationBarContrastEnforced = false
  }
}
