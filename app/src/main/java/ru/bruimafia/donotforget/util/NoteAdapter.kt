package ru.bruimafia.donotforget.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import ru.bruimafia.donotforget.databinding.NoteBinding
import ru.bruimafia.donotforget.repository.local.Note


class NoteAdapter : ListAdapter<Note, NoteAdapter.NoteHolder>(NoteItemDiffCallback()) {

    var onNoteItemLongClickListener: ((Note) -> Unit)? = null
    var onNoteItemClickListener: ((Note) -> Unit)? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val view = NoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteHolder(view)
    }

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.bind(currentList[position])
    }


    inner class NoteHolder(private val binding: NoteBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(note: Note) {
            binding.note = note
            binding.executePendingBindings()
            itemView.setOnClickListener {
                onNoteItemClickListener?.invoke(note)
            }
            itemView.setOnLongClickListener {
                onNoteItemLongClickListener?.invoke(note)
                true
            }
        }
    }

}