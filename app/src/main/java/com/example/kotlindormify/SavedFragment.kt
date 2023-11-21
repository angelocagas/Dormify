package com.example.kotlindormify

import android.content.pm.PackageManager
import android.os.Bundle
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.Button // Import Button class
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.landlord.Dormitory
import com.google.firebase.firestore.FirebaseFirestore

class SavedFragment : Fragment(R.layout.saved_fragment) {




    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)



        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        // Initialize the RecyclerViews and adapters
        val allDormitoriesRecyclerView = view.findViewById<RecyclerView>(R.id.rvAllDormitories)
        val savedDormitoriesRecyclerView = view.findViewById<RecyclerView>(R.id.rvSavedDormitories)

        val allDormitoriesLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        allDormitoriesRecyclerView.layoutManager = allDormitoriesLayoutManager

        val savedDormitoriesLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        savedDormitoriesRecyclerView.layoutManager = savedDormitoriesLayoutManager

        val allDormitoriesList: MutableList<Dormitory> = mutableListOf()
        val savedDormitoriesList: MutableList<Dormitory> = mutableListOf()

        val allDormitoriesAdapter = AllDormitoriesAdapter(allDormitoriesList)
        val savedDormitoriesAdapter = AllDormitoriesAdapter(savedDormitoriesList)

        allDormitoriesRecyclerView.adapter = allDormitoriesAdapter
        savedDormitoriesRecyclerView.adapter = savedDormitoriesAdapter

        val activity = requireActivity() as DashboardActivity
        val currentUserEmail = activity.userEmail

        // Fetch saved dormitory IDs for the current user
        if (currentUserEmail != null) {
            fetchSavedDormitoryIds(currentUserEmail) { savedDormitoryIds ->
                // Query dormitory details based on saved IDs
                queryDormitoryDetails(savedDormitoryIds) { savedDormitories ->
                    // Add the retrieved saved dormitories to the list with reversing
                    savedDormitoriesList.clear()
                    savedDormitoriesList.addAll(savedDormitories.reversed())
                    savedDormitoriesAdapter.notifyDataSetChanged()
                }
            }
        }


        // Query and display all dormitories
        queryAllDormitories { allDormitories ->
            allDormitoriesList.addAll(allDormitories)
            allDormitoriesAdapter.notifyDataSetChanged()
        }




    }




    private fun queryAllDormitories(callback: (List<Dormitory>) -> Unit) {
        // Query all dormitory details
        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        dormitoriesRef
            .get()
            .addOnSuccessListener { dormQuerySnapshot ->
                val allDormitories = mutableListOf<Dormitory>()
                for (dormitoryDocument in dormQuerySnapshot.documents) {
                    // Retrieve individual dormitory data
                    val dormName = dormitoryDocument.getString("dormName")
                    val dormPrice = dormitoryDocument.getString("price")
                    val dormitoryId = dormitoryDocument.getString("dormId") ?: ""
                    val numOfRooms = dormitoryDocument.getLong("numOfRooms")?.toInt()
                    val maxCapacity = dormitoryDocument.getLong("maxCapacity")?.toInt()
                    val imageUrl = dormitoryDocument.get("images") as? List<String>
                    val landlordId = dormitoryDocument.getString("landlordId")
                    val qrCodeImageUrl = dormitoryDocument.getString("qrCodeImageUrl")
                    val latitude = dormitoryDocument.getDouble("latitude")
                    val longitude = dormitoryDocument.getDouble("longitude")
                    val address = dormitoryDocument.getString("address")
                    val phoneNumber = dormitoryDocument.getString("phoneNumber")
                    val email = dormitoryDocument.getString("email")
                    val username = dormitoryDocument.getString("username")
                    val description = dormitoryDocument.getString("description")
                    val permitImage = dormitoryDocument.getString("permitImage")
                    val pendingRequestsCount = dormitoryDocument.getLong("pendingRequestsCount")?.toInt()
                    val rentalTerm = dormitoryDocument.getString("rentalTerm")
                    val bathroom = dormitoryDocument.getString("bathroom")
                    val electric = dormitoryDocument.getString("electric")
                    val water = dormitoryDocument.getString("water")
                    val paymentOptions = dormitoryDocument.get("paymentOptions") as? List<String>
                    val amenities = dormitoryDocument.get("amenities") as? List<String>
                    val amenities2 = dormitoryDocument.get("amenities2") as? List<String>
                    val genderRestriction = dormitoryDocument.getString("genderRestriction")
                    // Add other dormitory fields as needed

                    // Create a Dormitory object and add it to the list
                    val dormitory = Dormitory(dormName, dormPrice, dormitoryId, numOfRooms, maxCapacity, imageUrl, landlordId, qrCodeImageUrl, latitude, longitude, address, phoneNumber, email, username, description, permitImage, pendingRequestsCount,rentalTerm, bathroom, electric, water, paymentOptions, amenities, amenities2, genderRestriction )

                }
                callback(allDormitories)
            }
            .addOnFailureListener { e ->
                // Handle the failure to query all dormitories
            }


    }

    private fun fetchSavedDormitoryIds(currentUserEmail: String, callback: (List<String>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        // Query user document by email
        firestore.collection("users")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val savedDormitoryIds = userDocument["savedDormitories"] as? List<String> ?: emptyList()
                    callback(savedDormitoryIds)
                } else {
                    // No user found, return an empty list
                    callback(emptyList())
                }
            }
            .addOnFailureListener { e ->
                // Handle the failure to query the user document
                callback(emptyList())
            }
    }

    private fun queryDormitoryDetails(dormitoryIds: List<String>, callback: (List<Dormitory>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        // Create an empty list to store dormitory details
        val dormitoriesList: MutableList<Dormitory> = mutableListOf()

        // Query dormitory details for each saved dormitory ID
        for (dormitoryId in dormitoryIds) {
            dormitoriesRef.document(dormitoryId)
                .get()
                .addOnSuccessListener { dormitoryDocument ->
                    // Retrieve individual dormitory data
                    val dormName = dormitoryDocument.getString("dormName")
                    val dormPrice = dormitoryDocument.getString("price")
                    val dormitoryId = dormitoryDocument.getString("dormId") ?: ""
                    val numOfRooms = dormitoryDocument.getLong("numOfRooms")?.toInt()
                    val maxCapacity = dormitoryDocument.getLong("maxCapacity")?.toInt()
                    val imageUrl = dormitoryDocument.get("images") as? List<String>
                    val landlordId = dormitoryDocument.getString("landlordId")
                    val qrCodeImageUrl = dormitoryDocument.getString("qrCodeImageUrl")
                    val latitude = dormitoryDocument.getDouble("latitude")
                    val longitude = dormitoryDocument.getDouble("longitude")
                    val address = dormitoryDocument.getString("address")
                    val phoneNumber = dormitoryDocument.getString("phoneNumber")
                    val email = dormitoryDocument.getString("email")
                    val username = dormitoryDocument.getString("username")
                    val description = dormitoryDocument.getString("description")
                    val permitImage = dormitoryDocument.getString("permitImage")
                    val pendingRequestsCount = dormitoryDocument.getLong("pendingRequestsCount")?.toInt()
                    val rentalTerm = dormitoryDocument.getString("rentalTerm")
                    val bathroom = dormitoryDocument.getString("bathroom")
                    val electric = dormitoryDocument.getString("electric")
                    val water = dormitoryDocument.getString("water")
                    val paymentOptions = dormitoryDocument.get("paymentOptions") as? List<String>
                    val amenities = dormitoryDocument.get("amenities") as? List<String>
                    val amenities2 = dormitoryDocument.get("amenities2") as? List<String>
                    val genderRestriction = dormitoryDocument.getString("genderRestriction")

                    // Add other dormitory fields as needed

                    // Create a Dormitory object and add it to the list
                    val dormitory = Dormitory(dormName, dormPrice, dormitoryId, numOfRooms, maxCapacity, imageUrl, landlordId, qrCodeImageUrl, latitude, longitude, address, phoneNumber, email, username, description, permitImage, pendingRequestsCount,rentalTerm, bathroom, electric, water, paymentOptions, amenities,amenities2, genderRestriction )
                    dormitoriesList.add(dormitory)

                    // Check if all dormitories have been retrieved
                    if (dormitoriesList.size == dormitoryIds.size) {
                        callback(dormitoriesList)
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the failure to query dormitory details
                    callback(emptyList())
                }
        }
    }
}