package com.example.kotlindormify

import android.app.ProgressDialog
import android.content.Intent
import android.graphics.Color
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.ArrayAdapter
import android.widget.ProgressBar
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.FragmentManager
import com.bumptech.glide.Glide
import com.example.kotlindormify.databinding.ActivitySignUpTenantBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.storage.FirebaseStorage
import java.io.IOException
import java.util.UUID

class SignUpTenantActivity : AppCompatActivity() {


    private lateinit var binding: ActivitySignUpTenantBinding
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private val PICK_IMAGE_REQUEST = 1
    private var isImageSelected = false
    private lateinit var selectedImageUri: Uri

    private val PICK_IM_IMAGE_REQUEST2 = 2
    private lateinit var selectedImageUri2: Uri
    private var isImageSelected2 = false

    private var progressDialog: ProgressDialog? = null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySignUpTenantBinding.inflate(layoutInflater)
        setContentView(binding.root)
        supportActionBar?.hide()



        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()


        binding.textView.setOnClickListener {
            val intent = Intent(this, SignInActivity::class.java)
            startActivity(intent)
            finish()
        }

        binding.btnAddImage2.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IM_IMAGE_REQUEST2
            )
        }

        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()
        binding.loginCountrycode.setCountryForPhoneCode(63)
        binding.loginCountrycode2.setCountryForPhoneCode(63)

        val genderAdapter = ArrayAdapter.createFromResource(
            this,
            R.array.gender_choices,
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGendertenant.adapter = genderAdapter

        binding.imageView.setOnClickListener {
            super.onBackPressed()
        }



        binding.buttonTenant.setOnClickListener {
            val email = binding.etEmail.text.toString()
            val password = binding.passET.text.toString()
            val confirmPassword = binding.confirmPassEt.text.toString()
            val username = binding.etFullName.text.toString()
            val progressBar = binding.progressBar
            val cbAgreement = binding.cbAgreement
            val phoneNumber = binding.etPhoneNumber.text.toString()

            val age = binding.etAge.text.toString()
            val username2 = binding.etFullName.text.toString()
            val email2 = binding.etEmail.text.toString()
            val address = binding.etAddress2.text.toString()
            val fullName = binding.etEmergencyFullName.text.toString()
            val emAddress = binding.etEmergencyAddress.text.toString()
            val emphoneNumber = binding.etEmergencyPhoneNumber.text.toString()
            val emEmail = binding.etEmergencyEmail.text.toString()
            val selectedGenderPosition = binding.spinnerGendertenant.selectedItemPosition



            if (username.isEmpty()) {
                binding.etFullNameLayout.error = "Full name is required"
                binding.lblFullName.text = "Full name is required" // Set error message in lblFullName
                binding.lblFullName2.visibility = View.VISIBLE
            } else {
                binding.etFullNameLayout.error = null // Clear the error if not empty
                binding.lblFullName.text = "Full Name" // Clear the error message in lblFullName
                binding.lblFullName2.visibility = View.INVISIBLE
            }
            if (age.isEmpty()) {
                binding.etAgeLayout.error = "Age is required"
                binding.lblAge.text = "Age is required" // Set error message in lblFullName
                binding.lblAge2.visibility = View.VISIBLE
            } else {
                binding.etAgeLayout.error = null // Clear the error if not empty
                binding.lblAge.text = "Age" // Clear the error message in lblFullName
                binding.lblAge2.visibility = View.INVISIBLE
            }
            if (address.isEmpty()) {
                binding.etAddressLayout.error = "Address is required"
                binding.lblAddress.text = "Address is required" // Set error message in lblFullName
                binding.lblAddress2.visibility = View.VISIBLE
            } else {
                binding.etAddressLayout.error = null // Clear the error if not empty
                binding.lblAddress.text = "Address" // Clear the error message in lblFullName
                binding.lblAddress2.visibility = View.INVISIBLE
            }
            if (phoneNumber.isEmpty()) {
                binding.etPhoneNumberLayout.error = "Phone number is required"
                binding.lbletPhoneNumber.text = "Phone Number is required" // Set error message in lblFullName
                binding.lbletPhoneNumber2.visibility = View.VISIBLE
            } else {
                binding.etPhoneNumberLayout.error = null // Clear the error if not empty
                binding.lbletPhoneNumber.text = "Phone Number" // Clear the error message in lblFullName
                binding.lbletPhoneNumber2.visibility = View.INVISIBLE
            }
            if (email.isEmpty()) {
                binding.etEmailLayout.error = "Email is required"
                binding.lbletEmail.text = "Email is required" // Set error message in lblFullName
                binding.lbletEmail2.visibility = View.VISIBLE
            } else {
                binding.etEmailLayout.error = null // Clear the error if not empty
                binding.lbletEmail.text = "Email" // Clear the error message in lblFullName
                binding.lbletEmail2.visibility = View.INVISIBLE
            }
            if (password.isEmpty()) {
                binding.passwordLayout.error = "Password is required"
                binding.lbletpassword.text = "Password is required" // Set error message in lblFullName
                binding.lbletpassword2.visibility = View.VISIBLE
            } else {
                binding.passwordLayout.error = null // Clear the error if not empty
                binding.lbletpassword.text = "Password" // Clear the error message in lblFullName
                binding.lbletpassword2.visibility = View.INVISIBLE
            }
            if (confirmPassword.isEmpty()) {
                binding.confirmPasswordLayout.error = "Confirm Password is required"
                binding.lbletrepassword.text = "Confirm Password is required" // Set error message in lblFullName
                binding.lbletrepassword2.visibility = View.VISIBLE
            } else {
                binding.confirmPasswordLayout.error = null // Clear the error if not empty
                binding.lbletrepassword.text = "Confirm Password" // Clear the error message in lblFullName
                binding.lbletrepassword2.visibility = View.INVISIBLE
            }

            if (selectedGenderPosition == 0 ) {
                binding.etGenderLayout.error = "Sex is required"
                binding.lblGender.text = "Sex is required" // Set error message in lblFullName
                binding.Gender2.visibility = View.VISIBLE
            } else {
                binding.etGenderLayout.error = null // Clear the error if not empty
                binding.lblGender.text = "Sex" // Clear the error message in lblFullName
                binding.Gender2.visibility = View.INVISIBLE
            }


            if (fullName.isEmpty()) {
                binding.etEmergencyFullNameLayout.error = "Full name is required"
                binding.lbletEmergencyFullName.text = "Full name is required" // Set error message in lblFullName
                binding.lbletEmergencyFullName2.visibility = View.VISIBLE
            } else {
                binding.etEmergencyFullNameLayout.error = null // Clear the error if not empty
                binding.lbletEmergencyFullName.text = "Full Name" // Clear the error message in lblFullName
                binding.lbletEmergencyFullName2.visibility = View.INVISIBLE
            }
            if (emAddress.isEmpty()) {
                binding.etEmergencyAddressLayout.error = "Address is required"
                binding.lbletEmergencyAddress.text = "Address is required" // Set error message in lblFullName
                binding.lbletEmergencyAddress2.visibility = View.VISIBLE
            } else {
                binding.etEmergencyAddressLayout.error = null // Clear the error if not empty
                binding.lbletEmergencyAddress.text = "Address" // Clear the error message in lblFullName
                binding.lbletEmergencyAddress2.visibility = View.INVISIBLE

            }
            if (emphoneNumber.isEmpty()) {
                binding.etEmergencyPhoneNumberLayout.error = "Phone Number is required"
                binding.lbletEmergencyPhoneNumber.text = "Phone Number is required" // Set error message in lblFullName
                binding.lbletEmergencyPhoneNumber2.visibility = View.VISIBLE
            } else {
                binding.etEmergencyPhoneNumberLayout.error = null // Clear the error if not empty
                binding.lbletEmergencyPhoneNumber.text = "Phone Numberd" // Clear the error message in lblFullName
                binding.lbletEmergencyPhoneNumber2.visibility = View.INVISIBLE

            }
            if (emEmail.isEmpty()) {
                binding.etEmergencyEmailLayout.error = "Email is required"
                binding.lbletEmergencyEmail.text = "Email is required" // Set error message in lblFullName
                binding.lbletEmergencyEmail2.visibility = View.VISIBLE
            } else {
                binding.etEmergencyEmailLayout.error = null // Clear the error if not empty
                binding.lbletEmergencyEmail.text = "Email" // Clear the error message in lblFullName
                binding.lbletEmergencyEmail2.visibility = View.INVISIBLE
            }

            // Check if an image has been selected
            if (!isImageSelected) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                binding.btnAddImage.setBackgroundResource(R.drawable.rectangle_radius_white_stroke_blackerror)
                binding.lblid.text = "Upload your Student I.D or COR below is required" // Set error message in lblFullName
                binding.lblid2.visibility = View.VISIBLE
            }else{
                binding.btnAddImage.setBackgroundResource(R.drawable.rectangle_radius_white_stroke_black)
                binding.lblid.text = "Upload your Student ID or COR below" // Clear the error message in lblFullName
                binding.lblid2 .visibility = View.INVISIBLE

            }

            if (!isImageSelected2) {
                Toast.makeText(this, "Please fill all fields.", Toast.LENGTH_SHORT).show()
                binding.btnAddImage2.setBackgroundResource(R.drawable.rectangle_radius_white_stroke_blackerror)
                binding.lbletprof.text = "Upload profile is required" // Set error message in lblFullName
                binding.lbletprof2.visibility = View.VISIBLE
                return@setOnClickListener
            }
            else{
                binding.btnAddImage2.setBackgroundResource(R.drawable.rectangle_radius_white_stroke_black)
                binding.lbletprof.text = "Upload profile" // Clear the error message in lblFullName
                binding.lbletprof2.visibility = View.INVISIBLE
            }
            if (!cbAgreement.isChecked) {
                Toast.makeText(
                    this,
                    "Please agree to the terms and conditions to continue",
                    Toast.LENGTH_SHORT
                ).show()
                return@setOnClickListener // Prevent further execution of the click listener
            }



            if (email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty() && username2.isNotEmpty() && age.isNotEmpty() && address.isNotEmpty() && email2.isNotEmpty() &&
                fullName.isNotEmpty() && emAddress.isNotEmpty() && emphoneNumber.isNotEmpty() && emEmail.isNotEmpty() &&
                selectedGenderPosition != 0 ) {
                showLoadingDialog()

                if(password.length >= 6){


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

                                                val enteredPhoneNumber = binding.etPhoneNumber.text.toString()
                                                val selectedRole = when (binding.rgRole.checkedRadioButtonId) {
                                                    R.id.rbTenant -> 1 // Tenant
                                                    R.id.rbLandlord -> 2 // Dorm Landlord
                                                    else -> 0 // Default or no selection
                                                }


                                                binding.loginCountrycode.setCountryForPhoneCode(63)
                                                val selectedCountryCode = binding.loginCountrycode.selectedCountryCode
                                                val phoneNumber = "+$selectedCountryCode$enteredPhoneNumber"

                                                val selectedGender = binding.spinnerGendertenant.selectedItem.toString()

                                                // Create a unique request ID
                                                val potentialId = UUID.randomUUID().toString()
                                                val enteredEmPhoneNumber = binding.etEmergencyPhoneNumber.text.toString()

                                                // Upload the image to Firebase Storage
                                                val storageRef = storage.reference.child("potential_tenant_details_id").child("$potentialId.jpg")
                                                val uploadTask = storageRef.putFile(selectedImageUri2)





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
                                                                        progressBar.visibility = ProgressBar.GONE
                                                                        progressDialog?.dismiss()
                                                                    }
                                                                    .addOnFailureListener { e ->
                                                                        Toast.makeText(this, "Registration failed. Please try again.", Toast.LENGTH_SHORT).show()
                                                                        progressDialog?.dismiss()
                                                                    }

                                                                uploadTask.addOnSuccessListener { _ ->
                                                                    // Get the download URL for the uploaded image
                                                                    storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                                                                        val selectedCountryCode = binding.loginCountrycode2.selectedCountryCode
                                                                        val phoneNumber2 = "+$selectedCountryCode$enteredEmPhoneNumber"
                                                                        // Store the download URL and userId in the potentialTenant data
                                                                        val potentialTenantData = hashMapOf(
                                                                            "userId" to FirebaseAuth.getInstance().currentUser?.uid, // Store the retrieved userId
                                                                            "timestamp" to FieldValue.serverTimestamp(),
                                                                            // Include other fields in your data here
                                                                            "requesterFullName" to username2,
                                                                            "age" to age,
                                                                            "address" to address,
                                                                            "phoneNumber" to phoneNumber,
                                                                            "email" to email2,
                                                                            "emergencyFullName" to fullName,
                                                                            "emergencyAddress" to emAddress,
                                                                            "emergencyPhoneNumber" to emphoneNumber,
                                                                            "emergencyEmail" to emEmail,
                                                                            "gender" to selectedGender,
                                                                            "idImageUrl" to downloadUri.toString()
                                                                        )

                                                                        // Store the rental request in Firestore under the document with potentialId
                                                                        firestore.collection("potential_tenant_details")
                                                                            .document(potentialId)
                                                                            .set(potentialTenantData)
                                                                            .addOnSuccessListener {
                                                                                progressBar.visibility = ProgressBar.GONE
                                                                                val intent = Intent(this, SignInActivity::class.java)
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
                                                                                // Handle failure to store rental request data
                                                                                Toast.makeText(this, "Failed to store potential tenant details data", Toast.LENGTH_SHORT).show()
                                                                                progressDialog?.dismiss()
                                                                            }
                                                                    }
                                                                }




                                                            } else {
                                                                Toast.makeText(this, "Full Name already taken. Please choose a different one.", Toast.LENGTH_SHORT).show()
                                                                binding.etFullNameLayout.error = "Full Name already exist!"
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
                                Toast.makeText(this, "Email already exist!. Please try again.", Toast.LENGTH_SHORT).show()
                                binding.etEmailLayout.error = "!"
                                progressDialog?.dismiss()
                            }
                        }




                } else {
                    Toast.makeText(this, "Password is not matching", Toast.LENGTH_SHORT).show()
                    binding.confirmPasswordLayout.error = "!"
                    binding.passwordLayout.error = "!"
                    progressDialog?.dismiss()
                }
                }else{
                    Toast.makeText(this, "Password must be at least 6 characters.", Toast.LENGTH_SHORT).show()
                    binding.confirmPasswordLayout.error = "!"
                    binding.passwordLayout.error = "!"
                    progressDialog?.dismiss() }
            } else {
                Toast.makeText(this, "Please fill up all fields.", Toast.LENGTH_SHORT).show()
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

        if (requestCode == PICK_IM_IMAGE_REQUEST2 && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri2 = data.data!!
            Glide.with(this)
                .load(selectedImageUri2)
                .into(binding.ivId)
            isImageSelected2 = true
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

