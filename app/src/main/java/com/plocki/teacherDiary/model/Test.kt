package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class Test(val id: Int,
           val topic: String,
           val type: String,
           val subjectId: Int,
           val graded: String,
           val date: String,
           val time: String) {

    fun insert(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, topic)
            put(COL3, type)
            put(COL4, subjectId)
            put(COL5, graded)
            put(COL6,date)
            put(COL7,time)
        }
        db.insert(TABLE_NAME, null, values)
    }

    fun delete(db: SQLiteDatabase){
        db.delete(TABLE_NAME, "$COL1 = $id", null)
    }

    fun update(db: SQLiteDatabase){
        val values = ContentValues().apply {
            put(COL2, topic)
            put(COL3, type)
            put(COL5, graded)
        }
        val selection = "$COL1 = ?"
        val selectionArgs = arrayOf(id.toString())

        db.update(TABLE_NAME,values,selection,selectionArgs)
    }


    companion object{

        fun readAll(db: SQLiteDatabase): ArrayList<Test> {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5, COL6, COL7)


            val sortOrder = COL6

            val cursor = db.query(
                    TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            )
            val entries = ArrayList<Test>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = Test(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getInt(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6)
                    )
                    entries.add(entry)
                }
            }
            return entries

        }

        fun readOne(db: SQLiteDatabase, id: String): Test {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5, COL6, COL7)

            val selection = "$COL1 = ?"
            val selectionArgs = arrayOf(id)
            val sortOrder = COL6

            val cursor = db.query(
                    TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            )
            val entries = ArrayList<Test>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = Test(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getInt(3),
                            cursor.getString(4),
                            cursor.getString(5),
                            cursor.getString(6)
                    )
                    entries.add(entry)
                }
            }
            return entries[0]

        }

        fun countFresh(db: SQLiteDatabase): Int {
            val selectQuery = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL5 = 'F'"
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            val sum: Int = cursor.getInt(0)
            cursor.close()
            return sum
        }

        fun countUnchecked(db: SQLiteDatabase): Int {
            val selectQuery = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL5 = 'N'"
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            val sum: Int = cursor.getInt(0)
            cursor.close()
            return sum
        }

        private const val TABLE_NAME = "TEST"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "INTEGER"
        private const val COL2 = "TOPIC"
        private const val COL2_TYPE = "TEXT"
        private const val COL3 = "TYPE"
        private const val COL3_TYPE = "TEXT"
        private const val COL4 = "SUBJECT_ID"
        private const val COL4_TYPE = "INTEGER"
        private const val COL5 = "GRADED"
        private const val COL5_TYPE = "TEXT"
        private const val COL6 = "DATE"
        private const val COL6_TYPE = "TEXT"
        private const val COL7 = "TIME"
        private const val COL7_TYPE = "TEXT"

        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 ${COL1_TYPE}, " +
                "$COL2 ${COL2_TYPE}, " +
                "$COL3 ${COL3_TYPE}, " +
                "$COL4 ${COL4_TYPE}, " +
                "$COL5 ${COL5_TYPE}, " +
                "$COL6 ${COL6_TYPE}, " +
                "$COL7 ${COL7_TYPE}); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"

    }
}