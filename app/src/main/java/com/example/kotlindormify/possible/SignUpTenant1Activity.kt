package com.example.kotlindormify.possible

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindormify.databinding.ZactivitySignUp1Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpTenant1Activity : AppCompatActivity() {

    private lateinit var binding: ZactivitySignUp1Binding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ZactivitySignUp1Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.loginCountrycode.setCountryForPhoneCode(63)

        binding.selectbtn1.setOnClickListener {


            val username = binding.etFullNametenant.text.toString()
            val age = binding.etAgetenant.text.toString()
           // val sex = binding.etAddress2tenant.text.toString()
            val address = binding.etAddress2tenant.text.toString()
            val phoneNumber = binding.etPhoneNumbertenant.text.toString()
            val email = binding.etEmailtenant.text.toString()
            val password = binding.passETtenant.text.toString()


            val intent = Intent(this, SignUpTenant2Activity::class.java)
            startActivity(intent)
        }





    }

}
