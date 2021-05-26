package com.plocki.teacherDiary.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.textfield.TextInputEditText
import com.plocki.teacherDiary.AddTestMutation
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.SetTopicMutation
import com.plocki.teacherDiary.model.SubjectEntry
import com.plocki.teacherDiary.model.Test
import com.plocki.teacherDiary.type.TEST_insert_input
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SubjectEntryActivity : AppCompatActivity()  {

    private var isOnline = true
    private var isLate = false
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

        if(subjectEntry.late == "Y"){
            isLate = true
        }
        updateUI()
    }

    override fun onResume() {
        updateUI()
        super.onResume()
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

    fun checkPresence(view: View){
        if(isOnline){

            val intent = Intent(MainApplication.appContext, PresenceActivity::class.java)
            intent.putExtra("className", subjectEntry.className)
            if(subjectEntry.presence.isEmpty()){
                intent.putExtra("isChecked", "NIE")
            }
            else{
                intent.putExtra("isChecked", "TAK")
            }
            intent.putExtra("subjectEntryId", id.toString())
            startActivity(intent)
        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie obecności możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun setGrade(view: View){
        if(isOnline){
            val intent = Intent(this, GradeActivity::class.java)
            intent.putExtra("className", subjectEntry.className)
            intent.putExtra("subjectEntryId", id.toString())
            startActivity(intent)
        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie ocen możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }

    fun setTest(view: View){
        if(isOnline){

            val builder: AlertDialog.Builder = AlertDialog.Builder(this)
            val customAlertDialogView = View.inflate(this,R.layout.dialog_test,null)

            val topic = customAlertDialogView.findViewById<TextInputEditText>(R.id.dialog_test_topic_input)
            val type = customAlertDialogView.findViewById<TextInputEditText>(R.id.dialog_test_type_input)

            builder.setView(customAlertDialogView)
                    .setTitle("Dodawanie testu")
                    .setMessage("Dodaj test")
                    .setPositiveButton("OK") { dialog, _ ->

                        val test = Test(
                                0,
                                topic.text.toString(),
                                type.text.toString(),
                                subjectEntry.id,
                                "F",
                                subjectEntry.date,
                                subjectEntry.startTime)

                        addTestMutation(test,dialog)
                    }
                    .setNegativeButton("ANULUJ") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()


        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie zadania możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTestMutation(test: Test, dialog: DialogInterface){

        val input = TEST_insert_input(
                topic = test.topic.toInput(),
                type = test.type.toInput(),
                subject_entry_id = test.subjectId.toInput(),
                graded = test.graded.toInput()
        )

        val mutation = AddTestMutation(input)

        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                            MainApplication.appContext, "Test dodany",
                            Toast.LENGTH_SHORT).show()
                    val resultTest = result.data!!.insert_TEST_one!!
                    val test = Test(
                        resultTest.id,
                        resultTest.topic,
                        resultTest.type,
                        resultTest.subject_entry_id,
                        resultTest.graded!!,
                        resultTest.sUBJECT_ENTRY.date.toString(),
                        resultTest.sUBJECT_ENTRY.lESSON.start_time.toString()
                    )
                    test.insert(DatabaseHelper(MainApplication.appContext).writableDatabase)
                    subjectEntry.testID = test.id.toString()
                    subjectEntry.updateTest(db)
                    updateUI()
                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd dodawania testu",
                            Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            } catch (e: Error){
                Toast.makeText(
                        MainApplication.appContext, "Błąd dodawania testu",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun setTopicMutation(topic: String){
        val mutation = SetTopicMutation(id.toInput(), topic.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(result.data!!.update_SUBJECT_ENTRY!!.returning[0].id == id){
                    subjectEntry.topic = result.data!!.update_SUBJECT_ENTRY!!.returning[0].topic!!
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

    private fun updateUI(){
        subjectEntry = SubjectEntry.readOne(db, id)

        val dateTextView = findViewById<TextView>(R.id.subject_top_date)
        val dateStartTimeView = findViewById<TextView>(R.id.subject_top_time_start)
        val dateEndTimeView = findViewById<TextView>(R.id.subject_top_time_end)
        val topicTextView = findViewById<TextView>(R.id.subject_topic_topic)
        val testTextView = findViewById<TextView>(R.id.subject_test_topic)

        val bannerButton = findViewById<Button>(R.id.subject_top_banner)
        val topicButton = findViewById<Button>(R.id.subject_topic_button)
        val presenceButton = findViewById<Button>(R.id.subject_presence_button)
        val testButton = findViewById<Button>(R.id.subject_test_button)


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
            if(isLate){
                topicButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Secondary))
            }
        }
        else{
            topicButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
            topicButton.text = "POPRAW"
            topicTextView.text = subjectEntry.topic
        }

        if(subjectEntry.presence == "" && isLate){
            presenceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Secondary))
        }
        else if(subjectEntry.presence == "Y"){
            presenceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
            presenceButton.text = "POPRAW"
        }

        if(subjectEntry.testID != ""){
            testButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
            testButton.text = "POPRAW"
            testTextView.text = Test.readOne(db,subjectEntry.testID).topic
        }

        bannerButton.setBackgroundColor(ContextCompat.getColor(this, subjectEntry.getColor()))
    }



}