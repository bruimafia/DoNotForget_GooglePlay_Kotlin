package ru.bruimafia.donotforget.util

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.bruimafia.donotforget.databinding.NoteBinding
import ru.bruimafia.donotforget.fragment.tasks.OnItemClickListener
import ru.bruimafia.donotforget.repository.local.Note


class NoteAdapter(private var listener: OnItemClickListener, private var list: MutableList<Note> = mutableListOf()) :
    RecyclerView.Adapter<NoteAdapter.NoteHolder>() {

    fun setData(newNotes: MutableList<Note>) {
        list.clear()
        list.addAll(newNotes)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteHolder {
        val view = NoteBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return NoteHolder(view)
    }

    override fun getItemCount() = list.size

    override fun onBindViewHolder(holder: NoteHolder, position: Int) {
        holder.bind(list[position], listener)
    }


    inner class NoteHolder(private val binding: NoteBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(note: Note, listener: OnItemClickListener) {
            binding.note = note
            binding.executePendingBindings()
            itemView.setOnClickListener {
                listener.onItemClick(note)
            }
            itemView.setOnLongClickListener {
                listener.onItemLongClick(note)
                true
            }
        }

    }

}