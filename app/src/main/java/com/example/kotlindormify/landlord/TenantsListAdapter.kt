package com.example.kotlindormify.landlord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import com.google.firebase.firestore.FirebaseFirestore

class TenantsListAdapter(private var tenantList: List<Tenant>) :
    RecyclerView.Adapter<TenantsListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tenantName: TextView = itemView.findViewById(R.id.textTenantName)
        val roomNum: TextView = itemView.findViewById(R.id.textRoom)
        val payment: TextView = itemView.findViewById(R.id.textPayment)

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.tenant_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val tenant = tenantList[position]

        // Bind room data to the views
        holder.tenantName.text = tenant.tenantFullName
        holder.roomNum.text = "Room number: ${tenant.roomNumber.toString()}"

        fetchPendingRequestsCount(tenant.tenantId.toString()) { pendingRequestsCount ->
            // Update the pending requests count in the UI
            holder.payment.text = "${pendingRequestsCount} payment requests"

        }


    }

    override fun getItemCount(): Int {
        return tenantList.size
    }

    fun updateTenantList(newTenantList: List<Tenant>) {
        tenantList = newTenantList
        notifyDataSetChanged()
    }

    private fun fetchPendingRequestsCount(tenantId: String, callback: (Int) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        // Reference to the "tenant" collection
        val tenantCollection = firestore.collection("tenant")

        // Query for tenants with the specified tenantId
        val tenantQuery = tenantCollection.whereEqualTo("tenantId", tenantId)

        tenantQuery.get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    // Assuming there's only one tenant with the given tenantId (or choose the appropriate one)
                    val tenantDocument = querySnapshot.documents[0]

                    // Reference to the "payment_history" subcollection for the tenant
                    val paymentHistoryRef = tenantDocument.reference.collection("payment_history")

                    // Query pending requests (where status is "pending")
                    val pendingRequestsQuery = paymentHistoryRef.whereEqualTo("status", "pending")

                    pendingRequestsQuery.get()
                        .addOnSuccessListener { pendingRequestsSnapshot ->
                            // Get the count of pending requests
                            val pendingRequestsCount = pendingRequestsSnapshot.size()

                            // Call the callback with the pending requests count
                            callback(pendingRequestsCount)
                        }
                        .addOnFailureListener { exception ->
                            // Handle errors
                            callback(0) // In case of failure, callback with count 0
                        }
                } else {
                    // No matching tenant found, callback with count 0
                    callback(0)
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
                callback(0) // In case of failure, callback with count 0
            }
    }

}
