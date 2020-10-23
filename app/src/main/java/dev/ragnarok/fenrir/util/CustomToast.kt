package dev.ragnarok.fenrir.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Color
import android.view.Gravity
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.cardview.widget.CardView
import androidx.core.graphics.ColorUtils
import dev.ragnarok.fenrir.R
import dev.ragnarok.fenrir.settings.CurrentTheme


class CustomToast private constructor(context: Context?, Timage: Bitmap?) {
    private val M_context: Context?
    private var duration: Int
    private var image: Bitmap?
    fun setDuration(duration: Int): CustomToast {
        this.duration = duration
        return this
    }

    fun setBitmap(Timage: Bitmap?): CustomToast {
        image = Timage
        return this
    }

    fun showToast(message: String?) {
        if (M_context == null) return
        val t = getToast(M_context, message, CurrentTheme.getColorToast(M_context))
        t.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, 15)
        t.show()
    }

    fun showToast(@StringRes message: Int, vararg params: Any?) {
        if (M_context == null) return
        showToast(M_context.resources.getString(message, *params))
    }

    fun showToastBottom(message: String?) {
        if (M_context == null) return
        val t = getToast(M_context, message, CurrentTheme.getColorToast(M_context))
        t.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 40)
        t.show()
    }

    fun showToastBottom(@StringRes message: Int, vararg params: Any?) {
        if (M_context == null) return
        showToastBottom(M_context.resources.getString(message, *params))
    }

    fun showToastSuccessBottom(message: String?) {
        if (M_context == null) return
        val t = getToast(M_context, message, Color.parseColor("#AA48BE2D"))
        t.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.BOTTOM, 0, 40)
        t.show()
    }

    fun showToastSuccessBottom(@StringRes message: Int, vararg params: Any?) {
        if (M_context == null) return
        showToastSuccessBottom(M_context.resources.getString(message, *params))
    }

    fun showToastInfo(message: String?) {
        if (M_context == null) return
        val t = getToast(M_context, message, Utils.getThemeColor(true))
        t.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, 15)
        t.show()
    }

    fun showToastInfo(@StringRes message: Int, vararg params: Any?) {
        if (M_context == null) return
        showToastInfo(M_context.resources.getString(message, *params))
    }

    @Suppress("DEPRECATION")
    fun showToastError(message: String?) {
        if (M_context == null) return
        val view = View.inflate(M_context, R.layout.toast_error, null)
        val subtitle = view.findViewById<TextView>(R.id.text)
        val imagev = view.findViewById<ImageView>(R.id.icon_toast_error)
        if (image != null)
            imagev.setImageBitmap(image)
        subtitle.text = message
        val toast = Toast(M_context)
        toast.duration = duration
        toast.view = view
        toast.setGravity(Gravity.CENTER_HORIZONTAL or Gravity.TOP, 0, 0)
        toast.show()
    }

    fun showToastError(@StringRes message: Int, vararg params: Any?) {
        if (M_context == null) return
        showToastError(M_context.resources.getString(message, *params))
    }

    companion object {
        @JvmStatic
        fun CreateCustomToast(context: Context?): CustomToast {
            return CustomToast(context, null)
        }
    }

    @Suppress("DEPRECATION")
    private fun getToast(context: Context, message: String?, bgColor: Int): Toast {
        val toast = Toast(context)
        val view: View = View.inflate(context, R.layout.custom_toast_base, null)
        val cardView: CardView = view.findViewById(R.id.toast_card_view)
        cardView.setCardBackgroundColor(bgColor)
        val textView: AppCompatTextView = view.findViewById(R.id.toast_text_view)
        if (message != null) textView.text = message
        if (isColorDark(bgColor)) textView.setTextColor(Color.WHITE)
        toast.view = view
        val iconIV: AppCompatImageView = view.findViewById(R.id.toast_image_view)
        if (image != null)
            iconIV.setImageBitmap(image)
        else
            iconIV.setImageResource(R.mipmap.ic_launcher_round)
        toast.duration = duration
        return toast
    }

    private fun isColorDark(color: Int): Boolean {
        return ColorUtils.calculateLuminance(color) < 0.5
    }

    init {
        duration = Toast.LENGTH_SHORT
        M_context = context
        image = Timage
    }
}
