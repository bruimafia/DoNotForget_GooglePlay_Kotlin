package ru.bruimafia.donotforget.fragment.edit

import ru.bruimafia.donotforget.repository.local.Note


interface OnClickMethod {
    fun onCreateNote(note: Note)
    fun onUpdateNote(note: Note)
    fun onDeleteNote(id: Long)
    fun onRecoverNote(id: Long)
    fun onChooseColor()
    fun onChooseDate()
    fun onChooseTime()
}