package com.cyberdiviner

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.view.WindowCompat
import androidx.navigation.compose.rememberNavController
import com.cyberdiviner.ui.navigation.CyberDivinerNavGraph
import com.cyberdiviner.ui.theme.CyberDivinerTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)
        enableEdgeToEdge()
        setContent {
            CyberDivinerTheme {
                val navController = rememberNavController()
                CyberDivinerNavGraph(navController)
            }
        }
    }
}
