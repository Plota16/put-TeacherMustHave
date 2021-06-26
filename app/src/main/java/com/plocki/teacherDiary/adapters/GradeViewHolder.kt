package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Spinner
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.Grade
import com.plocki.teacherDiary.model.GradeName
import com.plocki.teacherDiary.model.Student
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication


class GradeViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
    RecyclerView.ViewHolder(inflater.inflate(R.layout.test_grade, parent, false)), AdapterView.OnItemSelectedListener {

    private val gradeNameMap = HashMap<Int, Int>()
    private var chosenNameId = 0
    private var testId = 0
    private var studentId = 0
    private var subjectId = 0

    private var nameTextView = itemView.findViewById<TextView>(R.id.test_grade_text)
    var gradeSpinner = itemView.findViewById<Spinner>(R.id.test_grade_grade)!!

    var pos = 0

    fun bind(student: Student, test: Int, subject: Int) {
        val studentName = "${student.firstName} ${student.lastName}"
        testId = test
        studentId = student.id
        subjectId = subject
        nameTextView.text = studentName
        setGradeNameSpinner()
    }

    private fun setGradeNameSpinner(){

        val gradeNames = GradeName.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val gradeNameSpinnerList = ArrayList<String>()

        gradeNameSpinnerList.add("Brak")
        gradeNameMap[0] = 0
        for((counter, name: GradeName) in gradeNames.withIndex()){
            gradeNameSpinnerList.add(name.symbol)
            gradeNameMap[counter+1] = name.id
        }
        chosenNameId = gradeNameMap[0]!!

        val gradeNameAdapter = ArrayAdapter(MainApplication.appContext!!, android.R.layout.simple_spinner_item, gradeNameSpinnerList)

        gradeNameAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        gradeSpinner.adapter = gradeNameAdapter
        gradeSpinner.onItemSelectedListener = this

        pos = try{
            val gradeNameId = Grade.readOne(DatabaseHelper(MainApplication.appContext).readableDatabase, studentId.toString(), testId.toString(), subjectId.toString()).grade
            gradeNameAdapter.getPosition(gradeNameSpinnerList[gradeNameId])
        }catch (e : IndexOutOfBoundsException){
            0
        }

    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        chosenNameId = gradeNameMap[position+1]!!
        Grade.updateGrade(DatabaseHelper(MainApplication.appContext).writableDatabase, testId, chosenNameId, studentId)
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }

}