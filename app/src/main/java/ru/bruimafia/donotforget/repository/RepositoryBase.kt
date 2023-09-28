package ru.bruimafia.donotforget.repository

import kotlinx.coroutines.flow.Flow
import ru.bruimafia.donotforget.repository.local.Note


interface RepositoryBase {
    suspend fun get(id: Long): Note
    fun getAll(): Flow<List<Note>>
    fun getAllActualOrderById(): Flow<List<Note>>
    fun getAllActualOrderByRelevance(): Flow<List<Note>>
    fun getHistory(): Flow<List<Note>>
    suspend fun create(note: Note): Long
    suspend fun update(note: Note)
    suspend fun recover(id: Long)
    suspend fun delete(id: Long)
    suspend fun deleteAll()
    suspend fun clearHistory()
}