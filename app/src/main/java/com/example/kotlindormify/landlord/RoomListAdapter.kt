package com.example.kotlindormify.landlord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R

class RoomListAdapter(private var roomList: List<Room>) :
    RecyclerView.Adapter<RoomListAdapter.ViewHolder>() {

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNumberTextView: TextView = itemView.findViewById(R.id.textRoomNumber)
        val availabilityTextView: TextView = itemView.findViewById(R.id.textAvailability)
        val capacityTextView: TextView = itemView.findViewById(R.id.textCapacity)
        val maxCapacityTextView: TextView = itemView.findViewById(R.id.textMaxCapacity)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.room_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val room = roomList[position]

        // Bind room data to the views
        holder.roomNumberTextView.text = "Room ${room.roomNumber}"
        holder.availabilityTextView.text = room.availability
        holder.capacityTextView.text = room.capacity.toString()
        holder.maxCapacityTextView.text = room.maxCapacity.toString()

        if (holder.availabilityTextView.text == "available") {
            holder.availabilityTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.available_success
                )
            )
        } else {
            holder.availabilityTextView.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.occupied_error
                )
            )
        }


    }

    override fun getItemCount(): Int {
        return roomList.size
    }

    fun updateRoomList(newRoomList: List<Room>) {
        roomList = newRoomList
        notifyDataSetChanged()
    }
}
