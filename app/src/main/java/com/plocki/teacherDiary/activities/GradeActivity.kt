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
import com.plocki.teacherDiary.model.MyClassStudent
import com.plocki.teacherDiary.type.GRADE_insert_input
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.time.Instant

class GradeActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val map = HashMap<Int, Int>()
    private var chosenStudentId = 0
    private var subjectId = 0

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_grade)

        val className = intent.getStringExtra("className")!!
        subjectId = intent.getStringExtra("subjectEntryId")!!.toInt()

        val myClass = MyClassStudent.readOneClass(DatabaseHelper(MainApplication.appContext).readableDatabase, className)
        val spinnerList = ArrayList<String>()
        for((counter, student: MyClassStudent) in myClass.withIndex()){
            spinnerList.add("${student.firstName} ${student.lastName}")
            map[counter] = student.id
        }
        chosenStudentId = map[0]!!

        val spinner = findViewById<Spinner>(R.id.grade_studnet)
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, spinnerList)

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinner.adapter = adapter
        spinner.onItemSelectedListener = this

        findViewById<TextInputEditText>(R.id.grade_grade_input).setOnFocusChangeListener{ _, hasFocus ->
            if(!hasFocus){
                validateGrade()
            }
        }

        findViewById<TextInputEditText>(R.id.grade_weight_input).setOnFocusChangeListener{ _, hasFocus ->
            if(!hasFocus){
                validateWeight()
            }
        }

        findViewById<TextInputEditText>(R.id.grade_description_input).setOnFocusChangeListener{ _, hasFocus ->
            if(!hasFocus){
                validateDescription()
            }
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        chosenStudentId = map[position]!!
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

    fun submit(view: View){
        if(validateDescription() && validateGrade() && validateWeight()){
            val grade = Grade(
                    0,
                    subjectId,
                    chosenStudentId,
                    findViewById<TextInputEditText>(R.id.grade_grade_input).text.toString().toDouble(),
                    findViewById<TextInputEditText>(R.id.grade_weight_input).text.toString().toInt(),
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

    private fun validateGrade() : Boolean{
        return if(findViewById<TextInputEditText>(R.id.grade_grade_input).text.toString() == ""){
            findViewById<TextInputLayout>(R.id.grade_grade).error = "Nie podano oceny"
            false
        } else{
            findViewById<TextInputLayout>(R.id.grade_grade).error = ""
            true
        }

    }

    private fun validateWeight() : Boolean{
        return if(findViewById<TextInputEditText>(R.id.grade_weight_input).text.toString() == ""){
            findViewById<TextInputLayout>(R.id.grade_weight).error = "Nie podano wagi"
            false
        } else{
            findViewById<TextInputLayout>(R.id.grade_weight).error = ""
            true
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