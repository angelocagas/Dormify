package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.ChatMessage
import com.example.kotlindormify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

class LandlordChatFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LandlordChatAdapter
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.chat_fragment, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LandlordChatAdapter()
        recyclerView.adapter = adapter

        // Get conversation ID passed from LandlordConversationsFragment
        val conversationId = arguments?.getString("conversationId")

        // Load messages for the given conversation ID
        loadChatMessages(conversationId)

        return view
    }

    private fun loadChatMessages(conversationId: String?) {
        if (conversationId != null) {
            val messagesRef = db.collection("conversations")
                .document(conversationId)
                .collection("messages")
                .orderBy("timestamp")

            messagesRef.addSnapshotListener { querySnapshot, _ ->
                if (querySnapshot != null) {
                    val messages = mutableListOf<ChatMessage>()
                    for (document in querySnapshot.documents) {
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
        }
    }


}
