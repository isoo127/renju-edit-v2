package com.renju_note.isoo.setting

import com.renju_note.isoo.util.PreferenceUtil

data class ModeSetting(
    var canUseTextMode : Boolean,
    var canUseDrawMode : Boolean
) {

    companion object {
        fun getDefaultSetting() : ModeSetting {
            val canUseTextMode = true
            val canUseDrawMode = true
            return ModeSetting(canUseTextMode, canUseDrawMode)
        }
    }

    fun save(pref : PreferenceUtil) {
        pref.setString("canUseTextMode", canUseTextMode.toString())
        pref.setString("canUseDrawMode", canUseDrawMode.toString())
    }

    fun load(pref : PreferenceUtil) {
        canUseTextMode = pref.getString("canUseTextMode", "true").toBoolean()
        canUseDrawMode = pref.getString("canUseDrawMode", "true").toBoolean()
    }

}