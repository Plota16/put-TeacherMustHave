package com.plocki.teacherDiary.model

class Grade(var id:Int,
            var subjectForClassId: Int,
            var studentId: Int,
            var grade: Int,
            var weight: Int,
            var date: String,
            var descroption: String,
            var testid : String?
            ) {


    companion object{
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
    }
}