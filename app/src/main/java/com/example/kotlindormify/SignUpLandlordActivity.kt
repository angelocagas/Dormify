package com.example.kotlindormify

import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.example.kotlindormify.databinding.ActivitySignUpBinding
import com.example.kotlindormify.databinding.ActivitySignUpLandlordBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException

class SignUpLandlordActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySignUpLandlordBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpLandlordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()

        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }


        binding.loginCountrycode.setCountryForPhoneCode(63)
        binding.imageView.setOnClickListener {
            super.onBackPressed()
        }





        binding.buttonlandlord.setOnClickListener {
            val email = binding.emailEt.text.toString()
            val password = binding.passET.text.toString()
            val confirmPassword = binding.confirmPassEt.text.toString()
            val username = binding.usernameEt.text.toString()
            val phoneNumber = binding.phoneNumberEt.text.toString()

            val progressBar = binding.progressBar
            val cbAgreement = binding.cbAgreement


            if (username.isEmpty()) {
                binding.usernameLayout.error = "Full name is required"
                binding.lbletEmergencyFullName.text = "Full name is required" // Set error message in lblFullName
                binding.lbletEmergencyFullName2.visibility = View.VISIBLE
            } else {
                binding.usernameLayout.error = null // Clear the error if not empty
                binding.lbletEmergencyFullName.text = "Full Name" // Clear the error message in lblFullName
                binding.lbletEmergencyFullName2.visibility = View.INVISIBLE
            }

            if (phoneNumber.isEmpty()) {
                binding.phoneNumberLayout.error = "Phone number is required"
                binding.lblphoneNumberLayout.text = "Phone Number is required" // Set error message in lblFullName
                binding.lblphoneNumberLayout2.visibility = View.VISIBLE
            } else {
                binding.phoneNumberLayout.error = null // Clear the error if not empty
                binding.lblphoneNumberLayout.text = "Phone Number" // Clear the error message in lblFullName
                binding.lblphoneNumberLayout2.visibility = View.INVISIBLE
            }
            if (email.isEmpty()) {
                binding.emailLayout.error = "Email is required"
                binding.lbletemailLayout.text = "Email is required" // Set error message in lblFullName
                binding.lbletemailLayout2.visibility = View.VISIBLE
            } else {
                binding.emailLayout.error = null // Clear the error if not empty
                binding.lbletemailLayout.text = "Email" // Clear the error message in lblFullName
                binding.lbletemailLayout2.visibility = View.INVISIBLE
            }
            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password is required"
                binding.lbletpasswordLayout.text = "Password is required" // Set error message in lblFullName
                binding.lbletpasswordLayout2.visibility = View.VISIBLE
            } else {
                binding.passwordLayout.error = null // Clear the error if not empty
                binding.lbletpasswordLayout.text = "Password" // Clear the error message in lblFullName
                binding.lbletpasswordLayout2.visibility = View.INVISIBLE
            }
            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordLayout.error = "Confirm Password is required"
                binding.lbletpassword2Layout.text = "Re-type Password is required" // Set error message in lblFullName
                binding.lbletpassword2Layout2.visibility = View.VISIBLE
            } else {
                binding.confirmPasswordLayout.error = null // Clear the error if not empty
                binding.lbletpassword2Layout.text = "Re-type Password" // Clear the error message in lblFullName
                binding.lbletpassword2Layout2.visibility = View.INVISIBLE
            }


            // Check if an image has been selected
            if (!isImageSelected) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                binding.btnAddImage.setBackgroundResource(R.drawable.rectangle_radius_white_stroke_blackerror)
                binding.lbletbtnAddImage.text = "Upload Profile Photo is required" // Set error message in lblFullName
                binding.lbletbtnAddImage2.visibility = View.VISIBLE
                return@setOnClickListener
            }else{
                binding.btnAddImage.setBackgroundResource(R.drawable.rectangle_radius_white_stroke_black)
                binding.lbletbtnAddImage.text = "Upload Profile Photo" // Clear the error message in lblFullName
                binding.lbletbtnAddImage2.visibility = View.INVISIBLE
            }
            if (!cbAgreement.isChecked) {
                Toast.makeText(this, "Please agree to the terms and conditions to continue", Toast.LENGTH_SHORT
                ).show()
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

                                                val enteredPhoneNumber =
                                                    binding.phoneNumberEt.text.toString()
                                                val selectedRole =
                                                    when (binding.rgRole.checkedRadioButtonId) {
                                                        R.id.rbTenant -> 1 // Tenant
                                                        R.id.rbLandlord -> 2 // Dorm Landlord
                                                        else -> 0 // Default or no selection
                                                    }
                                                binding.loginCountrycode.setCountryForPhoneCode(63)
                                                val selectedCountryCode =
                                                    binding.loginCountrycode.selectedCountryCode
                                                val phoneNumber =
                                                    "+$selectedCountryCode$enteredPhoneNumber"

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
                                                                        progressBar.visibility =
                                                                            ProgressBar.GONE
                                                                        uploadProfilePicture(userId)
                                                                        val intent = Intent(
                                                                            this,
                                                                            SignInActivity::class.java
                                                                        )

                                                                        AlertDialog.Builder(this)
                                                                            .setMessage("User Registered successfully")
                                                                            .setPositiveButton("OK") { dialog, _ ->
                                                                                dialog.dismiss()
                                                                                startActivity(intent)
                                                                                finish()}
                                                                            .show()

                                                                        progressDialog?.dismiss()
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        showAlert("Invalid email or already taken. Please try again.")
                                                                        progressDialog?.dismiss()
                                                                    }
                                                            } else {
                                                                Toast.makeText(this, "Full Name already taken. Please choose a different one.", Toast.LENGTH_SHORT).show()
                                                                binding.usernameLayout.error = "Full Name already exist!"
                                                                progressDialog?.dismiss()
                                                            }
                                                        } else {
                                                            showAlert("Error checking username uniqueness. Please try again.")
                                                            progressDialog?.dismiss()
                                                        }
                                                    }
                                            } else {
                                                Toast.makeText(
                                                    this,
                                                    "Failed to obtain FCM token. Please try again.",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                                progressDialog?.dismiss()
                                            }
                                        }
                                } else {
                                    Toast.makeText(
                                        this,
                                        "Failed to get user ID.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressDialog?.dismiss()
                                }
                            } else {
                                Toast.makeText(this, "Email already exist!. Please try again.", Toast.LENGTH_SHORT).show()
                                binding.emailLayout.error = "!"
                                progressDialog?.dismiss()
                            }
                        }
                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                    binding.confirmPasswordLayout.error = "!"
                    binding.passwordLayout.error = "!"
                    progressDialog?.dismiss()
                }
            } else {
                Toast.makeText(
                    this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                binding.confirmPasswordLayout.error = "!"
                binding.passwordLayout.error = "!"
                progressDialog?.dismiss()
            }
        }

        binding.btnAddImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Profile Photo to Continue"),
                PICK_IMAGE_REQUEST
            )
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

    fun showAlert(message: String) {
        AlertDialog.Builder(this)
            .setMessage(message)
            .setPositiveButton("OK") { dialog, _ -> dialog.dismiss() }
            .show()
    }


}

