package com.plocki.teacherDiary.utility

import android.content.SharedPreferences
import android.util.Base64
import androidx.preference.PreferenceManager
import com.plocki.teacherDiary.cipher.Decrypt
import com.plocki.teacherDiary.cipher.Encrypt


class Store {

    private val context  = MainApplication.appContext

    private val encrypt = Encrypt()
    private val decrypt  = Decrypt()

    private val pref  = PreferenceManager.getDefaultSharedPreferences(context)
    private val editor : SharedPreferences.Editor = pref.edit()



    fun store(value: String, label: String){
        editor.putString(label,value)
        editor.apply()
        editor.commit()
    }

    fun retrieve(label: String) : String?{
        return pref.getString(label,"")
    }

    fun remove(label: String) {
        editor.remove(label)
        editor.commit()
    }

    fun storeToken(value: String){
        val cipherClass = encrypt.encryptText("ALIAS",value)
        val encodedValueString = Base64.encodeToString(cipherClass.encrypter, Base64.DEFAULT)
        val encodedIVString = Base64.encodeToString(cipherClass.iv, Base64.DEFAULT)
        editor.putString("userToken",encodedValueString)
        editor.apply()
        editor.putString("iv",encodedIVString)
        editor.apply()
        editor.commit()
    }

    fun retrieveToken() : String{
        val encodedValue = pref.getString("userToken","")
        val encodedIV = pref.getString("iv","")
        val encodedValueByteArray = Base64.decode(encodedValue, Base64.DEFAULT)
        val encodedIVByteArray = Base64.decode(encodedIV, Base64.DEFAULT)
        return decrypt.decryptData("ALIAS",encodedValueByteArray, encodedIVByteArray)
    }

    fun removeToken(){
        editor.remove("userToken")
        editor.remove("iv")
        editor.commit()
    }

    fun storePassword(value: String){
        val cipherClass = encrypt.encryptText("ALIAS",value)
        val encodedValueString = Base64.encodeToString(cipherClass.encrypter, Base64.DEFAULT)
        val encodedIVString = Base64.encodeToString(cipherClass.iv, Base64.DEFAULT)
        editor.putString("password",encodedValueString)
        editor.apply()
        editor.putString("iv2",encodedIVString)
        editor.apply()
        editor.commit()
    }

    fun retrievePassword() : String{
        val encodedValue = pref.getString("password","")
        val encodedIV = pref.getString("iv2","")
        val encodedValueByteArray = Base64.decode(encodedValue, Base64.DEFAULT)
        val encodedIVByteArray = Base64.decode(encodedIV, Base64.DEFAULT)
        return decrypt.decryptData("ALIAS",encodedValueByteArray, encodedIVByteArray)
    }

    fun removePassword(){
        editor.remove("password")
        editor.remove("iv2")
        editor.commit()
    }
}