package com.magda.noteapp.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.magda.noteapp.R
import com.magda.noteapp.model.Note


class NotesAdapter(private val context: Context, private val itemClickListener: ItemClickListener) :
    RecyclerView.Adapter<NotesAdapter.ViewHolder>() {
    private var notes: MutableList<Note> = mutableListOf()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val itemView = layoutInflater.inflate(R.layout.note_view_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int {
        return notes.size
    }

  fun addNotes(updatedList: MutableList<Note>) {
      notes = updatedList
    }

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView),
        View.OnClickListener {
        private val noteTitle = itemView.findViewById<TextView>(R.id.tvTitle)
        private val noteDate = itemView.findViewById<TextView>(R.id.tvDate)
        private val noteCardView = itemView.findViewById<MaterialCardView>(R.id.noteCardView)

        fun bind(note: Note) {
            noteTitle.text = note.title
            noteDate.text = note.timeStamp
            noteCardView.setOnClickListener(this)
        }

        override fun onClick(view: View?) {
            val note = notes[adapterPosition]
            if (view != null) {
                itemClickListener.itemClicked(view, note)
            }


        }

    }

    interface ItemClickListener {
        fun itemClicked(view: View, note: Note)
    }
}