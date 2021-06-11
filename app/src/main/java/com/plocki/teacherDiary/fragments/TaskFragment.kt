package com.plocki.teacherDiary.fragments

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText
import com.plocki.teacherDiary.AddTaskMutation
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.adapters.TaskListAdapter
import com.plocki.teacherDiary.model.Task
import com.plocki.teacherDiary.type.TASK_insert_input
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import com.plocki.teacherDiary.utility.Store
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskFragment : Fragment() {

    private val isOnline = true
    private var userId: Int = 0

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_task, container, false)

    }

    override fun onStart() {

        val store = Store()
        userId = store.retrieve("id")!!.toInt()

        applyRecycler()

        requireView().findViewById<FloatingActionButton>(R.id.task_button).setOnClickListener{
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

                        val task = Task(0, title.text.toString(), description.text.toString(), date.text.toString(), "ACTIVE")
                        addTaskMutation(task,dialog)

                    }
                    .setNegativeButton("ANULUJ") { dialog, _ ->
                        dialog.dismiss()
                    }
                    .show()


        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie zadania możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun addTaskMutation(task: Task, dialog:DialogInterface){

        val input = TASK_insert_input(  name = task.name.toInput(),
                                        description = task.description.toInput(),
                                        end_date = task.end_date.toInput(),
                                        state = task.state.toInput(),
                                        user_id = userId.toInput())

        val mutation = AddTaskMutation(input)

        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                            MainApplication.appContext, "Zadanie dodane",
                            Toast.LENGTH_SHORT).show()
                    task.insert(DatabaseHelper(MainApplication.appContext).writableDatabase)
                    applyRecycler()
                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd dodawania zadania",
                            Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            } catch (e: Error){
                Toast.makeText(
                        MainApplication.appContext, "Błąd dodawania zadania",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun applyRecycler(){
        val list = Task.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val recycler = requireView().findViewById<RecyclerView>(R.id.task_recycler)

        recycler.apply {
            layoutManager = LinearLayoutManager(MainApplication.appContext)
            adapter = TaskListAdapter(list)
        }
    }

}