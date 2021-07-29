package dev.ragnarok.fenrir.util

import androidx.preference.PreferenceManager
import dev.ragnarok.fenrir.Injection

object HelperSimple {
    const val DIALOG_SEND_HELPER = "dialog_send_helper"
    const val PLAYLIST_HELPER = "playlist_helper"
    const val LOLLIPOP_21 = "lollipop21"
    const val DEDICATED_COUNTER = "dedicated_counter"
    fun needHelp(key: String, count: Int): Boolean {
        val app = Injection.provideApplicationContext()
        val ret = PreferenceManager.getDefaultSharedPreferences(app).getInt(key, 0)
        if (ret < count) {
            PreferenceManager.getDefaultSharedPreferences(app).edit().putInt(key, ret + 1).apply()
            return true
        }
        return false
    }
}
