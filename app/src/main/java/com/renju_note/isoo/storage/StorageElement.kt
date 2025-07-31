package com.renju_note.isoo.storage

import android.net.Uri
import androidx.core.net.toUri
import com.renju_note.isoo.board.Stone
import io.realm.kotlin.ext.realmListOf
import io.realm.kotlin.types.RealmList
import io.realm.kotlin.types.RealmObject
import io.realm.kotlin.types.annotations.PrimaryKey
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class StorageElement : RealmObject {

    @PrimaryKey
    var location : String = ""
    var title : String = ""
    var date : String = ""
    var sequence: RealmList<String> = realmListOf()

    companion object {
        fun create(title: String, uri: Uri, seq: ArrayList<Stone>): StorageElement {
            val element = StorageElement()
            element.title = title

            val current = LocalDateTime.now()
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
            element.date = current.format(formatter)

            for (stone in seq) {
                val str = stone.x.toString() + "/" + stone.y.toString()
                element.sequence.add(str)
            }

            element.location = uri.toString()
            return element
        }
    }

    fun getParsedUri() : Uri {
        return location.toUri()
    }

}