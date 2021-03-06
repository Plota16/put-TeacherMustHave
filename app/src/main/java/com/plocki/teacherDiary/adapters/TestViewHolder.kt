package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.card.MaterialCardView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.GradeWeight
import com.plocki.teacherDiary.model.Test
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication


class TestViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.test, parent, false)) {

    lateinit var test : Test
    private val topicTextView: TextView = itemView.findViewById(R.id.test_topic)
    private val typeTextView: TextView = itemView.findViewById(R.id.test_type)
    private val stateTextView: TextView = itemView.findViewById(R.id.test_state)
    private val dateTextView: TextView =  itemView.findViewById(R.id.test_end_date)
    private val timeTextView: TextView = itemView.findViewById(R.id.test_end_time)
    val testCard: MaterialCardView = itemView.findViewById(R.id.test_card)


    fun bind(test: Test) {
        val gradeWeights = GradeWeight.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)

        topicTextView.text = test.topic
        typeTextView.text = gradeWeights[test.type.toInt() - 1].name
        stateTextView.text = test.graded
        dateTextView.text = test.date
        timeTextView.text = test.time
        when(test.graded){
            "F" -> {
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_red))
                stateTextView.text = MainApplication.appContext!!.getString(R.string.test_state_waiting)
            }
            "N" -> {
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_yellow))
                stateTextView.text = MainApplication.appContext!!.getString(R.string.test_state_unchecked)
            }
            "Y" -> {
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_green))
                stateTextView.text = MainApplication.appContext!!.getString(R.string.test_state_checked)
            }
        }

        this.test = test
    }
}