package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.PooshakApp
import com.example.ui.PooshakViewModel
import com.example.ui.theme.MyApplicationTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Request immersive Edge-To-Edge content drawing under system bars
        enableEdgeToEdge()

        // Direct instantiation of shared state core
        val viewModel = PooshakViewModel(application)

        setContent {
            MyApplicationTheme {
                PooshakApp(viewModel = viewModel)
            }
        }
    }
}
