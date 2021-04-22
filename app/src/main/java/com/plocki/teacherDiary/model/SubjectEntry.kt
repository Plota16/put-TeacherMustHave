package com.plocki.teacherDiary.model

import com.plocki.teacherDiary.MyCalendarQuery
import java.text.SimpleDateFormat
import java.util.*

class SubjectEntry(myCalendarQuery: MyCalendarQuery.SUBJECT_ENTRY) {

    private var id : Int = 0
    private var date: Date = Date()
    private var startTime: Date = Date()
    private var endTime: Date = Date()
    private var className : String = ""
    private var topic: String = ""

    init {

        val formatDate = SimpleDateFormat("yyyyy-mm-dd", Locale.ENGLISH)
        val formatTime = SimpleDateFormat("hh:mm:ss", Locale.ENGLISH)

        this.id = myCalendarQuery.iD
        val tmp = myCalendarQuery.dATE.toString()
        this.date = formatDate.parse(tmp)!!
        this.startTime = formatTime.parse(myCalendarQuery.lESSON.sTART_TIME.toString())!!
        this.endTime = formatTime.parse(myCalendarQuery.lESSON.eND_TIME.toString())!!
        this.className = myCalendarQuery.sUBJECT_FOR_CLASS.cLASS.nAME
        if (!myCalendarQuery.tOPIC.isNullOrEmpty()){
            this.topic = myCalendarQuery.tOPIC
        }
    }
}