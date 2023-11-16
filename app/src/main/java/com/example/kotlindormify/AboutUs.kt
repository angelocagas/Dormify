package com.example.kotlindormify.profile

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import com.example.kotlindormify.R

class AboutUs : AppCompatActivity() {
    private lateinit var backButton: Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about_us)

        backButton = findViewById(R.id.backbtn)
        backButton.setOnClickListener {
            // Call the onBackPressed function to simulate the back button press
            onBackPressed()
        }
    }

}