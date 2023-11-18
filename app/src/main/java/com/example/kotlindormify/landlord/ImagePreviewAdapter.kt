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
    RecyclerView.Adapter<ImagePreviewAdapter.ImagePreviewViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ImagePreviewViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val itemView = inflater.inflate(R.layout.image_item, parent, false)
        return ImagePreviewViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ImagePreviewViewHolder, position: Int) {
        val imageUri = imageUris[position]
        Glide.with(holder.itemView.context)
            .load(imageUri)
            .into(holder.imageView)
    }

    override fun getItemCount(): Int {
        return imageUris.size
    }

    class ImagePreviewViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.imageView)
    }
}
