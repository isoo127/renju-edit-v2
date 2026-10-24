package com.renju_note.isoo.storage

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverter
import androidx.room.TypeConverters

@Database(entities = [StorageElement::class], version = 1)
@TypeConverters(StorageDatabase.Converters::class)
abstract class StorageDatabase : RoomDatabase() {

    abstract fun storageDao() : StorageDao

    class Converters {
        // sequence is stored as "x/y,x/y,..."
        @TypeConverter
        fun fromSequence(sequence : List<String>) : String = sequence.joinToString(",")

        @TypeConverter
        fun toSequence(value : String) : List<String> = if (value.isEmpty()) emptyList() else value.split(",")
    }

    companion object {
        fun create(context : Context) : StorageDatabase =
            Room.databaseBuilder(context, StorageDatabase::class.java, "storage.db").build()
    }

}
