package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.fragment.app.Fragment
import com.example.kotlindormify.R
import com.example.kotlindormify.databinding.LandlordAddTenantFragmentBinding
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query


class LandlordAddTenantFragment : Fragment() {
    private lateinit var binding: LandlordAddTenantFragmentBinding
    private var landlordId: String? = null


    private val firestore = FirebaseFirestore.getInstance()
    private val usersRef = firestore.collection("users")
    private val dormitoriesRef = firestore.collection("dormitories")
    private val tenantsRef = firestore.collection("tenants")


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LandlordAddTenantFragmentBinding.inflate(inflater, container, false)

        // Initialize views


        // Fetch the landlord's user ID using the user's email
        val activity = requireActivity() as LandlordDashboardActivity
        val userEmail = activity.userEmail

        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    for (document in querySnapshot.documents) {
                        // You can access the userId from the document
                        landlordId = document.id
                        Toast.makeText(
                            requireContext(),
                            "$landlordId $userEmail",
                            Toast.LENGTH_SHORT
                        ).show()

                    }
                } else {
                    println("No user found with the provided email.")
                }
            }
            .addOnFailureListener { e ->
                println("Error searching for user: $e")
            }





        binding.btnAddTenant.setOnClickListener {
            // Retrieve tenant data from the input fields
            val selectedDormitory = binding.spinnerDormName.selectedItem.toString()
            val selectedRoomNumber = binding.spinnerRoomNo.selectedItem.toString()
            val tenantName = binding.etTenantName.text.toString()
            val tenantEmail = binding.etTenantEmail.text.toString()
            val tenantAddress = binding.etTenantAddress.text.toString()
            val tenantPhoneNumber = binding.etTenantPhoneNumber.text.toString()

            // Validate tenant data and add the tenant to Firestore
            if (validateTenantData(
                    selectedDormitory,
                    selectedRoomNumber,
                    tenantName,
                    tenantEmail,
                    tenantAddress,
                    tenantPhoneNumber
                )
            ) {
                addTenantToFirestore(
                    selectedDormitory,
                    selectedRoomNumber,
                    tenantName,
                    tenantEmail,
                    tenantAddress,
                    tenantPhoneNumber
                )
            }
        }

        return view
    }

    private fun validateTenantData(
        selectedDormitory: String,
        selectedRoomNumber: String,
        tenantName: String,
        tenantEmail: String,
        tenantAddress: String,
        tenantPhoneNumber: String
    ): Boolean {
        // Implement your validation logic here
        // Ensure that all fields are filled and tenantEmail is valid
        // Return true if data is valid, false otherwise
        return true
    }

    private fun addTenantToFirestore(
        selectedDormitory: String,
        selectedRoomNumber: String,
        tenantName: String,
        tenantEmail: String,
        tenantAddress: String,
        tenantPhoneNumber: String
    ) {
        // Implement adding tenant data to Firestore here
        // You can use selectedDormitory and selectedRoomNumber to determine the location
        // of the tenant's data in Firestore, and tenantName, tenantEmail, tenantAddress,
        // tenantPhoneNumber to populate the tenant's document.
    }
}
