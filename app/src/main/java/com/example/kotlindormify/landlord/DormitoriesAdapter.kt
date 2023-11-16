package com.example.kotlindormify.landlord

import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.os.Bundle
import android.os.Environment
import android.text.InputFilter
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.R
import com.google.firebase.firestore.FirebaseFirestore
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition  // Import for Transition
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class DormitoriesAdapter(private val dormitoriesList: MutableList<Dormitory>) :
    RecyclerView.Adapter<DormitoriesAdapter.ViewHolder>() {
    private val availableRoomsMap: MutableMap<String, Int> = mutableMapOf()
    private val occupiedRoomsMap: MutableMap<String, Int> = mutableMapOf()

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val dormNameTextView: TextView = itemView.findViewById(R.id.textDormName)
        val dormPriceTextView: TextView = itemView.findViewById(R.id.textDormPrice)
        val dormNumOfRooms: TextView = itemView.findViewById(R.id.tvUnits)
        val btnEdit: ImageButton = itemView.findViewById(R.id.ibEdit)
        val btnDelete: ImageButton = itemView.findViewById(R.id.ibDelete)
        val btnRequests: Button = itemView.findViewById(R.id.btnRequests)
        val btnView: Button = itemView.findViewById(R.id.btnView)
        val availableRooms: TextView = itemView.findViewById(R.id.tvAvailable)
        val occupiedRooms: TextView = itemView.findViewById(R.id.tvOccupied)
        val qrCodeImage: ImageView = itemView.findViewById(R.id.ivQrCode)
    }

    interface OnItemClickListener {
        fun onDeleteClick(position: Int)
    }

    private var mListener: OnItemClickListener? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.dormitory_item_layout, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val dormitory = dormitoriesList[position]

        // Bind data to the views
        holder.dormNameTextView.text = dormitory.dormName
        holder.dormPriceTextView.text = "₱${dormitory.dormPrice.toString()}"
        holder.dormNumOfRooms.text = "${dormitory.dormRooms.toString()} rooms"

        // Call fetchPendingRequestsCount with a callback
        fetchPendingRequestsCount(dormitory.dormitoryId) { pendingRequestsCount ->
            // Update the pending requests count in the UI
            holder.btnRequests.text = "${pendingRequestsCount} pending requests"
        }


        // Check if counts are available for this dormitory
        if (availableRoomsMap.containsKey(dormitory.dormitoryId) && occupiedRoomsMap.containsKey(
                dormitory.dormitoryId
            )
        ) {
            val availableRoomsCount = availableRoomsMap[dormitory.dormitoryId]!!
            val occupiedRoomsCount = occupiedRoomsMap[dormitory.dormitoryId]!!
            holder.availableRooms.text = "${availableRoomsCount} available"
            holder.occupiedRooms.text = "${occupiedRoomsCount} occupied"

            // Load the QR code image into the ImageView using Glide
            dormitory.qrCodeImageUrl?.let { imageUrl ->
                Glide.with(holder.itemView)
                    .load(imageUrl)
                    .placeholder(R.drawable.rectangle_radius_super_light_gray) // Placeholder image while loading
                    .into(holder.qrCodeImage)
            }
        } else {
            holder.availableRooms.text = "Loading..."
            holder.occupiedRooms.text = "Loading..."
            // Track room status for this dormitory
            trackRoomStatus(dormitory.dormitoryId)
        }

        holder.btnEdit.setOnClickListener {
            val builder = AlertDialog.Builder(holder.itemView.context)
            builder.setTitle("Update Dormitory Price")

            // Set up the input
            val input = EditText(holder.itemView.context)
            input.inputType = InputType.TYPE_CLASS_NUMBER
            input.setText("${dormitory.dormPrice}")

            // Set a maximum length filter
            val maxLength = 5
            val filters = arrayOf<InputFilter>(InputFilter.LengthFilter(maxLength))
            input.filters = filters

            builder.setView(input)

            // Set up the buttons
            builder.setPositiveButton("Update") { _, _ ->
                // Show a confirmation dialog before updating the price
                AlertDialog.Builder(holder.itemView.context)
                    .setMessage("Are you sure you want to update the dormitory price?")
                    .setPositiveButton("Yes") { _, _ ->
                        // Update the price when the positive button is clicked
                        val newPrice = input.text.toString()
                        if (newPrice.isNotBlank()) {
                            // Handle the case when the input is not empty
                            updatePrice(newPrice, dormitory.dormitoryId, holder.itemView.context)
                        } else {
                            // Handle the case when the input is empty
                            Toast.makeText(holder.itemView.context, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                        }
                    }
                    .setNegativeButton("No") { _, _ ->
                        // Handle the case when the user cancels the update
                    }
                    .create()
                    .show()
            }.setNegativeButton("Cancel") { _, _ ->
                // Handle the case when the user cancels the update
            }
            builder.show()
        }


        holder.btnRequests.setOnClickListener {
            // Open the RentalRequests ListFragment and pass the dormitory ID
            val fragment = RentalRequestsListFragment()
            val bundle = Bundle()
            bundle.putString("dormitoryId", dormitory.dormitoryId)
            fragment.arguments = bundle

            val transaction = (holder.itemView.context as AppCompatActivity)
                .supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }

        holder.btnDelete.setOnClickListener {
            // Call the deleteDormitory function to remove the dormitory from Firestore
            mListener?.onDeleteClick(holder.adapterPosition)
        }

        holder.qrCodeImage.setOnLongClickListener {
            val imageUrl = dormitory.qrCodeImageUrl
            if (imageUrl != null) {
                // Download the image and save it to the device's storage
                downloadImage(holder.itemView.context, imageUrl)
            } else {
                // Handle the case when the image URL is not available
                Toast.makeText(
                    holder.itemView.context,
                    "Image URL not available",
                    Toast.LENGTH_SHORT
                ).show()
            }
            true // Return true to indicate that the long click event is consumed
        }

        holder.btnView.setOnClickListener {
            // Open the RoomListFragment and pass the dormitory ID
            val fragment = RoomListFragment()
            val bundle = Bundle()
            bundle.putString("dormitoryId", dormitory.dormitoryId)
            fragment.arguments = bundle

            val transaction = (holder.itemView.context as AppCompatActivity)
                .supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, fragment)
            transaction.addToBackStack(null)
            transaction.commit()
        }
    }

    override fun getItemCount(): Int {
        return dormitoriesList.size
    }

    fun deleteDormitory(position: Int) {
        // Check if the position is valid
        if (position >= 0 && position < dormitoriesList.size) {
            val dormitory = dormitoriesList[position]

            // Reference to the Firestore collection containing dormitories
            val firestore = FirebaseFirestore.getInstance()
            val dormitoriesRef = firestore.collection("dormitories")

            // Reference to the specific dormitory document to delete
            val dormitoryDocRef = dormitoriesRef.document(dormitory.dormitoryId)

            // Delete the dormitory document and its subcollections
            // First, delete the subcollections
            val subcollectionName = "rooms" // Replace with your actual subcollection name
            val subcollectionRef = dormitoryDocRef.collection(subcollectionName)

            subcollectionRef.get()
                .addOnSuccessListener { querySnapshot ->
                    val batch = firestore.batch()

                    for (document in querySnapshot) {
                        val documentRef = subcollectionRef.document(document.id)
                        batch.delete(documentRef)
                    }

                    // Now, delete the main dormitory document
                    batch.delete(dormitoryDocRef)

                    // Commit the batch
                    batch.commit()

                    // Remove the dormitory from the list
                    dormitoriesList.removeAt(position)
                    // Notify the adapter that the item is removed
                    notifyItemRemoved(position)

                    // Remove the dormitory from the savedDormitories field of users
                    removeDormitoryFromSavedUsers(dormitory.dormitoryId)
                }
        }
    }

    private fun removeDormitoryFromSavedUsers(dormitoryId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val usersRef = firestore.collection("users")

        // Query users who have saved the dormitory
        usersRef.whereArrayContains("savedDormitories", dormitoryId)
            .get()
            .addOnSuccessListener { querySnapshot ->
                val batch = firestore.batch()

                for (userDocument in querySnapshot) {
                    val updatedSavedDormitories = (userDocument["savedDormitories"] as? List<String>
                        ?: emptyList()).toMutableList()
                    updatedSavedDormitories.remove(dormitoryId)
                    batch.update(
                        userDocument.reference,
                        "savedDormitories",
                        updatedSavedDormitories
                    )
                }

                // Commit the batch to update all affected user documents
                batch.commit()
            }
    }


    fun setOnItemClickListener(listener: OnItemClickListener) {
        mListener = listener
    }

    private fun trackRoomStatus(dormitoryId: String) {
        val firestore = FirebaseFirestore.getInstance()
        val dormitoryRef = firestore.collection("dormitories").document(dormitoryId)
        val roomsRef = dormitoryRef.collection("rooms")

        // Query rooms with availability set to "available"
        val availableQuery = roomsRef.whereEqualTo("availability", "available")
        availableQuery.get().addOnSuccessListener { availableSnapshot ->
            val availableRoomsCount = availableSnapshot.size()

            // Query rooms with availability set to "occupied"
            val occupiedQuery = roomsRef.whereEqualTo("availability", "occupied")
            occupiedQuery.get().addOnSuccessListener { occupiedSnapshot ->
                val occupiedRoomsCount = occupiedSnapshot.size()

                // Update the counts in the adapter
                setAvailableRoomsForDormitory(dormitoryId, availableRoomsCount)
                setOccupiedRoomsForDormitory(dormitoryId, occupiedRoomsCount)
            }
        }
    }

    private fun setAvailableRoomsForDormitory(dormitoryId: String, availableRooms: Int) {
        availableRoomsMap[dormitoryId] = availableRooms
        notifyDataSetChanged() // Notify the adapter to update the UI
    }

    private fun setOccupiedRoomsForDormitory(dormitoryId: String, occupiedRooms: Int) {
        occupiedRoomsMap[dormitoryId] = occupiedRooms
        notifyDataSetChanged() // Notify the adapter to update the UI
    }

    fun downloadImage(context: Context, imageUrl: String) {
        // Show a confirmation dialog
        showConfirmationDialog(context) { downloadConfirmed ->
            if (downloadConfirmed) {
                Glide.with(context)
                    .asBitmap()
                    .load(imageUrl)
                    .into(object : CustomTarget<Bitmap>() {
                        override fun onResourceReady(
                            resource: Bitmap,
                            transition: Transition<in Bitmap>?
                        ) {
                            try {
                                // Get the internal storage directory
                                val internalStorageDir = context.getExternalFilesDir(null)

                                // Check if the directory exists, or create it if it doesn't
                                if (internalStorageDir != null && !internalStorageDir.exists()) {
                                    internalStorageDir.mkdirs()
                                }

                                // Generate a unique filename for the downloaded image
                                val fileName = "QRCode_${System.currentTimeMillis()}.png"
                                val file = File(internalStorageDir, fileName)

                                // Save the image to the internal storage
                                val fos = FileOutputStream(file)
                                resource.compress(Bitmap.CompressFormat.PNG, 100, fos)
                                fos.close()

                                // Notify the user that the image has been saved
                                showImageSavedDialog(context, file.absolutePath)

                            } catch (e: IOException) {
                                e.printStackTrace()
                                // Handle the case when the image fails to save
                            }
                        }

                        override fun onLoadCleared(placeholder: Drawable?) {
                            // Handle the case when the image load is canceled or cleared
                        }
                    })
            }
        }
    }

    private fun showConfirmationDialog(context: Context, callback: (Boolean) -> Unit) {
        AlertDialog.Builder(context)
            .setMessage("Do you want to download the QR code?")
            .setPositiveButton("Yes") { _, _ ->
                callback(true)
            }
            .setNegativeButton("No") { _, _ ->
                callback(false)
            }
            .create()
            .show()
    }

    private fun showImageSavedDialog(context: Context, filePath: String) {
        AlertDialog.Builder(context)
            .setMessage("Image saved to $filePath")
            .setPositiveButton("Okay") { _, _ ->
                // Dismiss the dialog when "Okay" is clicked
            }
            .create()
            .show()
    }

    private fun fetchPendingRequestsCount(dormitoryId: String, callback: (Int) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val requestsRef = firestore
            .collection("dormitories")
            .document(dormitoryId)
            .collection("rental_requests")

        // Query pending requests (where status is "pending")
        val pendingRequestsQuery = requestsRef.whereEqualTo("status", "pending")

        pendingRequestsQuery.get()
            .addOnSuccessListener { querySnapshot ->
                // Get the count of pending requests
                val pendingRequestsCount = querySnapshot.size()

                // Call the callback with the pending requests count
                callback(pendingRequestsCount)
            }
    }

    private fun updatePrice(newPrice: String, dormId: String, context: Context) {
        // Reference to the Firestore collection containing dormitories
        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        // Reference to the specific dormitory document to update
        val dormitoryDocRef = dormitoriesRef.document(dormId)

        // Convert the new price to a String
        val stringNewPrice = newPrice.toString()

        // Update the dormitory document with the new price
        dormitoryDocRef
            .update("price", stringNewPrice)
            .addOnSuccessListener {
                // Successfully updated the price
                // Show an alert message
                val successMessage = "Dormitory price updated successfully."
                showAlertDialog("Success", successMessage, context)
                notifyDataSetChanged() // Notify the adapter to update the UI
            }
            .addOnFailureListener { e ->
                // Handle the failure to update the price
                // You might want to log the error or show an error message to the user
                val errorMessage = "Failed to update dormitory price"
                showAlertDialog("Error", errorMessage, context)
            }
        notifyDataSetChanged() // Notify the adapter to update the UI
    }



    private fun showAlertDialog(title: String, message: String, context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle(title)
        builder.setMessage(message)
        builder.setPositiveButton("OK") { _, _ -> }

        val dialog = builder.create()
        dialog.show()
    }







}
