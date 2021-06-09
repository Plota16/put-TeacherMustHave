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
    private var titleTextView= itemView.findViewById<TextView>(R.id.task_title)!!
    private var endDateTextView = itemView.findViewById<TextView>(R.id.task_end_date)!!
    private var descriptionTextView = itemView.findViewById<TextView>(R.id.task_description)!!
    var deleteButton = itemView.findViewById<Button>(R.id.task_delete)!!
    var editButton = itemView.findViewById<Button>(R.id.task_edit)!!

    init {

    }

    fun bind(task: Task) {
        titleTextView.text = task.name
        endDateTextView.text = task.end_date
        descriptionTextView.text = task.description
        this.task = task


    }



}