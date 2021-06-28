package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class SubjectForClass(var id: Int,
                      var classId : Int,
                      var className : String,
                      var subjectName : String
                   ) {


    fun insert(db: SQLiteDatabase){
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, classId)
            put(COL3, className)
            put(COL4, subjectName)
        }
        db.insert(TABLE_NAME, null, values)
    }

    companion object{

        fun readAll(db: SQLiteDatabase): ArrayList<SubjectForClass> {
            val projection = arrayOf(COL1, COL2, COL3, COL4)

            val sortOrder = "$COL1 DESC"

            val cursor = db.query(
                TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                null,              // The columns for the WHERE clause
                null,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
            )

            val entries = ArrayList<SubjectForClass>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = SubjectForClass(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getString(2),
                            cursor.getString(3)
                    )
                    entries.add(entry)
                }
            }
            return entries
        }


        private const val TABLE_NAME = "SUBJECT_FOR_CLASS"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "SMALLINT"
        private const val COL2 = "CLASS_ID"
        private const val COL2_TYPE = "SMALLINT"
        private const val COL3 = "CLASS_NAME"
        private const val COL3_TYPE = "TEXT"
        private const val COL4 = "SUBJECT_NAME"
        private const val COL4_TYPE = "TEXT"


        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 $COL1_TYPE, " +
                "$COL2 $COL2_TYPE, " +
                "$COL3 $COL3_TYPE, " +
                "$COL4 $COL4_TYPE); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }

}