package com.example.kotlindormify.landlord

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlindormify.R

class ImageAdapter(private val imageUris: List<Uri>) :
    RecyclerView.Adapter<ImageAdapter.ImageViewHolder>() {

    // Inner ViewHolder class to hold the ImageView
    class ImageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }

    // Create and return a new ViewHolder for the item view
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImageViewHolder {
        // Inflate the item view layout
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.image_item, parent, false)
        return ImageViewHolder(itemView)
    }

    // Bind data to the ViewHolder at the specified position
    override fun onBindViewHolder(holder: ImageViewHolder, position: Int) {
        // Get the URI of the image at the current position
        val imageUri = imageUris[position]

        // Use Glide to load the image from the URI into the ImageView
        Glide.with(holder.itemView)
            .load(imageUri)
            .centerCrop()
            .into(holder.imageView)
    }

    // Return the number of items in the data set
    override fun getItemCount(): Int {
        return imageUris.size
    }
}
