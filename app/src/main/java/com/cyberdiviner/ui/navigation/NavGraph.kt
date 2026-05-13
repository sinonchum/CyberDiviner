package com.cyberdiviner.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.cyberdiviner.ui.consult.ConsultScreen
import com.cyberdiviner.ui.epiphany.EpiphanyScreen
import com.cyberdiviner.ui.terminal.TerminalScreen

/**
 * v4.0 Navigation: Three-layer progressive architecture.
 *
 * Layer 1: Epiphany (Splash) -> tap to enter
 * Layer 2: Terminal (Main hub) -> 3 abstract entries
 * Layer 3: Ritual execution (feature screens)
 */

object Routes {
    // Layer 1
    const val EPIPHANY = "epiphany"
    // Layer 2
    const val TERMINAL = "terminal"
    // Layer 3
    const val CONSULT = "consult"
    const val RITUAL = "ritual"
    const val ARCHIVE = "archive"
    // Legacy (kept for backward compat with old screens)
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
    NavHost(navController = navController, startDestination = Routes.EPIPHANY) {
        // Layer 1: Epiphany (Splash)
        composable(Routes.EPIPHANY) {
            EpiphanyScreen(
                onEnter = {
                    navController.navigate(Routes.TERMINAL) {
                        popUpTo(Routes.EPIPHANY) { inclusive = true }
                    }
                }
            )
        }

        // Layer 2: Terminal (Main hub)
        composable(Routes.TERMINAL) {
            TerminalScreen(
                onConsult = { navController.navigate(Routes.CONSULT) },
                onRitual = { navController.navigate(Routes.RITUAL) },
                onArchive = { navController.navigate(Routes.ARCHIVE) }
            )
        }

        // Layer 3: Consult (Agent interview)
        composable(Routes.CONSULT) {
            ConsultScreen(
                onComplete = { soulHash ->
                    navController.navigate(Routes.TERMINAL) {
                        popUpTo(Routes.CONSULT) { inclusive = true }
                    }
                },
                onBack = { navController.popBackStack() }
            )
        }

        // Layer 3: Ritual (feature selection)
        composable(Routes.RITUAL) {
            com.cyberdiviner.ui.ritual.RitualScreen(
                onBack = { navController.popBackStack() }
            )
        }

        // Layer 3: Archive
        composable(Routes.ARCHIVE) {
            com.cyberdiviner.ui.archive.ArchiveScreen(
                onBack = { navController.popBackStack() }
            )
        }
    }
}
