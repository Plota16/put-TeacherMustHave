package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.Task


class TaskViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.task, parent, false)) {



    lateinit var task : Task
    var titleTextView: TextView? = null
    var endDateTextView: TextView? = null
    var descriptionTextView: TextView? = null
    var deleteButton: Button? = null
    var editButton: Button? = null

    init {
        titleTextView = itemView.findViewById(R.id.task_title)
        endDateTextView = itemView.findViewById(R.id.task_end_date)
        descriptionTextView = itemView.findViewById(R.id.task_description)
        deleteButton = itemView.findViewById(R.id.task_delete)
        editButton = itemView.findViewById(R.id.task_edit)
    }

    fun bind(task: Task) {
        titleTextView!!.text = task.name
        endDateTextView!!.text = task.end_date
        descriptionTextView!!.text = task.description
        this.task = task


    }



}