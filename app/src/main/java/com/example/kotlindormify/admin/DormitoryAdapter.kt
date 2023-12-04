package com.example.kotlindormify.admin

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.Dormitories
import com.example.kotlindormify.R
import com.example.kotlindormify.admin.DormitoryItem



class DormitoryAdapter(private val onItemClickListener: (DormitoryItem) -> Unit) :
    RecyclerView.Adapter<DormitoryAdapter.ViewHolder>() {

    private var dormList: List<DormitoryItem> = emptyList()

    fun setData(newData: List<DormitoryItem>) {
        dormList = newData
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.request_dormitory_layout, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dormItem = dormList[position]
        holder.bind(dormItem)

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClickListener.invoke(dormItem)
        }
    }

    override fun getItemCount(): Int {
        return dormList.size
    }

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val textDormName: TextView = itemView.findViewById(R.id.tvRequestDormitory)
        private val textDormAddress: TextView = itemView.findViewById(R.id.tvRequestDate)
        //private val textStatus: TextView = itemView.findViewById(R.id.)

        fun bind(item: DormitoryItem) {
            textDormName.text = item.dormName
            textDormAddress.text = item.address
            //textStatus.text = item.status
        }
    }
}
