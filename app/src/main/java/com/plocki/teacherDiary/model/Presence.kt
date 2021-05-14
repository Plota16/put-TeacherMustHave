package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class Presence(val subjectEntryId: Int, val studentId: Int, val name: String, var presence: String) {


    fun insert(db: SQLiteDatabase){
        val values = ContentValues().apply {
            put(COL1, subjectEntryId)
            put(COL2, studentId)
            put(COL3, name)
            put(COL4, presence)
        }
        db.insert(TABLE_NAME, null, values)
    }


    fun updatePresence(db: SQLiteDatabase){

        val contentValues = ContentValues()
        contentValues.put(COL4,presence)
        val selection = "$COL1 = ? AND $COL2 = ?"
        val selectionArgs = arrayOf(subjectEntryId.toString(),studentId.toString())


        db.update(TABLE_NAME,contentValues,selection,selectionArgs)
    }

    companion object{


        fun readAll(db: SQLiteDatabase): ArrayList<Presence> {
            val projection = arrayOf(
                COL1,
                COL2,
                COL3,
                COL4
            )

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

            val entries = ArrayList<Presence>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = Presence(
                        cursor.getInt(0),
                        cursor.getInt(1),
                        cursor.getString(2),
                        cursor.getString(3)
                    )
                    entries.add(entry)
                }
            }
            db.execSQL(DELETE_TABLE)
            return entries
        }



        private const val TABLE_NAME = "PRESENCE"
        private const val COL1 = "SUBJECT_ENTRY_ID"
        private const val COL1_TYPE = "SMALLINT"
        private const val COL2 = "STUDENT_ID"
        private const val COL2_TYPE = "SMALLINT"
        private const val COL3 = "NAME"
        private const val COL3_TYPE = "TEXT"
        private const val COL4 = "PRESENCE"
        private const val COL4_TYPE = "TEXT"



        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 $COL1_TYPE, " +
                "$COL2 $COL2_TYPE, " +
                "$COL3 $COL3_TYPE, " +
                "$COL4 $COL4_TYPE); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"

        private const val DELETE_TABLE = "DELETE FROM $TABLE_NAME;"
    }
}