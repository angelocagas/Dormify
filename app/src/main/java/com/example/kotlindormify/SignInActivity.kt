package com.example.kotlindormify

import android.app.ProgressDialog
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindormify.databinding.ActivitySignInBinding
import com.example.kotlindormify.landlord.LandlordDashboardActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class SignInActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignInBinding
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore
    private lateinit var loadingProgressBar: ProgressBar // Added ProgressBar
    private var progressDialog: ProgressDialog? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignInBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Reference the loading ProgressBar
        loadingProgressBar = findViewById(R.id.progressBar)

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Show loading ProgressBar
                showLoadingDialog()

                auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener { authTask ->
                        // Hide loading ProgressBar
                        if (authTask.isSuccessful) {
                            val user = auth.currentUser
                            if (user != null) {
                                // Successfully authenticated
                                firestore.collection("users")
                                    .whereEqualTo("email", email)
                                    .get()
                                    .addOnSuccessListener { querySnapshot ->
                                        loadingProgressBar.visibility = View.GONE
                                        if (!querySnapshot.isEmpty) {
                                            for (document in querySnapshot) {
                                                val user = document.toObject(Users::class.java)
                                                val selectedRole = user.role
                                                when (selectedRole) {
                                                    1 -> {
                                                        // Proceed to MainActivity for Tenant
                                                        val intent = Intent(this@SignInActivity, DashboardActivity::class.java)
                                                        val userEmail = email // Retrieve the user's email from the sign-in response or authentication object
                                                        val prefManager = PrefManager(this@SignInActivity) // Initialize PrefManager with the context
                                                        prefManager.setUserEmail(userEmail)
                                                        startActivity(intent)
                                                        finish()
                                                        progressDialog?.dismiss()
                                                    }
                                                    2 -> {
                                                        // Proceed to SecondaryActivity for Dorm Landlord
                                                        val intent = Intent(this@SignInActivity, LandlordDashboardActivity::class.java)
                                                        val userEmail = email // Retrieve the user's email from the sign-in response or authentication object
                                                        val prefManager = PrefManager(this@SignInActivity) // Initialize PrefManager with the context
                                                        prefManager.setUserEmail(userEmail)
                                                        startActivity(intent)
                                                        finish()
                                                        progressDialog?.dismiss()
                                                    }
                                                    else -> {
                                                        Toast.makeText(this@SignInActivity, "Invalid role", Toast.LENGTH_SHORT).show()
                                                        progressDialog?.dismiss()
                                                    }
                                                }
                                                return@addOnSuccessListener // Exit the loop since we found a matching user
                                            }
                                        }
                                        // If execution reaches here, no matching user was found
                                        Toast.makeText(this@SignInActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                                        progressDialog?.dismiss()
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle database error
                                        Toast.makeText(this@SignInActivity, "Database error:", Toast.LENGTH_SHORT).show()
                                        progressDialog?.dismiss()
                                    }
                            }
                        } else {
                            // Authentication failed
                            Toast.makeText(this@SignInActivity, "Authentication failed.", Toast.LENGTH_SHORT).show()
                            progressDialog?.dismiss()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Hide loading ProgressBar
                        progressDialog?.dismiss()
                        // Handle authentication error
                        Toast.makeText(this@SignInActivity, "Authentication error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            } else {
                Toast.makeText(this@SignInActivity, "Empty fields are not allowed.", Toast.LENGTH_SHORT).show()
                progressDialog?.dismiss()
            }
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Logging In ...") // Set the message you want to display
        progressDialog?.setCancelable(false) // Prevents user from dismissing the dialog by tapping outside
        progressDialog?.show()
    }
}
