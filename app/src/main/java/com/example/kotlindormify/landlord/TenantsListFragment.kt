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

class TenantsListFragment : Fragment() {
    private lateinit var recyclerView: RecyclerView
    private lateinit var tenantsListAdapter: TenantsListAdapter
    private val tenantList = mutableListOf<Tenant>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.landlord_tenants_fragment, container, false)
        recyclerView = rootView.findViewById(R.id.recyclerTenants)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        tenantsListAdapter = TenantsListAdapter(tenantList)
        recyclerView.adapter = tenantsListAdapter

        // Initialize filter buttons (if needed)

        // Add this code to fetch tenants from Firestore
        val firestore = FirebaseFirestore.getInstance()
        val dormitoryId = arguments?.getString("dormitoryId")


            fetchTenantList()


        return rootView
    }

    private fun fetchTenantList() {
        val firestore = FirebaseFirestore.getInstance()
        val tenantsRef = firestore.collection("tenant")

        tenantsRef.get()
            .addOnSuccessListener { querySnapshot ->
                tenantList.clear()

                for (tenantDocument in querySnapshot.documents) {
                    // Retrieve tenant data and add it to the list
                    val requesterFullName = tenantDocument.getString("requesterFullName")
                    val roomNumber = tenantDocument.getLong("roomNumber")?.toInt() // Retrieve as Long and convert to Int

                    if (requesterFullName != null && roomNumber != null) {
                        tenantList.add(Tenant(roomNumber, requesterFullName))
                    }
                }

                // Update the RecyclerView with the complete tenant list
                tenantsListAdapter.updateTenantList(tenantList)
            }
            .addOnFailureListener { e ->
                // Handle errors or show a message if data retrieval fails
            }
    }

}
