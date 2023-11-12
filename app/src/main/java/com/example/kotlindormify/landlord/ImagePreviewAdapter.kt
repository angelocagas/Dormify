package com.example.kotlindormify.landlord

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.kotlindormify.R

class ImagePreviewAdapter(private val imageUris: List<Uri>) :
    RecyclerView.Adapter<LandlordAddDormitoryFragment.ImagePreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LandlordAddDormitoryFragment.ImagePreviewViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.image_preview_item, parent, false)
        return LandlordAddDormitoryFragment.ImagePreviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: LandlordAddDormitoryFragment.ImagePreviewViewHolder, position: Int) {
        val imageUri = imageUris[position]


        // Load the image from the URI and set it to the ImageView
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

}