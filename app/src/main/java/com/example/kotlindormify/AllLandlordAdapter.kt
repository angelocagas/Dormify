package com.example.kotlindormify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlindormify.landlord.Dormitory
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso



class AllLandlordAdapter (private val usersList: List<Users>) :
    RecyclerView.Adapter<AllLandlordAdapter.ViewHolder>() {
    private lateinit var userId: String


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        val textemail: TextView = itemView.findViewById(R.id.tvEmail)
        val textusername: TextView = itemView.findViewById(R.id.tvUserName)
        val textphonenumber : TextView = itemView.findViewById(R.id.etPhoneNumber)
        // Add other views as needed based on your layout




        init {
            // Set a click listener for the item
            itemView.setOnClickListener {
                // Get the clicked dormitory item
                val clickeduser = usersList[adapterPosition]


                // Create a bundle to pass dormitory data to the detail fragment
                val bundle = Bundle()
                bundle.putString("email", clickeduser.email)
                bundle.putString("username", clickeduser.username)
                bundle.putString("phoneNumber", clickeduser.phoneNumber)
              //  bundle.putString("profileImageUrl", clickeduser.profileImageUrl)


                // Add other dormitory details to the bundle as needed

                // Create and navigate to the detail fragment
                val detailFragment = DormitoryDetailFragment()
                detailFragment.arguments = bundle
                val fragmentManager = (itemView.context as AppCompatActivity).supportFragmentManager
                fragmentManager.beginTransaction()
                    .replace(R.id.fragment_container, detailFragment)
                    .addToBackStack(null) // Optional: Add to back stack for fragment navigation
                    .commit()
            }
        }




    }
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllLandlordAdapter.ViewHolder {
      val itemView = LayoutInflater.from(parent.context)
           .inflate(R.layout.fragment_request_detail, parent, false)
      return ViewHolder(itemView)
    }

   override fun onBindViewHolder(holder: AllLandlordAdapter.ViewHolder, position: Int) {
        val Users = usersList[position]
       holder.textusername.text = Users.username
       holder.textemail.text = Users.email
       holder.textphonenumber.text = Users.phoneNumber

    //  if (Users.image?.isNotEmpty() == true) {
    //  Picasso.get().load(Users.image).into(holder.dormImage)
    //          } else {
    //      Handle the case where there's no image URL provided
    //       You can set a default image or hide the ImageView
    //       }

        // Bind other dormitory data to views as needed based on your layout
    }


    override fun getItemCount(): Int {
        return usersList.size

    }

}

