package com.renju_note.isoo.board

import com.renju_note.isoo.board.Stone

interface BoardManager {

    fun getNowBoardStatus() : ArrayList<Stone>

    fun setNowTextBoxString(text : String)

    fun getNowTextBoxString() : String

    fun getNowIndex() : Int

    fun undo() : Boolean

    fun undoAll() : Boolean

    fun redo() : Boolean

    fun deleteBranch() : Boolean

    fun putStone(x : Int, y : Int) : Boolean

    fun addNewChild(x : Int, y : Int, text : String) : Boolean

    fun <T> loadNodes(tree : T)

}