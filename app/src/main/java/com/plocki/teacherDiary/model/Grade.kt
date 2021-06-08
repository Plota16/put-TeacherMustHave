package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class Grade(var id:Int,
            var subjectForClassId: Int,
            var studentId: Int,
            var grade: Int,
            var weight: Int,
            var date: String,
            var descroption: String,
            var testid : String?
            ) {


    fun insert(db: SQLiteDatabase){
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, subjectForClassId)
            put(COL3, studentId)
            put(COL4, grade)
            put(COL5, weight)
            put(COL6, date)
            put(COL7, descroption)
            put(COL8, testid)
        }
        db.insert(TABLE_NAME, null, values)
    }


    companion object{

        fun readAll(db: SQLiteDatabase): ArrayList<Grade> {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5, COL6, COL7, COL8)


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
            val entries = ArrayList<Grade>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = Grade(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getInt(3),
                            cursor.getInt(4),
                            cursor.getString(5),
                            cursor.getString(6),
                            cursor.getInt(7).toString()
                    )
                    entries.add(entry)
                }
            }
            return entries

        }

        fun readOne(db: SQLiteDatabase, studentId: String, testId: String, subjectForClassId: String): Grade {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5, COL6, COL7, COL8)

            val selection = "$COL2= ? AND $COL3 = ? AND $COL8 = ?"
            val selectionArgs = arrayOf(subjectForClassId,studentId,testId)
            val sortOrder = COL1

            val cursor = db.query(
                    TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            )
            val entries = ArrayList<Grade>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = Grade(
                            cursor.getInt(0),
                            cursor.getInt(1),
                            cursor.getInt(2),
                            cursor.getInt(3),
                            cursor.getInt(4),
                            cursor.getString(5),
                            cursor.getString(6),
                            cursor.getInt(7).toString()
                    )
                    entries.add(entry)
                }
            }
            return entries[0]

        }

        fun updateGrade(db: SQLiteDatabase, testId: Int, gradeId: Int, studentId: Int){

            val contentValues = ContentValues()
            contentValues.put(COL4,gradeId)
            val selection = "$COL3 = ? AND $COL8 = ?"
            val selectionArgs = arrayOf(studentId.toString(),testId.toString())


            db.update(TABLE_NAME,contentValues,selection,selectionArgs)
        }


        private const val TABLE_NAME = "GRADE"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "INTEGER"
        private const val COL2 = "SUBJECT_FOR_CLASS_ID"
        private const val COL2_TYPE = "INTEGER"
        private const val COL3 = "STUDENT_ID"
        private const val COL3_TYPE = "INTEGER"
        private const val COL4 = "GRADE"
        private const val COL4_TYPE = "REAL"
        private const val COL5 = "WEIGHT"
        private const val COL5_TYPE = "INTEGER"
        private const val COL6 = "DATE"
        private const val COL6_TYPE = "TEXT"
        private const val COL7 = "DESCRIPTION"
        private const val COL7_TYPE = "TEXT"
        private const val COL8 = "TEST_ID"
        private const val COL8_TYPE = "INTEGER"


        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 ${COL1_TYPE}, " +
                "$COL2 ${COL2_TYPE}, " +
                "$COL3 ${COL3_TYPE}, " +
                "$COL4 ${COL4_TYPE}, " +
                "$COL5 ${COL5_TYPE}, " +
                "$COL6 ${COL6_TYPE}, " +
                "$COL7 ${COL7_TYPE}, " +
                "$COL8 ${COL8_TYPE}); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"

        const val DELETE_TABLE = "DELETE FROM $TABLE_NAME;"
    }
}