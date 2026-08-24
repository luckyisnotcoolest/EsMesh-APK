package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.example.ui.navigation.EsMeshMainScreen
import com.example.ui.theme.EsMeshTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val app = application as EsMeshApplication

        setContent {
            EsMeshTheme {
                EsMeshMainScreen(app = app)
            }
        }
    }
}
