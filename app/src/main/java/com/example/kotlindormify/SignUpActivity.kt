package com.example.kotlindormify

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.kotlindormify.databinding.ActivitySignUpBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignUpBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        binding.loginCountrycode.setCountryForPhoneCode(63)

        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.button.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            val confirmPassword = binding.confirmPassEt.text.toString()
            val username = binding.usernameEt.text.toString()
            val progressBar = binding.progressBar
            val cbAgreement = binding.cbAgreement



            // Check if an image has been selected
            if (!isImageSelected) {
                Toast.makeText(this, "Please select an image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Prevent further execution of the click listener
            }
            if (!cbAgreement.isChecked) {
                Toast.makeText(this, "Please agree to the terms and conditions to continue", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Prevent further execution of the click listener
            }


            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && password.length >= 6) {
                showLoadingDialog()
                if (password == confirmPassword) {
                    auth.createUserWithEmailAndPassword(email, password)
                        .addOnCompleteListener { authTask ->
                            if (authTask.isSuccessful) {
                                val firebaseUser = auth.currentUser
                                val userId = firebaseUser?.uid

                                if (userId != null) {
                                    FirebaseMessaging.getInstance().token
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val fcmToken = task.result

                                                val enteredPhoneNumber = binding.phoneNumberEt.text.toString()
                                                val selectedRole = when (binding.rgRole.checkedRadioButtonId) {
                                                    R.id.rbTenant -> 1 // Tenant
                                                    R.id.rbLandlord -> 2 // Dorm Landlord
                                                    else -> 0 // Default or no selection
                                                }
                                                binding.loginCountrycode.setCountryForPhoneCode(63)
                                                val selectedCountryCode = binding.loginCountrycode.selectedCountryCode
                                                val phoneNumber = "+$selectedCountryCode$enteredPhoneNumber"

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
                                                                        progressBar.visibility = ProgressBar.GONE
                                                                        uploadProfilePicture(userId)
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
                            } else {
                                Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                                progressDialog?.dismiss()
                            }
                        }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            } else {
                Toast.makeText(this, "Invalid email or password. Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                progressDialog?.dismiss()
            }
        }

        binding.btnAddImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Profile Photo to Continue"), PICK_IMAGE_REQUEST)
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            try {
                val bitmap =
                    MediaStore.Images.Media.getBitmap(this.contentResolver, selectedImageUri)
                // Display the selected image or do any other processing as needed
                // For example, you can set it in an ImageView:
                // binding.ivSelectedImage.setImageBitmap(bitmap)
                binding.ivSelectedImage.setImageBitmap(bitmap)

                isImageSelected = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    private fun uploadProfilePicture(userId: String) {
        if (::selectedImageUri.isInitialized) {
            val storageRef = FirebaseStorage.getInstance().reference.child("profile_pictures")
            val imageRef = storageRef.child("$userId.jpg")

            imageRef.putFile(selectedImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully
                    // Now, you can handle success, update the user's profile with the image URL, etc.
                    imageRef.downloadUrl.addOnCompleteListener { downloadUrlTask ->
                        if (downloadUrlTask.isSuccessful) {
                            val imageUrl = downloadUrlTask.result.toString()
                            // Update user's profile with the image URL in Firestore or Realtime Database
                            updateUserProfileImage(userId, imageUrl)
                        } else {
                            // Handle error while getting the image URL
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle image upload failure
                }
        }
    }

    private fun updateUserProfileImage(userId: String, imageUrl: String) {
        // Update the user's profile in Firestore or Realtime Database with the image URL
        // For example, if you are using Firestore:
        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val userDocument = usersCollection.document(userId)

        userDocument.update("profileImageUrl", imageUrl)
            .addOnSuccessListener {
                // Profile image URL updated successfully
                // You can also navigate to the next screen or perform any other action here
            }
            .addOnFailureListener { e ->
                // Handle profile image URL update failure
            }
    }
    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Signing Up ...") // Set the message you want to display
        progressDialog?.setCancelable(false) // Prevents user from dismissing the dialog by tapping outside
        progressDialog?.show()
    }



}
