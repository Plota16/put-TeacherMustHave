package com.plocki.teacherDiary.fragments

import android.graphics.RectF
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.alamkanak.weekview.WeekView
import com.alamkanak.weekview.WeekViewEvent
import com.plocki.teacherDiary.R


class CalendarFragment : Fragment() {

    private lateinit var mWeekView : WeekView

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {


        return inflater.inflate(R.layout.calendar_fragment, container, false)


    }

    override fun onStart() {

        mWeekView = view!!.findViewById(R.id.weekView);

        mWeekView.setOnEventClickListener { weekViewEvent: WeekViewEvent, rectF: RectF ->

        }

        mWeekView.setMonthChangeListener{ newYear, newMonth -> // Populate the week view with some events.
            ArrayList<WeekViewEvent>()
        }

        mWeekView.setEventLongPressListener{ weekViewEvent: WeekViewEvent, rectF: RectF ->

        };


        super.onStart()
    }



}


