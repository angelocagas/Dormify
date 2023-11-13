package com.example.kotlindormify

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kotlindormify.databinding.FragmentRequestRentBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.text.SimpleDateFormat
import java.util.*
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import java.io.File

class RequestRentFragment : Fragment() {
    private lateinit var binding: FragmentRequestRentBinding
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false
    private var progressDialog: ProgressDialog? = null
    private lateinit var idImageUrl: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRequestRentBinding.inflate(inflater, container, false)
        val dormName = arguments?.getString("dormName")
        val dormitoryId = arguments?.getString("dormitoryId")

        binding.loginCountrycode.setCountryForPhoneCode(63)
        binding.loginCountrycode.setCcpClickable(false)

        val firestore = FirebaseFirestore.getInstance()
        val storage = FirebaseStorage.getInstance()

        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.imageView.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        val genderAdapter = ArrayAdapter.createFromResource(
            requireContext(),
            R.array.gender_choices,
            android.R.layout.simple_spinner_item
        )
        genderAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerGender.adapter = genderAdapter

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            binding.etEmail.setText(currentUser.email)
            // Query Firestore to get user data based on the current user ID
            firestore.collection("potential_tenant_details")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Check if any documents were found
                    if (!querySnapshot.isEmpty) {
                        // Get the first document (assuming there's only one document per user)
                        val documentSnapshot = querySnapshot.documents[0]

                        // Retrieve data from the document
                        val requesterFullName = documentSnapshot.getString("requesterFullName")
                        val age = documentSnapshot.getString("age")
                        val address = documentSnapshot.getString("address")
                        val phoneNumber = documentSnapshot.getString("phoneNumber")
                        val email = documentSnapshot.getString("email")
                        val emergencyFullName = documentSnapshot.getString("emergencyFullName")
                        val emergencyAddress = documentSnapshot.getString("emergencyAddress")
                        val emergencyPhoneNumber = documentSnapshot.getString("emergencyPhoneNumber")
                        val emergencyEmail = documentSnapshot.getString("emergencyEmail")
                        val selectedGender = documentSnapshot.getString("gender")
                        idImageUrl = documentSnapshot.getString("idImageUrl").toString()

                        // Set the retrieved data to the respective EditText fields
                        binding.etFullName.setText(requesterFullName)
                        binding.etAge.setText(age)
                        binding.etAddress2.setText(address)
                        binding.etPhoneNumber.setText(phoneNumber?.substring(3))
                        binding.etEmail.setText(email)
                        binding.etEmergencyFullName.setText(emergencyFullName)
                        binding.etEmergencyAddress.setText(emergencyAddress)
                        binding.etEmergencyPhoneNumber.setText(emergencyPhoneNumber)
                        binding.etEmergencyEmail.setText(emergencyEmail)

                        binding.etFullName.isEnabled = false
                        binding.etAge.isEnabled = false
                        binding.etAddress2.isEnabled = false
                        binding.etPhoneNumber.isEnabled = false
                        binding.etEmail.isEnabled = false
                        binding.etEmergencyFullName.isEnabled = false
                        binding.etEmergencyAddress.isEnabled = false
                        binding.etEmergencyPhoneNumber.isEnabled = false
                        binding.etEmergencyEmail.isEnabled = false


                        binding.spinnerGender.isEnabled = false

                        if (idImageUrl != null) {
                            // Load the selected image into the ImageView using Glide
                            Glide.with(requireContext())
                                .load(idImageUrl)
                                .error(R.drawable.error_image) // Optional error image
                                .into(binding.ivId)

                            // Set the flag to indicate that an image is selected
                            isImageSelected = true
                        } else {
                            isImageSelected = false
                        }

                        // Set the selected gender in the spinner
                        val genderArray = resources.getStringArray(R.array.gender_choices)
                        val genderIndex = genderArray.indexOf(selectedGender)
                        binding.spinnerGender.setSelection(genderIndex)


                    } else {
                        // Handle the case where no data is found for the current user
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the failure to retrieve user data
                    Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
        }


/*
        binding.btnAddImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(
                Intent.createChooser(intent, "Select Picture"),
                PICK_IMAGE_REQUEST
            )
        }
        */
 */

        binding.btnSubmit.setOnClickListener {
            val requesterFullName = binding.etFullName.text.toString()
            val age = binding.etAge.text.toString()
            val address = binding.etAddress2.text.toString()
            val phoneNumber = binding.etPhoneNumber.text.toString()
            val email = binding.etEmail.text.toString()
            val emergencyFullName = binding.etEmergencyFullName.text.toString()
            val emergencyAddress = binding.etEmergencyAddress.text.toString()
            val emergencyPhoneNumber = binding.etEmergencyPhoneNumber.text.toString()
            val emergencyEmail = binding.etEmergencyEmail.text.toString()
            val cbAgreement = binding.cbAgreement
            val selectedGenderPosition = binding.spinnerGender.selectedItemPosition

            if (requesterFullName.isNotEmpty() && age.isNotEmpty() && address.isNotEmpty() &&
                phoneNumber.isNotEmpty() && email.isNotEmpty() && emergencyFullName.isNotEmpty() &&
                emergencyAddress.isNotEmpty() && emergencyPhoneNumber.isNotEmpty() && emergencyEmail.isNotEmpty() &&
                selectedGenderPosition != 0
            ) {

                showLoadingDialog()

                if (isImageSelected) {
                    if (cbAgreement.isChecked) {
                        val selectedGender = binding.spinnerGender.selectedItem.toString()

                        // Create a unique request ID
                        val requestId = UUID.randomUUID().toString()

                        // Use Glide to download the image and then upload it to Firebase Storage
                        Glide.with(requireContext())
                            .asFile()
                            .load(idImageUrl)
                            .into(object : CustomTarget<File>() {
                                override fun onResourceReady(resource: File, transition: Transition<in File>?) {
                                    // Upload the file to Firebase Storage
                                    val storageRef =
                                        storage.reference.child("rental_requests_id").child("$requestId.jpg")

                                    storageRef.putFile(Uri.fromFile(resource)).addOnSuccessListener { _ ->
                                        // Continue with the rest of your success logic
                                        // Store the download URL in the rental request data
                                        val rentalRequestData = hashMapOf(
                                            "requesterId" to FirebaseAuth.getInstance().currentUser?.uid,
                                            "dormitoryId" to dormitoryId,
                                            "timestamp" to FieldValue.serverTimestamp(),
                                            "status" to "pending",
                                            "requesterFullName" to requesterFullName,
                                            "age" to age,
                                            "address" to address,
                                            "phoneNumber" to phoneNumber,
                                            "email" to email,
                                            "emergencyFullName" to emergencyFullName,
                                            "emergencyAddress" to emergencyAddress,
                                            "emergencyPhoneNumber" to emergencyPhoneNumber,
                                            "emergencyEmail" to emergencyEmail,
                                            "gender" to selectedGender,
                                            "idImageUrl" to idImageUrl
                                        )

                                        val doubleCheckDialog = AlertDialog.Builder(requireContext())
                                            .setTitle("Double Check")
                                            .setMessage(
                                                "Please double-check your information before submitting.\n\n" +
                                                        "Full Name: $requesterFullName\n" +
                                                        "Age: $age\n" +
                                                        "Gender: $selectedGender\n" +
                                                        "Address: $address\n" +
                                                        "Phone Number: $phoneNumber\n" +
                                                        "Email: $email\n" +
                                                        "Emergency Contact Name: $emergencyFullName\n" +
                                                        "Emergency Contact Address: $emergencyAddress\n" +
                                                        "Emergency Contact Phone: $emergencyPhoneNumber\n" +
                                                        "Emergency Contact Email: $emergencyEmail\n"
                                            )
                                            .setPositiveButton("Submit") { _, _ ->
                                                // Store the rental request in Firestore under the document with requestId
                                                firestore.collection("rental_requests")
                                                    .document(requestId)
                                                    .set(rentalRequestData)
                                                    .addOnSuccessListener {
                                                        // Successfully stored rental request in Firestore
                                                        val successDialog =
                                                            AlertDialog.Builder(requireContext())
                                                                .setTitle("Success!")
                                                                .setMessage("You've sent a rental request for $dormName.")
                                                                .setPositiveButton("OK") { _, _ ->
                                                                    requireActivity().supportFragmentManager.popBackStack()
                                                                }
                                                                .create()
                                                        progressDialog?.dismiss()
                                                        successDialog.show()

                                                        Toast.makeText(
                                                            requireContext(),
                                                            "Request submitted successfully.",
                                                            Toast.LENGTH_SHORT
                                                        ).show()

                                                        // Add the requestId to the dormitory's rental_requests subcollection
                                                        val dormitoryRequestsRef =
                                                            firestore.collection("dormitories")
                                                                .document(dormitoryId!!)
                                                                .collection("rental_requests")
                                                        dormitoryRequestsRef.document(requestId)
                                                            .set(
                                                                mapOf(
                                                                    "timestamp" to FieldValue.serverTimestamp(),
                                                                    "status" to "pending",
                                                                    "requesterFullName" to requesterFullName,
                                                                    "dormitoryId" to dormitoryId
                                                                )
                                                            )
                                                            .addOnSuccessListener {
                                                                // Successfully updated dormitory with rental request
                                                                // Optionally, navigate to a success screen or perform other actions
                                                            }
                                                            .addOnFailureListener { e ->
                                                                // Handle the failure to update the dormitory document
                                                            }
                                                    }
                                                    .addOnFailureListener { e ->
                                                        // Handle the failure to store the rental request
                                                    }
                                            }
                                            .setNegativeButton("Cancel", null)
                                            .create()
                                        progressDialog?.dismiss()
                                        doubleCheckDialog.show()
                                    }.addOnFailureListener { e ->
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to upload image: $e",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }

                                override fun onLoadCleared(placeholder: Drawable?) {
                                    // Not used in this example
                                }
                            })
                    } else {

                        AlertDialog.Builder(requireContext())
                            .setMessage("Please agree to the terms and conditions to continue.")
                            .setPositiveButton("OK") { dialog, _ ->
                                dialog.dismiss()
                            }
                            .show()

                        progressDialog?.dismiss()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please upload an image.", Toast.LENGTH_SHORT)
                        .show()
                    progressDialog?.dismiss()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please fill in all fields and select a gender.",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog?.dismiss()
            }
        }


        return binding.root
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Creating your request ...") // Set the message you want to display
        progressDialog?.setCancelable(false) // Prevents user from dismissing the dialog by tapping outside
        progressDialog?.show()
    }
}
