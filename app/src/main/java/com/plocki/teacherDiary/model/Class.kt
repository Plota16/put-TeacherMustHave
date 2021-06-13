package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class Class(var id:Int,
            var name: String,
            var studentCount: Int,
            var subjectCount: Int
            ) {


    fun insert(db: SQLiteDatabase){
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, name)
            put(COL3, studentCount)
            put(COL4, subjectCount)

        }
        db.insert(TABLE_NAME, null, values)
    }


    companion object{

        fun readAll(db: SQLiteDatabase): ArrayList<Class> {
            val projection = arrayOf(COL1, COL2, COL3, COL4)


            val sortOrder = COL1

            val cursor = db.query(
                    TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            )
            val entries = ArrayList<Class>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = Class(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getInt(2),
                            cursor.getInt(3),
                    )
                    entries.add(entry)
                }
            }
            return entries

        }


        private const val TABLE_NAME = "CLASS"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "INTEGER"
        private const val COL2 = "NAME"
        private const val COL2_TYPE = "TEXT"
        private const val COL3 = "STUDENT_COUNT"
        private const val COL3_TYPE = "INTEGER"
        private const val COL4 = "SUBJECT_COUNT"
        private const val COL4_TYPE = "INTEGER"


        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 $COL1_TYPE, " +
                "$COL2 $COL2_TYPE, " +
                "$COL3 $COL3_TYPE, " +
                "$COL4 $COL4_TYPE); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"

    }
}