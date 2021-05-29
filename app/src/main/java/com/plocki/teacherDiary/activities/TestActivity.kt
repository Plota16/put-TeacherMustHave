package com.plocki.teacherDiary.activities

import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.SubjectEntry
import com.plocki.teacherDiary.model.Test
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication

class TestActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)


        val testId = intent.getStringExtra("testId")
        val db = DatabaseHelper(MainApplication.appContext).readableDatabase
        val test = Test.readOne(db,testId!!)
        val subjectEntry = SubjectEntry.readOne(db,test.subjectId)

        setTextViews(test,subjectEntry)

        setOnClicks()
    }

    private fun setTextViews(test: Test, subjectEntry: SubjectEntry){
        findViewById<TextView>(R.id.test_details_topic).text = test.topic
        findViewById<TextView>(R.id.test_details_type).text = test.type
        findViewById<TextView>(R.id.test_details_date).text = test.date
        findViewById<TextView>(R.id.test_details_time).text = test.time
        findViewById<TextView>(R.id.test_details_class).text = subjectEntry.className
        findViewById<TextView>(R.id.test_details_subject).text = subjectEntry.subjectName

        val stateTextView: TextView = findViewById(R.id.test_details_state)
        when(test.graded){
            "F" -> {
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_red))
                stateTextView.text = getString(R.string.test_state_waiting)
            }
            "N" -> {
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_yellow))
                stateTextView.text = getString(R.string.test_state_unchecked)
            }
            "Y" -> {
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_green))
                stateTextView.text = getString(R.string.test_state_checked)
            }
        }
    }

    private fun setOnClicks(){
        findViewById<Button>(R.id.test_details_confirm).setOnClickListener {
            submit()
        }
        findViewById<Button>(R.id.test_details_delete).setOnClickListener {
            deleteTest()
        }
        findViewById<Button>(R.id.test_details_edit).setOnClickListener {
            editTest()
        }
    }

    private fun submit(){

    }

    private fun editTest(){

    }

    private fun deleteTest(){

    }

}