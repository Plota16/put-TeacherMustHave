package com.plocki.teacherDiary.fragments

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.fragment.app.Fragment
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.activities.PresenceActivity
import com.plocki.teacherDiary.utility.MainApplication
import com.plocki.teacherDiary.utility.Store

class SettingsFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onStart() {

        view!!.findViewById<Button>(R.id.settings_button).setOnClickListener {
            val intent = Intent(MainApplication.appContext, PresenceActivity::class.java)
            val store = Store()
            try{
                store.removeToken()
            }catch (ex :Exception){

            }
            startActivity(intent)
        }
        super.onStart()
    }
}