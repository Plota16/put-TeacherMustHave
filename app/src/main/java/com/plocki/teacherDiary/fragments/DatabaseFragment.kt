package com.plocki.teacherDiary.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.model.Class
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication


class DatabaseFragment : Fragment() {
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_database, container, false)
    }

    override fun onStart() {
        val db = DatabaseHelper(MainApplication.appContext).readableDatabase
        val classes = Class.readAll(db)
        val dataTable = requireView().findViewById<TableLayout>(R.id.database_layout)


        for((index, myClass) in classes.withIndex()){
            val row = LayoutInflater.from(requireContext()).inflate(R.layout.classes_row, null)
            row.findViewById<TextView>(R.id.database_row1).text = myClass.name
            row.findViewById<TextView>(R.id.database_row2).text = myClass.subjectCount.toString()
            row.findViewById<TextView>(R.id.database_row3).text = myClass.studentCount.toString()
            if(index%2==1){
                row.findViewById<MaterialCardView>(R.id.classes_row_id).setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.background_light))
            }
            row.setOnClickListener {
                Toast.makeText(
                    MainApplication.appContext, (index+1).toString(),
                    Toast.LENGTH_SHORT).show()
            }
            dataTable.addView(row,index+1)
        }
        super.onStart()
    }
}