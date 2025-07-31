package com.renju_note.isoo.setting

import com.renju_note.isoo.util.PreferenceUtil

data class BoardColorSetting (
    var boardColor : String,
    var lineColor : String,
    var lastStoneStrokeColor : String,
    var textColor : String,
    var nodeColor : String,
    var drawLineColor : String,
    var drawAreaColor : String,
    var drawArrowColor : String
) {

    companion object {
        fun getDefaultSetting() : BoardColorSetting {
            val boardColor = "#F2CA94"
            val lineColor = "#666666"
            val lastStoneStrokeColor = "#D32560"
            val textColor = "#0000FF"
            val nodeColor = "#0000FF"
            val drawLineColor = "#FF0000"
            val drawAreaColor = "#2B000000"
            val drawArrowColor = "#65000000"
            return BoardColorSetting(boardColor, lineColor, lastStoneStrokeColor, textColor, nodeColor,
                drawLineColor, drawAreaColor, drawArrowColor)
        }
    }

    fun save(pref : PreferenceUtil) {
        pref.setString("boardColor", boardColor)
        pref.setString("lineColor", lineColor)
        pref.setString("lastStoneStrokeColor", lastStoneStrokeColor)
        pref.setString("BoardTextColor", textColor)
        pref.setString("nodeColor", nodeColor)
        pref.setString("drawLineColor", drawLineColor)
        pref.setString("drawAreaColor", drawAreaColor)
        pref.setString("drawArrowColor", drawArrowColor)
    }

    fun load(pref : PreferenceUtil) {
        boardColor = pref.getString("boardColor", "#F2CA94")
        lineColor = pref.getString("lineColor", "#666666")
        lastStoneStrokeColor = pref.getString("lastStoneStrokeColor", "#D32560")
        textColor = pref.getString("BoardTextColor", "#0000FF")
        nodeColor = pref.getString("nodeColor", "#0000FF")
        drawLineColor = pref.getString("drawLineColor", "#FF0000")
        drawAreaColor = pref.getString("drawAreaColor", "#2B000000")
        drawArrowColor = pref.getString("drawArrowColor", "#65000000")
    }

}