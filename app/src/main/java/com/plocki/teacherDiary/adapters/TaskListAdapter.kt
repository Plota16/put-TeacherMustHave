package com.plocki.teacherDiary.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.plocki.teacherDiary.model.Task

class TaskListAdapter(private val list: List<Task>)
    : RecyclerView.Adapter<TaskViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val movie: Task = list[position]
        holder.bind(movie)


    }

    override fun getItemCount(): Int = list.size

}
