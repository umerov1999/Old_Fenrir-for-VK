package dev.ragnarok.fenrir.view.verticalswipe

import android.view.View
import androidx.customview.widget.ViewDragHelper

/**
 * Responsible for changing the view position after the gesture is completed
 */
interface PostAction {

    fun onViewCaptured(child: View)

    /**
     * View was released below initial position
     * @param helper motion animation "visitor"
     * @param diff released distance
     * @param child target view
     * @return whether or not the motion settle was triggered
     */
    fun releasedBelow(helper: ViewDragHelper, diff: Int, child: View): Boolean

    /**
     * View was released above initial position
     * @param helper motion animation "visitor"
     * @param diff released distance
     * @param child target view
     * @return whether or not the motion settle was triggered
     */
    fun releasedAbove(helper: ViewDragHelper, diff: Int, child: View): Boolean
}
