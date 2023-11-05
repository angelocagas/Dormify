package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import androidx.fragment.app.FragmentManager
import com.google.firebase.firestore.FirebaseFirestore

class RentalRequestsListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnPending: Button
    private lateinit var btnAccepted: Button
    private lateinit var btnRejected: Button
    private lateinit var rentalRequestsAdapter: RentalRequestsAdapter
    private val rentalRequestsList = mutableListOf<RentalRequest>()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_rental_requests_list, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerRequestsList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        rentalRequestsAdapter = RentalRequestsAdapter(rentalRequestsList)
        recyclerView.adapter = rentalRequestsAdapter
        btnPending = rootView.findViewById(R.id.btnPending)
        btnAccepted = rootView.findViewById(R.id.btnAccepted)
        btnRejected =
            rootView.findViewById(R.id.btnRejected) // Assuming you have buttons with IDs btnPending, btnAccepted, btnRejected

        // Retrieve dormitory ID passed from LandlordUnitsFragment
        val dormitoryId = arguments?.getString("dormitoryId")
        //Initial
        if (dormitoryId != null) {
            // Fetch the list of rental requests for the selected dormitory using dormitoryId
            fetchRentalRequestsList(dormitoryId)
            btnPending.setBackgroundResource(R.drawable.bg_btn_bck_light)
            btnPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))
        }


        btnPending.setOnClickListener {
            // Filter and show only pending rental requests
            val pendingRequests = rentalRequestsList.filter { it.status == "pending" }
            rentalRequestsAdapter.updateRentalRequestsList(pendingRequests)
            btnPending.setBackgroundResource(R.drawable.bg_btn_bck_light)
            btnPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            btnAccepted.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnAccepted.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            btnRejected.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnRejected.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        btnAccepted.setOnClickListener {
            // Filter and show only accepted rental requests
            val acceptedRequests = rentalRequestsList.filter { it.status == "accepted" }
            rentalRequestsAdapter.updateRentalRequestsList(acceptedRequests)
            btnAccepted.setBackgroundResource(R.drawable.bg_btn_bck_light)
            btnAccepted.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            btnPending.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            btnRejected.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnRejected.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        btnRejected.setOnClickListener {
            // Filter and show only rejected rental requests
            val rejectedRequests = rentalRequestsList.filter { it.status == "declined" }
            rentalRequestsAdapter.updateRentalRequestsList(rejectedRequests)
            btnRejected.setBackgroundResource(R.drawable.bg_btn_bck_light)
            btnRejected.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            btnPending.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            btnAccepted.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnAccepted.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }


        // Add this code to your app's initialization (e.g., in your Application class or activity's onCreate)
        val firestore = FirebaseFirestore.getInstance()


        val ibBack = rootView.findViewById<ImageView>(R.id.ibBack)
        ibBack.setOnClickListener {
            // Get the fragment manager
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
        }

        return rootView
    }

    private fun fetchRentalRequestsList(dormitoryId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val requestsRef = firestore
            .collection("dormitories")
            .document(dormitoryId)
            .collection("rental_requests")

        requestsRef.get()
            .addOnSuccessListener { querySnapshot ->
                rentalRequestsList.clear()

                for (requestDocument in querySnapshot.documents) {
                    val requestId = requestDocument.id
                    val status = requestDocument.getString("status")
                    val requesterFullName = requestDocument.getString("requesterFullName")
                    val timestamp = requestDocument.getTimestamp("timestamp")
                    val dormitoryId = requestDocument.getString("dormitoryId")

                    // Add rental request to the list
                    rentalRequestsList.add(
                        RentalRequest(
                            requestId,
                            status,
                            requesterFullName,
                            timestamp,
                            dormitoryId
                        )
                    )
                }

                // Update the RecyclerView with the complete rental requests list
                rentalRequestsAdapter.updateRentalRequestsList(rentalRequestsList.filter { it.status == "pending" })
            }
            .addOnFailureListener { e ->
                // Handle errors or show a message if data retrieval fails
            }
    }
}