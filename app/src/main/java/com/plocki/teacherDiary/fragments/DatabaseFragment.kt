package com.plocki.teacherDiary.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TableLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.material.card.MaterialCardView
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.activities.SelectStudentActivity
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
        val dataTable = requireView().findViewById<TableLayout>(R.id.select_class_tableLayout)

        for((index, myClass) in classes.withIndex()){
            val row = LayoutInflater.from(requireContext()).inflate(R.layout.template_table_row, null)
            row.findViewById<TextView>(R.id.template_row_column1).text = myClass.name
            row.findViewById<TextView>(R.id.template_row_column2).text = myClass.teacherName
            if(index%2==1){
                row.findViewById<MaterialCardView>(R.id.template_row_tableLayout).setBackgroundColor(ContextCompat.getColor(requireContext(), android.R.color.background_light))
            }
            row.setOnClickListener {
                val intent = Intent(MainApplication.appContext, SelectStudentActivity::class.java)
                intent.putExtra("classId", myClass.id.toString())
                intent.putExtra("className", myClass.name)
                startActivity(intent)
            }
            dataTable.addView(row,index+1)
        }
        super.onStart()
    }
}