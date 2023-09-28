package ru.bruimafia.donotforget.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Query
import kotlinx.coroutines.flow.Flow


@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE id = :id")
    suspend fun get(id: Long): Note

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAll(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY id DESC")
    fun getAllActualOrderById(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY is_fix DESC, date ASC, id DESC")
    fun getAllActualOrderByRelevance(): Flow<List<Note>>

    @Query("SELECT * FROM notes WHERE in_history = 1 ORDER BY date_delete DESC")
    fun getHistory(): Flow<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun create(note: Note): Long

    @Update
    suspend fun update(note: Note)

    @Query("UPDATE notes SET in_history = 0, date_delete = 0, date_update = :dateUpdate WHERE id = :id")
    suspend fun recover(id: Long, dateUpdate: Long)

    @Query("UPDATE notes SET in_history = 1, date_delete = :dateDelete, date_update = :dateDelete WHERE id = :id")
    suspend fun delete(id: Long, dateDelete: Long)

    @Query("UPDATE notes SET in_history = 1, date_delete = :dateDelete, date_update = :dateDelete WHERE in_history = 0")
    suspend fun deleteAll(dateDelete: Long)

    @Query("DELETE FROM notes WHERE in_history = 1")
    suspend fun clearHistory()
}