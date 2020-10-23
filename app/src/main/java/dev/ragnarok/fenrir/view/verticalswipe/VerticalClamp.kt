package dev.ragnarok.fenrir.view.verticalswipe

/**
 * Sets limits on moving the view vertically
 */
interface VerticalClamp {

    fun onViewCaptured(top: Int)

    /**
     * Limits maximum and/or minimum position for view
     * @param height height of view
     * @param top position of view
     * @param dy last movement of view
     * @return new position for view, see [android.view.View.getTop]
     */
    fun constraint(height: Int, top: Int, dy: Int): Int

    /**
     * Calculate movement progress down
     * @param distance total distance
     * @param top position of view
     * @param height height of view
     * @param dy last movement of view
     * @return movement progress down from 0 to 1
     * @see [SideEffect.apply]
     */
    fun downCast(distance: Int, top: Int, height: Int, dy: Int): Float

    /**
     * Calculate movement progress up
     * @param distance total distance
     * @param top position of view
     * @param height height of view
     * @param dy last movement of view
     * @return movement progress up from 0 to 1
     * @see [SideEffect.apply]
     */
    fun upCast(distance: Int, top: Int, height: Int, dy: Int): Float
}
