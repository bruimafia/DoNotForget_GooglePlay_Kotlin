package ru.bruimafia.donotforget.repository

import kotlinx.coroutines.flow.Flow
import ru.bruimafia.donotforget.repository.local.Note
import ru.bruimafia.donotforget.repository.local.NoteDao


class Repository(private val dao: NoteDao) : RepositoryBase {

    override suspend fun get(id: Long): Note = dao.get(id)

    override fun getAll(): Flow<List<Note>> = dao.getAll()

    override fun getAllOrderById(): Flow<List<Note>> = dao.getAllOrderById()

    override fun getAllOrderByRelevance(): Flow<List<Note>> = dao.getAllOrderByRelevance()

    override fun getHistory(): Flow<List<Note>> = dao.getHistory()

    override suspend fun create(note: Note) = dao.insert(note)

    override suspend fun update(note: Note) = dao.update(note)

    override suspend fun recover(id: Long) = dao.recover(id)

    override suspend fun delete(id: Long) = dao.delete(id, System.currentTimeMillis())

    override suspend fun deleteAll() = dao.deleteAll(System.currentTimeMillis())

    override suspend fun clearHistory() = dao.clearHistory()

    override fun syncing() {
        TODO("Not yet implemented")
    }

}