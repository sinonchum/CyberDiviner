package com.cyberdiviner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cyberdiviner.ui.archive.ArchiveScreen
import com.cyberdiviner.ui.consult.ConsultScreen
import com.cyberdiviner.ui.epiphany.EpiphanyScreen
import com.cyberdiviner.ui.liuyao.LiuyaoResultScreen
import com.cyberdiviner.ui.liuyao.LiuyaoScreen
import com.cyberdiviner.ui.settings.SettingsScreen
import com.cyberdiviner.ui.tarot.TarotScreen
import com.cyberdiviner.ui.terminal.TerminalScreen
import com.cyberdiviner.ui.vision.VisionScreen

/**
 * v5.0 Navigation: Flat route map with ritual sub-paths.
 *
 * Splash → Terminal hub → feature screens.
 * Ritual entry points route to specific feature screens directly.
 */

object Routes {
    const val SPLASH = "splash"
    const val TERMINAL = "terminal"
    const val AGENT_CHAT = "agent_chat"
    const val RITUAL_ICHING = "ritual/iching"
    const val RITUAL_TAROT = "ritual/tarot"
    const val RITUAL_VISION = "ritual/vision"
    const val ARCHIVE_LIST = "archive_list"
    const val LIUYAO = "liuyao"
    const val LIUYAO_RESULT = "liuyao_result"
    const val SETTINGS = "settings"
    // Legacy routes kept for HomeScreen compatibility (dead code)
    const val TAROT = "ritual/tarot"
    const val VISION = "ritual/vision"
    const val MUYU = "muyu"
}

@Composable
fun CyberDivinerNavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = Routes.SPLASH) {

        // Splash / Epiphany entry
        composable(Routes.SPLASH) {
            EpiphanyScreen(
                onEnter = {
                    navController.navigate(Routes.TERMINAL) {
                        popUpTo(Routes.SPLASH) { inclusive = true }
                    }
                }
            )
        }

        // Terminal hub
        composable(Routes.TERMINAL) {
            TerminalScreen(
                onConsult = { navController.navigate(Routes.AGENT_CHAT) },
                onRitual = { navController.navigate(Routes.RITUAL_ICHING) },
                onArchive = { navController.navigate(Routes.ARCHIVE_LIST) }
            )
        }

        // Agent chat (consultation interview)
        composable(Routes.AGENT_CHAT) {
            ConsultScreen(
                onComplete = { soulHash ->
                    navController.navigate(Routes.TERMINAL) {
                        popUpTo(Routes.AGENT_CHAT) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Ritual sub-paths — each routes to its specific feature screen
        composable(Routes.RITUAL_ICHING) {
            LiuyaoScreen(navController = navController)
        }

        composable(Routes.RITUAL_TAROT) {
            TarotScreen(navController = navController)
        }

        composable(Routes.RITUAL_VISION) {
            VisionScreen(navController = navController)
        }

        // Archive list
        composable(Routes.ARCHIVE_LIST) {
            ArchiveScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Internal navigation targets (Liuyao flow)
        composable(Routes.LIUYAO) {
            LiuyaoScreen(navController = navController)
        }

        composable(Routes.LIUYAO_RESULT) {
            LiuyaoResultScreen(navController = navController)
        }

        // Settings
        composable(Routes.SETTINGS) {
            SettingsScreen(navController = navController)
        }
    }
}
