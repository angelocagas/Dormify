package com.example.kotlindormify.admin

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.databinding.FragmentAdminListBinding  // Add this import
import com.example.kotlindormify.R
import com.google.firebase.firestore.FirebaseFirestore

class AdminListFragment : Fragment(R.layout.fragment_admin_list) {
    private var _binding: FragmentAdminListBinding? = null
    private val binding get() = _binding!!
    private lateinit var recyclerView: RecyclerView
    private lateinit var status: String


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAdminListBinding.inflate(inflater, container, false)

        // Initialize RecyclerView
        recyclerView = binding.recyclerRequestsList
        val layoutManager = LinearLayoutManager(requireContext())
        recyclerView.layoutManager = layoutManager

        // Pass a lambda to the adapter to handle item clicks
        val adapter = DormitoryAdapter { dormitoryItem ->
            // Handle item click
            navigateToDormRequestDetails(dormitoryItem)
        }
        recyclerView.adapter = adapter


        status = "pending"
        fetchData()
        binding.btnPending.setOnClickListener{
            status = "pending"
            fetchData()
            binding.btnPending.setBackgroundResource(R.drawable.bg_btn_bck_light)
            binding.btnPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.btnApproved.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            binding.btnApproved.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            binding.btnDenied.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            binding.btnDenied.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        binding.btnApproved.setOnClickListener {
            status = "accepted"
            fetchData()
            binding.btnApproved.setBackgroundResource(R.drawable.bg_btn_bck_light)
            binding.btnApproved.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.btnPending.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            binding.btnPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            binding.btnDenied.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            binding.btnDenied.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))
        }

        binding.btnDenied.setOnClickListener {
            status = "denied"
            fetchData()
            binding.btnDenied.setBackgroundResource(R.drawable.bg_btn_bck_light)
            binding.btnDenied.setTextColor(ContextCompat.getColor(requireContext(), R.color.white))

            binding.btnPending.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            binding.btnPending.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

            binding.btnApproved.setBackgroundResource(R.drawable.rectangle_radius_light_gray)
            binding.btnApproved.setTextColor(ContextCompat.getColor(requireContext(), R.color.black))

        }



        return binding.root
    }

    private fun fetchData() {
        // Fetch data from Firestore based on the selected status
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("dormitories")
            .whereEqualTo("status", status)
            .get()
            .addOnSuccessListener { documents ->
                val dormList = mutableListOf<DormitoryItem>()
                for (document in documents) {
                    val dormId = document.id
                    val dormName = document.getString("dormName") ?: ""
                    // Get other fields as needed
                    dormList.add(DormitoryItem(dormId, dormName, status))
                }
                updateUI(dormList)
            }
            .addOnFailureListener { e ->
                Toast.makeText(requireContext(), "Error fetching data: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun updateUI(dormList: List<DormitoryItem>) {
        // Update RecyclerView adapter with the new data
        val adapter = recyclerView.adapter as DormitoryAdapter
        adapter.setData(dormList)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun navigateToDormRequestDetails(dormitoryItem: DormitoryItem) {
        val fragment = DormRequestDetailsFragment()

        // Pass data to the fragment using arguments
        val bundle = Bundle()
        bundle.putString("dormId", dormitoryItem.dormId)
        bundle.putString("status", dormitoryItem.status)
        fragment.arguments = bundle

        // Navigate to DormRequestDetailsFragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .addToBackStack(null)
            .commit()
    }



}
