package com.example.kotlindormify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ChatFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUserUid: String
    private lateinit var conversationId: String
    private var landlordId: String? = null
    private var dormName: String? = null
    private var adapter = ChatAdapter()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDormName: TextView


            override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat_fragment, container, false)

        arguments?.let {
            landlordId = it.getString("landlordId")
            dormName = it.getString("dormName")
        }

        // Initialize Firebase Firestore and Firebase Authentication
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()
        currentUserUid = auth.currentUser?.uid ?: ""

        // Generate a unique conversation ID based on user IDs
        val otherUserId = arguments?.getString("landlordId") ?: ""
        conversationId = if (currentUserUid < otherUserId) {
            "$currentUserUid-$otherUserId"
        } else {
            "$otherUserId-$currentUserUid"
        }

        // Initialize RecyclerView and Adapter
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(context)
        recyclerView.adapter = adapter

        tvDormName = view.findViewById(R.id.textView3)
        tvDormName.setText(dormName)
        // Load chat messages
        loadChatMessages()

        // Send button click listener
        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val messageEditText = view.findViewById<EditText>(R.id.messageEditText)
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText)
                messageEditText.text.clear()
            }
        }

        return view
    }

    private fun loadChatMessages() {
        firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .orderBy("timestamp")
            .addSnapshotListener { querySnapshot, exception ->
                if (exception != null) {
                    // Handle the error
                    return@addSnapshotListener
                }

                val messages = mutableListOf<ChatMessage>()
                for (document in querySnapshot?.documents ?: emptyList()) {
                    val sender = document.getString("sender") ?: ""
                    val text = document.getString("text") ?: ""
                    val timestamp = document.getTimestamp("timestamp")

                    if (sender.isNotEmpty() && text.isNotEmpty() && timestamp != null) {
                        val message = ChatMessage(sender, text, timestamp)
                        messages.add(message)
                    }
                }

                adapter.submitList(messages)
                recyclerView.scrollToPosition(messages.size - 1)
            }
    }

    private fun sendMessage(messageText: String) {
        val messageTimestamp = FieldValue.serverTimestamp()
        val messageSenderId = currentUserUid
        val messageReceiverId = landlordId

        val newMessage = hashMapOf(
            "sender" to messageSenderId,
            "text" to messageText,
            "timestamp" to messageTimestamp
        )

        firestore.collection("conversations")
            .document(conversationId)
            .collection("messages")
            .add(newMessage)
            .addOnSuccessListener {
                // Message sent successfully
            }
            .addOnFailureListener { exception ->
                // Handle the failure to send the message
            }

        // Update conversation data
        val conversationData = hashMapOf(
            "lastMessage" to messageText,
            "lastMessageSenderId" to messageSenderId,
            "lastMessageTimestamp" to messageTimestamp,
            "userIds" to listOf(currentUserUid, landlordId)
        )

        firestore.collection("conversations")
            .document(conversationId)
            .set(conversationData, SetOptions.merge())
            .addOnSuccessListener {
                // Conversation data updated successfully
            }
            .addOnFailureListener { exception ->
                // Handle the failure to update conversation data
            }
    }

}
