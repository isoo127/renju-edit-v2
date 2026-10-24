package com.renju_note.isoo.storage

import android.net.Uri
import androidx.core.net.toUri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.renju_note.isoo.board.Stone
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

@Entity(tableName = "storage_element")
data class StorageElement(
    @PrimaryKey
    val location : String,
    val title : String,
    val date : String,
    val sequence : List<String>
) {

    companion object {
        private val dateFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")

        fun create(title: String, uri: Uri, seq: List<Stone>, date: LocalDateTime = LocalDateTime.now()): StorageElement {
            return StorageElement(
                location = uri.toString(),
                title = title,
                date = date.format(dateFormatter),
                sequence = seq.map { stone -> stone.x.toString() + "/" + stone.y.toString() }
            )
        }
    }

    fun getParsedUri() : Uri {
        return location.toUri()
    }

}
