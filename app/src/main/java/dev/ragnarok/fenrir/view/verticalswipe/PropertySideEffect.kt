package dev.ragnarok.fenrir.view.verticalswipe

import android.util.Property
import android.view.View
import kotlin.math.abs

/**
 * Common way for changing several properties of view at the same time
 */
class PropertySideEffect(vararg props: Property<View, Float>) : SideEffect {

    private val properties = props
    private val capturedValues: Array<Float> = Array(props.size) { 0f }

    override fun onViewCaptured(child: View) {
        for ((index, property) in properties.withIndex()) {
            val value = property.get(child)
            capturedValues[index] = value
        }
    }

    override fun apply(child: View, factor: Float) {
        for ((index, property) in properties.withIndex()) {
            val value = capturedValues[index] * (1 - abs(factor))
            property.set(child, value)
        }
    }
}
