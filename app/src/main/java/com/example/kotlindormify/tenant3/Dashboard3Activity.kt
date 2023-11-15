package com.example.kotlindormify.tenant3

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.bumptech.glide.Glide
import com.example.kotlindormify.PrefManager
import com.example.kotlindormify.R
import com.example.kotlindormify.databinding.ActivityDashboard3Binding
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore


class Dashboard3Activity : AppCompatActivity() {

    private lateinit var binding: ActivityDashboard3Binding
    private lateinit var userId: String
    private lateinit var ivProfilePicture: ImageView


    var userEmail: String? = null // Initialize it as null


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityDashboard3Binding.inflate(layoutInflater)
        setContentView(binding.root)
        ivProfilePicture = binding.ivTopProfilePicture

        window.decorView.systemUiVisibility =
            (View.SYSTEM_UI_FLAG_FULLSCREEN or View.SYSTEM_UI_FLAG_HIDE_NAVIGATION)


        val prefManager = PrefManager(this) // Initialize PrefManager with the context
        userEmail = prefManager.getUserEmail() // Set the class-level property userEmail


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


        val homeFragment = DashboardPaymentTenantFragment()
        val paymentFragment = PaymentFragment()
        val chatFragment = Chat3Fragment()
        val profileFragment = Profile3Fragment()

        setCurrentFragment(homeFragment)

        binding.bottomNavigation.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.menu_home -> {
                    setCurrentFragment(homeFragment)
                    binding.tvDormify3.text = "Dormify"
                    binding.cardViewImage.visibility = View.VISIBLE
                }

                R.id.menu_payment -> {
                    setCurrentFragment(paymentFragment)
                    binding.tvDormify3.text = "Payment"
                    binding.cardViewImage.visibility = View.VISIBLE
                }
                R.id.menu_chat -> {
                    setCurrentFragment(chatFragment)
                    binding.tvDormify3.text = "Messages"
                    binding.cardViewImage.visibility = View.VISIBLE
                }


                R.id.menu_person -> {
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
            binding.bottomNavigation.menu.findItem(R.id.menu_person).isChecked = true
            binding.cardViewImage.visibility = View.GONE
        }



    }



    private fun setCurrentFragment(fragment: Fragment) =
        supportFragmentManager.beginTransaction().apply {
            replace(R.id.fragment_container, fragment)
            commit()
        }

    override fun onBackPressed() {
        val fragmentTransaction = supportFragmentManager.beginTransaction()

        if (supportFragmentManager.backStackEntryCount > 0) {
            // Pop the fragment transaction if there are fragments in the back stack
            supportFragmentManager.popBackStack()
        } else {
            // Show exit confirmation dialog if there are no fragments in the back stack
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


}
