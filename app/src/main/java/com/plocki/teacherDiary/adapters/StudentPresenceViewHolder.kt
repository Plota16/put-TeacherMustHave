package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.StudentPresence
import com.plocki.teacherDiary.utility.MainApplication


class StudentPresenceViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.single_presence, parent, false)) {

    val presenceCard = itemView.findViewById<MaterialCardView>(R.id.single_presence_card)!!

    fun bind(presence: StudentPresence) {
        val presenceTextView = itemView.findViewById<TextView>(R.id.single_presence_presence)
        presenceTextView.text = presence.presence
        itemView.findViewById<TextView>(R.id.single_presence_date).text = presence.date
        itemView.findViewById<TextView>(R.id.single_presnece_time).text = presence.time
        itemView.findViewById<TextView>(R.id.single_presnce_topic).text = presence.topic

        when(presence.presence){
            "OBECNY" -> presenceTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_green))
            "NIEOBECNY" -> presenceTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_red))
            "USPRAWIEDLOWIONY" -> presenceTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_blue))
            "SPOZNIONY" -> presenceTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_yellow))
        }
    }



}