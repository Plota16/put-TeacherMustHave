package com.plocki.teacherDiary.utility

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context


class MainApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        context = applicationContext
    }

    companion object {
        @SuppressLint("StaticFieldLeak")
        private var context: Context? = null
        val appContext: Context?
            get() = context
    }
}