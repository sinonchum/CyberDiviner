package com.cyberdiviner.ui.navigation

import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally

/**
 * Screen transition presets for CyberDiviner navigation.
 * All transitions are B&W-appropriate: slides and fades, no color effects.
 */
object ScreenTransitions {
    private const val DURATION = 300

    // Enter from right (going deeper into a sub-screen)
    val slideInFromRight: EnterTransition = slideInHorizontally(
        initialOffsetX = { it },
        animationSpec = tween(DURATION)
    ) + fadeIn(animationSpec = tween(DURATION))

    // Exit to left (being pushed back)
    val slideOutToLeft: ExitTransition = slideOutHorizontally(
        targetOffsetX = { -it / 3 },
        animationSpec = tween(DURATION)
    ) + fadeOut(animationSpec = tween(DURATION))

    // Pop enter from left (coming back)
    val slideInFromLeft: EnterTransition = slideInHorizontally(
        initialOffsetX = { -it },
        animationSpec = tween(DURATION)
    ) + fadeIn(animationSpec = tween(DURATION))

    // Pop exit to right (popping off)
    val slideOutToRight: ExitTransition = slideOutHorizontally(
        targetOffsetX = { it },
        animationSpec = tween(DURATION)
    ) + fadeOut(animationSpec = tween(DURATION))

    // Crossfade for tab switches
    val crossfadeIn: EnterTransition = fadeIn(animationSpec = tween(DURATION))
    val crossfadeOut: ExitTransition = fadeOut(animationSpec = tween(DURATION))
}
