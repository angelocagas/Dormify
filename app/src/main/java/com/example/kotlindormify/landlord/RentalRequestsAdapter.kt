package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import java.text.SimpleDateFormat
import java.util.Locale

class RentalRequestsAdapter(private var rentalRequestsList: List<RentalRequest>) :
    RecyclerView.Adapter<RentalRequestsAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val requesterNameTextView: TextView = itemView.findViewById(R.id.textRequesterName)
        val requestDate: TextView = itemView.findViewById(R.id.tvRequestDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_item_layout, parent, false)


        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val rentalRequest = rentalRequestsList[position]

        // Bind rental request data to the views
        holder.requesterNameTextView.text = "${rentalRequest.requesterFullName}"
        holder.requestDate.text = "Request Date: ${formatTimestamp(rentalRequest.timestamp)}"

        holder.itemView.setOnClickListener {
            val requestId = rentalRequest.requestId// Replace with how you retrieve requestId

            val fragment = RequestDetailFragment()
            val bundle = Bundle()
            bundle.putString("requestId", requestId)
            bundle.putString("dormitoryId", rentalRequest.dormitoryId)
            fragment.arguments = bundle


            val fragmentManager = (holder.itemView.context as AppCompatActivity).supportFragmentManager
            fragmentManager.beginTransaction()
                .replace(R.id.fragment_container, fragment)
                .addToBackStack(null)
                .commit()
        }

    }

    override fun getItemCount(): Int {
        return rentalRequestsList.size
        notifyDataSetChanged()
    }

    fun updateRentalRequestsList(newRentalRequestsList: List<RentalRequest>) {
        rentalRequestsList = newRentalRequestsList
        notifyDataSetChanged()
    }

    fun formatTimestamp(timestamp: com.google.firebase.Timestamp?): String {
        if (timestamp == null) return ""

        val date = timestamp.toDate()
        val sdf = SimpleDateFormat("MMMM dd, yyyy hh:mm a", Locale.getDefault())
        return sdf.format(date)
    }
}


