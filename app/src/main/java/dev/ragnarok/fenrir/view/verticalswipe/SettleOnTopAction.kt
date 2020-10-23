package dev.ragnarok.fenrir.view.verticalswipe

import android.view.View
import androidx.customview.widget.ViewDragHelper

/**
 * When view moved downwards, it returns to the initial position.
 * Moves above - takes away from the screen.
 */
@Suppress("unused")
class SettleOnTopAction : PostAction {

    private var originTop: Int = -1

    override fun onViewCaptured(child: View) {
        originTop = child.top
    }

    override fun releasedBelow(helper: ViewDragHelper, diff: Int, child: View): Boolean {
        return helper.settleCapturedViewAt(child.left, originTop)
    }

    override fun releasedAbove(helper: ViewDragHelper, diff: Int, child: View): Boolean {
        return helper.settleCapturedViewAt(child.left, if (diff < 0) -child.height else child.height)
    }
}
