package dev.ragnarok.fenrir.view.verticalswipe

import kotlin.math.min

/**
 *  Restricts movement down a part of the view height
 *  @param maxFraction maximum position limit factor
 *  @param minFraction upward progress factor
 */
class BelowFractionalClamp(
        private val maxFraction: Float = 1f,
        private val minFraction: Float = 1f
) : VerticalClamp {

    init {
        require(maxFraction > 0)
        require(minFraction > 0)
    }

    private var originTop: Int = -1

    override fun onViewCaptured(top: Int) {
        originTop = top
    }

    override fun constraint(height: Int, top: Int, dy: Int): Int {
        return min(top.toFloat(), originTop + height * maxFraction).toInt()
    }

    override fun downCast(distance: Int, top: Int, height: Int, dy: Int): Float {
        return distance / (height * maxFraction)
    }

    override fun upCast(distance: Int, top: Int, height: Int, dy: Int): Float {
        return distance / (height * minFraction)
    }
}
