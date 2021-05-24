package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class MyClassStudent(var id: Int, var className: String, var firstName: String, var lastName: String, var classId: Int) {


    fun insert(db: SQLiteDatabase) {
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, className)
            put(COL3, firstName)
            put(COL4, lastName)
            put(COL5, className)

        }
        db.insert(TABLE_NAME, null, values)
    }


    companion object{

        fun readOneClass(db: SQLiteDatabase, id: String): ArrayList<MyClassStudent> {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5)


            val selection = "$COL2 = ?"
            val selectionArgs = arrayOf(id.toString())

            val sortOrder = "$COL2 DESC"

            val cursor = db.query(
                    TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    selection,              // The columns for the WHERE clause
                    selectionArgs,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            )



            val entries = ArrayList<MyClassStudent>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = MyClassStudent(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getInt(4)

                    )
                    entries.add(entry)
                }
            }
            return entries
        }

        fun classSize(db: SQLiteDatabase, className: String): Int {


            val selectQuery = "SELECT COUNT( $COL1 ) FROM $TABLE_NAME WHERE $COL2 = '$className'"
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            val sum: Int = cursor.getInt(0)
            cursor.close()
            return sum
        }

        fun getStudentName(db: SQLiteDatabase, id: String): String {

            val selectQuery = "SELECT $COL3, $COL4 FROM $TABLE_NAME WHERE $COL1 = $id"
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            val firstName = cursor.getString(0)
            val lastName  = cursor.getString(1)
            cursor.close()
            return "$firstName $lastName"
        }

        private const val TABLE_NAME = "CLASS_STUDENT"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "INTEGER"
        private const val COL2 = "CLASS_NAME"
        private const val COL2_TYPE = "TEXT"
        private const val COL3 = "FIRST_NAME"
        private const val COL3_TYPE = "TEXT"
        private const val COL4 = "LAST_NAME"
        private const val COL4_TYPE = "TEXT"
        private const val COL5 = "CLASS_ID"
        private const val COL5_TYPE = "INTEGER"

        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 ${COL1_TYPE}, " +
                "$COL2 ${COL2_TYPE}, " +
                "$COL3 ${COL3_TYPE}, " +
                "$COL4 ${COL4_TYPE}, " +
                "$COL5 ${COL5_TYPE}); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }
}