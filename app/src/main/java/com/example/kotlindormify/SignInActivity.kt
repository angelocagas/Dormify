package com.example.kotlindormify

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
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

            if (email.isEmpty()) {
                binding.emailLayout.error = "Email is required"
            } else {
                binding.emailLayout.error = null // Clear the error if not empty
            }

            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password is required"
            } else {
                binding.passwordLayout.error = null // Clear the error if not empty
            }

            if (email.isNotEmpty() && password.isNotEmpty()) {
                // Show loading ProgressBar
                showLoadingDialog()

                if (!isValidEmail(email)) {
                    binding.emailLayout.error = "Invalid email format"
                    progressDialog?.dismiss()
                    return@setOnClickListener
                } else {
                    binding.emailLayout.error = null // Clear the error if the email format is valid
                }

                // Check if the email exists in Firestore before attempting authentication
                firestore.collection("users")
                    .whereEqualTo("email", email)
                    .get()
                    .addOnSuccessListener { querySnapshot ->
                        if (!querySnapshot.isEmpty) {
                            // The email exists in Firestore, proceed with authentication
                            auth.signInWithEmailAndPassword(email, password)
                                .addOnCompleteListener { authTask ->
                                    // Hide loading ProgressBar
                                    binding.passwordLayout.error = null

                                    if (authTask.isSuccessful) {
                                        // User successfully authenticated
                                        val user = auth.currentUser
                                        if (user != null) {
                                            // Proceed to fetch user data from Firestore
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
                                                                    val intent = Intent( this@SignInActivity, DashboardActivity::class.java )
                                                                    val userEmail = email
                                                                    val prefManager = PrefManager(this@SignInActivity)
                                                                    prefManager.setUserEmail( userEmail)
                                                                    startActivity(intent)
                                                                    finish()
                                                                    progressDialog?.dismiss()
                                                                }
                                                                2 -> {
                                                                    // Proceed to SecondaryActivity for Dorm Landlord
                                                                    val intent = Intent(this@SignInActivity, LandlordDashboardActivity::class.java)
                                                                    val userEmail = email
                                                                    val prefManager = PrefManager(this@SignInActivity)
                                                                    prefManager.setUserEmail( userEmail )
                                                                    startActivity(intent)
                                                                    finish()
                                                                    progressDialog?.dismiss()
                                                                }
                                                                else -> {
                                                                    showAlert("Invalid role")
                                                                    progressDialog?.dismiss()
                                                                }
                                                            }
                                                        }
                                                    } else {
                                                        // No matching user found in Firestore
                                                        showAlert("We couldn't find any registered users with this email address.")
                                                        progressDialog?.dismiss()
                                                    }
                                                }
                                                .addOnFailureListener { e ->
                                                    // Handle Firestore database error
                                                    showAlert("Database error: $e")
                                                    progressDialog?.dismiss()
                                                }
                                        }
                                    } else {
                                        // Authentication failed
                                        showAlert("Invalid password.")
                                        binding.passwordLayout.error = "Invalid password."
                                        progressDialog?.dismiss()
                                    }
                                }
                                .addOnFailureListener { e ->
                                    // Hide loading ProgressBar
                                    progressDialog?.dismiss()
                                }
                        } else {
                            // No matching user found in Firestore
                            showAlert("We couldn't find any registered users with this email address.")
                            progressDialog?.dismiss()
                        }
                    }
                    .addOnFailureListener { e ->
                        // Handle Firestore database error
                        showAlert("Database error: $e")
                        progressDialog?.dismiss()
                    }
            } else {
                showAlert("Please fill all fields.")
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

    fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }

    // Function to validate email format
    private fun isValidEmail(email: String): Boolean {
        val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+"
        return email.matches(emailPattern.toRegex())
    }

}
