package dev.ragnarok.fenrir.view.verticalswipe

import android.view.View

/**
 * Change of view properties depending on the progress of movement.
 * @see VerticalClamp
 */
interface SideEffect {

    fun onViewCaptured(child: View)

    /**
     * Apply new property value for [child] depends on [factor]
     * @param child target movement
     * @param factor movement progress, from 0 to 1
     * @see [VerticalClamp.downCast]
     * @see [VerticalClamp.upCast]
     */
    fun apply(child: View, factor: Float)
}
