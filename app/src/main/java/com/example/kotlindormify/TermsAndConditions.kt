package com.example.kotlindormify.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.kotlindormify.R

class TermsAndConditions : AppCompatActivity() {
    private lateinit var backButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_terms_and_conditions)

        backButton = findViewById(R.id.backbtn)
        backButton.setOnClickListener {
            // Call the onBackPressed function to simulate the back button press
            onBackPressed()
        }
    }
}