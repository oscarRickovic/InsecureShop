package com.insecureshop.util

import android.content.Context
import android.content.SharedPreferences

object Prefs {

    lateinit var sharedpreferences: SharedPreferences
    var prefs : Prefs? = null

    fun getInstance(context: Context): Prefs {
        if (prefs == null) {
            sharedpreferences =
                context.getSharedPreferences("Prefs", Context.MODE_PRIVATE)
            prefs = this
        }
        return prefs!!
    }

    var data: String?
        get() = sharedpreferences.getString("data","")
        set(value) {
            sharedpreferences.edit().putString("data", value).apply()
        }

    var username: String?
        get() = sharedpreferences.getString("username","")
        set(value) {
            sharedpreferences.edit().putString("username", value).apply()
        }

    // Replace direct password storage with a session token
    var sessionToken: String?
        get() = sharedpreferences.getString("session_token","")
        set(value) {
            sharedpreferences.edit().putString("session_token", value).apply()
        }
    
    // Maintain this for backward compatibility but don't use it for actual password
    var password: String?
        get() = "" // Don't return actual password
        set(value) {
            // Don't store the password anymore
        }

    var productList: String?
        get() = sharedpreferences.getString("productList","")
        set(value) {
            sharedpreferences.edit().putString("productList", value).apply()
        }

    fun clearAll(){
        sharedpreferences.edit().clear().apply()
    }
}