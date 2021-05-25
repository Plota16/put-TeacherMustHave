package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.model.Test
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication

class TestListAdapter(private var list: List<Test>)
    : RecyclerView.Adapter<TestViewHolder>() {

    private val isOnline = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TestViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TestViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TestViewHolder, position: Int) {
        val test: Test = list[position]
        holder.bind(test)


    }

    private fun refresh(){
        this.list = Test.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size


}
