package ru.bruimafia.donotforget.util

import androidx.recyclerview.widget.DiffUtil
import ru.bruimafia.donotforget.repository.local.Note


class NoteItemDiffCallback : DiffUtil.ItemCallback<Note>() {
    override fun areItemsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem.id == newItem.id
    }

    override fun areContentsTheSame(oldItem: Note, newItem: Note): Boolean {
        return oldItem == newItem
    }
}