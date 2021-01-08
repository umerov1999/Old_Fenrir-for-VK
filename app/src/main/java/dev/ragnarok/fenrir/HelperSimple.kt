package dev.ragnarok.fenrir

import androidx.preference.PreferenceManager

object HelperSimple {
    const val DIALOG_SEND_HELPER = "dialog_send_helper"
    const val NOT_GP_HELPER = "not_gp_helper"
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