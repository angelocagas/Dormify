package com.example.kotlindormify.landlord

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.GridLayoutManager
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import java.util.Calendar

class AssignTenantFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnOccupied: Button
    private lateinit var btnAvailable: Button
    private lateinit var btnAll: Button
    private lateinit var roomListAdapter: RoomList2Adapter
    private val roomList = mutableListOf<Room>()
    private var selectedRoom: Room? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_assign_tenant, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerRequestsList)
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        roomListAdapter = RoomList2Adapter(roomList) { room ->
            selectedRoom = room
            showDateSelectionDialog()
        }

        recyclerView.adapter = roomListAdapter


        // Add this code to your app's initialization (e.g., in your Application class or activity's onCreate)
        val firestore = FirebaseFirestore.getInstance()

        // Retrieve dormitory ID passed from LandlordUnitsFragment
        val dormitoryId = arguments?.getString("dormitoryId")
        val requestId = arguments?.getString("requestId")

        if (dormitoryId != null) {
            // Fetch the list of rooms for the selected dormitory using dormitoryId
            fetchRoomList(dormitoryId)
        }

        val ibBack = rootView.findViewById<ImageView>(R.id.ibBack)
        ibBack.setOnClickListener {
            // Get the fragment manager
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
        }

        return rootView
    }

    private fun fetchRoomList(dormitoryId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val roomsRef = firestore.collection("dormitories").document(dormitoryId).collection("rooms")

        roomsRef.get()
            .addOnSuccessListener { querySnapshot ->
                roomList.clear()

                for (roomDocument in querySnapshot.documents) {
                    val roomNumber = roomDocument.getLong("roomNumber")
                        ?.toInt() // Retrieve as Long and convert to Int
                    val availability = roomDocument.getString("availability")
                    val tenantId = roomDocument.getString("tenantId")
                    val tenantName = roomDocument.getString("tenantName")
                    val capacity = roomDocument.getLong("capacity")?.toInt()
                    val maxCapacity = roomDocument.getLong("maxCapacity")?.toInt()


                    // Check the data type of roomNumber and ensure it's not null before adding it to the list
                    if (roomNumber != null && availability != null) {
                        roomList.add(Room(roomNumber, availability, tenantId, tenantName, capacity, maxCapacity))
                    }
                }

                // Sort the room list by room number
                roomList.sortBy { it.roomNumber }

                // Update the RecyclerView with the complete room list
                roomListAdapter.updateRoomList(roomList.filter { it.availability == "available" })
            }
            .addOnFailureListener { e ->
                // Handle errors or show a message if data retrieval fails
            }
    }

    private fun showDateSelectionDialog() {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentDay = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                // Handle date selection here
                onDateSelected(year, month, day)
            },
            currentYear,
            currentMonth,
            currentDay
        )

        // Set a minimum date if needed
        // datePickerDialog.datePicker.minDate = System.currentTimeMillis() - 1000

        datePickerDialog.show()
    }

    private fun onDateSelected(year: Int, month: Int, day: Int) {
        if (selectedRoom != null) {
            // Prepare the contract end date
            val calendar = Calendar.getInstance()
            calendar.set(year, month, day)
            val contractEndDate = calendar.time

            // Update the request's status to "accepted" and set the acceptedDate
            val firestore = FirebaseFirestore.getInstance()
            val requesterFullName = arguments?.getString("requesterFullName")
            val requesterId = arguments?.getString("requesterId")
            val requestId = arguments?.getString("requestId")
            val dormitoryId = arguments?.getString("dormitoryId")

            if (requestId != null && dormitoryId != null) {
                // Update the request status in the main collection
                val rentalRequestsRef = firestore.collection("rental_requests")
                val requestUpdateData = mapOf(
                    "status" to "accepted",
                    "acceptedDate" to FieldValue.serverTimestamp()
                )
                rentalRequestsRef.document(requestId).update(requestUpdateData)
                    .addOnSuccessListener {
                        // Handle the successful update

                        // Update the request status in the dormitory subcollection
                        val dormitoryRef = firestore.collection("dormitories").document(dormitoryId)
                            .collection("rental_requests")
                        dormitoryRef.document(requestId).update(requestUpdateData)
                            .addOnSuccessListener {
                                // Handle the successful update

                                // Now update the room's availability to "occupied"
                                // Now update the room's availability to "occupied"
                                val roomNumber = selectedRoom!!.roomNumber
                                val roomRef = firestore.collection("dormitories").document(dormitoryId)
                                    .collection("rooms")
                                    .whereEqualTo("roomNumber", roomNumber)

                                roomRef.get()
                                    .addOnSuccessListener { querySnapshot ->
                                        if (!querySnapshot.isEmpty) {
                                            val roomDocument = querySnapshot.documents[0]

                                            val roomUpdateData = hashMapOf(
                                                //if(capacity)
                                                "availability" to "occupied",
                                                "tenantId" to requesterId,
                                                "tenantName" to requesterFullName,
                                                "capacity" to FieldValue.increment(1)
                                            )

                                            roomDocument.reference.update(roomUpdateData)
                                        }

                                        // Clone the requester's data to the "tenant" collection
                                        val tenantRef = firestore.collection("tenant").document(requestId)
                                        val tenantData = mapOf(
                                            "requesterFullName" to requesterFullName,
                                            "requesterId" to requesterId,
                                            "dormitoryId" to dormitoryId,
                                            "roomNumber" to roomNumber,
                                            // Add other fields similar to the "requester_data"
                                        )
                                        tenantRef.set(tenantData)
                                            .addOnSuccessListener {
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Request has been accepted",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                            .addOnFailureListener { e ->
                                                Toast.makeText(
                                                    requireContext(),
                                                    "Error accepting the request",
                                                    Toast.LENGTH_SHORT
                                                ).show()
                                            }
                                    }
                                    .addOnFailureListener { e ->
                                        // Handle the failure to query and update room availability
                                        Toast.makeText(
                                            requireContext(),
                                            "Error accepting the request",
                                            Toast.LENGTH_SHORT
                                        ).show()
                                    }

                            }
                            .addOnFailureListener { e ->
                                // Handle the failure to update the dormitory's rental request subcollection
                            }
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure to update the request status
                        Toast.makeText(
                            requireContext(),
                            "Error accepting the request",
                            Toast.LENGTH_SHORT
                        ).show()
                    }
            }
        } else {
            // Handle the case where no room is selected
            Toast.makeText(requireContext(), "Please select a room first.", Toast.LENGTH_SHORT)
                .show()
        }
    }


}
