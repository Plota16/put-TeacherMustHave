package com.plocki.teacherDiary.utility

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.plocki.teacherDiary.model.*


class DatabaseHelper(
    context: Context?
) :
    SQLiteOpenHelper(context, DB_NAME, null, DB_VERSION) {



    override fun onCreate(db: SQLiteDatabase) {
        db.execSQL(DB_CREATE_ALL)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL(DB_DELETE_ALL)
        db.execSQL(DB_CREATE_ALL)
    }

    companion object {

        const val DB_VERSION = 1
        const val DB_NAME = "teacherMust.db"

        const val DB_CREATE_ALL =   SubjectEntry.CREATE_TABLE +
                                    Student.CREATE_TABLE +
                                    Presence.CREATE_TABLE +
                                    Task.CREATE_TABLE +
                                    Grade.CREATE_TABLE +
                                    Test.CREATE_TABLE +
                                    GradeName.CREATE_TABLE +
                                    GradeWeight.CREATE_TABLE +
                                    Class.CREATE_TABLE +
                                    SubjectForClass.CREATE_TABLE

        const val DB_DELETE_ALL =   SubjectEntry.DROP_TABLE +
                                    Student.DROP_TABLE +
                                    Presence.DROP_TABLE +
                                    Task.DROP_TABLE +
                                    Grade.DROP_TABLE +
                                    Test.DROP_TABLE +
                                    GradeName.DROP_TABLE +
                                    GradeWeight.DROP_TABLE +
                                    Class.DROP_TABLE +
                                    SubjectForClass.DROP_TABLE
    }
}