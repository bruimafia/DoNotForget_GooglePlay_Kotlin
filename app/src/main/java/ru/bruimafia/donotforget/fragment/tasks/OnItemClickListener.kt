package ru.bruimafia.donotforget.fragment.tasks

import ru.bruimafia.donotforget.repository.local.Note

interface OnItemClickListener {
    fun onItemClick(note: Note)
    fun onItemLongClick(note: Note): Boolean
}