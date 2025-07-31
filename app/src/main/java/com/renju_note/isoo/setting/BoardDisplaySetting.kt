package com.renju_note.isoo.setting

import com.renju_note.isoo.util.PreferenceUtil

data class BoardDisplaySetting (
    var sequenceVisible : Boolean,
    var nextNodeVisible : Boolean
) {

    var startPoint = 0

    companion object {
        fun getDefaultSetting() : BoardDisplaySetting {
            val sequenceVisible = true
            val nextNodeVisible = true
            return BoardDisplaySetting(sequenceVisible, nextNodeVisible)
        }
    }

    fun save(pref : PreferenceUtil) {
        pref.setString("sequenceVisible", sequenceVisible.toString())
        pref.setString("nextNodeVisible", nextNodeVisible.toString())
    }

    fun load(pref : PreferenceUtil) {
        sequenceVisible = pref.getString("sequenceVisible", "true").toBoolean()
        nextNodeVisible = pref.getString("nextNodeVisible", "true").toBoolean()
        startPoint = 0
    }

}
