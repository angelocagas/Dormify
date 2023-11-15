package com.example.kotlindormify.landlord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R

class TenantsListAdapter(private var tenantList: List<Tenant>) :
    RecyclerView.Adapter<TenantsListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tenantName: TextView = itemView.findViewById(R.id.textTenantName)
        val roomNum: TextView = itemView.findViewById(R.id.textRoom)
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


    }

    override fun getItemCount(): Int {
        return tenantList.size
    }

    fun updateTenantList(newTenantList: List<Tenant>) {
        tenantList = newTenantList
        notifyDataSetChanged()
    }
}
