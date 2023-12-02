package com.example.kotlindormify.admin

import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import com.example.kotlindormify.R
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Locale

class DormRequestDetailsFragment : Fragment(R.layout.fragment_request_dorm_detail) {
    private lateinit var tvDormName: TextView
    private lateinit var tvPrice: TextView
    private lateinit var tvPhoneNumber: TextView
    private lateinit var tvEmail: TextView
    private lateinit var tvAddress: TextView
    private lateinit var tvnumOfRooms: TextView
    private lateinit var tvgenderRestriction: TextView
    private lateinit var ivBusinessPermit: ImageView
    private lateinit var ivDormitoryProfile: ImageView
    private lateinit var tvTimestamp: TextView
    private lateinit var btnAccept: Button
    private lateinit var btnDecline: Button









    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize TextViews
        tvDormName = view.findViewById(R.id.tvDormitoryName)
        tvPrice = view.findViewById(R.id.tvPrice)
        tvPhoneNumber = view.findViewById(R.id.tvPhoneNumber)
        tvEmail = view.findViewById(R.id.tvEmail)
        tvAddress = view.findViewById(R.id.tvAddress)
        tvnumOfRooms = view.findViewById(R.id.tvNumberOfRooms)
        tvgenderRestriction = view.findViewById(R.id.tvGender)
        ivBusinessPermit = view.findViewById(R.id.ivBusinessPermit)
        ivDormitoryProfile = view.findViewById(R.id.ivDormitoryProfile)
        tvTimestamp = view.findViewById(R.id.tvTimestamp)

        btnAccept = view.findViewById(R.id.btnAccept)
        btnDecline = view.findViewById(R.id.btnDecline)






        // Get data from arguments
        val dormId = arguments?.getString("dormId", "")
        val status = arguments?.getString("status", "")

        if (!dormId.isNullOrEmpty()) {
            // Fetch data based on dormId and status
            fetchData(dormId, status.toString())
        }
    }

    private fun fetchData(dormId: String, status: String) {
        val firestore = FirebaseFirestore.getInstance()
        val dormitoryRef = firestore.collection("dormitories").document(dormId)

        dormitoryRef.get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    // Document exists, handle the data
                    val dormName = documentSnapshot.getString("dormName") ?: ""
                    val address = documentSnapshot.getString("address") ?: ""
                    val numOfRooms: Long = documentSnapshot.getLong("numOfRooms") ?: 0L
                    val genderRestriction = documentSnapshot.getString("genderRestriction") ?: ""
                    val email = documentSnapshot.getString("email") ?: ""
                    val phoneNumber = documentSnapshot.getString("phoneNumber") ?: ""
                    val price = documentSnapshot.getString("price") ?: ""
                    val businessPermit = documentSnapshot.getString("permitImage") ?: ""
                    val imageUrl = documentSnapshot.get("images") as? List<String>
                    val createdTimeStamp = documentSnapshot.getTimestamp("dormCreatedTimestamp")
                    val status = documentSnapshot.getString("status") ?: ""



                    if (imageUrl != null && imageUrl.isNotEmpty()) {
                        // Load the first image using Picasso
                        Picasso.get().load(imageUrl[0]).into(ivDormitoryProfile)
                    }

                    if (businessPermit != null) {
                        // Load the first image using Picasso
                        Picasso.get().load(businessPermit).into(ivBusinessPermit)
                    }

                    if (status == "pending") {
                        btnAccept.setOnClickListener {
                            showConfirmationDialog(dormId, "accepted")
                        }

                        btnDecline.setOnClickListener {
                            showConfirmationDialog(dormId, "denied")
                        }
                    } else {
                        // If status is not "pending," hide the buttons
                        btnAccept.visibility = View.GONE
                        btnDecline.visibility = View.GONE
                    }








                    // Get other fields as needed

                    // Now you have the dormitory details, update UI or perform other actions
                    tvDormName.text = dormName
                    tvPrice.text = "Php ${price}"
                    tvPhoneNumber.text = phoneNumber
                    tvEmail.text = email
                    tvAddress.text = address
                    tvnumOfRooms.text = numOfRooms.toString()
                    tvgenderRestriction.text = genderRestriction

                    val date = createdTimeStamp?.toDate()
                    val simpleDateFormat = SimpleDateFormat("MMMM dd, yyyy hh:mm:ss a", Locale.getDefault())
                    val formattedDate = simpleDateFormat.format(date)

                    tvTimestamp.text = formattedDate

                } else {
                    // Document doesn't exist
                    Toast.makeText(
                        requireContext(),
                        "Dormitory details not found.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
            .addOnFailureListener { e ->
                // Handle failure to fetch dormitory details
                Toast.makeText(
                    requireContext(),
                    "Error fetching dormitory details: ${e.message}",
                    Toast.LENGTH_SHORT
                ).show()
            }
    }


    private fun showConfirmationDialog(dormId: String, newStatus: String) {
        val actionVerb = if (newStatus == "accepted") "approve" else "deny"
        val confirmationDialog = AlertDialog.Builder(requireContext())
            .setTitle("Confirmation")
            .setMessage("Are you sure you want to $actionVerb this dormitory listing?")
            .setPositiveButton("Yes") { _, _ ->
                // User clicked Yes, update the status
                updateStatus(dormId, newStatus)
            }
            .setNegativeButton("Cancel") { dialog, _ ->
                // User clicked Cancel, do nothing or handle accordingly
                dialog.dismiss()
            }
            .create()

        confirmationDialog.show()
    }


    private fun updateStatus(dormId: String, newStatus: String) {
        val firestore = FirebaseFirestore.getInstance()
        val dormitoryRef = firestore.collection("dormitories").document(dormId)

        // Update the status field
        dormitoryRef.update("status", newStatus)
            .addOnSuccessListener {
                // Handle success, if needed
                val actionVerb = if (newStatus == "accepted") "approved" else "denied"
                val confirmationMessage = "This dormitory listing has been $actionVerb."

                // Show confirmation alert
                AlertDialog.Builder(requireContext())
                    .setTitle("Success")
                    .setMessage(confirmationMessage)
                    .setPositiveButton("OK") { dialog, _ ->
                        // Dismiss the dialog
                        dialog.dismiss()

                        // Navigate back to the previous fragment
                        requireActivity().supportFragmentManager.popBackStack()
                    }
                    .create()
                    .show()
            }
            .addOnFailureListener { e ->
                // Handle failure
                val errorMessage = "Failed to update status: ${e.message}"

                // Show error alert
                AlertDialog.Builder(requireContext())
                    .setTitle("Error")
                    .setMessage(errorMessage)
                    .setPositiveButton("OK") { dialog, _ ->
                        // Dismiss the dialog
                        dialog.dismiss()
                    }
                    .create()
                    .show()
            }
    }




}
