package com.example.kotlindormify

import android.content.Context
import android.content.SharedPreferences

class PrefManager(context: Context) {
    private val prefName = "MyAppPref"
    private val pref: SharedPreferences =
        context.getSharedPreferences(prefName, Context.MODE_PRIVATE)


    fun setFreshInstall(isFreshInstall: Boolean) {
        pref.edit().putBoolean("isFreshInstall", isFreshInstall).apply()
    }

    fun isFreshInstall(): Boolean {
        return pref.getBoolean("isFreshInstall", true)
    }

    fun setUserEmail(email: String) {
        pref.edit().putString("user_email", email).apply()
    }

    fun getUserEmail(): String? {
        return pref.getString("user_email", null)
    }

}