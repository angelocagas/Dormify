package com.example.kotlindormify.landlord

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.ChatMessage
import com.example.kotlindormify.R
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class LandlordChatAdapter :
    ListAdapter<ChatMessage, LandlordChatAdapter.MessageViewHolder>(DiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val view = if (viewType == VIEW_TYPE_USER_MESSAGE) {
            inflater.inflate(R.layout.item_chat_message_right, parent, false)
        } else {
            inflater.inflate(R.layout.item_chat_message_left, parent, false)
        }
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    override fun getItemViewType(position: Int): Int {
        val message = getItem(position)
        return if (message.sender == currentUserUid) {
            VIEW_TYPE_USER_MESSAGE
        } else {
            VIEW_TYPE_OTHER_MESSAGE
        }
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val messageText: TextView = itemView.findViewById(R.id.item_chat_message_left)
        private val timestamp: TextView = itemView.findViewById(R.id.tvtimestampright)


        fun bind(message: ChatMessage) {
            messageText.text = message.text
            val dateFormat = SimpleDateFormat("MMM d, yyyy h:mm a", Locale.getDefault())
            val formattedDate = message.timestamp.toDate()?.let { dateFormat.format(it) }
            timestamp.text = formattedDate        }


    }

    private class DiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.text == newItem.text
        }
    }

    companion object {
        private const val VIEW_TYPE_USER_MESSAGE = 1
        private const val VIEW_TYPE_OTHER_MESSAGE = 2
        private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""
    }
}
