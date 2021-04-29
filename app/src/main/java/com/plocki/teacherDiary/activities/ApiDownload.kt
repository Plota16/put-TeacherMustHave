package com.plocki.teacherDiary.activities

import android.widget.Toast
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.ApolloInstance
import com.plocki.teacherDiary.DatabaseHelper
import com.plocki.teacherDiary.MainApplication
import com.plocki.teacherDiary.MyCalendarQuery
import com.plocki.teacherDiary.model.SubjectEntry
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ApiDownload(val id : Int) {

    fun init(){

        GlobalScope.launch(Dispatchers.Main) {
            val db = DatabaseHelper(MainApplication.appContext).readableDatabase
            db.execSQL(DatabaseHelper.DB_DELETE_ALL)
            db.execSQL(DatabaseHelper.DB_CREATE_ALL)
            val calendarJob = myCalendar(id)
            calendarJob.join()
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

                    for(entry in tmp.data!!.sUBJECT_ENTRY){
                        val subjectEntry = SubjectEntry(entry)

                        subjectEntry.insert(db)
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