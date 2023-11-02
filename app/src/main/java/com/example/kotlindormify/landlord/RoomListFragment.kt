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

class RoomListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var btnOccupied: Button
    private lateinit var btnAvailable: Button
    private lateinit var btnAll: Button
    private lateinit var roomListAdapter: RoomListAdapter
    private val roomList = mutableListOf<Room>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.fragment_room_list, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerRequestsList)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        roomListAdapter = RoomListAdapter(roomList)
        recyclerView.adapter = roomListAdapter
        btnAvailable = rootView.findViewById(R.id.btnAvailable)
        btnOccupied = rootView.findViewById(R.id.btnOccupied)
        btnAll = rootView.findViewById(R.id.btnAll) // Assuming you have a button with ID btnAll


        btnAvailable.setOnClickListener {
            // Filter and show only available rooms
            val availableRooms = roomList.filter { it.availability == "available" }
            roomListAdapter.updateRoomList(availableRooms)
            btnAvailable.setBackgroundResource(R.drawable.bg_btn_bck_light)
            btnAvailable.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            btnOccupied.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnOccupied.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
            btnAll.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        }

        btnOccupied.setOnClickListener {
            // Filter and show only occupied rooms
            val occupiedRooms = roomList.filter { it.availability == "occupied" }
            roomListAdapter.updateRoomList(occupiedRooms)
            btnOccupied.setBackgroundResource(R.drawable.bg_btn_bck_light)
            btnOccupied.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            btnAvailable.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnAvailable.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            btnAll.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))


        }

        btnAll.setOnClickListener {
            // Show all rooms (no filtering)
            roomListAdapter.updateRoomList(roomList)
            btnAll.setBackgroundResource(R.drawable.bg_btn_bck_light)
            btnAll.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            btnAvailable.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnAvailable.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            btnOccupied.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            btnOccupied.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        }

        btnAll.performClick()


        // Add this code to your app's initialization (e.g., in your Application class or activity's onCreate)
        val firestore = FirebaseFirestore.getInstance()

        // Retrieve dormitory ID passed from LandlordUnitsFragment
        val dormitoryId = arguments?.getString("dormitoryId")

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
                    val roomNumber = roomDocument.getLong("roomNumber")?.toInt() // Retrieve as Long and convert to Int
                    val availability = roomDocument.getString("availability")
                    val tenantId = roomDocument.getString("tenantId")
                    val tenantName = roomDocument.getString("tenantName")

                    // Check the data type of roomNumber and ensure it's not null before adding it to the list
                    if (roomNumber != null && availability != null) {
                        roomList.add(Room(roomNumber, availability, tenantId, tenantName))
                    }
                }

                // Sort the room list by room number
                roomList.sortBy { it.roomNumber }

                // Update the RecyclerView with the complete room list
                roomListAdapter.updateRoomList(roomList)
            }
            .addOnFailureListener { e ->
                // Handle errors or show a message if data retrieval fails
            }
    }
}
