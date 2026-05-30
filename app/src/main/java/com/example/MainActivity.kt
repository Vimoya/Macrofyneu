package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import com.example.ui.screens.MacrofyAppContent
import com.example.ui.theme.MyApplicationTheme
import com.example.viewmodel.MacrofyViewModel

class MainActivity : ComponentActivity() {
  private val viewModel: MacrofyViewModel by viewModels()

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    try {
      com.example.data.PlayBillingHelper.initBilling(applicationContext)
    } catch (t: Throwable) {
      android.util.Log.e("MainActivity", "Failed to init billing", t)
    }
    enableEdgeToEdge()
    setContent {
      MyApplicationTheme {
        Surface(
          modifier = Modifier.fillMaxSize(),
          color = com.example.ui.theme.ObsidianDarkBg
        ) {
          MacrofyAppContent(viewModel)
        }
      }
    }
  }
}
