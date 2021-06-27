package com.plocki.teacherDiary.activities

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.plocki.teacherDiary.AddGradeMutation
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.Grade
import com.plocki.teacherDiary.model.GradeName
import com.plocki.teacherDiary.model.GradeWeight
import com.plocki.teacherDiary.model.Student
import com.plocki.teacherDiary.type.GRADE_insert_input
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant

class GradeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val studentMap = HashMap<Int, Int>()
    private val gradeNameMap = HashMap<Int, Int>()
    private val gradeWeightMap = HashMap<Int, Int>()

    private var chosenStudentId = 0
    private var chosenWeightId = 0
    private var chosenNameId = 0
    private var subjectId = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grade)

        subjectId = intent.getStringExtra("subjectEntryId")!!.toInt()

        setStudentSpinner()
        setGradeNameSpinner()
        setGradeWeightSpinner()

        setOnFocusChangeListeners()

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        when(parent!!.id){
            R.id.grade_studnet -> chosenStudentId = studentMap[position]!!
            R.id.grade_name -> chosenNameId = gradeNameMap[position]!!
            R.id.grade_weight -> chosenWeightId = gradeWeightMap[position]!!
        }
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    fun setupListeners(view: View){
        when(view.id){
            R.id.grade_submit -> submit()
        }
    }

    private fun setStudentSpinner(){

        val className = intent.getStringExtra("className")!!
        val myClass = Student.readOneClass(DatabaseHelper(MainApplication.appContext).readableDatabase, className)
        val studentSpinnerList = ArrayList<String>()
        for((counter, student: Student) in myClass.withIndex()){
            studentSpinnerList.add("${student.firstName} ${student.lastName}")
            studentMap[counter] = student.id
        }
        chosenStudentId = studentMap[0]!!

        val studentSpinner = findViewById<Spinner>(R.id.grade_studnet)
        val studentAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, studentSpinnerList)

        studentAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        studentSpinner.adapter = studentAdapter
        studentSpinner.onItemSelectedListener = this
    }

    private fun setGradeNameSpinner(){

        val gradeNames = GradeName.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val gradeNameSpinnerList = ArrayList<String>()
        for((counter, name: GradeName) in gradeNames.withIndex()){
            gradeNameSpinnerList.add(name.symbol)
            gradeNameMap[counter] = name.id
        }
        chosenNameId = gradeNameMap[0]!!

        val gradeNameSpinner = findViewById<Spinner>(R.id.grade_name)
        val gradeNameAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gradeNameSpinnerList)

        gradeNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeNameSpinner.adapter = gradeNameAdapter
        gradeNameSpinner.onItemSelectedListener = this
    }

    private fun setGradeWeightSpinner(){

        val gradeWeights = GradeWeight.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val gradeWeightSpinnerList = ArrayList<String>()
        for((counter, weight: GradeWeight) in gradeWeights.withIndex()){
            gradeWeightSpinnerList.add(weight.name)
            gradeWeightMap[counter] = weight.id
        }
        chosenWeightId = gradeWeightMap[0]!!

        val gradeWeightSpinner = findViewById<Spinner>(R.id.grade_weight)
        val gradeWeightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gradeWeightSpinnerList)

        gradeWeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeWeightSpinner.adapter = gradeWeightAdapter
        gradeWeightSpinner.onItemSelectedListener = this
    }

    private fun setOnFocusChangeListeners(){

        findViewById<TextInputEditText>(R.id.grade_description_input).setOnFocusChangeListener{ _, hasFocus ->
            if(!hasFocus){
                validateDescription()
            }
        }
    }

    private fun submit() {
        if(validateDescription()){
            val grade = Grade(
                    0,
                    subjectId,
                    chosenStudentId,
                    chosenNameId,
                    chosenWeightId,
                    Instant.now().toString(),
                    findViewById<TextInputEditText>(R.id.grade_description_input).text.toString(),
                    null
            )


            val input = GRADE_insert_input(  date = grade.date.toInput(),
                                            student_id = grade.studentId.toInput(),
                                            subject_for_class_id = grade.subjectForClassId.toInput(),
                                            description = grade.descroption.toInput(),
                                            grade = grade.grade.toInput(),
                                            weight = grade.weight.toInput())

            val mutation = AddGradeMutation(input)

            GlobalScope.launch(Dispatchers.Main) {
                val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

                try{
                    if(!result.hasErrors())
                    Toast.makeText(
                        MainApplication.appContext, "Dodano ocenę",
                        Toast.LENGTH_SHORT).show()
                    finish()

                } catch (e: Error){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd dodawania obecności",
                        Toast.LENGTH_SHORT).show()
                }
            }
            }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Nie wybrano ucznia",
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun validateDescription(): Boolean {
        return if(findViewById<TextInputEditText>(R.id.grade_description_input).text.toString() ==  ""){
            findViewById<TextInputLayout>(R.id.grade_description).error = "Nie podano opisu"
            false
        } else{
            findViewById<TextInputLayout>(R.id.grade_description).error = ""
            true
        }

    }

}