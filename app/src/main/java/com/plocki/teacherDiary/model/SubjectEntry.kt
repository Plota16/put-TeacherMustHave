package com.plocki.teacherDiary.model

import android.content.ContentValues
import android.database.sqlite.SQLiteDatabase
import com.plocki.teacherDiary.R

class SubjectEntry(var id: Int,
                   var date : String,
                   var startTime : String,
                   var endTime : String,
                   var className: String,
                   var topic: String,
                   var subjectName: String,
                   var testID: String,
                   var presence: String,
                   var late : String
                   ) {


    fun insert(db: SQLiteDatabase){
        val values = ContentValues().apply {
            put(COL1, id)
            put(COL2, date)
            put(COL3, startTime)
            put(COL4, endTime)
            put(COL5, topic)
            put(COL6, className)
            put(COL7,subjectName)
            put(COL8,testID)
            put(COL9,presence)
            put(COL10,late)
        }
        db.insert(TABLE_NAME, null, values)
    }

    fun getColor(): Int {
        return when(className.last()){
            'A' -> R.color.light_blue
            'B' -> R.color.light_green
            'C' -> R.color.light_purple
            'D' -> R.color.light_red
            'E' -> R.color.light_yellow
            else -> R.color.light_unknown
        }
    }

    fun updateTopic(db: SQLiteDatabase){

        val contentValues = ContentValues()
        contentValues.put(COL5,this.topic)
        val selection = "$COL1 = ?"
        val selectionArgs = arrayOf(id.toString())


        db.update(TABLE_NAME,contentValues,selection,selectionArgs)
    }

    fun updateTest(db: SQLiteDatabase){

        val contentValues = ContentValues()
        contentValues.put(COL8,this.testID)
        val selection = "$COL1 = ?"
        val selectionArgs = arrayOf(id.toString())


        db.update(TABLE_NAME,contentValues,selection,selectionArgs)
    }


    companion object{

        fun readAll(db: SQLiteDatabase): ArrayList<SubjectEntry> {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5, COL6, COL7, COL8, COL9, COL10)

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

            val entries = ArrayList<SubjectEntry>()
            with(cursor) {
                while (moveToNext()) {
                    val entry = SubjectEntry(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(5),
                            cursor.getString(4),
                            cursor.getString(6),
                            cursor.getString(7),
                            cursor.getString(8),
                            cursor.getString(9)
                    )
                    entries.add(entry)
                }
            }
            return entries
        }

        fun readOne(db: SQLiteDatabase, id: Int): SubjectEntry {
            val projection = arrayOf(COL1, COL2, COL3, COL4, COL5, COL6, COL7, COL8, COL9, COL10)


            val selection = "$COL1 = ?"
            val selectionArgs = arrayOf(id.toString())

            val sortOrder = "$COL1 DESC"

            val cursor = db.query(
                TABLE_NAME,   // The table to query
                projection,             // The array of columns to return (pass null to get all)
                selection,              // The columns for the WHERE clause
                selectionArgs,          // The values for the WHERE clause
                null,                   // don't group the rows
                null,                   // don't filter by row groups
                sortOrder               // The sort order
            )

            val entries = ArrayList<SubjectEntry>()
            with(cursor) {
                while (moveToNext()) {

                    val entry = SubjectEntry(
                            cursor.getInt(0),
                            cursor.getString(1),
                            cursor.getString(2),
                            cursor.getString(3),
                            cursor.getString(5),
                            cursor.getString(4),
                            cursor.getString(6),
                            cursor.getString(7),
                            cursor.getString(8),
                            cursor.getString(9)

                                                        )
                    entries.add(entry)
                }
            }
            return entries[0]
        }

        fun updatePresence(db: SQLiteDatabase, id: Int){

            val contentValues = ContentValues()
            contentValues.put(COL9,"Y")
            val selection = "$COL1 = ?"
            val selectionArgs = arrayOf(id.toString())

            db.update(TABLE_NAME,contentValues,selection,selectionArgs)
        }

        fun countTopics(db: SQLiteDatabase): Int {
            val selectQuery = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL10 ='Y' AND $COL5 =''"
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            val sum: Int = cursor.getInt(0)
            cursor.close()
            return sum
        }

        fun countPresence(db: SQLiteDatabase): Int {
            val selectQuery = "SELECT COUNT(*) FROM $TABLE_NAME WHERE $COL10 ='Y' AND $COL9 =''"
            val cursor = db.rawQuery(selectQuery, null)
            cursor.moveToFirst()
            val sum: Int = cursor.getInt(0)
            cursor.close()
            return sum
        }

        private const val TABLE_NAME = "SUBJECT_ENTRY"
        private const val COL1 = "ID"
        private const val COL1_TYPE = "SMALLINT"
        private const val COL2 = "DATE"
        private const val COL2_TYPE = "TEXT"
        private const val COL3 = "START_TIME"
        private const val COL3_TYPE = "TEXT"
        private const val COL4 = "END_TIME"
        private const val COL4_TYPE = "TEXT"
        private const val COL5 = "TOPIC"
        private const val COL5_TYPE = "TEXT"
        private const val COL6 = "CLASS_NAME"
        private const val COL6_TYPE = "TEXT"
        private const val COL7 = "SUBJECT_NAME"
        private const val COL7_TYPE = "TEXT"
        private const val COL8 = "TEST_ID"
        private const val COL8_TYPE = "TEXT"
        private const val COL9 = "PRESENCE"
        private const val COL9_TYPE = "TEXT"
        private const val COL10 = "LATE"
        private const val COL10_TYPE = "TEXT"


        const val CREATE_TABLE = "Create Table $TABLE_NAME (" +
                "$COL1 $COL1_TYPE, " +
                "$COL2 $COL2_TYPE, " +
                "$COL3 $COL3_TYPE, " +
                "$COL4 $COL4_TYPE, " +
                "$COL5 $COL5_TYPE, " +
                "$COL6 $COL6_TYPE, " +
                "$COL7 $COL7_TYPE, " +
                "$COL8 $COL8_TYPE, " +
                "$COL9 $COL9_TYPE, " +
                "$COL10 $COL10_TYPE); "


        const val DROP_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME;"
    }

}