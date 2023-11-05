package com.example.kotlindormify

import android.app.Activity
import android.app.ProgressDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.bumptech.glide.Glide
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.hbb20.CountryCodePicker

class PersonalInformationActivity : AppCompatActivity() {
    private lateinit var ivProfilePicture: ImageView
    private lateinit var usernameEditText: EditText
    private lateinit var emailEditText: EditText
    private lateinit var mobileEditText: EditText
    private lateinit var confirmPassword: EditText
    private lateinit var updateButton: Button
    private lateinit var userId: String
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()
    private lateinit var countryCodePicker: CountryCodePicker
    private var selectedImageUri: Uri? = null

    private var progressDialog: ProgressDialog? = null


    companion object {
        private const val PICK_IMAGE_REQUEST = 1
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_personal_information)

        ivProfilePicture = findViewById(R.id.ivProfilePicture)
        usernameEditText = findViewById(R.id.profile_username)
        emailEditText = findViewById(R.id.profile_email)
        mobileEditText = findViewById(R.id.login_mobile_number)
        updateButton = findViewById(R.id.profle_update_btn)
        confirmPassword = findViewById(R.id.confirmPassword)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val userDocument = usersCollection.document(userId)

        countryCodePicker = findViewById(R.id.login_countrycode)
        countryCodePicker.setCountryForPhoneCode(63)

        userDocument.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val userData = documentSnapshot.data
                    val userProfileImageUrl = userData?.get("profileImageUrl") as String
                    Glide.with(this)
                        .load(userProfileImageUrl)
                        .into(ivProfilePicture)
                } else {
                    // Handle the case where user data does not exist
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors while fetching user data
            }

        updateButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val email = emailEditText.text.toString()
            val mobileNumber = mobileEditText.text.toString()
            val password = confirmPassword.text.toString()

            if (username.isBlank() || email.isBlank() || mobileNumber.isBlank() || password.isBlank()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                progressDialog?.dismiss()
            } else {
                showLoadingDialog()
                val selectedCountryCode = countryCodePicker.selectedCountryCode
                val fullMobileNumber = "+$selectedCountryCode$mobileNumber"
                updateUserData(username, email, fullMobileNumber, password, selectedImageUri)
            }
        }

        ivProfilePicture.setOnClickListener {
            val intent = Intent(Intent.ACTION_PICK)
            intent.type = "image/*"
            startActivityForResult(intent, PICK_IMAGE_REQUEST)
        }

        populateUserDataFromFirestore()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == Activity.RESULT_OK && data != null) {
            selectedImageUri = data.data
            Glide.with(this)
                .load(selectedImageUri)
                .into(ivProfilePicture)
        }
    }

    private fun uploadImageToStorage(imageUri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_pictures/$userId.jpg")

        imageRef.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                imageRef.downloadUrl.addOnSuccessListener { uri ->
                    updateProfileImageUrl(uri.toString())
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Image upload failed: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateProfileImageUrl(imageUrl: String) {
        val userDocRef = db.collection("users").document(userId)
        val updatedUserData = hashMapOf(
            "profileImageUrl" to imageUrl
        )

        userDocRef
            .update(updatedUserData as Map<String, Any>)
            .addOnSuccessListener {
                Toast.makeText(this, "Profile updated successfully", Toast.LENGTH_SHORT).show()
                val intent = Intent(this, DashboardActivity::class.java)
                startActivity(intent)
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, "Failed to update profile: ${e.message}", Toast.LENGTH_SHORT)
                    .show()
            }
    }

    private fun populateUserDataFromFirestore() {
        val user = auth.currentUser

        if (user != null) {
            val userDocRef = db.collection("users").document(user.uid)

            userDocRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val userData = documentSnapshot.toObject(Users::class.java)
                        if (userData != null) {
                            usernameEditText.setText(userData.username)
                            emailEditText.setText(userData.email)
                            val phoneNumber = userData.phoneNumber
                            if (phoneNumber != null) {
                                if (phoneNumber.length >= 4) {
                                    mobileEditText.setText(phoneNumber.substring(3))
                                } else {
                                    mobileEditText.setText(phoneNumber)
                                }
                            }
                        }
                    }
                }
                .addOnFailureListener { _ ->
                    Toast.makeText(this, "Failed to fetch user data", Toast.LENGTH_SHORT).show()
                }
        }
    }

    private fun updateUserData(
        username: String,
        email: String,
        mobileNumber: String,
        password: String,
        profilePictureUri: Uri?
    ) {
        val user = auth.currentUser

        if (user != null) {
            val credential = EmailAuthProvider.getCredential(user.email!!, password)
            user.reauthenticate(credential)
                .addOnSuccessListener {
                    val userDocRef = db.collection("users").document(user.uid)
                    val updatedUserData = hashMapOf(
                        "username" to username,
                        "email" to email,
                        "phoneNumber" to mobileNumber
                    )

                    userDocRef
                        .update(updatedUserData as Map<String, Any>)
                        .addOnSuccessListener {
                            if (profilePictureUri != null) {
                                uploadImageToStorage(profilePictureUri)
                            } else {
                                val intent = Intent(this, DashboardActivity::class.java)
                                startActivity(intent)
                                finish()
                                progressDialog?.dismiss()
                                Toast.makeText(
                                    this,
                                    "Profile updated successfully",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        }
                        .addOnFailureListener { e ->
                            Toast.makeText(
                                this,
                                "Failed to update profile: ${e.message}",
                                Toast.LENGTH_SHORT
                            ).show()
                            progressDialog?.dismiss()
                        }
                }
                .addOnFailureListener { e ->
                    Toast.makeText(
                        this,
                        "Reauthentication failed: ${e.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog?.dismiss()
                }
        } else {
            Toast.makeText(this, "User not authenticated", Toast.LENGTH_SHORT).show()
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(this)
        progressDialog?.setMessage("Updating your profile ...") // Set the message you want to display
        progressDialog?.setCancelable(false) // Prevents user from dismissing the dialog by tapping outside
        progressDialog?.show()
    }
}
