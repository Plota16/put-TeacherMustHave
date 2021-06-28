package com.plocki.teacherDiary.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.database.sqlite.SQLiteDatabase
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat
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
import com.plocki.teacherDiary.utility.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TestActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    lateinit var test: Test
    private var studentList = ArrayList<Student>()
    lateinit var  db : SQLiteDatabase

    private val gradeWeightMap = HashMap<Int, Int>()
    private var chosenWeightId = 0
    private var chosenDateId = 0


    private lateinit var testCheckSwitch : SwitchCompat
    private lateinit var testWaitSwitch : SwitchCompat
    private val subjectEntryLists = ArrayList<Int>()
    private val dateSpinnerList = ArrayList<String>()
    private val gradeWeightSpinnerList = ArrayList<String>()
    var isOnline = true
    //overrides

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)


        val testId = intent.getStringExtra("testId")
        db = DatabaseHelper(MainApplication.appContext).writableDatabase
        testCheckSwitch = findViewById(R.id.test_state_checked)
        testWaitSwitch = findViewById(R.id.test_state_waiting)

        test = Test.readOne(db,testId!!)
        val subjectEntry = SubjectEntry.readOne(db,test.subjectId)
//        populateGrades()
        setTextViews(test,subjectEntry)
        setOnClicks()

        applyRecycler()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent!!.id){
            R.id.dialog_test_type -> chosenWeightId = gradeWeightMap[position]!!
            R.id.dialog_test_date -> chosenDateId = subjectEntryLists[position]
        }
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
                testWaitSwitch.isChecked = true
                testCheckSwitch.isClickable = true
            }
            "Y" -> {
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_green))
                stateTextView.text = getString(R.string.test_state_checked)
                findViewById<RecyclerView>(R.id.test_recycler).visibility = View.VISIBLE
                testCheckSwitch.isChecked = true
                testCheckSwitch.isClickable = true
                testWaitSwitch.isChecked = true
                testWaitSwitch.isClickable = false
            }
        }
    }

    private fun setOnClicks(){
        val stateTextView: TextView = findViewById(R.id.test_details_state)

        findViewById<Button>(R.id.test_details_confirm).setOnClickListener {
            submit()
        }
        findViewById<Button>(R.id.test_details_delete).setOnClickListener {
            deleteTest()
        }
        findViewById<Button>(R.id.test_details_edit).setOnClickListener {
            editTest()
        }

        testWaitSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                testCheckSwitch.isClickable = true
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_yellow))
                stateTextView.text = getString(R.string.test_state_unchecked)
            }else{
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_red))
                stateTextView.text = getString(R.string.test_state_waiting)
                testCheckSwitch.isClickable = false
            }
        }
        testCheckSwitch.setOnCheckedChangeListener { _, isChecked ->
            if(isChecked){
                findViewById<RecyclerView>(R.id.test_recycler).visibility = View.VISIBLE
                testWaitSwitch.isClickable = false
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_green))
                stateTextView.text = getString(R.string.test_state_checked)
            }
            else{
                testWaitSwitch.isClickable = true
                findViewById<RecyclerView>(R.id.test_recycler).visibility = View.GONE
                stateTextView.setTextColor(ContextCompat.getColor(MainApplication.appContext!!, R.color.light_yellow))
                stateTextView.text = getString(R.string.test_state_unchecked)
            }
        }

    }

    private fun applyRecycler() {
        val db = DatabaseHelper(MainApplication.appContext).readableDatabase
        val subjectEntry = SubjectEntry.readOne(db, test.subjectId)
        studentList = Student.readOneClass(db, subjectEntry.className)

        populateGrades()

    }


    //onClick
    private fun submit(){
        if(testCheckSwitch.isChecked){
            if(test.graded == "Y"){
            updateGrades("update")
        }
            else{
                insertNewGrades()
            }
        }
        else if(!testCheckSwitch.isChecked && testWaitSwitch.isChecked){
            updateGrades("delete")
            test.graded = "N"
            updateTestState(null)
        }
        else{
            updateGrades("delete")
            test.graded = "F"
            updateTestState(null)
        }
    }

    private fun editTest(){
        if(isOnline){

            val teacherId = Store().retrieve("teacherId")
            val query = SelectSubjectEntriesQuery(teacherId!!.toInt().toInput(),test.subjectId.toInput())

            GlobalScope.launch(Dispatchers.Main) {
                try{
                    val tmp  = ApolloInstance.get().query(query).toDeferred().await()
                    try {
                        if(!tmp.hasErrors()){
                            dateSpinnerList.clear()
                            subjectEntryLists.clear()
                            for (entry in tmp.data!!.sUBJECT_ENTRY){
                                val label = "${entry.date} - ${entry.dAY.name_pl} - ${entry.lESSON.start_time}"
                                dateSpinnerList.add(label)
                                subjectEntryLists.add(entry.id)
                            }
                            setupSpinners()
                        }
                        else{
                            Toast.makeText(
                                    MainApplication.appContext, "Błąd edycji testu",
                                    Toast.LENGTH_SHORT
                            ).show()
                        }

                    }catch (e: NullPointerException){
                        Toast.makeText(
                                MainApplication.appContext, "Błąd edycji testu",
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
            else{
                Toast.makeText(
                    MainApplication.appContext, "Edycja tetu możłiwa tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
            }

    }

    private fun deleteTest(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("Test")
                .setMessage("Czy chcesz usunąć test?")
                .setPositiveButton("Tak") { dialog, _ ->
                    deleteTestMutation(dialog)
                }
                .setNegativeButton("Nie") { dialog, _ ->
                    dialog.cancel()}
                .show()
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
        for(student: Student in studentList){
            val grade = Grade(
                    0,
                    test.subjectId,
                    student.id,
                    0,
                    test.type.toInt(),
                    test.date,
                    test.topic,
                    test.id.toString()
            )
            grade.insert(DatabaseHelper(MainApplication.appContext).writableDatabase)
        }
        val recycler = findViewById<RecyclerView>(R.id.test_recycler)
        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = GradeListAdapter(studentList, test.id, test.subjectId)
        }
    }

    private fun setupSpinners(){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val customAlertDialogView = View.inflate(this,R.layout.dialog_test,null)

        val topic = customAlertDialogView.findViewById<TextInputEditText>(R.id.dialog_test_topic_input)
        topic.text = Editable.Factory.getInstance().newEditable(test.topic)
        gradeWeightSpinnerList.clear()


        val gradeWeights = GradeWeight.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        var current = 0
        for((counter, weight: GradeWeight) in gradeWeights.withIndex()){
            gradeWeightSpinnerList.add(weight.name)
            gradeWeightMap[counter] = weight.id
            if(weight.id == test.type.toInt()){
                current = counter
            }
        }

        chosenWeightId = test.type.toInt()
        val type = customAlertDialogView.findViewById<Spinner>(R.id.dialog_test_type)
        val gradeWeightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gradeWeightSpinnerList)
        gradeWeightAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        type.adapter = gradeWeightAdapter
        type.onItemSelectedListener = this
        type.setSelection(current)

        chosenDateId = test.subjectId
        val allTestDates = customAlertDialogView.findViewById<Spinner>(R.id.dialog_test_date)
        val dateAdapter = ArrayAdapter(this@TestActivity, android.R.layout.simple_spinner_item, dateSpinnerList)
        dateAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        allTestDates.adapter = dateAdapter
        allTestDates.onItemSelectedListener = this@TestActivity
        allTestDates.setSelection(subjectEntryLists.indexOf(test.subjectId))


        type.setSelection(test.type.toInt()-1)

        builder.setView(customAlertDialogView)
                .setTitle("Edytowanie testu")
                .setPositiveButton("OK") { _, _ ->
                    val oldSubjectId = test.subjectId
                    test.topic = topic.text.toString()
                    test.date = dateSpinnerList[subjectEntryLists.indexOf(chosenDateId)].split(" - ")[0]
                    test.time = dateSpinnerList[subjectEntryLists.indexOf(chosenDateId)].split(" - ")[2]
                    test.type = chosenWeightId.toString()
                    test.subjectId = chosenDateId
                    updateTestState(oldSubjectId)
                }
                .setNegativeButton("ANULUJ") { dialog, _ ->
                    dialog.dismiss()
                }
                .show()

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
                        getZeroGrades()
                        val recycler = findViewById<RecyclerView>(R.id.test_recycler)
                        recycler.apply {
                            layoutManager = LinearLayoutManager(context)
                            adapter = GradeListAdapter(studentList, test.id, test.subjectId)
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
                grade = (grade.grade).toInput(),
                student_id = grade.studentId.toInput(),
                subject_for_class_id = grade.subjectForClassId.toInput(),
                testId = grade.testid!!.toInt().toInput(),
                weight = grade.weight.toInput()
            )
            if(grade.grade != 0){
                input.add(gradeInput)
            }
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
                        updateTestState(null)
                    }
                    else{
                        val c =0
                        Toast.makeText(
                            MainApplication.appContext, "Błąd dodawania ocen",
                            Toast.LENGTH_SHORT
                        ).show()
                    }

                }catch (e: NullPointerException){
                    val c = e
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

    private fun updateGrades(mode : String){
        val mutation = DelTestGradesMutation(test.id.toInput())

        GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().mutate(mutation).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        if(mode == "update"){
                            insertNewGrades()
                        }
                        if(mode == "delete"){
                            updateTestState(null)
                        }
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

    private fun deleteTestMutation(dialog : DialogInterface){
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

    private fun updateTestState(oldSubjectId: Int?) {
        val mutation = UpdateTestMutation(test.id.toInput(),test.graded.toInput(), test.topic.toInput(), test.type.toInput(), test.subjectId.toInput())

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
                        if(oldSubjectId != null && oldSubjectId != test.subjectId){
                            val oldSubjectEntry = SubjectEntry.readOne(db,oldSubjectId)
                            oldSubjectEntry.testID = ""
                            oldSubjectEntry.updateTest(db)
                        }
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

    private fun getZeroGrades(){
        for (student in studentList){
            var isGraded = false
            val gradeList = Grade.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
            for(grade in gradeList){
                if(student.id == grade.studentId){
                    isGraded = true
                }
            }
            if(!isGraded){
                val grade = Grade(
                        0,
                        test.subjectId,
                        student.id,
                        0,
                        test.type.toInt(),
                        test.date,
                        test.topic,
                        test.id.toString()
                )
                grade.insert(DatabaseHelper(MainApplication.appContext).writableDatabase)
            }
        }
    }


}