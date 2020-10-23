package dev.ragnarok.fenrir.view.verticalswipe

import kotlin.math.max
import kotlin.math.min

/**
 * Restricts movement up and down by part of the view height
 * @param maxFraction maximum position limit factor
 * @param minFraction minimum position limit factor
 */
class FractionClamp(
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
        val min = min(top, originTop + (height * minFraction).toInt())
        return max(min, originTop - (height * maxFraction).toInt())
    }

    override fun downCast(distance: Int, top: Int, height: Int, dy: Int): Float {
        return distance / (height * maxFraction)
    }

    override fun upCast(distance: Int, top: Int, height: Int, dy: Int): Float {
        return distance / (height * minFraction)
    }
}
