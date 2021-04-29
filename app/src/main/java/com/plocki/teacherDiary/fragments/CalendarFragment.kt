package com.plocki.teacherDiary.fragments

import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.plocki.teacherDiary.DatabaseHelper
import com.plocki.teacherDiary.MainApplication
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.SubjectEntry
import java.util.*
import kotlin.collections.ArrayList


class CalendarFragment : Fragment() {

    private lateinit var mWeekView : WeekView
    private  var entries = ArrayList<WeekViewEvent>()

    override fun onCreateView(
            inflater: LayoutInflater,
            container: ViewGroup?,
            savedInstanceState: Bundle?
    ): View? {

        return inflater.inflate(R.layout.calendar_fragment, container, false)

    }

    override fun onStart() {

        mWeekView = view!!.findViewById(R.id.weekView);
        mWeekView.setOnEventClickListener { weekViewEvent: WeekViewEvent, rectF: RectF ->
        }

        val db = DatabaseHelper(MainApplication.appContext).readableDatabase

        entries = toWeekViewEvents(SubjectEntry.readAll(db))
        mWeekView.setMonthChangeListener{ newYear, newMonth -> // Populate the week view with some events.
            val month: Int = newMonth - 1
            getEventsForMonth(newYear, month)
        }

        mWeekView.setEventLongPressListener{ _: WeekViewEvent, _: RectF ->

        }

        println("ok")
        super.onStart()
    }

    private fun getEventsForMonth(year: Int, month: Int): List<WeekViewEvent>? {
        val tempList: MutableList<WeekViewEvent> = ArrayList()
        for (weekViewEvent in entries) {
            if (weekViewEvent.startTime[Calendar.MONTH] == month && weekViewEvent.startTime[Calendar.YEAR] ==
                    year) {
                tempList.add(weekViewEvent)
            }
        }
        return tempList
    }


    private fun toWeekViewEvents(subjectEntries: ArrayList<SubjectEntry>): ArrayList<WeekViewEvent> {

        val weekViewEvents = ArrayList<WeekViewEvent>()

        for( entry: SubjectEntry in subjectEntries){
            val event = WeekViewEvent(entry.id.toLong(),
                    entry.subjectName,
                    entry.date.split("-")[0].toInt(),
                    entry.date.split("-")[1].toInt(),
                    entry.date.split("-")[2].toInt(),
                    entry.startTime.split(":")[0].toInt(),
                    entry.startTime.split(":")[1].toInt(),
                    entry.date.split("-")[0].toInt(),
                    entry.date.split("-")[1].toInt(),
                    entry.date.split("-")[2].toInt(),
                    entry.endTime.split(":")[0].toInt(),
                    entry.endTime.split(":")[1].toInt()
            )
            event.location = entry.className
            event.color = resources.getColor(R.color.Secondary)
            weekViewEvents.add(event)
        }
        return weekViewEvents
    }




}


