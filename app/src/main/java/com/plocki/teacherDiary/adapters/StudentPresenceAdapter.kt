package com.plocki.teacherDiary.adapters

import android.app.AlertDialog
import android.content.DialogInterface
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.UpdatePresneceMutation
import com.plocki.teacherDiary.model.StudentPresence
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

class StudentPresenceAdapter(private val list: List<StudentPresence>)
    : RecyclerView.Adapter<StudentPresenceViewHolder>() {



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudentPresenceViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return StudentPresenceViewHolder(inflater, parent)
    }

    override fun onBindViewHolder(holder: StudentPresenceViewHolder, position: Int) {
        val presence: StudentPresence = list[position]
        holder.bind(presence)
        holder.presenceCard.setOnClickListener {
            onCardClick(presence,holder)
        }
    }

    override fun getItemCount(): Int = list.size

    private fun onCardClick(presence: StudentPresence, holder: StudentPresenceViewHolder) {
        val builder: AlertDialog.Builder = AlertDialog.Builder(holder.itemView.context)
        val customAlertDialogView = View.inflate(holder.itemView.context, R.layout.presence, null)
        val presenceRadioGroup = customAlertDialogView.findViewById<RadioGroup>(R.id.presence_radio_group)!!

        customAlertDialogView.findViewById<TextView>(R.id.presence_text).text = "OBECNOŚĆ"

        val id = when (presence.presence) {
            "OBECNY" -> R.id.presence_radio_present
            "NIEOBECNY" -> R.id.presence_radio_absent
            "USPRAWIEDLOWIONY" -> R.id.presence_radio_justified
            "SPOZNIONY" -> R.id.presence_radio_late
            "BRAK" -> R.id.presence_radio_unknown
            else -> R.id.presence_radio_unknown
        }

        presenceRadioGroup.check(id)
        presenceRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            presence.presence = when (checkedId) {
                R.id.presence_radio_present -> "OBECNY"
                R.id.presence_radio_absent -> "NIEOBECNY"
                R.id.presence_radio_justified -> "USPRAWIEDLOWIONY"
                R.id.presence_radio_late -> "SPOZNIONY"
                R.id.presence_radio_unknown -> "BRAK"
                else -> "BRAK"
            }

        }
        builder.setView(customAlertDialogView)
                .setTitle("Obecność")
                .setTitle(presence.date)
                .setPositiveButton("OK") { dialog, _ ->
                    editPresenceMutation(presence, dialog)
                }
                .setNegativeButton("Anuluj") { dialog, _ ->
                    dialog.cancel()
                }
                .show()

    }

    private fun editPresenceMutation(presence: StudentPresence, dialog: DialogInterface){
        val mutation = UpdatePresneceMutation(presence.studentId.toInput(),presence.subjectEntryId.toInput(),presence.presence.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                            MainApplication.appContext, "Obecność Edytowana",
                            Toast.LENGTH_SHORT).show()
                    notifyDataSetChanged()

                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd edytowania obecności",
                            Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            } catch (e: Error){
                val c = e
                Toast.makeText(
                        MainApplication.appContext, "Błąd edytowania oceny",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.cancel()
    }


}