package com.example.kotlindormify.tenant3

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import com.example.kotlindormify.landlord.Tenant

class PaymentHistoryAdapter(private var paymentHistoryList2: List<PaymentHistoryItem>) :
    RecyclerView.Adapter<PaymentHistoryAdapter.ViewHolder>() {

    // ViewHolder class definition goes here

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_payment_history, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val paymentHistoryItem = paymentHistoryList2[position]
        holder.paymentDateTextView.text = paymentHistoryItem.paymentDate
        holder.paymentId.text = paymentHistoryItem.paymentId
        holder.status.text = paymentHistoryItem.status
        // Implement onBindViewHolder
    }

    override fun getItemCount(): Int {
        return paymentHistoryList2.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val paymentDateTextView: TextView = itemView.findViewById(R.id.textPaymentDate)
        val paymentId: TextView = itemView.findViewById(R.id.textPaymentId)
        val status: TextView = itemView.findViewById(R.id.textStatus)


        // ViewHolder implementation goes here
    }
    fun updateRecyclerView(newList: List<PaymentHistoryItem>) {
        paymentHistoryList2 = newList
        notifyDataSetChanged()
    }
}
