package com.plocki.teacherDiary

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.plocki.teacherDiary.model.SubjectEntry


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




        const val DB_VERSION = 1;
        const val DB_NAME = "teacherMust.db";


        val DB_CREATE_ALL = SubjectEntry.CREATE_TABLE
        val DB_DELETE_ALL = SubjectEntry.DROP_TABLE
    }
}