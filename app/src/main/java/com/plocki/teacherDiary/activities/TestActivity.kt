package com.plocki.teacherDiary.activities

import android.app.AlertDialog
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.textfield.TextInputEditText
import com.plocki.teacherDiary.*
import com.plocki.teacherDiary.adapters.GradeListAdapter
import com.plocki.teacherDiary.model.*
import com.plocki.teacherDiary.type.GRADE_insert_input
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var test: Test
    private var studentList = ArrayList<MyClassStudent>()
    lateinit var  db : SQLiteDatabase

    private val gradeWeightMap = HashMap<Int, Int>()
    private var chosenWeightId = 0

    var isOnline = true

    //overrides

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)


        val testId = intent.getStringExtra("testId")
        db = DatabaseHelper(MainApplication.appContext).writableDatabase


        test = Test.readOne(db,testId!!)
        val subjectEntry = SubjectEntry.readOne(db,test.subjectId)
        populateGrades()
        setTextViews(test,subjectEntry)
        setOnClicks()

        applyRecycler()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        chosenWeightId = gradeWeightMap[position]!!
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    //init

    private fun setTextViews(test: Test, subjectEntry: SubjectEntry){

        val gradeWeights = GradeWeight.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)


        findViewById<TextView>(R.id.test_details_topic).text = test.topic
        findViewById<TextView>(R.id.test_details_type).text = gradeWeights[test.type.toInt() - 1].name
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

    private fun applyRecycler() {
        val db = DatabaseHelper(MainApplication.appContext).readableDatabase
        val subjectEntry = SubjectEntry.readOne(db, test.subjectId)
        studentList = MyClassStudent.readOneClass(db, subjectEntry.className)
        val recycler = findViewById<RecyclerView>(R.id.test_recycler)
        populateGrades()
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GradeListAdapter(studentList, test.id, test.subjectId)
        }

    }


    //onClick
    private fun submit(){
        if(test.graded == "Y"){
            updateGrades()
        }
        else{
            insertNewGrades()
        }
    }

    private fun editTest(){
        if(isOnline){
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

            topic.text = Editable.Factory.getInstance().newEditable(test.topic)
            type.setSelection(test.type.toInt()-1)

            //TODO zmiana daty?

            builder.setView(customAlertDialogView)
                .setTitle("Dodawanie testu")
                .setMessage("Dodaj test")
                .setPositiveButton("OK") { dialog, _ ->

                    test.topic = topic.text.toString()
                    test.type = chosenWeightId.toString()
                    updateTestState()
                }
                .setNegativeButton("ANULUJ") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()
        }
        else{
            Toast.makeText(
                MainApplication.appContext, "Edycja tetu możłiwa tylko w trybie online",
                Toast.LENGTH_SHORT).show()
        }

    }

    private fun deleteTest(){
        val mutation = DelTetsMutation(test.id.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            try{
                val result  = ApolloInstance.get().mutate(mutation).toDeferred().await()

                try {
                    if(!result.hasErrors()){
                        Toast.makeText(
                            MainApplication.appContext, "Usunięto test",
                            Toast.LENGTH_SHORT
                        ).show()
                        Test.delete(db,test.id.toString())
                        val subjectEntry = SubjectEntry.readOne(db,test.subjectId)
                        subjectEntry.testID = ""
                        subjectEntry.updateTest(db)
                        finish()
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd pobierania obecności",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }catch (e: Exception){
                Toast.makeText(
                    MainApplication.appContext, "Bład pobierania obecności",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }

    //private

    private fun populateGrades(){
        if(test.graded == "Y"){
            getTestGrades()
        }
        else{
            createEmptyGrades()
        }
    }

    private fun createEmptyGrades(){
        DatabaseHelper(MainApplication.appContext).writableDatabase.execSQL(Grade.DROP_TABLE)
        DatabaseHelper(MainApplication.appContext).writableDatabase.execSQL(Grade.CREATE_TABLE)
        for(student: MyClassStudent in studentList){
            val grade = Grade(
                    0,
                    test.subjectId,
                    student.id,
                    1,
                    test.type.toInt(),
                    test.date,
                    test.topic,
                    test.id.toString()
            )
            grade.insert(DatabaseHelper(MainApplication.appContext).writableDatabase)
        }
    }


    //graphql

    private fun getTestGrades(){
        val query = SelectGradeQuery(test.id.toInput())
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(Grade.DROP_TABLE)
                        db.execSQL(Grade.CREATE_TABLE)
                        for (grade in tmp.data!!.gRADE){
                            val gradeVal = Grade(grade.id,grade.subject_for_class_id,grade.student_id,grade.grade,grade.weight,grade.date.toString(),grade.description,grade.testId.toString())
                            gradeVal.insert(db)
                        }
                    }
                    else{
                        Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania ocen",
                            Toast.LENGTH_SHORT
                        ).show()
                        finish()
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd pobierania ocen",
                        Toast.LENGTH_SHORT
                    ).show()
                    createEmptyGrades()
                }
            }catch (e: Exception){
                Toast.makeText(
                    MainApplication.appContext, "Bład połączenia z serwerem",
                    Toast.LENGTH_SHORT
                ).show()
                createEmptyGrades()
            }
        }
    }

    private fun insertNewGrades(){
        val gradeList = Grade.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val input = ArrayList<GRADE_insert_input>()
        for(grade in gradeList){
            val gradeInput = GRADE_insert_input(
                date = grade.date.toInput(),
                description = grade.descroption.toInput(),
                grade = grade.grade.toInput(),
                student_id = grade.studentId.toInput(),
                subject_for_class_id = grade.subjectForClassId.toInput(),
                testId = grade.testid!!.toInt().toInput(),
                weight = grade.weight.toInput()
            )
            input.add(gradeInput)
        }

        val mutation = AddTestGradesMutation(input)

        GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().mutate(mutation).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        Toast.makeText(
                            MainApplication.appContext, "Dodano oceny",
                            Toast.LENGTH_SHORT
                        ).show()
                        test.graded = "Y"
                        updateTestState()
                    }
                    else{
                        Toast.makeText(
                            MainApplication.appContext, "Błąd dodawania ocen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }catch (e: NullPointerException){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd dodawania ocen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }catch (e: Exception){
                Toast.makeText(
                    MainApplication.appContext, "Bład połączenia z serwerem",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateGrades(){
        val mutation = DelTestGradesMutation(test.id.toInput())

        GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().mutate(mutation).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        insertNewGrades()
                    }
                    else{
                        Toast.makeText(
                            MainApplication.appContext, "Błąd dodawania ocen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }catch (e: NullPointerException){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd dodawania ocen",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }catch (e: Exception){
                Toast.makeText(
                    MainApplication.appContext, "Bład połączenia z serwerem",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    private fun updateTestState(){
        val mutation = UpdateTestMutation(test.id.toInput(),test.graded.toInput(), test.topic.toInput(), test.type.toInput())

        GlobalScope.launch(Dispatchers.Main) {
            try{
                val result  = ApolloInstance.get().mutate(mutation).toDeferred().await()

                try {
                    if(!result.hasErrors()){
                        Toast.makeText(
                            MainApplication.appContext, "edytowano test",
                            Toast.LENGTH_SHORT
                        ).show()
                        test.update(db)
                        finish()
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd edytowania testu",
                        Toast.LENGTH_SHORT
                    ).show()

                }
            }catch (e: Exception){
                Toast.makeText(
                    MainApplication.appContext, "Bład edytowania testu",
                    Toast.LENGTH_SHORT
                ).show()

            }
        }
    }
}