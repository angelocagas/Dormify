package com.example.kotlindormify

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.firebase.FirebaseApp

class MainActivity : AppCompatActivity() {
    private lateinit var prefManager: PrefManager

    override fun onCreate(savedInstanceState: Bundle?) {
        FirebaseApp.initializeApp(this)
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        getSupportActionBar()?.hide()


        prefManager = PrefManager(this)

        // Check if the app is a fresh install
        if (prefManager.isFreshInstall()) {
            // It's a fresh install, show the splash screen
            prefManager.setFreshInstall(false)
        } else {
            // Not a fresh install, redirect to SignInActivity
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish() // Finish MainActivity
        }

    }

    fun goToSignInPage(view: android.view.View) {
        val intent = Intent(this, SignInActivity::class.java)
        startActivity(intent)
        finish()
    }
}
