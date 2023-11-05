package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kotlindormify.databinding.LandlordUnitsFragmentBinding
import com.example.kotlindormify.R
import com.google.firebase.firestore.FirebaseFirestore
import android.app.ProgressDialog


class LandlordUnitsFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: LandlordUnitsFragmentBinding
    private lateinit var dormitoriesAdapter: DormitoriesAdapter
    private lateinit var dormitoriesList: MutableList<Dormitory>
    private lateinit var currentUserId: String // Declare currentUserId as a member variable
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoDormitories: TextView
    private var progressDialog: ProgressDialog? = null


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LandlordUnitsFragmentBinding.inflate(inflater, container, false)
        val activity = requireActivity() as LandlordDashboardActivity
        val userEmail = activity.userEmail
        // Initialize the SwipeRefreshLayout
        swipeRefreshLayout = binding.swapRefreshLayout

        progressBar = binding.progressBar
        textNoDormitories = binding.root.findViewById<TextView>(R.id.textNoDormitories)

        // Initialize the RecyclerView and adapter
        recyclerView = binding.recyclerDormitories
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        dormitoriesList = mutableListOf()
        dormitoriesAdapter = DormitoriesAdapter(dormitoriesList)
        recyclerView.adapter = dormitoriesAdapter



        swipeRefreshLayout.setOnRefreshListener {
            refreshDataFromFirestore()
        }
        swipeRefreshLayout.setColorSchemeColors(
            // Add your custom colors here

            ContextCompat.getColor(requireContext(), R.color.violet)
        )

        binding.btnAdd.setOnClickListener {
            // Create an instance of the fragment for adding dormitory information
            val addDormitoryFragment = LandlordAddDormitoryFragment()

            // Use a FragmentTransaction to replace the current fragment
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, addDormitoryFragment)
            transaction.addToBackStack(null) // Allows going back to the previous fragment
            transaction.commit()
        }

        // Show the progress bar while fetching data
        progressBar.visibility = View.VISIBLE


        // Reference to your "dormitories" collection in Firestore
        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")
        val usersRef = firestore.collection("users")
        val userQuery = usersRef.whereEqualTo("email", userEmail)

        // Query the Firestore database to find the user with the specified email
        usersRef.whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { userQuerySnapshot ->
                // Check if there is a user with the specified email
                if (!userQuerySnapshot.isEmpty) {
                    // Assuming there's only one matching user (email is unique)
                    val userDocument = userQuerySnapshot.documents[0]

                    // Retrieve the user's ID and store it in the variable currentID
                    currentUserId = userDocument.id // Store it in the class-scope variable

                    // Query the database to find dormitories where landlordId == currentID
                    dormitoriesRef.whereEqualTo("landlordId", currentUserId)
                        .get()
                        .addOnSuccessListener { dormQuerySnapshot ->
                            // Hide the progress bar as data fetching is complete
                            progressBar.visibility = View.GONE

                            if (!dormQuerySnapshot.isEmpty) {
                                // Iterate through matching dormitories
                                for (dormitoryDocument in dormQuerySnapshot.documents) {
                                    // Retrieve individual dormitory data
                                    val dormName = dormitoryDocument.getString("dormName")
                                    val dormPrice = dormitoryDocument.getString("price")
                                    val dormitoryId = dormitoryDocument.getString("dormId") ?: ""
                                    val numOfRooms =
                                        dormitoryDocument.getLong("numOfRooms")?.toInt()
                                    val imageUrl = dormitoryDocument.getString("image")
                                    val landlordId = dormitoryDocument.getString("landlordId")
                                    val qrCodeImageUrl =
                                        dormitoryDocument.getString("qrCodeImageUrl")
                                    val latitude = dormitoryDocument.getDouble("latitude")
                                    val longitude = dormitoryDocument.getDouble("longitude")
                                    val address = dormitoryDocument.getString("address")
                                    val phoneNumber = dormitoryDocument.getString("phoneNumber")
                                    val email = dormitoryDocument.getString("email")
                                    val username = dormitoryDocument.getString("username")
                                    val description = dormitoryDocument.getString("description")
                                    val permitImage = dormitoryDocument.getString("permitImage")
                                    val pendingRequestsCount =
                                        dormitoryDocument.getLong("pendingRequestsCount")?.toInt()
                                    val rentalterm = dormitoryDocument.getString("rentalterm")
                                    val bathroom = dormitoryDocument.getString("bathroom")
                                    val electric = dormitoryDocument.getString("electric")
                                    val water = dormitoryDocument.getString("water")
                                    val paymentOptions =
                                        dormitoryDocument.get("paymentOptions") as? List<String>
                                    val amenities =
                                        dormitoryDocument.get("amenities") as? List<String>

                                    // Add dormitory to the list
                                    dormitoriesList.add(
                                        Dormitory(
                                            dormName,
                                            dormPrice,
                                            dormitoryId,
                                            numOfRooms,
                                            imageUrl,
                                            landlordId,
                                            qrCodeImageUrl,
                                            latitude,
                                            longitude,
                                            address,
                                            phoneNumber,
                                            email,
                                            username,
                                            description,
                                            permitImage,
                                            pendingRequestsCount,
                                            rentalterm,
                                            bathroom,
                                            electric,
                                            water,
                                            paymentOptions,
                                            amenities
                                        )
                                    )
                                }

                                // Notify the adapter that data has changed
                                dormitoriesAdapter.notifyDataSetChanged()

                            } else {
                                // Handle the case when there are no matching dormitories
                                // For example, display a message or handle it as needed
                                textNoDormitories.visibility = View.VISIBLE
                            }
                        }
                        .addOnFailureListener { e ->
                            // Handle any errors that occur during the dormitory query
                            // You can log or display an error message here
                            progressBar.visibility = View.GONE
                        }
                } else {
                    // Hide the progress bar if there's no user with the specified email
                    progressBar.visibility = View.GONE

                    // Handle the case when there's no user with the specified email
                    // For example, display a message or handle it as needed
                    Toast.makeText(
                        requireContext(),
                        "User not found with email: $userEmail",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur during the user query
                // You can log or display an error message here
                progressBar.visibility = View.GONE
            }


        dormitoriesAdapter.setOnItemClickListener(object : DormitoriesAdapter.OnItemClickListener {
            override fun onDeleteClick(position: Int) {
                val dormitoryToDelete = dormitoriesList[position]

                // Ensure that dormitoryId is not null before passing it
                val dormitoryIdToDelete = dormitoryToDelete.dormitoryId

                if (dormitoryIdToDelete != null) {
                    val alertDialog = AlertDialog.Builder(requireContext())
                        .setTitle("Confirm Deletion")
                        .setMessage("Do you want to delete '${dormitoryToDelete.dormName}'?")
                        .setPositiveButton("Yes") { _, _ ->
                            // Reference to your "dormitories" collection in Firestore
                            val dormitoriesRef =
                                FirebaseFirestore.getInstance().collection("dormitories")

                            // Delete the dormitory from Firestore based on its ID
                            dormitoriesRef.document(dormitoryIdToDelete).delete()
                                .addOnCompleteListener { task ->
                                    if (task.isSuccessful) {
                                        // Dormitory deleted from Firestore, now remove it from the local list
                                        dormitoriesAdapter.deleteDormitory(position)

                                        if (dormitoriesList.isEmpty()) {
                                            textNoDormitories.visibility = View.VISIBLE
                                        }
                                    } else {
                                        Toast.makeText(
                                            requireContext(),
                                            "Failed to delete dormitory. Please try again.",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }
                                }
                        }
                        .setNegativeButton("No") { dialog, _ ->
                            dialog.dismiss()
                        }
                        .create()

                    alertDialog.show()
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Unable to delete dormitory without an ID.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        })

        return binding.root
    }

    private fun refreshDataFromFirestore() {
        // Clear the existing dormitoriesList
        dormitoriesList.clear()

        // Reference to your "dormitories" collection in Firestore
        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        // Query the database to find dormitories where landlordId == currentID
        dormitoriesRef.whereEqualTo("landlordId", currentUserId)
            .get()
            .addOnSuccessListener { dormQuerySnapshot ->
                // Hide the progress bar as data fetching is complete
                progressBar.visibility = View.GONE

                if (!dormQuerySnapshot.isEmpty) {
                    // Iterate through matching dormitories
                    for (dormitoryDocument in dormQuerySnapshot.documents) {
                        // Retrieve individual dormitory data
                        val dormName = dormitoryDocument.getString("dormName")
                        val dormPrice = dormitoryDocument.getString("price")
                        val dormitoryId = dormitoryDocument.getString("dormId") ?: ""
                        val numOfRooms = dormitoryDocument.getLong("numOfRooms")?.toInt()
                        val imageUrl = dormitoryDocument.getString("image")
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
                        val pendingRequestsCount =
                            dormitoryDocument.getLong("pendingRequestsCount")?.toInt()
                        val rentalterm = dormitoryDocument.getString("rentalterm")
                        val bathroom = dormitoryDocument.getString("bathroom")
                        val electric = dormitoryDocument.getString("electric")
                        val water = dormitoryDocument.getString("water")
                        val paymentOptions =
                            dormitoryDocument.get("paymentOptions") as? List<String>
                        val amenities = dormitoryDocument.get("amenities") as? List<String>


                        // Add dormitory to the list
                        dormitoriesList.add(
                            Dormitory(
                                dormName,
                                dormPrice,
                                dormitoryId,
                                numOfRooms,
                                imageUrl,
                                landlordId,
                                qrCodeImageUrl,
                                latitude,
                                longitude,
                                address,
                                phoneNumber,
                                email,
                                username,
                                description,
                                permitImage,
                                pendingRequestsCount,
                                rentalterm,
                                bathroom,
                                electric,
                                water,
                                paymentOptions,
                                amenities
                            )
                        )
                    }

                    // Notify the adapter that data has changed
                    dormitoriesAdapter.notifyDataSetChanged()
                } else {
                    // Handle the case when there are no matching dormitories
                    // For example, display a message or handle it as needed
                    textNoDormitories.visibility = View.VISIBLE
                }

                // Stop the refreshing indicator
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur during the dormitory query
                // You can log or display an error message here
                progressBar.visibility = View.GONE
                // Stop the refreshing indicator even in case of failure
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Loading...") // Set the message you want to display
        progressDialog?.setCancelable(false) // Prevents user from dismissing the dialog by tapping outside
        progressDialog?.show()
    }


}




