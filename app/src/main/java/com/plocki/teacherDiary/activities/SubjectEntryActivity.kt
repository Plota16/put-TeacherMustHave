package com.plocki.teacherDiary.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.content.Intent
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.textfield.TextInputEditText
import com.plocki.teacherDiary.AddTestMutation
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.SetTopicMutation
import com.plocki.teacherDiary.model.GradeWeight
import com.plocki.teacherDiary.model.SubjectEntry
import com.plocki.teacherDiary.model.Test
import com.plocki.teacherDiary.type.TEST_insert_input
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SubjectEntryActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener  {

    private var isOnline = true
    private var isLate = false
    private var id = 0

    private lateinit var subjectEntry: SubjectEntry
    private lateinit var db: SQLiteDatabase
    private val gradeWeightMap = HashMap<Int, Int>()
    private var chosenWeightId = 0

    //Overrides

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

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        chosenWeightId = gradeWeightMap[position]!!
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }
    //OnClicks

    private fun setTopic() {
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

    private fun checkPresence() {
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

    private fun setGrade() {
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

    private fun setTest() {
        if(isOnline){
            if(subjectEntry.testID == ""){
                addTest()
            }
            else{
                val intent = Intent(MainApplication.appContext, TestActivity::class.java)
                intent.putExtra("testId",subjectEntry.testID)
                MainApplication.appContext!!.startActivity(intent)
            }
        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie zadania możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }


    //Privates

    private fun addTest(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val customAlertDialogView = View.inflate(this,R.layout.dialog_test,null)

        val topic = customAlertDialogView.findViewById<TextInputEditText>(R.id.dialog_test_topic_input)
        val type = customAlertDialogView.findViewById<Spinner>(R.id.dialog_test_type)

        val gradeWeights = GradeWeight.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val gradeWeightSpinnerList = ArrayList<String>()
        for((counter, weight: GradeWeight) in gradeWeights.withIndex()){
            gradeWeightSpinnerList.add(weight.name)
            gradeWeightMap[counter] = weight.id
        }
        chosenWeightId = gradeWeightMap[0]!!

        val gradeWeightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gradeWeightSpinnerList)

        gradeWeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        type.adapter = gradeWeightAdapter
        type.onItemSelectedListener = this


        builder.setView(customAlertDialogView)
            .setTitle("Dodawanie testu")
            .setMessage("Dodaj test")
            .setPositiveButton("OK") { dialog, _ ->

                val test = Test(
                    0,
                    topic.text.toString(),
                    chosenWeightId.toString(),
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

    fun setOnClickListeners(view: View){
        when(view.id){
            R.id.subject_topic_button -> setTopic()
            R.id.subject_grade_button -> setGrade()
            R.id.subject_presence_button -> checkPresence()
            R.id.subject_test_button -> setTest()
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
            topicButton.text = getString(R.string.correct)
            topicTextView.text = subjectEntry.topic
        }

        if(subjectEntry.presence == "" && isLate){
            presenceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.Secondary))
        }
        else if(subjectEntry.presence == "Y"){
            presenceButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
            presenceButton.text = getString(R.string.correct)
        }

        if(subjectEntry.testID != ""){
            testButton.setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
            testButton.text = getString(R.string.correct)
            testTextView.text = Test.readOne(db,subjectEntry.testID).topic
        }

        bannerButton.setBackgroundColor(ContextCompat.getColor(this, subjectEntry.getColor()))
    }


    //GraphQL

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
                    val testInput = Test(
                        resultTest.id,
                        resultTest.topic,
                        resultTest.type,
                        resultTest.subject_entry_id,
                        resultTest.graded!!,
                        resultTest.sUBJECT_ENTRY.date.toString(),
                        resultTest.sUBJECT_ENTRY.lESSON.start_time.toString()
                    )
                    testInput.insert(DatabaseHelper(MainApplication.appContext).writableDatabase)
                    subjectEntry.testID = testInput.id.toString()
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

}