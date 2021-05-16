package com.plocki.teacherDiary.adapters

import android.app.AlertDialog
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.Task
import com.plocki.teacherDiary.utility.MainApplication


class TaskViewHolder(inflater: LayoutInflater, parent: ViewGroup) :
        RecyclerView.ViewHolder(inflater.inflate(R.layout.task, parent, false)) {


    private val isOnline = true

    private var titleTextView: TextView? = null
    private var endDateTextView: TextView? = null
    private var descriptionTextView: TextView? = null
    private var deleteButton: Button? = null
    private var editButton: Button? = null




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

        deleteButton!!.setOnClickListener{
            deleteTask(task)
        }

        editButton!!.setOnClickListener{
            editTask(task)
        }
    }


    private fun editTask(task: Task){
        if(isOnline){


            val builder: AlertDialog.Builder = AlertDialog.Builder(this.itemView.context)
            val customAlertDialogView = View.inflate(this.itemView.context,R.layout.dialog_task,null)

            val title = customAlertDialogView.findViewById<TextInputEditText>(R.id.task_title_input)
            val date = customAlertDialogView.findViewById<TextInputEditText>(R.id.task_date_input)
            val description = customAlertDialogView.findViewById<TextInputEditText>(R.id.task_description_input)


            title.text = Editable.Factory.getInstance().newEditable(task.name)
            date.text = Editable.Factory.getInstance().newEditable(task.end_date)
            description.text = Editable.Factory.getInstance().newEditable(task.description)

            builder.setView(customAlertDialogView)
                    .setTitle("Edycja zadania")
                    .setMessage("Edytuj zadanie")
                    .setPositiveButton("OK") { dialog, _ ->

                        // todo edycja
                        Toast.makeText(
                                MainApplication.appContext, "Zadanie edytowane",
                                Toast.LENGTH_SHORT).show()
                        dialog.dismiss()
                        dialog.dismiss()
                    }
                    .setNegativeButton("ANULUJ") { dialog, _ ->

                    }
                    .show()


        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie zadania możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(task: Task){
        if(isOnline){

            val builder: AlertDialog.Builder = AlertDialog.Builder(this.itemView.context)
            builder.setTitle("UWAGA")
            builder.setMessage("Czy na pewno chcesz usunąć zadanie?")
            builder.setPositiveButton("OK") { dialog, _ ->
                //todo mutacja usuwajaca
                Toast.makeText(
                        MainApplication.appContext, "Zadanie usunięte",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
                dialog.cancel()
            }
            builder.setNegativeButton("ANULUJ") { dialog, _ -> dialog.cancel() }
            builder.show()
        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie zadania możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }
}