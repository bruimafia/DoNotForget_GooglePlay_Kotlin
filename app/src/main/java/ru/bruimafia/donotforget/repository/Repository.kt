package ru.bruimafia.donotforget.repository

import kotlinx.coroutines.flow.Flow
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.repository.local.NoteDao
import ru.bruimafia.donotforget.repository.remote.FirebaseManager


class Repository(private val localStore: NoteDao) : RepositoryBase {

    override suspend fun get(id: Long): Note = localStore.get(id)

    override fun getAll(): Flow<List<Note>> = localStore.getAll()

    override fun getAllActualOrderById(): Flow<List<Note>> = localStore.getAllActualOrderById()

    override fun getAllActualOrderByRelevance(): Flow<List<Note>> = localStore.getAllActualOrderByRelevance()

    override fun getHistory(): Flow<List<Note>> = localStore.getHistory()

    override suspend fun create(note: Note): Long {
        val id = localStore.create(note)
        FirebaseManager.createOrUpdate(get(id))
        return id
    }


    override suspend fun update(note: Note) {
        FirebaseManager.createOrUpdate(note)
        localStore.update(note)
    }

    override suspend fun recover(id: Long)  {
        FirebaseManager.recover(id)
        localStore.recover(id, System.currentTimeMillis())
    }

    override suspend fun delete(id: Long) {
        FirebaseManager.delete(id)
        localStore.delete(id, System.currentTimeMillis())
    }

    override suspend fun deleteAll() = localStore.deleteAll(System.currentTimeMillis())

    override suspend fun clearHistory() {
        FirebaseManager.clearHistory()
        localStore.clearHistory()
    }

}