package com.plocki.teacherDiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.adapters.TestListAdapter
import com.plocki.teacherDiary.model.Test
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import com.plocki.teacherDiary.utility.Store
import java.time.LocalDate

class TestFragment : Fragment() {
//    private val isOnline = true
    private var userId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_test, container, false)

    }

    override fun onStart() {

        val store = Store()
        userId = store.retrieve("id")!!.toInt()
        applyRecycler()

        super.onStart()
    }


    private fun applyRecycler() {
        val list = Test.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val newList = list.filter {
            val localTime = LocalDate.now().minusMonths(2).toString()
            it.date > localTime
        }
        val recycler = requireView().findViewById<RecyclerView>(R.id.test_recycler)

        recycler.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = TestListAdapter(newList)
        }

    }

    override fun onResume() {
        applyRecycler()
        super.onResume()
    }


}