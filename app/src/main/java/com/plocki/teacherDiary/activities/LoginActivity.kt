package com.plocki.teacherDiary.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import com.apollographql.apollo.api.toInput
import com.apollographql.apollo.coroutines.toDeferred
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.plocki.teacherDiary.*
import com.plocki.teacherDiary.utility.ApiDownload
import com.plocki.teacherDiary.utility.ApolloInstance
import com.plocki.teacherDiary.utility.Store
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.concurrent.Executor


class LoginActivity : AppCompatActivity() {


    private lateinit var executor: Executor
    private lateinit var biometricPrompt: BiometricPrompt
    private lateinit var promptInfo: BiometricPrompt.PromptInfo

    var activeLayout = "login"
    private lateinit var auth: FirebaseAuth
    private lateinit var store: Store

    private lateinit var loginEmailInput : TextInputEditText
    private lateinit var loginEmailLayout : TextInputLayout
    private lateinit var registerEmailInput : TextInputEditText
    private lateinit var registerEmailLayout : TextInputLayout
    private lateinit var loginPasswordInput : TextInputEditText
    private lateinit var loginPasswordLayout : TextInputLayout
    private lateinit var registerPasswordInput :TextInputEditText
    private lateinit var registerPasswordLayout :TextInputLayout
    private lateinit var registerPasswordInput2 : TextInputEditText
    private lateinit var registerPasswordLayout2 : TextInputLayout

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        window.sharedElementsUseOverlay = false
        toSignIn()

        auth = FirebaseAuth.getInstance()
        store = Store()

        try{
            val password = store.retrievePassword()
            val email = store.retrieve("email")!!

            if(password != "" && email != ""){
                showBiometricDialog(email,password)
            }

        }catch (ex : Exception){}


    }

    fun setupListeners(view: View){
         when(view.id){
             R.id.login_signIn_button -> signIn()
             R.id.login_forgot_text -> forgotPassword()
             R.id.login_signIn_text -> toSignUp()
         }

    }

    private fun showBiometricDialog(email: String, password: String){
        executor = ContextCompat.getMainExecutor(this)
        biometricPrompt = BiometricPrompt(this, executor,
            object : BiometricPrompt.AuthenticationCallback() {
                override fun onAuthenticationError(errorCode: Int,
                                                   errString: CharSequence) {
                    super.onAuthenticationError(errorCode, errString)

                    Toast.makeText(applicationContext,
                        "Błąd autentykacji: $errString", Toast.LENGTH_SHORT)
                        .show()
                }

                override fun onAuthenticationSucceeded(
                    result: BiometricPrompt.AuthenticationResult) {
                    super.onAuthenticationSucceeded(result)
                    val tmp = findViewById<LinearLayout>(R.id.login_progressBar)
                    tmp.visibility = View.VISIBLE
                    loginEmailInput.text = Editable.Factory.getInstance().newEditable(email)
                    loginPasswordInput.text = Editable.Factory.getInstance().newEditable(password)
                    signIn()
                }

                override fun onAuthenticationFailed() {
                    super.onAuthenticationFailed()
                    Toast.makeText(applicationContext, "Autentykacja nieudana",
                        Toast.LENGTH_SHORT)
                        .show()
                }
            })

        promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("Logowanie biometryczne")
            .setSubtitle("Zaloguj się za pomocą odcisku palca")
            .setDeviceCredentialAllowed(true)
            .build()
        biometricPrompt.authenticate(promptInfo)

    }

    private fun forgotPassword() {

    }

    private fun toSignUp() {

        setContentView(R.layout.activity_register)

        registerEmailInput = findViewById(R.id.register_username_input)
        registerEmailLayout = findViewById(R.id.register_username_layout)
        registerPasswordInput =  findViewById(R.id.register_password_input)
        registerPasswordLayout =  findViewById(R.id.register_password_layout)
        registerPasswordInput2 =  findViewById(R.id.register_password2_input)
        registerPasswordLayout2 =  findViewById(R.id.register_password2_layout)

        val signUpButton = findViewById<Button>(R.id.register_signUp_button)
        signUpButton.setOnClickListener {
            signUp()
        }
        registerEmailInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validateEmail()
            }
        }
        registerPasswordInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validateOnePassword(1)
            }
        }
        registerPasswordInput2.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validateOnePassword(2)
            }
        }
        activeLayout = "register"
    }

    private fun toSignIn(){
        setContentView(R.layout.activity_login)

        loginEmailInput = findViewById(R.id.login_username_input)
        loginEmailLayout = findViewById(R.id.login_username_layout)
        loginPasswordInput = findViewById(R.id.login_password_input)
        loginPasswordLayout = findViewById(R.id.login_password_layout)

        loginEmailInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validateEmail()
            }
        }
        loginPasswordInput.setOnFocusChangeListener { _, hasFocus ->
            if(!hasFocus){
                validatePassword()
            }
        }
        activeLayout = "login"
    }

    private fun signIn(){
        val email = loginEmailInput.text.toString()
        val password = loginPasswordInput.text.toString()

        if(validateSign()){
            findViewById<LinearLayout>(R.id.login_progressBar).visibility = View.VISIBLE
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(this) { task ->
                        if (task.isSuccessful) {
                            val user = auth.currentUser!!
                            val loggedUser = user.email

                            user.getIdToken(false).addOnSuccessListener {
                                store.storeToken(it.token.toString())
                                whoAmI(loggedUser!!,password)
                            }


                        } else {
                            findViewById<LinearLayout>(R.id.login_progressBar).visibility = View.GONE
                            Toast.makeText(baseContext, "Błąd logowania",
                                    Toast.LENGTH_SHORT).show()
                        }
                    }
        }
    }

    private fun signUp() {
        val email = findViewById<TextInputEditText>(R.id.register_username_input).text.toString()
        val password = findViewById<TextInputEditText>(R.id.register_password_input).text.toString()

        if(validateSign()){
            findViewById<LinearLayout>(R.id.login_progressBar).visibility = View.VISIBLE
            auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(this) { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser!!

                        user.getIdToken(false).addOnSuccessListener {
                            store.storeToken(it.token.toString())

                            whoAmI(user.email!!,password)
                        }
                    } else {
                        findViewById<LinearLayout>(R.id.login_progressBar).visibility = View.GONE
                        when{
                            task.exception.toString().contains("The email address is already in use by another account") -> Toast.makeText(baseContext, "Podany adres email jest już zajęty", Toast.LENGTH_SHORT).show()
                            else -> Toast.makeText(baseContext, "Błąd rejestracji", Toast.LENGTH_SHORT).show()

                        }


                    }
                }
        }
    }

    override fun onBackPressed() {
        if(activeLayout == "register"){
            setContentView(R.layout.activity_login)
            toSignIn()
        }
        else{
            super.onBackPressed()
        }

    }

    private fun whoAmI(email: String, password: String){

        val whoAmIQuery = WhoAmIQuery(email.toInput())

        GlobalScope.launch {
            try{
                val tmp  = ApolloInstance.get().query(whoAmIQuery).toDeferred().await()
                try{
                    val user = tmp.data!!.uSER[0]
                    store.store(user.email,"email")
                    store.storePassword(password)
                    store.store(user.id.toString(),"id")
                    store.store(user.teacher_id.toString(),"teacherId")
                    store.store(user.tEACHER!!.first_name,"firstName")
                    store.store(user.tEACHER.last_name,"lastName")
                    val api = ApiDownload(user.teacher_id!!, user.id)
                    api.init()
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)

                } catch (ex : NullPointerException){
                    val intent = Intent(this@LoginActivity, NonAuthorizedEntryActivity::class.java)
                    startActivity(intent)
                }
            } catch (e : Exception){
                val intent = Intent(this@LoginActivity, NonAuthorizedEntryActivity::class.java)
                startActivity(intent)
            }
        }
    }


    private fun validateEmail(): Boolean {

        var allGood = true
        val email : String?
        val emailLayout: TextInputLayout?
        if(activeLayout == "login"){
            email = loginEmailInput.text.toString()
            emailLayout = loginEmailLayout
        }
        else{
            email = registerEmailInput.text.toString()
            emailLayout = registerEmailLayout
        }
        emailLayout.error = ""
        if (email.isNullOrEmpty()) {
            emailLayout.error = "Nie podano prawidłowego adresu email"
            allGood = false
        } else {
            if ((email.length < 6) or !email.contains("@")) {
                emailLayout.error = "Nie podano prawidłowego adresu email"
                allGood = false
            }
        }
        return allGood
    }

    private fun validateOnePassword(no : Int): Boolean {
        var allGood = true

        if(no == 1){
            registerPasswordLayout.error = ""
            val password = registerPasswordInput.text.toString()
            if(password.isEmpty() or (password.length < 8)){
                registerPasswordLayout.error = "Nie podano prawidłowego hasła"

                allGood = false
            }
        }
        else {
            registerPasswordLayout2.error = ""
            val password = registerPasswordInput2.text.toString()
            if (password.isEmpty() or (password.length < 8)) {
                registerPasswordLayout2.error = "Nie podano prawidłowego hasła"
                allGood = false
            }
        }
        return allGood
    }

    private fun validatePassword(): Boolean {
        var allGood = true

        if(activeLayout == "login"){
            loginPasswordLayout.error = ""
            if(loginPasswordInput.text.toString().isEmpty()){
                loginPasswordLayout.error = "Nie podano prawidłowego hasła"
                allGood = false
            }
            else{
                if(loginEmailInput.text.toString().length < 8){
                    loginPasswordLayout.error = "Nie podano prawidłowego hasła"
                    allGood = false
                }
            }
        }
        else{
            val password = registerPasswordInput.text.toString()
            val password2 = registerPasswordInput2.text.toString()

            if(validateOnePassword(1) && validateOnePassword(2)){
                if(password != password2){
                    registerPasswordLayout.error = "hasła różnią się od siebie"
                    registerPasswordLayout2.error = "hasła różnią się od siebie"
                    allGood = false
                }
            }
            else{
                allGood = false
            }
        }
        return allGood
    }

    private fun validateSign() : Boolean{
        return (validateEmail() and validatePassword())
    }
}