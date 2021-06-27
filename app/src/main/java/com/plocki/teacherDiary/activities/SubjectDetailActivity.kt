package com.plocki.teacherDiary.activities

import android.app.AlertDialog
import android.content.DialogInterface
import android.os.Bundle
import android.util.DisplayMetrics
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.apollographql.apollo.api.Response
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.card.MaterialCardView
import com.plocki.teacherDiary.DeleteGradeMutation
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.SelectStudentsGradesForSubjectQuery
import com.plocki.teacherDiary.UpdateGradeMutation
import com.plocki.teacherDiary.adapters.StudentPresenceAdapter
import com.plocki.teacherDiary.model.GradeForSubject
import com.plocki.teacherDiary.model.GradeName
import com.plocki.teacherDiary.model.StudentPresence
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.DatabaseHelper
import com.plocki.teacherDiary.utility.MainApplication
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch


class SubjectDetailActivity : AppCompatActivity(), AdapterView.OnItemSelectedListener {

    private val gradeList = ArrayList<GradeForSubject>()
    private val gradeMap = HashMap<Int, Int>()
    private var chosenGradeId = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_subject_detail)
        getGradesAndPresenceQuery()
    }

    override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        chosenGradeId = gradeMap[position]!!
    }

    override fun onNothingSelected(parent: AdapterView<*>?) {

    }


    private fun setupTitles(response: Response<SelectStudentsGradesForSubjectQuery.Data>) {

        val studentName =  "${response.data!!.sUBJECT_FOR_CLASS[0].cLASS.sTUDENTs[0].first_name} ${response.data!!.sUBJECT_FOR_CLASS[0].cLASS.sTUDENTs[0].last_name}"
        val teacherName =  "${response.data!!.sUBJECT_FOR_CLASS[0].tEACHER.first_name} ${response.data!!.sUBJECT_FOR_CLASS[0].tEACHER.last_name}"

        findViewById<TextView>(R.id.subject_detail_title).text = studentName
        findViewById<TextView>(R.id.subject_detail_teacher_input).text = teacherName
        findViewById<TextView>(R.id.subject_detail_subject_input).text = response.data!!.sUBJECT_FOR_CLASS[0].subject_name

        var sumWeight = 0.0
        var sumValue = 0.0

        for (grade in gradeList){
            if (grade.weightName == "Ocena końcowa"){
                findViewById<TextView>(R.id.subject_detail_grade_input).text = grade.gradeName
            }
            if (grade.weightName == "Ocena proponowana"){
                findViewById<TextView>(R.id.subject_detail_proposedGrade_input).text = grade.gradeName
            }

            sumValue += grade.gradeValue*grade.weight
            sumWeight += grade.weight
        }
        if (gradeList.size != 0){
            val avg = sumValue/sumWeight
            if(avg.toString().length > 3){
                findViewById<TextView>(R.id.subject_detail_gradeAvg_input).text =  avg.toString().substring(0, 4)
            }
            else{

                findViewById<TextView>(R.id.subject_detail_gradeAvg_input).text = avg.toString()
            }
        }

    }

    private fun setupGrades(grades: List<SelectStudentsGradesForSubjectQuery.GRADE>) {

        val layout = findViewById<RelativeLayout>(R.id.subject_detail_grade_realativeLayout)
        layout.removeAllViews()
        findViewById<ImageButton>(R.id.subject_detail_show_grades).setOnClickListener {
            it.rotation = it.rotation + 180
            if(layout.visibility == View.GONE){
                layout.visibility = View.VISIBLE
            }
            else{
                layout.visibility = View.GONE
            }
        }

        for ((index, gradeInput) in grades.withIndex()){
            val grade = GradeForSubject(
                    gradeInput.id,
                    gradeInput.gRADE_WEIGHT.weight,
                    gradeInput.gRADE_WEIGHT.name,
                    gradeInput.gRADE_NAME.name,
                    gradeInput.gRADE_NAME.symbol,
                    gradeInput.gRADE_NAME.value!!,
                    gradeInput.testId,
                    gradeInput.description,
                    gradeInput.date.toString(),
                    gradeInput.subject_for_class_id
            )
            gradeList.add(grade)
            val gradeBox = LayoutInflater.from(this).inflate(R.layout.template_grade, null)
            val desc = grade.weightName.replace(" ", "\n")
            val noColumns = calculateNoOfColumns()
            val column = index%noColumns

            when(grade.weight){
                1 -> gradeBox.findViewById<MaterialCardView>(R.id.template_grade_card).setBackgroundColor(ContextCompat.getColor(this, R.color.light_red))
                2 -> gradeBox.findViewById<MaterialCardView>(R.id.template_grade_card).setBackgroundColor(ContextCompat.getColor(this, R.color.light_blue))
                3 -> gradeBox.findViewById<MaterialCardView>(R.id.template_grade_card).setBackgroundColor(ContextCompat.getColor(this, R.color.light_green))
                4 -> gradeBox.findViewById<MaterialCardView>(R.id.template_grade_card).setBackgroundColor(ContextCompat.getColor(this, R.color.light_yellow))
                6 -> gradeBox.findViewById<MaterialCardView>(R.id.template_grade_card).setBackgroundColor(ContextCompat.getColor(this, R.color.light_unknown))
                else ->  gradeBox.findViewById<MaterialCardView>(R.id.template_grade_card).setBackgroundColor(ContextCompat.getColor(this, R.color.light_purple))
            }
            gradeBox.findViewById<TextView>(R.id.template_grade_grade).text = grade.gradeSymbol
            gradeBox.findViewById<TextView>(R.id.template_grade_desc).text = desc
            gradeBox.findViewById<MaterialCardView>(R.id.template_grade_card).setOnClickListener {
                onGradeClick(grade)
            }
            gradeBox.id = 1000000 + index

            val lp = RelativeLayout.LayoutParams(90 * this.resources.displayMetrics.density.toInt(), RelativeLayout.LayoutParams.WRAP_CONTENT)
            lp.setMargins(24, 24, 24, 24)

            when {
                column!= 0 -> {
                    lp.addRule(RelativeLayout.RIGHT_OF, gradeBox.id - 1)
                    lp.addRule(RelativeLayout.ALIGN_BASELINE, gradeBox.id - 1)
                }
                index != 0 -> {
                    lp.addRule(RelativeLayout.BELOW, gradeBox.id - 1)
                }
                else -> {
                    lp.addRule(RelativeLayout.BELOW, findViewById<TextView>(R.id.subject_deail_grade_title).id)
                }
            }
            layout.addView(gradeBox, lp)
        }

    }

    private fun setupPresences(studentSubjectEntryPresences: List<SelectStudentsGradesForSubjectQuery.STUDNET_SUBJECT_ENTRY_PRESENCE>, studentId: Int) {
        val recycler = findViewById<RecyclerView>(R.id.subject_detail_presence_recycler)
        findViewById<ImageButton>(R.id.subject_detail_show_presence).setOnClickListener {
            it.rotation = it.rotation + 180
            if(recycler.visibility == View.GONE){
                recycler.visibility = View.VISIBLE
            }
            else{
                recycler.visibility = View.GONE
            }
        }

        val presenceList = ArrayList<StudentPresence>()
        for(presence in studentSubjectEntryPresences){

            val topic = if(presence.sUBJECT_ENTRY.topic.isNullOrEmpty()){
                "BEZ TEMATU"
            }
            else{
                presence.sUBJECT_ENTRY.topic
            }
            val studentPresence = StudentPresence(
                    studentId,
                    presence.sUBJECT_ENTRY.id,presence.presence,
                    presence.sUBJECT_ENTRY.date.toString(),
                    presence.sUBJECT_ENTRY.lESSON.start_time.toString(),
                    topic
            )
            presenceList.add(studentPresence)
        }



        recycler.apply {
            layoutManager = LinearLayoutManager(MainApplication.appContext)
            adapter = StudentPresenceAdapter(presenceList)
        }
    }

    private fun calculateNoOfColumns(): Int {
        val displayMetrics: DisplayMetrics = this.resources.displayMetrics
        val screenWidthDp = displayMetrics.widthPixels / displayMetrics.density
        val card = screenWidthDp - 40 - 16
        return (card / 90f + 0.5).toInt()
    }


    private fun onGradeClick(grade: GradeForSubject){

        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val customAlertDialogView = View.inflate(this, R.layout.dialog_grade, null)

        customAlertDialogView.findViewById<TextView>(R.id.dialog_grade_grade_input).text = grade.gradeName
        customAlertDialogView.findViewById<TextView>(R.id.dialog_grade_type_input).text = grade.weightName
        customAlertDialogView.findViewById<TextView>(R.id.dialog_grade_weight_input).text = grade.weight.toString()
        customAlertDialogView.findViewById<TextView>(R.id.dialog_grade_desc_input).text = grade.description
        customAlertDialogView.findViewById<TextView>(R.id.dialog_grade_date_input).text = grade.date.substring(0, 10)

        builder.setView(customAlertDialogView)
            .setTitle("Ocena")

            .setPositiveButton("OK") { dialog, _ ->
                dialog.cancel()
            }
            .setNeutralButton("Edytuj") { _, _ ->
                editGrade(grade)
            }
            .setNegativeButton("Usuń") { _, _ ->
                deleteGrade(grade.id)}
            .show()
    }

    private fun editGrade(grade: GradeForSubject){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)
        val customAlertDialogView = View.inflate(this, R.layout.dialog_edit_grade, null)

        val type = customAlertDialogView.findViewById<Spinner>(R.id.dialog_edit_grade_spinner)

        val grades = GradeName.readAll(DatabaseHelper(MainApplication.appContext).readableDatabase)
        val gradeNamesSpinnerList = ArrayList<String>()
        for((counter, weight: GradeName) in grades.withIndex()){
            gradeNamesSpinnerList.add(weight.name)
            gradeMap[counter] = weight.id
        }
        chosenGradeId = gradeMap[0]!!

        val gradeWeightAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, gradeNamesSpinnerList)

        gradeWeightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        type.adapter = gradeWeightAdapter
        type.onItemSelectedListener = this

        builder.setView(customAlertDialogView)
                .setTitle("Ocena")
                .setMessage("Edycja oceny")
                .setPositiveButton("OK") { dialog, _ ->
                    editGradeMutation(grade.id, dialog)
                }
                .setNegativeButton("Anuluj") { _, _ ->
                    deleteGrade(grade.id)}
                .show()
    }

    private fun deleteGrade(gradeId: Int){
        val builder: AlertDialog.Builder = AlertDialog.Builder(this)

        builder.setTitle("Ocena")
                .setMessage("Czy chcesz usunąć ocenę?")
                .setPositiveButton("Tak") { dialog, _ ->
                    deleteGradeMutation(gradeId, dialog)
                }
                .setNegativeButton("Nie") { dialog, _ ->
                    dialog.cancel()}
                .show()
    }


    private fun getGradesAndPresenceQuery(){

        val studentId = intent.getStringExtra("studentId")!!.toInt()
        val subjectId = intent.getStringExtra("subjectId")!!.toInt()
        val query = SelectStudentsGradesForSubjectQuery(subjectId.toInput(), studentId.toInput())
        gradeList.clear()

        GlobalScope.launch(Dispatchers.Main) {
            try{
                val response  = ApolloInstance.get().query(query).toDeferred().await()
                try {
                    if(!response.hasErrors()){
                        setupPresences(response.data!!.sUBJECT_FOR_CLASS[0].cLASS.sTUDENTs[0].sTUDNET_SUBJECT_ENTRY_PRESENCEs,studentId)
                        setupGrades(response.data!!.sUBJECT_FOR_CLASS[0].cLASS.sTUDENTs[0].gRADEs)
                        setupTitles(response)

                        findViewById<View>(R.id.subject_detail_progressBar).visibility = View.GONE
                        findViewById<RelativeLayout>(R.id.subject_detail_relativeLayout).visibility = View.VISIBLE


                    }
                }catch (e: NullPointerException){
                    Toast.makeText(
                            MainApplication.appContext, "Błąd pobierania uczniów",
                            Toast.LENGTH_SHORT
                    ).show()
                    finish()
                }
            }catch (e: Exception){
                Toast.makeText(
                        MainApplication.appContext, "Bład połączenia z serwerem",
                        Toast.LENGTH_SHORT
                ).show()
                finish()
            }
        }
    }

    private fun deleteGradeMutation(gradeId: Int, dialog: DialogInterface){

        findViewById<View>(R.id.subject_detail_progressBar).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.subject_detail_relativeLayout).visibility = View.GONE

        val mutation = DeleteGradeMutation(gradeId.toInput())
        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()

            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                            MainApplication.appContext, "Ocena usunieta",
                            Toast.LENGTH_SHORT).show()
                    getGradesAndPresenceQuery()

                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd usuwania oceny",
                            Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            } catch (e: Error){
                Toast.makeText(
                        MainApplication.appContext, "Błąd usuwania oceny",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.cancel()
    }

    private fun editGradeMutation(gradeId: Int, dialog: DialogInterface){

        val mutation = UpdateGradeMutation(gradeId.toInput(), chosenGradeId.toInput())
        findViewById<View>(R.id.subject_detail_progressBar).visibility = View.VISIBLE
        findViewById<RelativeLayout>(R.id.subject_detail_relativeLayout).visibility = View.GONE

        GlobalScope.launch(Dispatchers.Main) {
            val result = ApolloInstance.get().mutate(mutation).toDeferred().await()
            try{
                if(!result.hasErrors()){
                    Toast.makeText(
                            MainApplication.appContext, "Ocena uaktualniona",
                            Toast.LENGTH_SHORT).show()
                    getGradesAndPresenceQuery()
                }
                else{
                    Toast.makeText(
                            MainApplication.appContext, "Błąd aktualizowania oceny",
                            Toast.LENGTH_SHORT).show()
                }
                dialog.dismiss()

            } catch (e: Error){
                val c = e
                Toast.makeText(
                        MainApplication.appContext, "Błąd aktualizowania oceny",
                        Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            }
        }
        dialog.cancel()
    }


}