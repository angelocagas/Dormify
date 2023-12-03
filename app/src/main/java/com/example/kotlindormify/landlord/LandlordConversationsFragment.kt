package com.example.kotlindormify.landlord

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.example.kotlindormify.databinding.LandlordConversationsFragmentBinding
import com.example.kotlindormify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class LandlordConversationsFragment : Fragment(), LandlordConversationsAdapter.OnItemClickListener {
    private lateinit var recyclerView: RecyclerView
    private lateinit var binding: LandlordConversationsFragmentBinding
    private lateinit var conversationsAdapter: LandlordConversationsAdapter
    private lateinit var conversationsList: MutableList<Conversation>
    private lateinit var currentUserUid: String // Declare currentUserUid as a member variable
    private lateinit var swipeRefreshLayout: SwipeRefreshLayout
    private lateinit var progressBar: ProgressBar
    private lateinit var textNoConversations: TextView

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = LandlordConversationsFragmentBinding.inflate(inflater, container, false)

        // Initialize the SwipeRefreshLayout
        swipeRefreshLayout = binding.swapRefreshLayout

        progressBar = binding.progressBar

        // Initialize the RecyclerView and adapter
        recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        textNoConversations = binding.textNoConversations
        conversationsList = mutableListOf()
        conversationsAdapter = LandlordConversationsAdapter(conversationsList, this)
        recyclerView.adapter = conversationsAdapter

        // Initialize Firebase Authentication
        currentUserUid = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Set up the swipe-to-refresh functionality
        swipeRefreshLayout.setOnRefreshListener {
            refreshConversations()
        }
        swipeRefreshLayout.setColorSchemeColors(
            ContextCompat.getColor(requireContext(), R.color.violet)
        )

        // Show the progress bar while fetching data
        progressBar.visibility = View.VISIBLE

        // Load conversations from Firestore
        loadConversationsFromFirestore()

        return binding.root
    }

    private fun loadConversationsFromFirestore() {
        // Reference to your "conversations" collection in Firestore
        val firestore = FirebaseFirestore.getInstance()
        val conversationsRef = firestore.collection("conversations")

        // Query the Firestore database to find conversations for the current user
        conversationsRef.whereArrayContains("userIds", currentUserUid)
            .get()
            .addOnSuccessListener { querySnapshot ->
                // Hide the progress bar as data fetching is complete
                progressBar.visibility = View.GONE

                if (!querySnapshot.isEmpty) {
                    // Iterate through matching conversations
                    for (conversationDocument in querySnapshot.documents) {
                        // Retrieve individual conversation data
                        val conversationTitle =
                            conversationDocument.id // You may need to adapt this based on your data model
                        val lastMessage = conversationDocument.getString("lastMessage") ?: ""
                        val Tenant = conversationDocument.getString("Tenant") ?: ""
                        val lastMessageTimestamp = conversationDocument.getTimestamp("lastMessageTimestamp")

                        // Add conversation to the list

                        // Fetch username from "users" collection
                        val usersCollection = FirebaseFirestore.getInstance().collection("users")
                        val userQuery = usersCollection.whereEqualTo("userId", Tenant)

                        userQuery.get().addOnSuccessListener { userQuerySnapshot ->
                            if (!userQuerySnapshot.isEmpty) {
                                val username = userQuerySnapshot.documents[0].getString("username")  ?: ""
                                val userPic = userQuerySnapshot.documents[0].getString("profileImageUrl")  ?: ""
                                conversationsList.add(Conversation(conversationTitle, lastMessage, Tenant, username, userPic, lastMessageTimestamp!!
                                ))
                                // Notify the adapter that data has changed
                                conversationsAdapter.notifyDataSetChanged()

                                // Now you have the username, you can use it as needed
                                // For example, you might want to update your Conversation object or do something else
                                // conversationsList.add(Conversation(conversationTitle, lastMessage, tenantId, username))
                            }

                        }.addOnFailureListener { exception ->
                            // Handle the failure
                            // Log.d(TAG, "Error getting documents: ", exception)
                        }


                    }


                } else {
                    // Handle the case when there are no matching conversations
                    // For example, display a message or handle it as needed
                    textNoConversations.visibility = View.VISIBLE
                }

                // Stop the refreshing indicator
                swipeRefreshLayout.isRefreshing = false
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur during the conversation query
                // You can log or display an error message here
                progressBar.visibility = View.GONE
                // Stop the refreshing indicator even in case of failure
                swipeRefreshLayout.isRefreshing = false
            }
    }

    private fun refreshConversations() {
        // Clear the existing conversationsList
        conversationsList.clear()

        // Load conversations from Firestore
        loadConversationsFromFirestore()
    }

    override fun onItemClick(conversation: Conversation) {
        // Handle the item click here
        // Create a new instance of LandlordChatFragment and pass the conversation ID
        val chatFragment = LandlordChatFragment()
        val bundle = Bundle()
        bundle.putString("conversationId", conversation.title) // Assuming the conversation title is used as an ID
        bundle.putString("Tenant", conversation.Tenant)
        bundle.putString("userPic", conversation.userPic)
        bundle.putString("username", conversation.username) // Assuming the conversation title is used as an ID
        chatFragment.arguments = bundle

        // Navigate to the chat fragment
        requireActivity().supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, chatFragment)
            .addToBackStack(null)
            .commit()
    }
}
