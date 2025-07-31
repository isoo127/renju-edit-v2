package com.renju_note.isoo.setting

import com.renju_note.isoo.util.PreferenceUtil

data class TextAreaSetting (
    var isVisible : Boolean,
    var backgroundColor : String,
    var strokeColor : String,
    var textColor : String
) {

    companion object {
        fun getDefaultSetting() : TextAreaSetting {
            val isVisible = true
            val backgroundColor = "#99BBFF"
            val strokeColor = "#99BBFF"
            val textColor = "#000000"
            return TextAreaSetting(isVisible, backgroundColor, strokeColor, textColor)
        }
    }

    fun save(pref : PreferenceUtil) {
        pref.setString("isVisible", isVisible.toString())
        pref.setString("backgroundColor", backgroundColor)
        pref.setString("strokeColor", strokeColor)
        pref.setString("TextAreaTextColor", textColor)
    }

    fun load(pref : PreferenceUtil) {
        isVisible = pref.getString("isVisible", "true").toBoolean()
        backgroundColor = pref.getString("backgroundColor", "#99BBFF")
        strokeColor = pref.getString("strokeColor", "#99BBFF")
        textColor = pref.getString("TextAreaTextColor", "#000000")
    }

}