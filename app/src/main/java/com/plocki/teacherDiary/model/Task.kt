package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class Task(var id: Int, var name: String, var description: String?, var end_date: String, var state: String) {


    fun insert(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, name)
            put(COL3, description)
            put(COL4, end_date)
            put(COL5, state)

        }
        db.insert(TABLE_NAME, null, values)
    }


    companion object{

        fun readAll(db: SQLiteDatabase): ArrayList<Task> {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5)


            val selection = "$COL5 = ?"
            val selectionArgs = arrayOf("ACTIVE")

            val sortOrder = "$COL4"

            val cursor = db.query(
                    TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            )
            val entries = ArrayList<Task>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = Task(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(4)

                    )
                    entries.add(entry)
                }
            }
            return entries

        }

        private const val TABLE_NAME = "TASK"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "INTEGER"
        private const val COL2 = "NAME"
        private const val COL2_TYPE = "TEXT"
        private const val COL3 = "DESCRIPTION"
        private const val COL3_TYPE = "TEXT"
        private const val COL4 = "END_DATE"
        private const val COL4_TYPE = "TEXT"
        private const val COL5 = "STATE"
        private const val COL5_TYPE = "TEXT"

        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 ${COL1_TYPE}, " +
                "$COL2 ${COL2_TYPE}, " +
                "$COL3 ${COL3_TYPE}, " +
                "$COL4 ${COL4_TYPE}, " +
                "$COL5 ${COL5_TYPE}); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }
}