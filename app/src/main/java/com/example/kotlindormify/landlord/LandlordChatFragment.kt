package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.ChatMessage
import com.example.kotlindormify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.squareup.picasso.Picasso
import java.sql.Timestamp

class LandlordChatFragment : Fragment() {

    private lateinit var firestore: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var userId: String? = null
    private lateinit var conversationId: String

    private var Tenant: String? = null
    private lateinit var nameOfChat: TextView
    private lateinit var profilePic: ImageView
    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: LandlordChatAdapter
    private val currentUserUid = FirebaseAuth.getInstance().currentUser?.uid
    private val db = FirebaseFirestore.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.landlord_messages_fragment, container, false)

        // Initialize RecyclerView
        recyclerView = view.findViewById(R.id.recyclerView)
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        adapter = LandlordChatAdapter()
        recyclerView.adapter = adapter
// Assuming nameOfChat is a TextView declared in your activity or fragment
        nameOfChat = view.findViewById(R.id.nameOfChat)
        // Get conversation ID passed from LandlordConversationsFragment
        val conversationId = arguments?.getString("conversationId")
        userId = arguments?.getString("sender")
        Tenant = arguments?.getString("tenant")

        val username = arguments?.getString("username")
        val userPic = arguments?.getString("userPic")

        nameOfChat = view.findViewById(R.id.nameOfChat)
        profilePic = view.findViewById(R.id.ivTopProfilePictureChat)

        nameOfChat.text = username
        Picasso.get().load(userPic)
            .error(R.drawable.error_image) // Add an error placeholder drawable
            .into(profilePic)


        // Initialize Firebase Firestore and Firebase Authentication
        firestore = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        // Load messages for the given conversation ID
        loadChatMessages(conversationId)
        // Send button click listener
        val sendButton = view.findViewById<Button>(R.id.sendButton)
        val messageEditText = view.findViewById<EditText>(R.id.messageEditText)
        sendButton.setOnClickListener {
            val messageText = messageEditText.text.toString().trim()
            if (messageText.isNotEmpty()) {
                sendMessage(messageText, conversationId)
                messageEditText.text.clear()
            }
        }

        userId = arguments?.getString("sender")

        // Call loadDetails to load username
        //loadDetails(userId)

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
                        val receiver =
                            document.getString("receiver") ?: ""  // Add this line for receiver
                        val text = document.getString("text") ?: ""
                        val timestamp = document.getTimestamp("timestamp")

                        if (sender.isNotEmpty() && receiver.isNotEmpty() && text.isNotEmpty() && timestamp != null) {
                            val message = ChatMessage(sender, receiver, text, timestamp)
                            messages.add(message)
                        }
                    }



                    adapter.submitList(messages)
                    recyclerView.scrollToPosition(messages.size - 1)
                }
            }
        }
    }




    private fun sendMessage(messageText: String, conversationId: String?) {
        if (conversationId != null) {



                    // Retrieve Tenant from the conversation
                    firestore.collection("conversations")
                        .document(conversationId)
                        .get()
                        .addOnSuccessListener { documentSnapshot ->
                            if (documentSnapshot.exists()) {
                                val tenant = documentSnapshot.getString("Tenant")

                                // Check if the Tenant value is not null before proceeding
                                if (tenant != null) {
                                    val messageTimestamp = FieldValue.serverTimestamp()
                                    val messageSenderId = currentUserUid

                                    val newMessage = hashMapOf(
                                        "sender" to messageSenderId,
                                        "text" to messageText,
                                        "receiver" to tenant, // Use the Tenant as the receiver
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
                                        "lastMessageReceiverId" to tenant, // Update the receiver ID
                                        "lastMessageTimestamp" to messageTimestamp
                                    )

                                    db.collection("conversations")
                                        .document(conversationId)
                                        .set(conversationData, SetOptions.merge())
                                        .addOnSuccessListener {
                                            // Conversation data updated successfully
                                        }
                                        .addOnFailureListener { exception ->
                                            // Handle the failure to update conversation data
                                        }
                                } else {
                                    // Handle the case where Tenant is null
                                }
                            } else {
                                // Handle the case where the conversation document does not exist
                            }
                        }
                        .addOnFailureListener { exception ->
                            // Handle the failure to retrieve conversation data
                        }
                }
        }

   /* private fun loadDetails(Tenant: String?) {
        if (Tenant != null) {
            // Retrieve Tenant from the conversation
            firestore.collection("conversations")
                .document(Tenant)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    if (documentSnapshot.exists()) {
                        val tenant = documentSnapshot.getString("Tenant")

                        // Check if the Tenant value is not null before proceeding
                        if (tenant != null) {
                            // Now, retrieve user details from the "users" collection
                            firestore.collection("users")
                                .document(tenant)
                                .get()
                                .addOnSuccessListener { userSnapshot ->
                                    if (userSnapshot.exists()) {
                                        val username = userSnapshot.getString("username")
                                        val profileImageUrl = userSnapshot.getString("profileImageUrl")

                                        if (username != null) {
                                            nameOfChat.text = username
                                            // You can also use profileImageUrl as needed
                                        }
                                    } else {
                                        // Handle the case where the user document does not exist
                                    }
                                }
                                .addOnFailureListener { exception ->
                                    // Handle the failure to retrieve user data
                                }
                        } else {
                            // Handle the case where Tenant is null
                        }
                    } else {
                        // Handle the case where the conversation document does not exist
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle the failure to retrieve conversation data
                }
        }
    }*/


}
