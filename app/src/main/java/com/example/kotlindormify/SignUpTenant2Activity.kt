package com.example.kotlindormify

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindormify.databinding.ActivitySignUpBinding
import com.example.kotlindormify.databinding.ZactivitySignUp2Binding
import com.example.kotlindormify.profile.TermsAndConditions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignUpTenant2Activity : AppCompatActivity() {

    private lateinit var binding: ZactivitySignUp2Binding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ZactivitySignUp2Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()





    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)


    }






}
