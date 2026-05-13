package com.cyberdiviner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cyberdiviner.ui.home.HomeScreen
import com.cyberdiviner.ui.liuyao.LiuyaoResultScreen
import com.cyberdiviner.ui.liuyao.LiuyaoScreen
import com.cyberdiviner.ui.muyu.MuyuScreen
import com.cyberdiviner.ui.settings.SettingsScreen
import com.cyberdiviner.ui.tarot.TarotScreen
import com.cyberdiviner.ui.vision.VisionScreen

object Routes {
    const val HOME = "home"
    const val LIUYAO = "liuyao"
    const val LIUYAO_RESULT = "liuyao_result"
    const val TAROT = "tarot"
    const val VISION = "vision"
    const val MUYU = "muyu"
    const val SETTINGS = "settings"
}

@Composable
fun CyberDivinerNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.HOME) {
        composable(Routes.HOME) { HomeScreen(navController) }
        composable(Routes.LIUYAO) { LiuyaoScreen(navController) }
        composable(Routes.LIUYAO_RESULT) { LiuyaoResultScreen(navController) }
        composable(Routes.TAROT) { TarotScreen(navController) }
        composable(Routes.VISION) { VisionScreen(navController) }
        composable(Routes.MUYU) { MuyuScreen(navController) }
        composable(Routes.SETTINGS) { SettingsScreen(navController) }
    }
}
