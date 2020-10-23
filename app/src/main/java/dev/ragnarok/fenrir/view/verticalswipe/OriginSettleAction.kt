package dev.ragnarok.fenrir.view.verticalswipe

import android.view.View
import androidx.customview.widget.ViewDragHelper

/**
 * When the gesture is complete, it moves the view to the starting position
 */
class OriginSettleAction : PostAction {

    private var originTop: Int = -1

    override fun onViewCaptured(child: View) {
        originTop = child.top
    }

    override fun releasedBelow(helper: ViewDragHelper, diff: Int, child: View): Boolean {
        return helper.settleCapturedViewAt(child.left, originTop)
    }

    override fun releasedAbove(helper: ViewDragHelper, diff: Int, child: View): Boolean {
        return helper.settleCapturedViewAt(child.left, originTop)
    }
}
