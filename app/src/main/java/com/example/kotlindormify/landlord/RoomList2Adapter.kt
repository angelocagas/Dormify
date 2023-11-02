package com.example.kotlindormify.landlord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R

class RoomList2Adapter(
    private var roomList: List<Room>,
    private val onRoomClick: (Room) -> Unit
) : RecyclerView.Adapter<RoomList2Adapter.ViewHolder>() {


    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val roomNumberTextView: TextView = itemView.findViewById(R.id.textRoomNumber)
        val availabilityTextView: TextView = itemView.findViewById(R.id.textAvailability)

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

        if (holder.availabilityTextView.text == "available"){
            holder.availabilityTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.available_success))
        }else{
            holder.availabilityTextView.setTextColor(ContextCompat.getColor(holder.itemView.context, R.color.occupied_error))
        }

        holder.itemView.setOnClickListener {
            val room = roomList[position]
            onRoomClick(room)
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
