package com.renju_note.isoo

import com.renju_note.isoo.board.SeqTreeBoardManager
import com.renju_note.isoo.board.Stone
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.io.ByteArrayInputStream
import java.io.ByteArrayOutputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.ObjectStreamClass

/**
 * Board files are SeqTree written with Java serialization, so files saved by released
 * versions must keep loading. The fixtures in resources/seqtree were written by the
 * SeqTree.java source of release 1.3.1 (commit 1de3b29), mutated the way 1.3.1's
 * SeqTreeBoardManager did. v1_2_3_text_box.bin additionally sets the legacy text_box field.
 * v1_3_1_device_*.bin were saved by the 1.3.1 app itself on an emulator:
 * H8 I8 I7 G9 J6 with a memo on G9, then a branch F6 (memo, hint "A" on E11) after I7.
 */
class SeqTreeCompatibilityTest {

    private fun readTree(bytes: ByteArray): SeqTree =
        ObjectInputStream(ByteArrayInputStream(bytes)).use { it.readObject() as SeqTree }

    private fun fixture(name: String): ByteArray =
        requireNotNull(javaClass.getResourceAsStream("/seqtree/$name")) { "missing fixture $name" }
            .use { it.readBytes() }

    private fun load(bytes: ByteArray) = SeqTreeBoardManager().apply { loadNodes(readTree(bytes)) }

    private fun SeqTreeBoardManager.stones() = getNowBoardStatus()
        .filter { it.type != Stone.Type.CHILD }
        .map { "${it.x},${it.y} ${it.type} ${it.text}" }
        .toSet()

    private fun SeqTreeBoardManager.hints() = getNowBoardStatus()
        .filter { it.type == Stone.Type.CHILD }
        .map { "${it.x},${it.y} ${it.text}" }
        .toSet()

    @Test
    fun serializedFormMatchesReleasedVersion() {
        fun describe(c: Class<*>) = ObjectStreamClass.lookup(c).let { osc ->
            listOf(osc.name, osc.serialVersionUID.toString()) +
                osc.fields.map { "${it.name}:${it.typeString ?: it.typeCode}" }.sorted()
        }
        val node = "Lcom/renju_note/isoo/SeqTree\$Node;"
        assertEquals(
            listOf(
                "com.renju_note.isoo.SeqTree", "2765198187236704398",
                "head:$node", "now:$node", "now_board:[[I", "text_box:Ljava/lang/String;"
            ),
            describe(SeqTree::class.java)
        )
        assertEquals(
            listOf(
                "com.renju_note.isoo.SeqTree\$Node", "-6403100239045118824",
                "boxText:Ljava/lang/String;", "chlid:$node", "next:$node", "parent:$node",
                "text:Ljava/lang/String;", "x:I", "y:I"
            ),
            describe(SeqTree.Node::class.java)
        )
    }

    @Test
    fun loadsEmptyBoard() {
        val manager = load(fixture("v1_3_1_empty.bin"))
        assertEquals(1, manager.getNowIndex())
        assertTrue(manager.getNowBoardStatus().isEmpty())
        assertEquals("", manager.getNowTextBoxString())
    }

    @Test
    fun loadsMainLineWithMemos() {
        val manager = load(fixture("v1_3_1_mainline.bin"))
        val fullBoard = setOf(
            "7,7 BLACK 1", "7,8 WHITE 2", "8,8 BLACK 3", "6,6 WHITE 4", "9,9 BLACK 5"
        )
        assertEquals(fullBoard, manager.stones())
        assertEquals(6, manager.getNowIndex())

        manager.undo()
        manager.undo()
        assertEquals("memo on move 3 / 한글 메모", manager.getNowTextBoxString())

        manager.undoAll()
        assertEquals(1, manager.getNowIndex())
        assertTrue(manager.stones().isEmpty())
        assertEquals("opening memo", manager.getNowTextBoxString())

        while (manager.redo()) { }
        assertEquals(fullBoard, manager.stones())
    }

    @Test
    fun loadsBranchesAndHints() {
        val manager = load(fixture("v1_3_1_branches.bin"))
        assertEquals(setOf("7,7 BLACK 1", "7,8 WHITE 2", "8,8 BLACK 3"), manager.stones())
        assertEquals(4, manager.getNowIndex())
        assertEquals(setOf("9,9 "), manager.hints())

        manager.undo()
        assertEquals("branch point", manager.getNowTextBoxString())
        assertEquals(setOf("8,8 ", "8,7 ", "6,8 ", "6,6 A", "5,5 B"), manager.hints())
        assertFalse("redo must stop at a branch", manager.redo())

        manager.putStone(8, 7)
        manager.putStone(9, 7)
        manager.putStone(10, 7)
        assertEquals("alt line end", manager.getNowTextBoxString())
        assertEquals(
            setOf("7,7 BLACK 1", "7,8 WHITE 2", "8,7 BLACK 3", "9,7 WHITE 4", "10,7 BLACK 5"),
            manager.stones()
        )
    }

    @Test
    fun loadsLegacyTextBoxIntoHeadMemo() {
        val manager = load(fixture("v1_2_3_text_box.bin"))
        assertEquals(setOf("7,7 BLACK 1", "8,8 WHITE 2", "9,9 BLACK 3"), manager.stones())
        manager.undoAll()
        assertEquals("legacy 1.2.3 memo", manager.getNowTextBoxString())
    }

    @Test
    fun loadsFilesSavedByTheReleasedApp() {
        val mainLine = load(fixture("v1_3_1_device_mainline.bin"))
        assertEquals(
            setOf("7,7 BLACK 1", "8,7 WHITE 2", "8,8 BLACK 3", "6,6 WHITE 4", "9,9 BLACK 5"),
            mainLine.stones()
        )
        mainLine.undo()
        assertEquals("memo_after_move_4", mainLine.getNowTextBoxString())

        val branch = load(fixture("v1_3_1_device_branch.bin"))
        assertEquals(setOf("7,7 BLACK 1", "8,7 WHITE 2", "8,8 BLACK 3", "5,9 WHITE 4"), branch.stones())
        assertEquals("branch_memo", branch.getNowTextBoxString())
        assertEquals(setOf("4,4 A"), branch.hints())
        branch.undo()
        assertEquals(setOf("6,6 ", "5,9 "), branch.hints())
        branch.putStone(6, 6)
        assertEquals("memo_after_move_4", branch.getNowTextBoxString())
        assertTrue(branch.redo())
        assertEquals(setOf("7,7 BLACK 1", "8,7 WHITE 2", "8,8 BLACK 3", "6,6 WHITE 4", "9,9 BLACK 5"), branch.stones())
    }

    @Test
    fun resavedFileKeepsTheSameTree() {
        val manager = load(fixture("v1_3_1_branches.bin"))
        val resaved = ByteArrayOutputStream().also { out ->
            ObjectOutputStream(out).use { it.writeObject(manager.getSeqTree()) }
        }.toByteArray()

        val reloaded = load(resaved)
        assertEquals(manager.stones(), reloaded.stones())
        assertEquals(manager.hints(), reloaded.hints())
        reloaded.undo()
        assertEquals("branch point", reloaded.getNowTextBoxString())
        assertEquals(setOf("8,8 ", "8,7 ", "6,8 ", "6,6 A", "5,5 B"), reloaded.hints())
    }
}
