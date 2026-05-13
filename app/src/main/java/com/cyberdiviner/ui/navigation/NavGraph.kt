package com.cyberdiviner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cyberdiviner.ui.archive.ArchiveScreen
import com.cyberdiviner.ui.home.HomeScreen
import com.cyberdiviner.ui.liuyao.LiuyaoResultScreen
import com.cyberdiviner.ui.liuyao.LiuyaoScreen
import com.cyberdiviner.ui.oracle.OracleScreen
import com.cyberdiviner.ui.rituals.RitualsMenuScreen
import com.cyberdiviner.ui.splash.SplashScreen
import com.cyberdiviner.ui.tarot.TarotScreen
import com.cyberdiviner.ui.vision.VisionScreen

/**
 * v5.1 Navigation: Clean single-page route hierarchy.
 *
 * splash -> home -> oracle | rituals_menu -> ritual/iching|tarot|vision | archive
 */
object Routes {
    const val SPLASH = "splash"
    const val HOME = "home"
    const val ORACLE = "oracle"
    const val RITUALS_MENU = "rituals_menu"
    const val RITUAL_ICHING = "ritual/iching"
    const val RITUAL_TAROT = "ritual/tarot"
    const val RITUAL_VISION = "ritual/vision"
    const val ARCHIVE = "archive"
}

@Composable
fun CyberDivinerNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        composable(Routes.SPLASH) {
            SplashScreen(
                onTimeout = {
                    navController.navigate(Routes.HOME) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        composable(Routes.HOME) {
            HomeScreen(
                onOracle = { navController.navigate(Routes.ORACLE) },
                onRituals = { navController.navigate(Routes.RITUALS_MENU) },
                onArchive = { navController.navigate(Routes.ARCHIVE) }
            )
        }

        composable(Routes.ORACLE) {
            OracleScreen(
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RITUALS_MENU) {
            RitualsMenuScreen(
                onIChing = { navController.navigate(Routes.RITUAL_ICHING) },
                onTarot = { navController.navigate(Routes.RITUAL_TAROT) },
                onVision = { navController.navigate(Routes.RITUAL_VISION) },
                onBack = { navController.popBackStack() }
            )
        }

        composable(Routes.RITUAL_ICHING) {
            LiuyaoScreen(navController = navController)
        }

        composable(Routes.RITUAL_TAROT) {
            TarotScreen(navController = navController)
        }

        composable(Routes.RITUAL_VISION) {
            VisionScreen(navController = navController)
        }

        composable(Routes.ARCHIVE) {
            ArchiveScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
