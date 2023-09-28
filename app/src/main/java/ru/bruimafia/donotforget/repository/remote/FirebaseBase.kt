package ru.bruimafia.donotforget.repository.remote

import ru.bruimafia.donotforget.repository.local.Note


interface FirebaseBase {
    fun getAll(): List<Note>
    fun putAll(notes: List<Note>)
    fun createOrUpdate(note: Note)
    fun recover(id: Long)
    fun delete(id: Long)
    fun deleteAll()
    fun clearHistory()
}