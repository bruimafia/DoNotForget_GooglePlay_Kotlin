package ru.bruimafia.donotforget.repository

import io.reactivex.Flowable
import io.reactivex.Single
import ru.bruimafia.donotforget.repository.local.Note

interface RepositoryBase {
    operator fun get(id: Long): Single<Note>

    fun getAll(): Flowable<List<Note>>

    fun getAllOrderById(): Flowable<List<Note>>

    fun getAllOrderByRelevance(): Flowable<List<Note>>

    fun getHistory(): Flowable<List<Note>>

    fun create(note: Note)

    fun update(note: Note)

    fun delete(id: Long)

    fun recover(id: Long)

    fun clear()

    fun clearHistory()

    fun syncing()

    fun onDestroy()
}