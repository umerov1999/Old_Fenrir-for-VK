package dev.ragnarok.fenrir.view.verticalswipe

import android.view.View
import kotlin.math.abs

/**
 * Applies the [delegate] only if view moves upwards
 */
@Suppress("unused")
class NegativeFactorFilterSideEffect(private val delegate: SideEffect) : SideEffect by delegate {

    override fun apply(child: View, factor: Float) {
        if (factor < 0) {
            delegate.apply(child, abs(factor))
        }
    }
}
