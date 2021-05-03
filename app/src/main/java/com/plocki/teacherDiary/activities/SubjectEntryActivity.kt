package com.plocki.teacherDiary.activities

import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.*
import com.plocki.teacherDiary.model.SubjectEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SubjectEntryActivity : AppCompatActivity() {

    private var isOnline = true
    private var id = 0
    private lateinit var subjectEntry: SubjectEntry
    private lateinit var db: SQLiteDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_entry)

        val subjectId = intent.getStringExtra("subjectId")
        id = subjectId!!.toInt()
        db = DatabaseHelper(MainApplication.appContext).readableDatabase
        subjectEntry = SubjectEntry.readOne(db, id)

        updateUI()
    }

    private fun updateUI(){
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

        val bannerText = subjectEntry.subjectName.substring(0, 3) + "\n" + subjectEntry.className
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


    fun setTopic(view: View){
        if(isOnline){
            var dialogText: String
            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            builder.setTitle("Podaj Temat")

            val input = EditText(this)
            input.inputType = InputType.TYPE_CLASS_TEXT
            input.setText(subjectEntry.topic)
            builder.setView(input)
            builder.setPositiveButton("OK") { _, _ ->
                dialogText = input.text.toString()
                setTopicMutation(dialogText)
            }
            builder.setNegativeButton("Cancel") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie tematu możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }



    private fun setTopicMutation(topic: String){
        val mutation = SetTopicMutation(id.toInput(),topic.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(result.data!!.update_SUBJECT_ENTRY!!.returning[0].iD == id){
                    subjectEntry.topic = result.data!!.update_SUBJECT_ENTRY!!.returning[0].tOPIC!!
                    subjectEntry.updateTopic(db)
                    updateUI()
                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd dodawania tematu",
                            Toast.LENGTH_SHORT).show()
                }
            } catch (e: Error){
                Toast.makeText(
                        MainApplication.appContext, "Błąd dodawania tematu",
                        Toast.LENGTH_SHORT).show()
            }
        }
    }

}