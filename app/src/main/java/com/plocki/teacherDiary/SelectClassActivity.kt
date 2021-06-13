package com.plocki.teacherDiary

import android.os.Bundle
import android.view.LayoutInflater
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class SelectClassActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_class)

        val classId = 0

        val query = SelectSubjectesQuery(classId.toInput())

        GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    if(!tmp.hasErrors()){

                        val dataTable = findViewById<TableLayout>(R.id.database_layout)

                        for((index, subject) in tmp.data!!.sUBJECT_FOR_CLASS.withIndex()){
                            val row = LayoutInflater.from(MainApplication.appContext).inflate(R.layout.classes_row, null)
                            row.findViewById<TextView>(R.id.database_row1).text = subject.subject_name
                            row.findViewById<TextView>(R.id.database_row2).text = "${subject.tEACHER.first_name} ${subject.tEACHER.last_name}"
                            row.findViewById<TextView>(R.id.database_row3).text = ""
                            row.setOnClickListener {
                                Toast.makeText(
                                    MainApplication.appContext, (index+1).toString(),
                                    Toast.LENGTH_SHORT).show()
                            }
                            dataTable.addView(row,index+1)
                        }

                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd pobierania klas",
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
}