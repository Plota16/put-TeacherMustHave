package com.plocki.teacherDiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.ScrollView
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.card.MaterialCardView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.SelectStudentsQuery
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SelectStudentActivity : AppCompatActivity() {

    private var subjectForClassId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_student)

        val classId = intent.getStringExtra("classId")!!.toInt()
        val className = intent.getStringExtra("className")
        val title = "Uczniowie klasy $className"

        val query = SelectStudentsQuery(classId.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            try{
                val response  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    if(!response.hasErrors()){

                        findViewById<View>(R.id.select_student_progressBar).visibility = View.GONE
                        findViewById<ScrollView>(R.id.select_student_visibility).visibility = View.VISIBLE

                        setupTableView(response)
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania uczniów",
                            Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }catch (e: Exception){
                Toast.makeText(
                        MainApplication.appContext, "Bład połączenia z serwerem",
                        Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }

        subjectForClassId = try {
            intent.getStringExtra("subjectForClassId")!!.toInt()
        }catch (e : Exception){
            0
        }
        findViewById<TextView>(R.id.select_student_title).text = title
    }

    private fun setupTableView(response: Response<SelectStudentsQuery.Data>) {

        val dataTable = findViewById<TableLayout>(R.id.select_student_tableLayout)
        for((index, student) in response.data!!.sTUDENT.withIndex()){

            val name = "${student.first_name} ${student.last_name}"
            val row = LayoutInflater.from(this@SelectStudentActivity).inflate(R.layout.template_table_row,null)
            if(index%2==1){
                row.findViewById<MaterialCardView>(R.id.template_row_tableLayout).setBackgroundColor(ContextCompat.getColor(this@SelectStudentActivity, android.R.color.background_light))
            }
            row.findViewById<TextView>(R.id.template_row_column1).text = (index+1).toString()
            row.findViewById<TextView>(R.id.template_row_column2).text = name

            row.setOnClickListener {

                if(subjectForClassId != 0) {
                    val intent = Intent(MainApplication.appContext, SubjectDetailActivity::class.java)
                    intent.putExtra("studentId", student.id.toString())
                    intent.putExtra("studentName", name)
                    intent.putExtra("subjectId", subjectForClassId.toString())
                    startActivity(intent)
                }
                else{
                    val intent = Intent(MainApplication.appContext, SelectSubjectActivity::class.java)
                    intent.putExtra("studentId", student.id.toString())
                    intent.putExtra("studentName", name)
                    intent.putExtra("studentId", student.id.toString())
                    startActivity(intent)
                }
            }
            dataTable.addView(row,index+2)
        }
    }
}