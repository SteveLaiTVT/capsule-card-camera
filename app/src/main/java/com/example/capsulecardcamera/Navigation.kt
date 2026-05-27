package com.example.capsulecardcamera

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.runtime.rememberNavBackStack
import androidx.navigation3.ui.NavDisplay
import com.example.capsulecardcamera.ui.main.MainScreen

@Composable
fun MainNavigation() {
  val backStack = rememberNavBackStack(CameraHome)

  NavDisplay(
    backStack = backStack,
    onBack = {
      if (backStack.size > 1) {
        backStack.removeLastOrNull()
      }
    },
    entryProvider =
      entryProvider {
        entry<CameraHome> {
          MainScreen(modifier = Modifier.fillMaxSize())
        }
      },
  )
}
