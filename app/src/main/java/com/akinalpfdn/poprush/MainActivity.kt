package com.akinalpfdn.poprush

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.akinalpfdn.poprush.game.presentation.GameScreen
import com.akinalpfdn.poprush.ui.theme.PopRushTheme
import dagger.hilt.android.AndroidEntryPoint

/**
 * Main activity for the PopRush mobile game.
 * Sets up Hilt dependency injection and renders the GameScreen.
 */
@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PopRushTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = Color.White // Force white background
                ) {
                    GameScreen()
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
fun GameScreenPreview() {
    PopRushTheme {
        Surface(
            modifier = Modifier.fillMaxSize(),
            color = MaterialTheme.colorScheme.background
        ) {
            // Note: This preview won't work fully with Hilt injection
            // In a real app, you'd create preview composable with mock data
            GameScreen()
        }
    }
}