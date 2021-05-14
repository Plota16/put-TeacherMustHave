package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.Presence
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication


class PresenceViewHolder (inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.presence, parent, false)) {


    private var presenceTextView: TextView? = null
    private var presenceRadioGroup: RadioGroup? = null




    init {
        presenceTextView = itemView.findViewById(R.id.presence_text)
        presenceRadioGroup = itemView.findViewById(R.id.presence_radio_group)


    }




    fun bind(presence: Presence) {

        val id = when(presence.presence){
            "OBECNY" -> R.id.presence_radio_present
            "NIEOBECNY" -> R.id.presence_radio_absent
            "USPRAWIEDLOWIONY" -> R.id.presence_radio_justified
            "SPOZNIONY" -> R.id.presence_radio_late
            "BRAK" -> R.id.presence_radio_unknown
            else -> R.id.presence_radio_unknown
        }
        presenceRadioGroup!!.check(id)
        presenceRadioGroup!!.setOnCheckedChangeListener { _, checkedId ->
             presence.presence = when(checkedId){
                R.id.presence_radio_present -> "OBECNY"
                R.id.presence_radio_absent -> "NIEOBECNY"
                R.id.presence_radio_justified -> "USPRAWIEDLOWIONY"
                R.id.presence_radio_late -> "SPOZNIONY"
                R.id.presence_radio_unknown -> "BRAK"
                else -> "BRAK"
            }
            val db = DatabaseHelper(MainApplication.appContext).writableDatabase
            presence.updatePresence(db)
        }
        presenceTextView!!.text = presence.name

    }

}