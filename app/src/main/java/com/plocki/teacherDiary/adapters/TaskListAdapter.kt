package com.plocki.teacherDiary.adapters

import android.app.AlertDialog
import android.content.DialogInterface
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.textfield.TextInputEditText
import com.plocki.teacherDiary.DeleteTaskMutation
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.UpdateTaskMutation
import com.plocki.teacherDiary.model.Task
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class TaskListAdapter(private var list: List<Task>)
    : RecyclerView.Adapter<TaskViewHolder>() {

    private val isOnline = true

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return TaskViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        val movie: Task = list[position]
        holder.bind(movie)
        holder.deleteButton.setOnClickListener{
            deleteTask(holder)
        }

        holder.editButton.setOnClickListener{
            editTask(holder)
        }

    }

    private fun refresh(){
        this.list = Task.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int = list.size

    private fun editTask(holder: TaskViewHolder){
        if(isOnline){

            val task = holder.task

            val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
            val customAlertDialogView = View.inflate(holder.itemView.context, R.layout.dialog_task,null)

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
                        val newTask = Task(task.id,title.text.toString(),description.text.toString(),date.text.toString(),task.state)
                        updateTaskMutation(newTask,dialog)
                    }
                    .setNegativeButton("ANULUJ") { dialog, _ ->
                        dialog.cancel()
                    }
                    .show()


        }
        else{
            Toast.makeText(
                    MainApplication.appContext, "Dodawanie zadania możliwe tylko w trybie online",
                    Toast.LENGTH_SHORT).show()
        }
    }

    private fun deleteTask(holder: TaskViewHolder){
        if(isOnline){

            val task = holder.task
            val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("UWAGA")
            builder.setMessage("Czy na pewno chcesz usunąć zadanie?")
            builder.setPositiveButton("OK") { dialog, _ ->
                deleteTaskMutation(task,dialog)
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

    private fun deleteTaskMutation(task: Task, dialog: DialogInterface){

        val mutation = DeleteTaskMutation(task.id.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                            MainApplication.appContext, "Zadanie usuniete",
                            Toast.LENGTH_SHORT).show()
                    task.delete(DatabaseHelper(MainApplication.appContext).writableDatabase)
                    refresh()

                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd usuwania zadania",
                            Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            } catch (e: Error){
                Toast.makeText(
                        MainApplication.appContext, "Błąd usuwania zadania",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }

    private fun updateTaskMutation(task: Task, dialog: DialogInterface){
        val mutation = UpdateTaskMutation(task.id.toInput(),task.description.toInput(),task.end_date.toInput(),task.name.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()
            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                            MainApplication.appContext, "Zadanie uaktualnione",
                            Toast.LENGTH_SHORT).show()
                    task.update(DatabaseHelper(MainApplication.appContext).writableDatabase)
                    refresh()
                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd aktualizowania zadania",
                            Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            } catch (e: Error){
                Toast.makeText(
                        MainApplication.appContext, "Błąd aktualizowania zadania",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
    }


}
