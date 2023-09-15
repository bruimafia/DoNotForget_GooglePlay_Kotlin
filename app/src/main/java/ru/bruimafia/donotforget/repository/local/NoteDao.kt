package ru.bruimafia.donotforget.repository.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Update
import androidx.room.Query
import io.reactivex.Completable
import io.reactivex.Flowable
import io.reactivex.Single

@Dao
interface NoteDao {
    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY id DESC")
    fun getRX(): Flowable<List<Note>>

    @Query("SELECT * FROM notes WHERE id = :id")
    operator fun get(id: Long): Single<Note>

    @Query("SELECT * FROM notes ORDER BY id DESC")
    fun getAll(): Flowable<List<Note>>

    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY id DESC")
    fun getAllOrderById(): Flowable<List<Note>>

    @Query("SELECT * FROM notes WHERE in_history = 0 ORDER BY is_fix DESC, date ASC, id DESC")
    fun getAllOrderByRelevance(): Flowable<List<Note>>

    @Query("SELECT * FROM notes WHERE in_history = 1 ORDER BY date_delete DESC")
    fun getHistory(): Flowable<List<Note>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(note: Note): Single<Long>

    @Update
    fun update(note: Note): Completable

    @Query("UPDATE notes SET in_history = 1, date_delete = :dateDelete WHERE id = :id")
    fun delete(id: Long, dateDelete: Long): Completable

    @Query("UPDATE notes SET in_history = 0, date_delete = 0 WHERE id = :id")
    fun recover(id: Long): Completable

    @Query("UPDATE notes SET in_history = 1, date_delete = :dateDelete WHERE in_history = 0")
    fun clear(dateDelete: Long): Completable

    @Query("DELETE FROM notes WHERE in_history = 1")
    fun clearHistory(): Completable
}