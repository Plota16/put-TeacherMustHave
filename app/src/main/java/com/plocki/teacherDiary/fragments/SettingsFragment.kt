package com.plocki.teacherDiary.fragments

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.plocki.teacherDiary.R
import com.plocki.teacherDiary.activities.PresenceActivity
import com.plocki.teacherDiary.utility.MainApplication
import com.plocki.teacherDiary.utility.Store


class SettingsFragment : Fragment() {


    private lateinit var newPasswordInput : TextInputEditText
    private lateinit var newPasswordLayout : TextInputLayout
    private lateinit var newPasswordInput2 : TextInputEditText
    private lateinit var newPasswordLayout2 : TextInputLayout
    private lateinit var oldPasswordInput : TextInputEditText
    private lateinit var oldPasswordLayout : TextInputLayout
    var email = ""


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_settings, container, false)
    }

    override fun onStart() {

        init()
        setupListeners()

        super.onStart()
    }

    private fun init(){
        email = Store().retrieve("email")!!

        requireView().findViewById<TextView>(R.id.settings_title).text = email

        newPasswordInput = requireView().findViewById(R.id.settings_change_new_password_input)
        newPasswordInput2 = requireView().findViewById(R.id.settings_change_new_password2_input)
        oldPasswordInput= requireView().findViewById(R.id.settings_change_old_password_input)

        newPasswordLayout = requireView().findViewById(R.id.settings_change_new_password)
        newPasswordLayout2 = requireView().findViewById(R.id.settings_change_new_password2)
        oldPasswordLayout = requireView().findViewById(R.id.settings_change_old_password)
    }

    private fun setupListeners(){
        requireView().findViewById<Button>(R.id.settings_button).setOnClickListener {
            logOut()
        }
        requireView().findViewById<Button>(R.id.settings_password_button).setOnClickListener {
            changePassword()
        }

        oldPasswordInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validateOldPassword()
            }
        }
        newPasswordInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validateOnePassword(1)
            }
        }
        newPasswordInput2.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validateOnePassword(2)
            }
        }
    }

    private fun logOut(){
        val intent = Intent(MainApplication.appContext, PresenceActivity::class.java)
        val store = Store()
        try{
            store.removeToken()
            store.removePassword()
            store.remove("email")
        }catch (ex: Exception){

        }
        startActivity(intent)
    }

    private fun changePassword(){
        if(validatePassword() && validateOldPassword()){
            val user = FirebaseAuth.getInstance().currentUser
            val credential = EmailAuthProvider.getCredential(email, oldPasswordInput.text.toString())
            user!!.reauthenticate(credential).addOnCompleteListener{ task ->
                if (task.isSuccessful) {
                    user.updatePassword(newPasswordInput.text.toString()).addOnCompleteListener{
                        if (it.isSuccessful) {
                            Toast.makeText(MainApplication.appContext, "Hasło zmienione", Toast.LENGTH_SHORT).show()
                            Store().removePassword()
                            Store().storePassword(newPasswordInput.text.toString())

                            newPasswordInput.text = Editable.Factory.getInstance().newEditable("")
                            newPasswordInput2.text = Editable.Factory.getInstance().newEditable("")
                            oldPasswordInput.text = Editable.Factory.getInstance().newEditable("")
                        } else {
                            Toast.makeText(MainApplication.appContext, "Błąd- nie zmieniono hasła", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
                else {
                    Toast.makeText(MainApplication.appContext, "Błąd autoryzacji", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun validateOnePassword(no : Int): Boolean {
        var allGood = true

        if(no == 1){
            newPasswordLayout.error = ""
            val password = newPasswordInput.text.toString()
            if(password.isEmpty() or (password.length < 8)){
                newPasswordLayout.error = "Nie podano prawidłowego hasła"
                allGood = false
            }
        }
        else {
            newPasswordLayout2.error = ""
            val password = newPasswordInput2.text.toString()
            if (password.isEmpty() or (password.length < 8)) {
                newPasswordLayout2.error = "Nie podano prawidłowego hasła"
                allGood = false
            }
        }
        return allGood
    }

    private fun validateOldPassword(): Boolean {
        var allGood = true

        val oldPassword = Store().retrievePassword()
        oldPasswordLayout.error = ""

        val password = oldPasswordInput.text.toString()
        if(password.isEmpty() or (password.length < 8)){
            oldPasswordLayout.error = "Nie podano prawidłowego hasła"
            allGood = false
        }
        else if(password != oldPassword){
            oldPasswordLayout.error = "Podane hasło nie jest aktualne"
            allGood = false
        }
        return allGood
    }

    private fun validatePassword(): Boolean {
        var allGood = true

            val password = newPasswordInput.text.toString()
            val password2 = newPasswordInput2.text.toString()

            if(validateOnePassword(1) && validateOnePassword(2)){
                if(password != password2){
                    newPasswordLayout.error = "hasła różnią się od siebie"
                    newPasswordLayout2.error = "hasła różnią się od siebie"
                    allGood = false
                }
            }
            else{
                allGood = false
            }

        return allGood
    }

}