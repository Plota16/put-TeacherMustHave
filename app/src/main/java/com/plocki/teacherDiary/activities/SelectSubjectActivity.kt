package com.plocki.teacherDiary.activities

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.card.MaterialCardView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.SelectSubjectesQuery
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.*

class SelectSubjectActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_subject)

        val studentId = intent.getStringExtra("studentId")!!.toInt()


        val query = SelectSubjectesQuery(studentId.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            try{
                val response  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    if(!response.hasErrors()){
                        findViewById<View>(R.id.select_subject_progressBar).visibility = View.GONE
                        findViewById<ScrollView>(R.id.select_subject_visible).visibility = View.VISIBLE

                        setupTextViews(response.data!!.sUBJECT_FOR_CLASS[0].cLASS.sTUDENTs[0])
                        setupTableView(response)
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania przedmiotów",
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
    }

    private fun setupTableView(response: Response<SelectSubjectesQuery.Data>) {
        val dataTable = findViewById<TableLayout>(R.id.select_subject_tableLayout)
        for((index, subject) in response.data!!.sUBJECT_FOR_CLASS.withIndex()){

            val teacherName = "${subject.tEACHER.first_name} ${subject.tEACHER.last_name}"
            val row = LayoutInflater.from(this@SelectSubjectActivity).inflate(R.layout.template_table_row,null)
            if(index%2==1){
                row.findViewById<MaterialCardView>(R.id.template_row_tableLayout).setBackgroundColor(ContextCompat.getColor(this@SelectSubjectActivity, android.R.color.background_light))
            }
            row.findViewById<TextView>(R.id.template_row_column1).text = subject.subject_name
            row.findViewById<TextView>(R.id.template_row_column2).text = teacherName

            row.setOnClickListener {
                val intent = Intent(MainApplication.appContext, SelectSubjectActivity::class.java)
                intent.putExtra("subjectId", subject.id.toString())
                intent.putExtra("studentId", response.data!!.sUBJECT_FOR_CLASS[0].cLASS.sTUDENTs[0].id.toString())
                startActivity(intent)
            }
            dataTable.addView(row,index+2)
        }
    }
    private fun setupTextViews(student: SelectSubjectesQuery.STUDENT){

        findViewById<TextView>(R.id.select_subject_main_firstName_input).text = student.first_name
        findViewById<TextView>(R.id.select_subject_main_secondName_input).text = student.second_name
        findViewById<TextView>(R.id.select_subject_main_LastName_input).text = student.last_name
        findViewById<TextView>(R.id.select_subject_main_pesel_input).text = student.pesel.toString()
        findViewById<TextView>(R.id.select_subject_main_gender_input).text = getGender(student.gender.toString())
        findViewById<TextView>(R.id.select_subject_main_citizen_input).text = student.citizen

        findViewById<TextView>(R.id.select_subject_adress_city_input).text = student.adress_city
        findViewById<TextView>(R.id.select_subject_adress_street_input).text = student.adress_street
        findViewById<TextView>(R.id.select_subject_adress_number_input).text = student.adress_number


        var name = "${student.first_name} ${student.last_name}".toUpperCase(Locale.getDefault())
        findViewById<TextView>(R.id.select_subject_contact_name).text = name
        findViewById<TextView>(R.id.select_subject_contact_number).text = getPhoneNumber(student.contact_phone!!)
        findViewById<TextView>(R.id.select_subject_contact_mail).text = student.contact_mail!!.toLowerCase(Locale.getDefault())
        name = "${student.parent1_name} ${student.last_name} (rodzic)".toUpperCase(Locale.getDefault())
        findViewById<TextView>(R.id.select_subject_contact_p1_name).text = name
        findViewById<TextView>(R.id.select_subject_contact_p1_number).text = getPhoneNumber(student.parent1_contact_phone!!)
        findViewById<TextView>(R.id.select_subject_contact_p1_mail).text = student.parent1_contact_mail.toString().toLowerCase(Locale.getDefault())
        name = "${student.parent2_name} ${student.last_name} (rodzic)".toUpperCase(Locale.getDefault())
        findViewById<TextView>(R.id.select_subject_contact_p2_name).text = name
        findViewById<TextView>(R.id.select_subject_contact_p2_number).text = getPhoneNumber(student.parent2_contact_phone!!)
        findViewById<TextView>(R.id.select_subject_contact_p2_mail).text = student.parent2_contact_mail.toString().toLowerCase(Locale.getDefault())

    }

    private fun getPhoneNumber(input: Int): String {
        val str = input.toString()
        return "+48 ${str.substring(0,3)} ${str.substring(3,6)} ${str.substring(6,9)}"
    }

    private fun getGender(symbol: String): String {
        return when(symbol){
            "M" -> "Meżczyzna"
            "K" -> "Kobieta"
            else -> "Inna"
        }
    }

}