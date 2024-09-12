package com.example.kotlindormify.possible

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindormify.R
import com.example.kotlindormify.SignInActivity
import com.example.kotlindormify.databinding.ZactivitySignUp3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging

class SignUpTenantMainActivity : AppCompatActivity() {

    private lateinit var binding: ZactivitySignUp3Binding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ZactivitySignUp3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()




        // Retrieve dormitory data from arguments
        // Retrieve data from the intent
        val username = intent.getStringExtra("username")
        val age = intent.getStringExtra("age")
        val sex = intent.getStringExtra("sex")
        val address = intent.getStringExtra("address")
        val phoneNumber = intent.getStringExtra("phoneNumber")
        val email = intent.getStringExtra("email")
        val password = intent.getStringExtra("password")

      //  val profile = arguments?.getString("profile")
       // val identification = arguments?.getString("identification")


        // Populate UI with dormitory details
        binding.username3.text = username
        binding.age3.text = age
        binding.sex3.text = sex
        binding.address3.text = address
        binding.phoneNumber3.text = phoneNumber
        binding.email3.text = email
        binding.password3.text = password




        binding.buttontenant.setOnClickListener {
            val usermame = binding.username3.text.toString()
            val age = binding.age3.text.toString()
            val sex = binding.sex3.text.toString()
            val address = binding.address3.text.toString()
            val phoneNumber = binding.phoneNumber3.text.toString()
            val email = binding.email3.text.toString()
            val password = binding.password3.text.toString()

            //emergency
            val etFullname = binding.etEmergencyFullName.text.toString()
            val etAddress = binding.etEmergencyAddress.text.toString()
            val etphoneNumber = binding.etEmergencyPhoneNumber.text.toString()
            val etEmail = binding.etEmergencyEmail.text.toString()



            if (!binding.cbAgreement.isChecked) {
                Toast.makeText(this, "Please agree to the terms and conditions to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Prevent further execution of the click listener
            }


            if (etFullname.isNotEmpty() && etAddress.isNotEmpty() && etphoneNumber.isNotEmpty() && etEmail.isNotEmpty()) {


                                val firebaseUser = auth.currentUser
                                val userId = firebaseUser?.uid

                                if (userId != null) {
                                    FirebaseMessaging.getInstance().token
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val fcmToken = task.result

                                                val selectedRole = when (binding.rgRole.checkedRadioButtonId) {
                                                    R.id.rbTenant -> 1 // Tenant
                                                    R.id.rbLandlord -> 2 // Dorm Landlord
                                                    else -> 0 // Default or no selection
                                                }


                                                firestore.collection("users")
                                                    .whereEqualTo("username", username)
                                                    .get()
                                                    .addOnCompleteListener { usernameTask ->
                                                        if (usernameTask.isSuccessful) {
                                                            if (usernameTask.result?.isEmpty == true) {
                                                                // Username is unique
                                                                val user = hashMapOf(
                                                                    "email" to email,
                                                                    "phoneNumber" to phoneNumber,
                                                                    "password" to password,
                                                                    "role" to selectedRole,
                                                                    "fcmToken" to fcmToken,
                                                                    "createdTimestamp" to FieldValue.serverTimestamp(),
                                                                    "userId" to userId,
                                                                    "username" to username
                                                                )

                                                                firestore.collection("users")
                                                                    .document(userId)
                                                                    .set(user)
                                                                    .addOnSuccessListener {


                                                                        val intent = Intent(this, SignInActivity::class.java)
                                                                        startActivity(intent)
                                                                        Toast.makeText(this, "User Registered Successfully", Toast.LENGTH_SHORT).show()
                                                                        finish()
                                                                        progressDialog?.dismiss()
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                                                                        progressDialog?.dismiss()
                                                                    }
                                                            } else {
                                                                Toast.makeText(this, "Username already taken. Please choose a different one.", Toast.LENGTH_SHORT).show()
                                                                progressDialog?.dismiss()
                                                            }
                                                        } else {
                                                            Toast.makeText(this, "Error checking username uniqueness. Please try again.", Toast.LENGTH_SHORT).show()
                                                            progressDialog?.dismiss()
                                                        }
                                                    }
                                            } else {
                                                Toast.makeText(this, "Failed to obtain FCM token. Please try again.", Toast.LENGTH_SHORT).show()
                                                progressDialog?.dismiss()
                                            }
                                        }
                                } else {
                                    Toast.makeText(this, "Failed to get user ID.", Toast.LENGTH_SHORT).show()
                                    progressDialog?.dismiss()
                                }
                            }
                }
            }




        }




