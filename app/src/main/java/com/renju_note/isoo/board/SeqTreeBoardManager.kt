package com.renju_note.isoo.board

import com.renju_note.isoo.board.Stone

class SeqTreeBoardManager : BoardManager {

    private var seqTree = SeqTree()
    private var nowIndex = 1

    fun getSeqTree() = seqTree

    override fun getNowBoardStatus(): ArrayList<Stone> {
        val status = ArrayList<Stone>()
        for(i in 0..14) {
            for(j in 0..14) {
                if(seqTree.now_board[i][j] > 0) {
                    status.add(
                        Stone(
                            i,
                            j,
                            (seqTree.now_board[i][j] - 1).toString(),
                            Stone.Type.BLACK
                        )
                    )
                } else if(seqTree.now_board[i][j] < 0) {
                    status.add(
                        Stone(
                            i,
                            j,
                            (seqTree.now_board[i][j] * (-1) - 1).toString(),
                            Stone.Type.WHITE
                        )
                    )
                }
            }
        }
        var tmp = seqTree.now.child
        while(tmp != null) {
            val text = tmp.text ?: ""
            status.add(Stone(tmp.x, tmp.y, text, Stone.Type.CHILD))
            tmp = tmp.next
        }
        return status
    }

    override fun setNowTextBoxString(text: String) {
        seqTree.now.boxText = text
    }

    override fun getNowTextBoxString(): String {
        return seqTree.now.boxText ?: ""
    }

    fun getSequence(boardStatus : ArrayList<Stone>): ArrayList<Stone> {
        boardStatus.removeIf { stone -> stone.type == Stone.Type.CHILD }
        boardStatus.sortWith(compareBy { it.text.toInt() })
        return boardStatus
    }

    override fun getNowIndex(): Int {
        return nowIndex
    }

    override fun undo(): Boolean {
        if(nowIndex != 1) {
            seqTree.undo(seqTree.now)
            nowIndex--
            return true
        }
        return false
    }

    override fun undoAll(): Boolean {
        val result = undo()
        while (seqTree.now !== seqTree.head && seqTree.now.child.next == null) {
            undo()
        }
        return result
    }

    override fun redo(): Boolean {
        if (seqTree.now.child != null && seqTree.now.child.next == null) {
            nowIndex++
            seqTree.redo(seqTree.now, nowIndex)
            return true
        }
        return false
    }

    override fun deleteBranch(): Boolean {
        if(nowIndex != 1) {
            seqTree.delete(seqTree.now)
            nowIndex--
            return true
        }
        return false
    }

    override fun putStone(x: Int, y: Int): Boolean {
        if(seqTree.now_board[x][y] == 0) {
            nowIndex++
            seqTree.next(seqTree.now, x, y, nowIndex)
            return true
        }
        return false
    }

    override fun addNewChild(x: Int, y: Int, text: String): Boolean {
        if(seqTree.now_board[x][y] == 0) {
            if (seqTree.now.child == null) {
                seqTree.createChild(seqTree.now, x, y)
                seqTree.now.child.text = text
            } else {
                var temp = seqTree.now.child
                while (temp != null) {
                    if (temp.x == x && temp.y == y) {
                        temp.text = text
                        break
                    }
                    if (temp.next == null) {
                        seqTree.createNext(temp, seqTree.now, x, y)
                        temp.next.text = text
                        break
                    }
                    temp = temp.next
                }
            }
            return true
        }
        return false
    }

    override fun <T> loadNodes(tree: T) {
        if(tree is SeqTree)
            seqTree = tree
        if (seqTree.text_box != null) { // for loading last ver text
            seqTree.head.boxText = seqTree.text_box
        }
        val seq = getSequence(getNowBoardStatus())
        if(seq.isNotEmpty()) {
            if(seq.first().text == seq.last().text) {
                seqTree.setNow_boardTo0()
                seqTree.now = seqTree.head
                nowIndex = 1
            } else {
                nowIndex = seq.last().text.toInt() + 1
            }
        } else {
            seqTree.setNow_boardTo0()
            seqTree.now = seqTree.head
            nowIndex = 1
        }
    }

}