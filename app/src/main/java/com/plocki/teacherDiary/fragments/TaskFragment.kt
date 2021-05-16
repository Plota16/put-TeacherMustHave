package com.plocki.teacherDiary.fragments

import android.app.AlertDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.adapters.TaskListAdapter
import com.plocki.teacherDiary.model.Task
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication

class TaskFragment : Fragment() {

    val isOnline = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task, container, false)

    }

    override fun onStart() {

        val list = Task.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)

        val recycler = view!!.findViewById<RecyclerView>(R.id.task_recycler)

        recycler.apply {
            layoutManager = LinearLayoutManager(MainApplication.appContext)
            adapter = TaskListAdapter(list)
        }

        view!!.findViewById<FloatingActionButton>(R.id.task_button).setOnClickListener{
            addTask()
        }
        super.onStart()
    }

    private fun addTask(){
        if(isOnline){

            val builder: AlertDialog.Builder = AlertDialog.Builder(this.context)
            val customAlertDialogView = View.inflate(this.context,R.layout.dialog_task,null)

            val title = customAlertDialogView.findViewById<TextInputEditText>(R.id.task_title_input)
            val date = customAlertDialogView.findViewById<TextInputEditText>(R.id.task_date_input)
            val description = customAlertDialogView.findViewById<TextInputEditText>(R.id.task_description_input)

            builder.setView(customAlertDialogView)
                    .setTitle("Dodawanie zadania")
                    .setMessage("Dodaj zadanie")
                    .setPositiveButton("OK") { dialog, _ ->

                        // todo dodawanie
                        Toast.makeText(
                                MainApplication.appContext, "Zadanie dodane",
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

}