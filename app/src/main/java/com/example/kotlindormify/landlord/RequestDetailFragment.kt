package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.kotlindormify.R
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

// RequestDetailFragment.kt

class RequestDetailFragment : Fragment() {
    // Define views and variables to display details
    private lateinit var textViewFullName: TextView
    private lateinit var textViewAge: TextView
    private lateinit var textViewAddress: TextView
    private lateinit var textViewPhoneNumber: TextView
    private lateinit var textViewEmail: TextView
    private lateinit var textViewEmergencyFullName: TextView
    private lateinit var textViewEmergencyAddress: TextView
    private lateinit var textViewEmergencyPhoneNumber: TextView
    private lateinit var textViewEmergencyEmail: TextView
    private lateinit var textViewSelectedGender: TextView
    private lateinit var textViewIdImageUrl: TextView
    private lateinit var textViewStatus: TextView
    private lateinit var textViewTimestamp: TextView
    private lateinit var textViewRequesterId: TextView


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val rootView = inflater.inflate(R.layout.fragment_request_detail, container, false)
        textViewFullName = rootView.findViewById(R.id.textViewFullName)
        textViewAge = rootView.findViewById(R.id.textViewAge)
        textViewAddress = rootView.findViewById(R.id.textViewAddress)
        textViewPhoneNumber = rootView.findViewById(R.id.textViewPhoneNumber)
        textViewEmail = rootView.findViewById(R.id.textViewEmail)
        textViewEmergencyFullName = rootView.findViewById(R.id.textViewEmergencyFullName)
        textViewEmergencyAddress = rootView.findViewById(R.id.textViewEmergencyAddress)
        textViewEmergencyPhoneNumber = rootView.findViewById(R.id.textViewEmergencyPhoneNumber)
        textViewEmergencyEmail = rootView.findViewById(R.id.textViewEmergencyEmail)
        textViewSelectedGender = rootView.findViewById(R.id.textViewSelectedGender)
        textViewIdImageUrl = rootView.findViewById(R.id.textViewIdImageUrl)
        textViewStatus = rootView.findViewById(R.id.textViewStatus)
        textViewTimestamp = rootView.findViewById(R.id.textViewTimestamp)
        textViewRequesterId = rootView.findViewById(R.id.textViewrequesterId)
        val btnDeclineRequest = rootView.findViewById<Button>(R.id.btnDecline)
        val btnAcceptRequest = rootView.findViewById<Button>(R.id.btnAccept)

        // Retrieve the requestId passed from the list fragment
        val requestId = arguments?.getString("requestId")

        // Query Firebase Firestore to get details based on requestId
        // Populate the views with the retrieved details


        if (requestId != null) {
            // Assuming you have a Firestore instance
            val firestore = FirebaseFirestore.getInstance()

            // Reference to the "rental_requests" collection
            val rentalRequestRef = firestore.collection("rental_requests").document(requestId)

            // Query Firestore to get the rental request details
            rentalRequestRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        // Retrieve the data from the document
                        val requesterId = documentSnapshot.getString("requesterId")
                        val requesterFullName = documentSnapshot.getString("requesterFullName")
                        val age = documentSnapshot.getString("age")
                        val address = documentSnapshot.getString("address")
                        val phoneNumber = documentSnapshot.getString("phoneNumber")
                        val email = documentSnapshot.getString("email")
                        val emergencyFullName = documentSnapshot.getString("emergencyFullName")
                        val emergencyAddress = documentSnapshot.getString("emergencyAddress")
                        val emergencyPhoneNumber =
                            documentSnapshot.getString("emergencyPhoneNumber")
                        val emergencyEmail = documentSnapshot.getString("emergencyEmail")
                        val selectedGender = documentSnapshot.getString("gender")
                        val idImageUrl = documentSnapshot.getString("idImageUrl")
                        val status = documentSnapshot.getString("status")
                        val timestamp = documentSnapshot.getTimestamp("timestamp")

                        // Now, you can use these values to populate your UI elements
                        textViewFullName.text = " $requesterFullName"
                        textViewAge.text = " $age"
                        textViewAddress.text = " $address"
                        textViewPhoneNumber.text = " $phoneNumber"
                        textViewEmail.text = " $email"
                        textViewEmergencyFullName.text = " $emergencyFullName"
                        textViewEmergencyAddress.text = " $emergencyAddress"
                        textViewEmergencyPhoneNumber.text = " $emergencyPhoneNumber"
                        textViewEmergencyEmail.text = " $emergencyEmail"
                        textViewSelectedGender.text = " $selectedGender"
                        textViewIdImageUrl.text = " $idImageUrl"
                        textViewStatus.text = " $status"
                        textViewTimestamp.text = " $timestamp"
                        textViewRequesterId.text = requesterId
                        if (status != "pending") {
                            btnDeclineRequest.visibility = View.GONE
                            btnAcceptRequest.visibility = View.GONE
                        }

                    } else {
                        // Handle the case where the document does not exist
                    }
                }

                .addOnFailureListener { exception ->
                    // Handle failures, such as Firestore errors
                }
        }


        btnDeclineRequest.setOnClickListener {
            // Create a confirmation dialog
            val alertDialog = AlertDialog.Builder(requireContext())
            alertDialog.setTitle("Decline Request")
            alertDialog.setMessage("Are you sure you want to decline this request?")

            alertDialog.setPositiveButton("Yes") { _, _ ->
                // User confirmed, update the request
                val requestId = arguments?.getString("requestId")
                val firestore = FirebaseFirestore.getInstance()

                if (requestId != null) {
                    val rentalRequestRef =
                        firestore.collection("rental_requests").document(requestId)

                    val updateData = mapOf(
                        "status" to "declined",
                        "declineDate" to FieldValue.serverTimestamp()
                    )

                    rentalRequestRef.update(updateData)
                        .addOnSuccessListener {
                            // Handle the successful update, such as navigating back to the previous fragment

                            val dormitoryId = arguments?.getString("dormitoryId")
                            if (dormitoryId != null) {
                                val dormitoryRequestsRef =
                                    firestore.collection("dormitories").document(dormitoryId)
                                        .collection("rental_requests")
                                dormitoryRequestsRef.document(requestId)
                                    .update(updateData)
                                    .addOnSuccessListener {
                                        // Successfully updated the dormitory with the declined request
                                        Toast.makeText(
                                            requireContext(),
                                            "Request has been declined",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                        requireActivity().supportFragmentManager.popBackStack()
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle the failure to update the dormitory's rental request subcollection
                                    }
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle failures, such as Firestore errors
                        }
                }
            }

            alertDialog.setNegativeButton("No") { _, _ ->
                // User canceled the decline action, do nothing or provide feedback
            }

            alertDialog.show()
        }

        btnAcceptRequest.setOnClickListener {
            // Check if the status is "pending" (only allow accepting if the request is pending)
            // Navigate to the AssignTenantFragment and pass the selected request details
            val args = Bundle()
            val dormitoryId = arguments?.getString("dormitoryId")
            val requesterFullName = textViewFullName.text.toString()
            val requesterId = textViewRequesterId.text.toString()
            args.putString("requestId", requestId)

            args.putString("requesterFullName", requesterFullName)
            args.putString("requesterId", requesterId)
            args.putString("dormitoryId", dormitoryId)


            val assignTenantFragment = AssignTenantFragment()
            assignTenantFragment.arguments = args

            // Use FragmentManager to navigate to the AssignTenantFragment
            val fragmentManager = requireActivity().supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, assignTenantFragment)
                .addToBackStack(null)
                .commit()

        }





















        return rootView


    }
}
