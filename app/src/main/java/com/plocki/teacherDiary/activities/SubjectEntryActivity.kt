package com.plocki.teacherDiary.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.plocki.teacherDiary.DatabaseHelper
import com.plocki.teacherDiary.MainApplication
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.SubjectEntry

class SubjectEntryActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_entry)

        val subjectId = intent.getStringExtra("subjectId")
        val db = DatabaseHelper(MainApplication.appContext).readableDatabase
        val subjectEntry = SubjectEntry.readOne(db, subjectId!!.toInt())

        val dateTextView = findViewById<TextView>(R.id.subject_top_date)
        val dateStartTimeView = findViewById<TextView>(R.id.subject_top_time_start)
        val dateEndTimeView = findViewById<TextView>(R.id.subject_top_time_end)
        val topicTextView = findViewById<TextView>(R.id.subject_topic_topic)
        val bannerButton = findViewById<Button>(R.id.subject_top_banner)

        dateTextView.text = subjectEntry.date
        var time = "Od: ${subjectEntry.startTime}"
        dateStartTimeView.text = time
        time = "Do : ${subjectEntry.endTime}"
        dateEndTimeView.text = time

        val bannerText = subjectEntry.subjectName.substring(0,3) + "\n" + subjectEntry.className
        bannerButton.text = bannerText

        if(subjectEntry.topic.isEmpty()){
            val topic = "Nie podano tematu!"
            topicTextView.text = topic
        }
        else{
            topicTextView.text = subjectEntry.topic
        }
        bannerButton.setBackgroundColor(resources.getColor(subjectEntry.getColor()))

    }
}