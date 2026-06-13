package com.example

import android.app.Application
import android.content.Context
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.test.core.app.ApplicationProvider
import com.example.ui.PooshakApp
import com.example.ui.PooshakViewModel
import com.example.ui.theme.MyApplicationTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  @get:Rule
  val composeTestRule = createComposeRule()

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("Pooshak", appName)
  }

  @Test
  fun `test viewModel initialization`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = PooshakViewModel(app)
    assert(viewModel != null)
  }

  @Test
  fun `test full app rendering on startup`() {
    val app = ApplicationProvider.getApplicationContext<Application>()
    val viewModel = PooshakViewModel(app)
    
    composeTestRule.setContent {
      MyApplicationTheme {
        PooshakApp(viewModel = viewModel)
      }
    }
    
    // Allow any initial launched effects/delays to complete or at least settle
    composeTestRule.waitForIdle()
  }
}

