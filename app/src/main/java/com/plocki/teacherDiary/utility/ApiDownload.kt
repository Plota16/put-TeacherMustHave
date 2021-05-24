package com.plocki.teacherDiary.utility

import android.widget.Toast
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.MyCalendarQuery
import com.plocki.teacherDiary.MyClassesQuery
import com.plocki.teacherDiary.SelectTaskQuery
import com.plocki.teacherDiary.SelectTestQuery
import com.plocki.teacherDiary.model.MyClassStudent
import com.plocki.teacherDiary.model.SubjectEntry
import com.plocki.teacherDiary.model.Task
import com.plocki.teacherDiary.model.Test
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ApiDownload(val id : Int, private val userId: Int) {

    fun init(){

        GlobalScope.launch(Dispatchers.Main) {

            val myClassesJob = myClasses(id)
            myClassesJob.join()
            val calendarJob = myCalendar(id)
            calendarJob.join()
            val task = selectTasks(userId)
            task.join()
            val test = selectTests(id)
            test.join()

            updateLate()
        }
    }

    private fun myCalendar(teacherId: Int): Job {

        val query = MyCalendarQuery(teacherId.toInput())
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        return GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(SubjectEntry.DROP_TABLE)
                        db.execSQL(SubjectEntry.CREATE_TABLE)
                        for(entry in tmp.data!!.sUBJECT_ENTRY){

                            var presence = ""
                            var testID = ""

                            if (entry.sTUDNET_SUBJECT_ENTRY_PRESENCEs.size == MyClassStudent.classSize(db, entry.sUBJECT_FOR_CLASS.cLASS.name)){
                                presence = "Y"
                            }
                            if(entry.tESTs.isNotEmpty()){
                               testID = entry.tESTs[0].id.toString()
                            }



                            val subjectEntry = SubjectEntry(entry.id,
                                                            entry.date.toString(),
                                                            entry.lESSON.start_time.toString(),
                                                            entry.lESSON.end_time.toString(),
                                                            entry.sUBJECT_FOR_CLASS.cLASS.name,
                                                            entry.topic.toString(),
                                                            entry.sUBJECT_FOR_CLASS.subject_name,
                                                            testID,
                                                            presence,
                                                        ""
                                                            )


                            subjectEntry.insert(db)
                        }
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                        MainApplication.appContext, "Błąd pobierania kalendarza",
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

    private fun myClasses(teacherId: Int): Job {

        val query = MyClassesQuery(teacherId.toInput())
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        return GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(MyClassStudent.DROP_TABLE)
                        db.execSQL(MyClassStudent.CREATE_TABLE)
                        for(studentClass in tmp.data!!.cLASS){
                            for (student in studentClass.sTUDENTs){
                                val myClassStudent = MyClassStudent(student.id,studentClass.name,student.first_name,student.last_name, studentClass.id)
                                myClassStudent.insert(db)
                            }
                        }
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania kalendarza",
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

    private fun selectTasks(userId: Int): Job {

        val query = SelectTaskQuery(userId.toInput())
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        return GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(Task.DROP_TABLE)
                        db.execSQL(Task.CREATE_TABLE)
                        for(iterator in tmp.data!!.tASK){
                            val task = Task(
                                    iterator.id,
                                    iterator.name,
                                    iterator.description,
                                    iterator.end_date.toString(),
                                    iterator.state
                            )
                            task.insert(db)
                        }
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania kalendarza",
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

    private fun selectTests(teacherId: Int): Job {

        val query = SelectTestQuery(teacherId.toInput())
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        return GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()

                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(Test.DROP_TABLE)
                        db.execSQL(Test.CREATE_TABLE)
                        for (testEntry in tmp.data!!.tEST){
                            val test = Test(testEntry.id, testEntry.topic,testEntry.type, testEntry.subject_entry_id, testEntry.graded!!,testEntry.sUBJECT_ENTRY.date.toString(),testEntry.sUBJECT_ENTRY.lESSON.start_time.toString())
                            test.insert(db)
                        }
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania sprawdzianów",
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

    private fun updateLate(){
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase
        db.execSQL("UPDATE SUBJECT_ENTRY SET LATE = \"Y\" WHERE (DATE < CURRENT_DATE) or (DATE = CURRENT_DATE and END_TIME < CURRENT_TIME )")
    }



}