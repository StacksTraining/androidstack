package com.magda.noteapp.utils

import android.content.Context
import android.content.SharedPreferences
import com.magda.noteapp.model.User

class SessionManager (context: Context) {
    // SharedPreference
    //saving our data
    //a small collection of primitive type
    //key -value pairs
    //user preference
    //persist data across
    //log in and the log out
    //public and private
    //store, retrieving our info
    private var sharedPreferences: SharedPreferences
    private  var editor : SharedPreferences.Editor
    init {
        sharedPreferences= context.getSharedPreferences(Constants.SHARED_PREF, Context.MODE_PRIVATE)
        editor= sharedPreferences.edit()
        editor.apply()
    }

    fun storeInfo(user :User){
        editor.putString(Constants.UUID, user.userId)
        editor.putString(Constants.FIRST_NAME, user.firstName)
        editor.putString(Constants.LAST_NAME, user.lastName)
        editor.putString(Constants.EMAIL_ADDRESS, user.emailAddress)
        editor.putBoolean(Constants.IS_LOGGED_IN, true)
        editor.apply()
    }

    fun getInfo():User{
        val user = User()
        user.userId= sharedPreferences.getString(Constants.UUID, "")
        user.firstName= sharedPreferences.getString(Constants.FIRST_NAME, "")
        user.lastName= sharedPreferences.getString(Constants.LAST_NAME, "")
        user.emailAddress= sharedPreferences.getString(Constants.EMAIL_ADDRESS, "")
        return user
    }

    fun  isLoggedIn():Boolean{
        return sharedPreferences.getBoolean(Constants.IS_LOGGED_IN,false)
    }

    fun clear(){
        editor.clear()
        editor.apply()
    }
}