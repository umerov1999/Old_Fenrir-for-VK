package dev.ragnarok.fenrir.view.verticalswipe

/**
 * Reduces each move by several times and delegates the definition of the restriction
 * @param upSensitivity Sensitivity when moving up
 * @param delegate delegate
 * @param downSensitivity Sensitivity when moving down
 */
class SensitivityClamp(
        private val upSensitivity: Float = 1f,
        private val delegate: VerticalClamp,
        private val downSensitivity: Float = 1f
) : VerticalClamp by delegate {

    override fun constraint(height: Int, top: Int, dy: Int): Int {
        val coefficient = if (dy > 0) downSensitivity else upSensitivity
        val newDy = (dy * coefficient).toInt()
        val newTop = top - dy + newDy
        return delegate.constraint(height, newTop, newDy)
    }
}
