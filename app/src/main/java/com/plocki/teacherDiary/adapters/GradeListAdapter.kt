package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.model.MyClassStudent

class GradeListAdapter(private val list: List<MyClassStudent>, val testId : Int, private val subjectId: Int)
    : RecyclerView.Adapter<GradeViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GradeViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return GradeViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: GradeViewHolder, position: Int) {
        val student: MyClassStudent = list[position]
        holder.bind(student, testId, subjectId)

        holder.gradeSpinner.setSelection(holder.pos-1,false)
    }

    override fun getItemCount(): Int = list.size


}