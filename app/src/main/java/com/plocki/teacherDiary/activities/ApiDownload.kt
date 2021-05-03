package com.plocki.teacherDiary

import android.widget.Toast
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.model.MyClassStudent
import com.plocki.teacherDiary.model.SubjectEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ApiDownload(val id : Int) {

    fun init(){

        GlobalScope.launch(Dispatchers.Main) {
            val calendarJob = myCalendar(id)
            val myClassesJob = myClasses(id)
            calendarJob.join()
            myClassesJob.join()
        }
    }
    @Insert(onConflict = OnConflictStrategy.REPLACE)
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
                            val subjectEntry = SubjectEntry(entry.iD,
                                                            entry.dATE.toString(),
                                                            entry.lESSON.sTART_TIME.toString(),
                                                            entry.lESSON.eND_TIME.toString(),
                                                            entry.sUBJECT_FOR_CLASS.cLASS.nAME.toString(),
                                                            entry.tOPIC.toString(),
                                                            entry.sUBJECT_FOR_CLASS.sUBJECT_NAME)


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
                                val myClassStudent = MyClassStudent(student.iD,studentClass.nAME,student.fIRST_NAME,student.lAST_NAME)
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

}