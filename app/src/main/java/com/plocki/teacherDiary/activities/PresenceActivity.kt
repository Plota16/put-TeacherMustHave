package com.plocki.teacherDiary.activities

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.*
import com.plocki.teacherDiary.adapters.PresenceListAdapter
import com.plocki.teacherDiary.model.Presence
import com.plocki.teacherDiary.model.Student
import com.plocki.teacherDiary.model.SubjectEntry
import com.plocki.teacherDiary.type.STUDNET_SUBJECT_ENTRY_PRESENCE_insert_input
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class PresenceActivity : AppCompatActivity() {

    private var subjectId = 0
    private val list = ArrayList<Presence>()
    private  lateinit var  recycler : RecyclerView
    private lateinit var progess : View
    private var className = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_presence)

        val isChecked = intent.getStringExtra("isChecked") == "TAK"
        className = intent.getStringExtra("className")!!
        subjectId = intent.getStringExtra("subjectEntryId")!!.toInt()
        recycler = findViewById(R.id.presence_recycler)
        progess = findViewById(R.id.presence_progressBar)

        val db = DatabaseHelper(MainApplication.appContext).writableDatabase
        db.execSQL(Presence.DROP_TABLE)
        db.execSQL(Presence.CREATE_TABLE)

        if(isChecked){
            selectPresence()
        }
        else{
            populateNewPresence()
        }

        SubjectEntry.updatePresence(db,subjectId)
    }

    fun setupListeners(view: View){
        when(view.id){
            R.id.button -> addPresence()
        }
    }

    private fun addPresence() {
        val db = DatabaseHelper(MainApplication.appContext).readableDatabase
        val presenceList = Presence.readAll(db)

        val input = ArrayList<STUDNET_SUBJECT_ENTRY_PRESENCE_insert_input>()
        for(presence : Presence in presenceList){

            val entry = STUDNET_SUBJECT_ENTRY_PRESENCE_insert_input(presence = presence.presence.toInput(),
                                                                    student_id = presence.studentId.toInput(),
                                                                    subject_entry_id = presence.subjectEntryId.toInput())

            input.add(entry)
        }


        val mutation = AddPresenceMutation(input)
        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                        MainApplication.appContext, "Dodano obecność",
                        Toast.LENGTH_SHORT).show()
                    finish()
                }
            } catch (e: Error){
                Toast.makeText(
                        MainApplication.appContext, "Błąd dodawania obecności",
                    Toast.LENGTH_SHORT).show()
            }
        }

    }

    private fun populateNewPresence() {
        val studentList = Student.readOneClass(DatabaseHelper(MainApplication.appContext).readableDatabase, className)
        for (student: Student in studentList) {

            val presence = Presence(subjectId, student.id, "${student.firstName} ${student.lastName}", "BRAK")
            list.add(presence)
            presence.insert(DatabaseHelper(MainApplication.appContext).writableDatabase)
        }

        recycler.apply {
            layoutManager = LinearLayoutManager(this@PresenceActivity)
            adapter = PresenceListAdapter(list)
        }
        progess.visibility = View.GONE
    }

    private fun selectPresence(){

            val query = SelectPresenceQuery(subjectId.toInput())
            val db = DatabaseHelper(MainApplication.appContext).writableDatabase

             GlobalScope.launch(Dispatchers.Main) {
                try{
                    val result  = ApolloInstance.get().query(query).toDeferred().await()

                    try {
                        if(!result.hasErrors()){

                            for( singlePresence : SelectPresenceQuery.STUDNET_SUBJECT_ENTRY_PRESENCE in result.data!!.sTUDNET_SUBJECT_ENTRY_PRESENCE){
                                val presence = Presence(
                                        singlePresence.subject_entry_id,
                                        singlePresence.student_id,
                                        Student.getStudentName(db,singlePresence.student_id.toString()),
                                        singlePresence.presence
                                )
                                list.add(presence)
                                presence.insert(db)
                            }
                            deletePresence()
                        }
                    }catch (e: NullPointerException){
                        Toast.makeText(
                                MainApplication.appContext, "Błąd pobierania obecności",
                                Toast.LENGTH_SHORT
                        ).show()

                        populateNewPresence()
                    }
                }catch (e: Exception){
                    Toast.makeText(
                            MainApplication.appContext, "Bład pobierania obecności",
                            Toast.LENGTH_SHORT
                    ).show()
                    populateNewPresence()
                }
            }

    }

    private fun deletePresence(){
        val mutation = DelPresenceMutation(subjectId.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            try{
                val result  = ApolloInstance.get().mutate(mutation).toDeferred().await()

                try {
                    if(!result.hasErrors()){
                        recycler.apply {
                            layoutManager = LinearLayoutManager(this@PresenceActivity)
                            adapter = PresenceListAdapter(list)
                        }
                        progess.visibility = View.GONE
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania obecności",
                            Toast.LENGTH_SHORT
                    ).show()

                    populateNewPresence()
                }
            }catch (e: Exception){
                Toast.makeText(
                        MainApplication.appContext, "Bład pobierania obecności",
                        Toast.LENGTH_SHORT
                ).show()
                populateNewPresence()
            }
        }

    }

}