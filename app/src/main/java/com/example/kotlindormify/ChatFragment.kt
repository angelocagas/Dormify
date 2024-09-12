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
import java.sql.Timestamp

class ChatFragment : Fragment() {

    // Firebase variables
    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth

    // User and conversation information
    private lateinit var currentUserUid: String
    private lateinit var conversationId: String
    private var landlordId: String? = null
    private var dormName: String? = null

    // RecyclerView and Adapter
    private var adapter = ChatAdapter()
    private lateinit var recyclerView: RecyclerView
    private lateinit var tvDormName: TextView

    // ...

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat_fragment, container, false)

        // Retrieve arguments passed to the fragment
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

        // Set dorm name in the TextView
        tvDormName = view.findViewById(R.id.nameOfChat)
        tvDormName.text = dormName

        // Load chat messages
        loadChatMessages()

        // Send button click listener
        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val messageEditText = view.findViewById<EditText>(R.id.messageEditText)
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                // Send the message and clear the input field
                sendMessage(messageText)
                messageEditText.text.clear()
            }
        }

        return view
    }

    // Function to load chat messages from Firestore
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
                    // Extract message details from Firestore document
                    val sender = document.getString("sender") ?: ""
                    val receiver = document.getString("receiver") ?: ""
                    val text = document.getString("text") ?: ""
                    val timestamp = document.getTimestamp("timestamp")

                    if (sender.isNotEmpty() && receiver.isNotEmpty() && text.isNotEmpty() && timestamp != null) {
                        // Pass receiver information to ChatMessage constructor
                        val message = ChatMessage(sender, receiver, text, timestamp)
                        messages.add(message)
                    }
                }

                // Update the RecyclerView with the new list of messages
                adapter.submitList(messages)
                recyclerView.scrollToPosition(messages.size - 1)
            }



    }

    // Function to send a new message to Firestore
    private fun sendMessage(messageText: String) {
        val messageTimestamp = FieldValue.serverTimestamp()

        // Create a new message data structure
        val newMessage = hashMapOf(
            "sender" to currentUserUid,
            "text" to messageText,
            "receiver" to landlordId,
            "timestamp" to messageTimestamp
        )

        // Add the new message to the "messages" collection in Firestore
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

        // Update conversation data with information about the last message
        val conversationData = hashMapOf(
            "lastMessage" to messageText,
            "lastMessageSenderId" to currentUserUid,
            "lastMessagReceiverId" to landlordId,
            "lastMessageTimestamp" to messageTimestamp,
            "Tenant" to currentUserUid,
            "Landlord" to landlordId,
            "userIds" to listOf(currentUserUid, landlordId)
        )

        // Update the conversation data in the "conversations" collection in Firestore
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
