package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase

class GradeName(val id: Int, val symbol: String, val name: String, val value: Double) {


    fun insert(db: SQLiteDatabase){
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, symbol)
            put(COL3, name)
            put(COL4, value)
        }
        db.insert(TABLE_NAME, null, values)
    }

    companion object{

        fun readAll(db: SQLiteDatabase): ArrayList<GradeName> {
            val projection = arrayOf(COL1, COL2, COL3, COL4)

            val sortOrder = COL4

            val cursor = db.query(
                    TABLE_NAME,   // The table to query
                    projection,             // The array of columns to return (pass null to get all)
                    null,              // The columns for the WHERE clause
                    null,          // The values for the WHERE clause
                    null,                   // don't group the rows
                    null,                   // don't filter by row groups
                    sortOrder               // The sort order
            )

            val entries = ArrayList<GradeName>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = GradeName(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getDouble(3)
                    )
                    entries.add(entry)
                }
            }
            return entries
        }


        private const val TABLE_NAME = "GRADE_NAME"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "INTEGER"
        private const val COL2 = "SYMBOL"
        private const val COL2_TYPE = "TEXT"
        private const val COL3 = "NAME"
        private const val COL3_TYPE = "TEXT"
        private const val COL4 = "VALUE"
        private const val COL4_TYPE = "REAL"

        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 $COL1_TYPE, " +
                "$COL2 $COL2_TYPE, " +
                "$COL3 $COL3_TYPE, " +
                "$COL4 $COL4_TYPE); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"

    }
}