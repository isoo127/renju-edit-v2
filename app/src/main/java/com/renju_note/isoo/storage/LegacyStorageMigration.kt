package com.renju_note.isoo.storage

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.renju_note.isoo.SeqTree
import com.renju_note.isoo.board.Stone
import java.io.File
import java.io.ObjectInputStream
import java.time.Instant
import java.time.LocalDateTime
import java.time.ZoneId
import kotlin.math.abs

/**
 * Versions <= 1.3.1 kept the recent file list in a Realm Java database (files/renju_edit.realm),
 * which this version can no longer read. Every entry of that list was saved or opened through the
 * document picker and the app took a persistable permission for it, so the list is rebuilt from
 * those permissions once, then the old realm files are removed.
 */
object LegacyStorageMigration {

    private const val LEGACY_REALM_PREFIX = "renju_edit."
    private const val LEGACY_REALM_FILE = "renju_edit.realm"
    private const val LEGACY_REALM_TEMP_DIR = ".realm.temp"

    suspend fun migrateIfNeeded(context : Context, dao : StorageDao) {
        val filesDir = context.filesDir
        if (!File(filesDir, LEGACY_REALM_FILE).exists()) return

        val elements = context.contentResolver.persistedUriPermissions
            .filter { it.isReadPermission }
            .mapNotNull { readElement(context, it.uri, it.persistedTime) }
        dao.insertIfAbsent(elements)

        filesDir.listFiles { file -> file.name.startsWith(LEGACY_REALM_PREFIX) }?.forEach { it.deleteRecursively() }
        File(filesDir, LEGACY_REALM_TEMP_DIR).deleteRecursively()
    }

    // files that were moved, deleted or are not board files are skipped
    private fun readElement(context : Context, uri : Uri, persistedTime : Long) : StorageElement? {
        return try {
            val tree = context.contentResolver.openInputStream(uri)?.use { input ->
                ObjectInputStream(input).use { it.readObject() as SeqTree }
            } ?: return null
            val title = context.contentResolver.query(uri, arrayOf(OpenableColumns.DISPLAY_NAME), null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) cursor.getString(0) else null
            } ?: return null
            val date = LocalDateTime.ofInstant(Instant.ofEpochMilli(persistedTime), ZoneId.systemDefault())
            StorageElement.create(title, uri, boardSequence(tree), date)
        } catch (e : Exception) {
            e.printStackTrace()
            null
        }
    }

    // stones on the board in move order, the same list the old version stored as the preview
    fun boardSequence(tree : SeqTree) : List<Stone> {
        val stones = ArrayList<Pair<Int, Stone>>()
        for (x in 0..14) {
            for (y in 0..14) {
                val value = tree.now_board[x][y]
                if (value != 0) {
                    val type = if (value > 0) Stone.Type.BLACK else Stone.Type.WHITE
                    stones.add(abs(value) to Stone(x, y, (abs(value) - 1).toString(), type))
                }
            }
        }
        return stones.sortedBy { it.first }.map { it.second }
    }

}
