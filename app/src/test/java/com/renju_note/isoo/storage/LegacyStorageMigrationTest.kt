package com.renju_note.isoo.storage

import com.renju_note.isoo.SeqTree
import org.junit.Assert.assertEquals
import org.junit.Test
import java.io.ObjectInputStream

class LegacyStorageMigrationTest {

    private fun tree(name : String) : SeqTree =
        requireNotNull(javaClass.getResourceAsStream("/seqtree/$name")) { "missing fixture $name" }
            .use { input -> ObjectInputStream(input).use { it.readObject() as SeqTree } }

    private fun previewOf(name : String) =
        LegacyStorageMigration.boardSequence(tree(name)).map { "${it.x}/${it.y}" }

    @Test
    fun previewFollowsMoveOrderOfTheSavedPosition() {
        // same "x/y" list 1.3.1 stored in its recent file list when the file was saved
        assertEquals(listOf("7/7", "8/7", "8/8", "6/6", "9/9"), previewOf("v1_3_1_device_mainline.bin"))
        assertEquals(listOf("7/7", "8/7", "8/8", "5/9"), previewOf("v1_3_1_device_branch.bin"))
        assertEquals(listOf("7/7", "7/8", "8/8"), previewOf("v1_3_1_branches.bin"))
        assertEquals(emptyList<String>(), previewOf("v1_3_1_empty.bin"))
    }

    @Test
    fun sequenceConverterRoundTrips() {
        val converters = StorageDatabase.Converters()
        listOf(emptyList(), listOf("7/7"), listOf("7/7", "8/7", "14/0")).forEach { sequence ->
            assertEquals(sequence, converters.toSequence(converters.fromSequence(sequence)))
        }
    }

}
