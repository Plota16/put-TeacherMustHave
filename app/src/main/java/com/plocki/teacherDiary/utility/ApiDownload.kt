package com.plocki.teacherDiary.utility

import android.widget.Toast
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.*
import com.plocki.teacherDiary.model.*
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
            val selectClasses = selectClasses(id)
            val task = selectTasks(userId)
            val test = selectTests(id)
            val gradeName = selectGradeNames()
            val gradeWeight = selectGradeWeights()
            task.join()
            test.join()
            gradeName.join()
            gradeWeight.join()
            selectClasses.join()
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

                            if (entry.sTUDNET_SUBJECT_ENTRY_PRESENCEs.size == Student.classSize(db, entry.sUBJECT_FOR_CLASS.cLASS.name)){
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
                        db.execSQL(Student.DROP_TABLE)
                        db.execSQL(Student.CREATE_TABLE)
                        for(studentClass in tmp.data!!.cLASS){
                            for (student in studentClass.sTUDENTs){
                                val myClassStudent = Student(student.id,studentClass.name,student.first_name,student.last_name, studentClass.id)
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

    private fun selectGradeNames(): Job {

        val query = SelectGradeNameQuery()
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        return GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(GradeName.DROP_TABLE)
                        db.execSQL(GradeName.CREATE_TABLE)

                        for(entry in tmp.data!!.gRADE_NAME){
                           val gradeName = GradeName(
                                                        entry.id,
                                                        entry.symbol,
                                                        entry.name,
                                                        entry.value!!
                           )
                            gradeName.insert(db)
                        }
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania nazw ocen",
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

    private fun selectGradeWeights(): Job {

        val query = SelectGradeWeightQuery()
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        return GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(GradeWeight.DROP_TABLE)
                        db.execSQL(GradeWeight.CREATE_TABLE)

                        for(entry in tmp.data!!.gRADE_WEIGHT){
                            val gradeWeight = GradeWeight(
                                    entry.id,
                                    entry.name,
                                    entry.weight
                            )
                            gradeWeight.insert(db)
                        }
                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania wag ocen",
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

    private fun selectClasses(teacherId: Int): Job {

        val query = SelectClassesQuery(teacherId.toInput())
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase

        return GlobalScope.launch(Dispatchers.Main) {
            try{
                val tmp  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    if(!tmp.hasErrors()){
                        db.execSQL(Class.DROP_TABLE)
                        db.execSQL(Class.CREATE_TABLE)

                        for(entry in tmp.data!!.cLASS){
                            val myClass = Class(
                                    entry.id,
                                    entry.name,
                                    entry.sTUDENTs.size,
                                    entry.sUBJECT_FOR_CLASSes.size,
                                    "${entry.tEACHER.first_name} ${entry.tEACHER.last_name}"
                            )
                            myClass.insert(db)
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

    private fun updateLate(){
        val db = DatabaseHelper(MainApplication.appContext).writableDatabase
        db.execSQL("UPDATE SUBJECT_ENTRY SET LATE = \"Y\" WHERE (DATE < CURRENT_DATE) or (DATE = CURRENT_DATE and END_TIME < CURRENT_TIME )")
    }

}