package com.cyberdiviner.ui.navigation

import androidx.compose.foundation.Canvas
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import com.cyberdiviner.ui.almanac.AlmanacScreen
import com.cyberdiviner.ui.archive.ArchiveScreen
import com.cyberdiviner.ui.config.ConfigScreen
import com.cyberdiviner.ui.consult.ConsultScreen
import com.cyberdiviner.ui.epiphany.EpiphanyScreen
import com.cyberdiviner.ui.liuyao.LiuyaoScreen
import com.cyberdiviner.ui.muyu.MuyuScreen
import com.cyberdiviner.ui.oracle.OracleScreen
import com.cyberdiviner.ui.rituals.RitualsMenuScreen
import com.cyberdiviner.ui.splash.SplashScreen
import com.cyberdiviner.ui.tarot.TarotScreen
import com.cyberdiviner.ui.theme.CyberBlack
import com.cyberdiviner.ui.theme.GrayCaption
import com.cyberdiviner.ui.vision.VisionScreen

/**
 * v6.0 Navigation: Bottom navigation bar with three main tabs.
 *
 * splash → epiphany → oracle (default) | rituals → ritual/iching|tarot|vision|muyu | archive
 * Config accessible via gear icon overlay on main tab screens.
 */
object Routes {
    const val SPLASH = "splash"
    const val EPIPHANY = "epiphany"
    const val ORACLE = "oracle"
    const val CONSULT = "consult"
    const val RITUALS = "rituals"
    const val RITUAL_ICHING = "ritual/iching"
    const val RITUAL_TAROT = "ritual/tarot"
    const val RITUAL_VISION = "ritual/vision"
    const val RITUAL_MUYU = "ritual/muyu"
    const val RITUAL_ALMANAC = "ritual/almanac"
    const val ARCHIVE = "archive"
    const val CONFIG = "config"
}

private val bottomNavRoutes = setOf(Routes.ORACLE, Routes.RITUALS, Routes.ARCHIVE)

@Composable
private fun CanvasGearIcon() {
    val color = GrayCaption
    Canvas(modifier = Modifier.size(20.dp)) {
        val sw = 1.5.dp.toPx()
        val cx = size.width / 2f
        val cy = size.height / 2f
        val outerR = size.width * 0.4f
        val innerR = size.width * 0.2f
        // Outer circle (gear body)
        drawCircle(color, radius = outerR, center = Offset(cx, cy), style = Stroke(sw, cap = StrokeCap.Square))
        // Inner circle (hole)
        drawCircle(color, radius = innerR, center = Offset(cx, cy), style = Stroke(sw, cap = StrokeCap.Square))
        // Gear teeth (4 short lines at cardinal points)
        val toothLen = 4.dp.toPx()
        val angles = listOf(0f, 90f, 180f, 270f)
        for (angle in angles) {
            val rad = Math.toRadians(angle.toDouble())
            val startR = outerR
            val endR = outerR + toothLen
            val startX = cx + startR * kotlin.math.cos(rad).toFloat()
            val startY = cy + startR * kotlin.math.sin(rad).toFloat()
            val endX = cx + endR * kotlin.math.cos(rad).toFloat()
            val endY = cy + endR * kotlin.math.sin(rad).toFloat()
            drawLine(color, Offset(startX, startY), Offset(endX, endY), sw, cap = StrokeCap.Square)
        }
    }
}

@Composable
fun CyberDivinerNavGraph(navController: NavHostController) {
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route
    val showBottomBar = currentRoute in bottomNavRoutes

    Scaffold(
        containerColor = CyberBlack,
        bottomBar = {
            if (showBottomBar) {
                BottomNavBar(
                    currentRoute = currentRoute,
                    onNavigate = { route ->
                        navController.navigate(route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                )
            }
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            NavHost(
                navController = navController,
                startDestination = Routes.SPLASH
            ) {
                composable(
                    Routes.SPLASH,
                    enterTransition = { fadeIn(animationSpec = tween(300)) },
                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                ) {
                    SplashScreen(
                        onTimeout = {
                            navController.navigate(Routes.EPIPHANY) {
                                popUpTo(Routes.SPLASH) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    Routes.EPIPHANY,
                    enterTransition = { fadeIn(animationSpec = tween(500)) },
                    exitTransition = { fadeOut(animationSpec = tween(300)) }
                ) {
                    EpiphanyScreen(
                        onEnter = {
                            navController.navigate(Routes.ORACLE) {
                                popUpTo(Routes.EPIPHANY) { inclusive = true }
                            }
                        }
                    )
                }

                composable(
                    Routes.ORACLE,
                    enterTransition = { ScreenTransitions.crossfadeIn },
                    exitTransition = { ScreenTransitions.crossfadeOut },
                    popEnterTransition = { ScreenTransitions.crossfadeIn },
                    popExitTransition = { ScreenTransitions.crossfadeOut }
                ) {
                    OracleScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    Routes.CONSULT,
                    enterTransition = { ScreenTransitions.slideInFromRight },
                    exitTransition = { ScreenTransitions.slideOutToLeft },
                    popEnterTransition = { ScreenTransitions.slideInFromLeft },
                    popExitTransition = { ScreenTransitions.slideOutToRight }
                ) {
                    ConsultScreen(
                        onComplete = { soulHash ->
                            navController.navigate(Routes.RITUALS) {
                                popUpTo(Routes.CONSULT) { inclusive = true }
                            }
                        },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    Routes.RITUALS,
                    enterTransition = { ScreenTransitions.crossfadeIn },
                    exitTransition = { ScreenTransitions.crossfadeOut },
                    popEnterTransition = { ScreenTransitions.crossfadeIn },
                    popExitTransition = { ScreenTransitions.crossfadeOut }
                ) {
                    RitualsMenuScreen(
                        onIChing = { navController.navigate(Routes.RITUAL_ICHING) },
                        onTarot = { navController.navigate(Routes.RITUAL_TAROT) },
                        onVision = { navController.navigate(Routes.RITUAL_VISION) },
                        onMuyu = { navController.navigate(Routes.RITUAL_MUYU) },
                        onAlmanac = { navController.navigate(Routes.RITUAL_ALMANAC) },
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    Routes.RITUAL_ICHING,
                    enterTransition = { ScreenTransitions.slideInFromRight },
                    exitTransition = { ScreenTransitions.slideOutToLeft },
                    popEnterTransition = { ScreenTransitions.slideInFromLeft },
                    popExitTransition = { ScreenTransitions.slideOutToRight }
                ) {
                    LiuyaoScreen(navController = navController)
                }

                composable(
                    Routes.RITUAL_TAROT,
                    enterTransition = { ScreenTransitions.slideInFromRight },
                    exitTransition = { ScreenTransitions.slideOutToLeft },
                    popEnterTransition = { ScreenTransitions.slideInFromLeft },
                    popExitTransition = { ScreenTransitions.slideOutToRight }
                ) {
                    TarotScreen(navController = navController)
                }

                composable(
                    Routes.RITUAL_VISION,
                    enterTransition = { ScreenTransitions.slideInFromRight },
                    exitTransition = { ScreenTransitions.slideOutToLeft },
                    popEnterTransition = { ScreenTransitions.slideInFromLeft },
                    popExitTransition = { ScreenTransitions.slideOutToRight }
                ) {
                    VisionScreen(navController = navController)
                }

                composable(
                    Routes.RITUAL_MUYU,
                    enterTransition = { ScreenTransitions.slideInFromRight },
                    exitTransition = { ScreenTransitions.slideOutToLeft },
                    popEnterTransition = { ScreenTransitions.slideInFromLeft },
                    popExitTransition = { ScreenTransitions.slideOutToRight }
                ) {
                    MuyuScreen(navController = navController)
                }

                composable(
                    Routes.RITUAL_ALMANAC,
                    enterTransition = { ScreenTransitions.slideInFromRight },
                    exitTransition = { ScreenTransitions.slideOutToLeft },
                    popEnterTransition = { ScreenTransitions.slideInFromLeft },
                    popExitTransition = { ScreenTransitions.slideOutToRight }
                ) {
                    AlmanacScreen(onBack = { navController.popBackStack() })
                }

                composable(
                    Routes.ARCHIVE,
                    enterTransition = { ScreenTransitions.crossfadeIn },
                    exitTransition = { ScreenTransitions.crossfadeOut },
                    popEnterTransition = { ScreenTransitions.crossfadeIn },
                    popExitTransition = { ScreenTransitions.crossfadeOut }
                ) {
                    ArchiveScreen(
                        onBack = { navController.popBackStack() }
                    )
                }

                composable(
                    Routes.CONFIG,
                    enterTransition = { ScreenTransitions.slideInFromRight },
                    exitTransition = { ScreenTransitions.slideOutToRight },
                    popEnterTransition = { ScreenTransitions.slideInFromRight },
                    popExitTransition = { ScreenTransitions.slideOutToRight }
                ) {
                    ConfigScreen(onBack = { navController.popBackStack() })
                }
            }

            // Config gear icon — visible only on main tab screens
            if (showBottomBar) {
                IconButton(
                    onClick = { navController.navigate(Routes.CONFIG) },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(top = 8.dp, end = 8.dp)
                ) {
                    CanvasGearIcon()
                }
            }
        }
    }
}
