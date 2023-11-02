package com.example.kotlindormify

import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
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

class RequestRentFragment : Fragment() {
    private lateinit var binding: FragmentRequestRentBinding
    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false
    private var progressDialog: ProgressDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRequestRentBinding.inflate(inflater, container, false)
        val dormName = arguments?.getString("dormName")
        val dormitoryId = arguments?.getString("dormitoryId")

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
        }

        binding.btnAddImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Picture"), PICK_IMAGE_REQUEST)
        }

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
                selectedGenderPosition != 0) {
                showLoadingDialog()

                if (isImageSelected) {
                    if (cbAgreement.isChecked) {
                        val selectedGender = binding.spinnerGender.selectedItem.toString()

                        // Create a unique request ID
                        val requestId = UUID.randomUUID().toString()

                        // Upload the image to Firebase Storage
                        val storageRef = storage.reference.child("rental_requests_id").child("$requestId.jpg")
                        val uploadTask = storageRef.putFile(selectedImageUri)
                        uploadTask.addOnSuccessListener { _ ->
                            // Get the download URL for the uploaded image
                            storageRef.downloadUrl.addOnSuccessListener { downloadUri ->
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
                                    "idImageUrl" to downloadUri.toString()
                                )

                                val doubleCheckDialog = AlertDialog.Builder(requireContext())
                                    .setTitle("Double Check")
                                    .setMessage("Please double-check your information before submitting.\n\n" +
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
                                                    firestore.collection("dormitories").document(dormitoryId!!)
                                                        .collection("rental_requests")
                                                dormitoryRequestsRef.document(requestId)
                                                    .set(mapOf("timestamp" to FieldValue.serverTimestamp(), "status" to "pending", "requesterFullName" to requesterFullName, "dormitoryId" to dormitoryId))
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
                            }
                        }
                    } else {
                        Toast.makeText(requireContext(), "Please agree to the terms and conditions.", Toast.LENGTH_SHORT).show()
                        progressDialog?.dismiss()
                    }
                } else {
                    Toast.makeText(requireContext(), "Please upload an image.", Toast.LENGTH_SHORT).show()
                    progressDialog?.dismiss()
                }
            } else {
                Toast.makeText(requireContext(), "Please fill in all fields and select a gender.", Toast.LENGTH_SHORT).show()
                progressDialog?.dismiss()
            }
        }

        return binding.root
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedImageUri = data.data!!
            Glide.with(this)
                .load(selectedImageUri)
                .into(binding.ivId)
            isImageSelected = true
        }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Creating your request ...") // Set the message you want to display
        progressDialog?.setCancelable(false) // Prevents user from dismissing the dialog by tapping outside
        progressDialog?.show()
    }
}
