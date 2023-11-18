package com.example.kotlindormify

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kotlindormify.PersonalInformationActivity
import com.example.kotlindormify.profile.AboutUs
import com.example.kotlindormify.profile.TermsAndConditions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class Profile3Fragment : Fragment(R.layout.profile_fragment) {
    private lateinit var ivProfilePicture: ImageView
    private lateinit var tvUserName: TextView
    private lateinit var tvEmail: TextView
    private lateinit var userId: String
    private lateinit var backButton: ImageView


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize the views
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)
        tvUserName = view.findViewById(R.id.tvUserName)
        tvEmail = view.findViewById(R.id.tvEmail)

        userId = FirebaseAuth.getInstance().currentUser?.uid ?: ""


        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val userDocument = usersCollection.document(userId)
        val btnPersonalInfo: View = view.findViewById(R.id.Personalinfo)
        val btnTermsAndConditions: View = view.findViewById(R.id.btnTermsAndConditions2)
        val btnAbout: View = view.findViewById(R.id.btnAboutUs2)


        userDocument.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // User data exists, populate the views with user information
                    val userData = documentSnapshot.data
                    val userName = userData?.get("username") as String
                    val userEmail = userData["email"] as String
                    val userProfileImageUrl = userData["profileImageUrl"] as String

                    // Load the user's profile image using a library like Glide or Picasso
                    // For example, using Glide:
                    Glide.with(requireContext())
                        .load(userProfileImageUrl)
                        .into(ivProfilePicture)

                    // Set the user's name and email
                    tvUserName.text = userName
                    tvEmail.text = userEmail
                } else {
                    // Handle the case where user data does not exist
                    // You can show a placeholder image or display an error message
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors while fetching user data
            }


        val btnLogout: Button = view.findViewById(R.id.btnLogout)

        btnLogout.setOnClickListener {
            val alertDialogBuilder = AlertDialog.Builder(requireContext())

            // Set the title and message for the dialog
            alertDialogBuilder.setTitle("Logout")
            alertDialogBuilder.setMessage("Are you sure you want to logout?")

            // Set a positive button and its click listener
            alertDialogBuilder.setPositiveButton("Yes") { _, _ ->
                // User clicked Yes, perform logout
                FirebaseAuth.getInstance().signOut()

                // Clear any session or user data if applicable

                // Navigate to the SignInActivity
                val intent = Intent(requireContext(), SignInActivity::class.java)
                startActivity(intent)
                requireActivity().finish() // Close the current activity
            }

            // Set a negative button and its click listener
            alertDialogBuilder.setNegativeButton("No") { _, _ ->
                // User clicked No, do nothing or dismiss the dialog
            }

            // Create and show the alert dialog
            val alertDialog = alertDialogBuilder.create()
            alertDialog.show()
        }
        // Set a click listener for the Personal Info RelativeLayout
        btnPersonalInfo.setOnClickListener {
            // Call the function to navigate to PersonalInformationActivity
            navigateToPersonalInformation()
        }

        // Set a click listener for the Personal Info RelativeLayout
        btnTermsAndConditions.setOnClickListener {
            // Call the function to navigate to TermsAndConditionsActivity
            navigateToTermsAndConditions()
        }

        btnAbout.setOnClickListener {
            navigateToAbout()
        }

    }

    // Function to navigate to PersonalInformationActivity
    private fun navigateToPersonalInformation() {
        val intent = Intent(requireContext(), PersonalInformationActivity::class.java)
        startActivity(intent)
    }

    // Function to navigate to TermsAndConditionsActivity
    private fun navigateToTermsAndConditions() {
        val intent = Intent(requireContext(), TermsAndConditions::class.java)
        startActivity(intent)
    }

    private fun navigateToAbout() {
        val intent = Intent(requireContext(), AboutUs::class.java)
        startActivity(intent)
    }

}
