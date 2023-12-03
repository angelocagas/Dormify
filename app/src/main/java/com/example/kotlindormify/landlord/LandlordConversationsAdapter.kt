package com.example.kotlindormify.landlord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter


class LandlordConversationsAdapter(
    private var conversations: List<Conversation>,
    private val itemClickListener: OnItemClickListener
) : RecyclerView.Adapter<LandlordConversationsAdapter.ConversationViewHolder>() {

    inner class ConversationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val titleTextView: TextView = itemView.findViewById(R.id.conversationTitle)
        val lastMessageTextView: TextView = itemView.findViewById(R.id.lastMessage)
        val userProfile: ImageView = itemView.findViewById(R.id.profile)
        val lastMessageTimestamp: TextView = itemView.findViewById(R.id.last_message_time_text)


    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConversationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_landlord_conversation, parent, false)
        return ConversationViewHolder(view)
    }

    override fun onBindViewHolder(holder: ConversationViewHolder, position: Int) {
        val conversation = conversations[position]
        holder.titleTextView.text = conversation.username
        holder.lastMessageTextView.text = conversation.lastMessage
        Picasso.get().load(conversation.userPic)
            .error(R.drawable.error_image) // Add an error placeholder drawable
            .into(holder.userProfile)


        holder.itemView.setOnClickListener {
            itemClickListener.onItemClick(conversation)
        }
    }

    interface OnItemClickListener {
        fun onItemClick(conversation: Conversation)
    }

    override fun getItemCount(): Int {
        return conversations.size
    }

    // Function to update the dataset
    fun updateConversations(newConversations: List<Conversation>) {
        conversations = newConversations
        notifyDataSetChanged()
    }
}