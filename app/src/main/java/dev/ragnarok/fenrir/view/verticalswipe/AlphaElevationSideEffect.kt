package dev.ragnarok.fenrir.view.verticalswipe

import android.view.View
import kotlin.math.abs

/**
 * Changing alpha and elevation of view
 */
class AlphaElevationSideEffect : SideEffect {

    private var elevation: Float = 0f

    override fun onViewCaptured(child: View) {
        elevation = child.elevation
    }

    override fun apply(child: View, factor: Float) {
        child.elevation = elevation * (1f - abs(factor)) // special for elevation-aware view
        child.alpha = 1f - abs(factor)
    }
}
