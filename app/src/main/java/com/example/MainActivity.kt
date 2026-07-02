package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.data.AppDatabase
import com.example.data.EqRepository
import com.example.ui.EqViewModel
import com.example.ui.EqViewModelFactory
import com.example.ui.HammerEqScreen
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    enableEdgeToEdge()

    // Initialize Room Database & Repository
    val database = AppDatabase.getDatabase(applicationContext)
    val repository = EqRepository(database.eqPresetDao())

    setContent {
      // Default to Dark Mode because premium audio/hardware apps look gorgeous in dark theme,
      // but let the user toggle seamlessly.
      var isDarkTheme by rememberSaveable { mutableStateOf(true) }

      MyApplicationTheme(darkTheme = isDarkTheme) {
        val viewModel: EqViewModel = viewModel(
          factory = EqViewModelFactory(repository)
        )
        HammerEqScreen(
          viewModel = viewModel,
          isDarkTheme = isDarkTheme,
          onThemeToggle = { isDarkTheme = it },
          modifier = Modifier.fillMaxSize()
        )
      }
    }
  }
}

