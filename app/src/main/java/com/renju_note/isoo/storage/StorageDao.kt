package com.renju_note.isoo.storage

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Upsert
import kotlinx.coroutines.flow.Flow

@Dao
interface StorageDao {

    @Query("SELECT * FROM storage_element ORDER BY date DESC")
    fun observeAll() : Flow<List<StorageElement>>

    @Upsert
    suspend fun upsert(element : StorageElement)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertIfAbsent(elements : List<StorageElement>)

    @Delete
    suspend fun delete(element : StorageElement)

}
