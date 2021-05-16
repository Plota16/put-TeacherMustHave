package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.model.Presence

class PresenceListAdapter(private val list: List<Presence>)
    : RecyclerView.Adapter<PresenceViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PresenceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return PresenceViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: PresenceViewHolder, position: Int) {
        val movie: Presence = list[position]
        holder.bind(movie)
    }

    override fun getItemCount(): Int = list.size

}