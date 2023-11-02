package com.example.kotlindormify

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import com.example.kotlindormify.landlord.Dormitory
import com.google.android.gms.maps.model.LatLng
import com.squareup.picasso.Picasso
import kotlin.math.*


class AllDormitoriesAdapter(private val dormitoriesList: List<Dormitory>) :
    RecyclerView.Adapter<AllDormitoriesAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val DEFAULT_LATITUDE = 14.998027206214473 // Replace with your desired default latitude
        private val DEFAULT_LONGITUDE = 120.65611294250105 // Replace with your desired default longitude

        val textDormName: TextView = itemView.findViewById(R.id.textDormName)
        val textDormPrice: TextView = itemView.findViewById(R.id.textDormPrice)
        val tvRentalTerm: TextView = itemView.findViewById(R.id.tvRentalTerm)
        val tvDistance: TextView = itemView.findViewById(R.id.tvDorm2Distance)

        val dormImage : ImageView = itemView.findViewById(R.id.ivDorm2)
        // Add other views as needed based on your layout

        init {
            // Set a click listener for the item
            itemView.setOnClickListener {
                // Get the clicked dormitory item
                val clickedDormitory = dormitoriesList[adapterPosition]


                // Create a bundle to pass dormitory data to the detail fragment
                val bundle = Bundle()
                bundle.putString("dormName", clickedDormitory.dormName)
                bundle.putString("dormPrice", clickedDormitory.dormPrice)
                bundle.putString("dormitoryId", clickedDormitory.dormitoryId)
                bundle.putString("imageUrl", clickedDormitory.image)
                bundle.putString("landlordId", clickedDormitory.landlordId)
                bundle.putString("qrCodeImageUrl", clickedDormitory.qrCodeImageUrl)
                bundle.putDouble("latitude", clickedDormitory.latitude!!)
                bundle.putDouble("longitude", clickedDormitory.longitude!!)
                bundle.putString("address", clickedDormitory.address)
                bundle.putString("phoneNumber", clickedDormitory.phoneNumber)
                bundle.putString("description", clickedDormitory.description)
                bundle.putString("permitImage", clickedDormitory.permitImage)
                bundle.putInt("dormRooms", clickedDormitory.dormRooms!!)
                bundle.putString("username", clickedDormitory.username)
                bundle.putString("rentalTerm", clickedDormitory.rentalTerm)
                bundle.putString("bathroom", clickedDormitory.bathroom)
                bundle.putString("electric", clickedDormitory.electric)
                bundle.putString("water", clickedDormitory.water)
                bundle.putString("email", clickedDormitory.email)


                val paymentOptionsList = clickedDormitory.paymentOptions?.toList()
                bundle.putStringArrayList("paymentOptions", ArrayList(paymentOptionsList))

                val amenitiesList = clickedDormitory.amenities?.toList()
                bundle.putStringArrayList("amenities", ArrayList(amenitiesList))






                //info of the landlord


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



    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.all_dormitory_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dormitory = dormitoriesList[position]
        holder.textDormName.text = dormitory.dormName
        holder.textDormPrice.text = "₱ ${dormitory.dormPrice}"
        holder.tvRentalTerm.text = dormitory.rentalTerm

        //distance of dorm from dhvsu
        if (dormitory.latitude != null && dormitory.longitude != null) {
            val distance = calculateDistance(
                dormitory.latitude!!,
                dormitory.longitude!!,
                14.998027206214473,
                120.65611294250105
            )
            val formattedDistance = String.format("%.2f", distance)
            holder.tvDistance.text = "${formattedDistance}km. to DHVSU"
        } else {
            // Handle the case where latitude or longitude is null (empty)
            holder.tvDistance.text = "Location not available"
        }




        if (dormitory.image?.isNotEmpty() == true) {
            Picasso.get().load(dormitory.image).into(holder.dormImage)
        } else {
            // Handle the case where there's no image URL provided
            // You can set a default image or hide the ImageView
        }

        // Bind other dormitory data to views as needed based on your layout
    }



    override fun getItemCount(): Int {
        return dormitoriesList.size

    }
    fun calculateDistance(lat1: Double, lon1: Double, lat2: Double, lon2: Double): Double {
        val R = 6371.0 // Earth's radius in kilometers

        // Convert latitude and longitude from degrees to radians
        val lat1Rad = Math.toRadians(lat1)
        val lon1Rad = Math.toRadians(lon1)
        val lat2Rad = Math.toRadians(lat2)
        val lon2Rad = Math.toRadians(lon2)

        // Haversine formula
        val dLat = lat2Rad - lat1Rad
        val dLon = lon2Rad - lon1Rad

        val a = sin(dLat / 2).pow(2.0) + cos(lat1Rad) * cos(lat2Rad) * sin(dLon / 2).pow(2.0)
        val c = 2 * atan2(sqrt(a), sqrt(1 - a))

        // Calculate the distance
        return R * c
    }
}
