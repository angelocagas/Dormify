package com.example.kotlindormify.landlord

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import com.example.kotlindormify.R
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kotlindormify.LandlordProfileFragment
import com.example.kotlindormify.PrefManager
import com.example.kotlindormify.databinding.ActivityLandlordDashboardBinding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class LandlordDashboardActivity : AppCompatActivity() {
    private lateinit var binding: ActivityLandlordDashboardBinding
    private lateinit var userId: String
    private lateinit var ivProfilePicture: ImageView
    var userEmail: String? = null // Initialize it as null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLandlordDashboardBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val prefManager = PrefManager(this) // Initialize PrefManager with the context
        userEmail = prefManager.getUserEmail() // Set the class-level property userEmail
        ivProfilePicture = binding.ivTopProfilePicture

        val auth = FirebaseAuth.getInstance()
        val currentUser = auth.currentUser
        userId = currentUser?.uid ?: ""

        val usersCollection = FirebaseFirestore.getInstance().collection("users")
        val userDocument = usersCollection.document(userId)

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
                    Glide.with(this)
                        .load(userProfileImageUrl)
                        .into(ivProfilePicture)


                } else {
                    // Handle the case where user data does not exist
                    // You can show a placeholder image or display an error message
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors while fetching user data
            }



        val unitsFragment = LandlordUnitsFragment()
        val tenantsFragment = TenantsListFragment()
        val messagesFragment = LandlordConversationsFragment()
        val profileFragment = LandlordProfileFragment()

        setCurrentFragment(unitsFragment)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {

                R.id.landlord_menu_units -> {
                    setCurrentFragment(unitsFragment)
                    binding.tvDormify3.text = "Dormitory"
                    binding.cardViewImage.visibility = View.VISIBLE
                }

                R.id.landlord_menu_tenants -> {
                    setCurrentFragment(tenantsFragment)
                    binding.tvDormify3.text = "Tenants"
                    binding.cardViewImage.visibility = View.VISIBLE
                }

                R.id.landlord_menu_messages -> {
                    setCurrentFragment(messagesFragment)
                    binding.tvDormify3.text = "Messages"
                    binding.cardViewImage.visibility = View.VISIBLE
                }

                R.id.landlord_menu_profile -> {
                    setCurrentFragment(profileFragment)
                    binding.tvDormify3.text = "Profile"
                    binding.cardViewImage.visibility = View.GONE
                }

            }
            true
        }

        ivProfilePicture.setOnClickListener {
            setCurrentFragment(profileFragment)
            binding.tvDormify3.text = "Profile"
            binding.bottomNavigation.menu.findItem(R.id.landlord_menu_profile).isChecked = true
            binding.cardViewImage.visibility = View.GONE
        }
    }

    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }

    override fun onBackPressed() {
        val builder = AlertDialog.Builder(this)
        builder.setMessage("Do you want to exit?")
            .setPositiveButton("Yes") { _, _ ->
                // Handle user confirmation to exit the app
                finish()
            }
            .setNegativeButton("No") { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            .show()
    }


}