package com.plocki.teacherDiary.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.SubjectEntry
import com.plocki.teacherDiary.model.Task
import com.plocki.teacherDiary.model.Test
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import com.plocki.teacherDiary.utility.Store


class HomeFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_home, container, false)
    }

    override fun onStart() {
        val store = Store()
        val firstName = store.retrieve("firstName")
        val lastName = store.retrieve("lastName")
        val name = "$firstName $lastName"

        val db = DatabaseHelper(MainApplication.appContext).readableDatabase

        view!!.findViewById<TextView>(R.id.home_title_name).text = name
        view!!.findViewById<TextView>(R.id.home_task_count).text = Task.count(db).toString()

        view!!.findViewById<TextView>(R.id.home_test_count).text = Test.countFresh(db).toString()
        view!!.findViewById<TextView>(R.id.home_test_grade_count).text = Test.countUnchecked(db).toString()

        view!!.findViewById<TextView>(R.id.home_classes_presence_count).text = SubjectEntry.countPresence(db).toString()
        view!!.findViewById<TextView>(R.id.home_classes_count).text = SubjectEntry.countTopics(db).toString()

        super.onStart()
    }

}