package com.example.kotlindormify.tenant3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RatingBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.ChatFragment
import com.example.kotlindormify.R
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardPaymentTenantFragment : Fragment() {
    private val firestore = FirebaseFirestore.getInstance()
    private lateinit var paymentHistoryList: RecyclerView
    private lateinit var paymentHistoryAdapter: PaymentHistoryAdapter
    private val paymentHistoryList2 = mutableListOf<PaymentHistoryItem>()
    private lateinit var btnRateDorm: CardView




    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_dashboard_payment_tenant, container, false)

        val btnpay: CardView = view.findViewById(R.id.ivpay)

        // Find the TextView with ID tvdormnae
        val tvDormName: TextView = view.findViewById(R.id.tvlandlordname)
        val imgDormProfile: ImageView = view.findViewById(R.id.dormprofile)
        val btnPayNow: CardView = view.findViewById(R.id.ivpay)
        val tvDueDate: TextView = view.findViewById(R.id.tvduedate)
        val tvDuePayment: TextView = view.findViewById(R.id.tvduepayment)
        val tvEndContractDate: TextView = view.findViewById(R.id.tvrendcontractdate)

        btnRateDorm = view.findViewById(R.id.ivrate)

        // Initialize RecyclerView and its adapter
        paymentHistoryList = view.findViewById(R.id.recyclerpayment)
        paymentHistoryList.layoutManager = LinearLayoutManager(requireContext())
        paymentHistoryAdapter = PaymentHistoryAdapter(paymentHistoryList2) // Initialize with an empty list
        paymentHistoryList.adapter = paymentHistoryAdapter


        btnPayNow.setOnClickListener {
            var paymentFragment = PaymentFragment()


        // Replace the current fragment with ChatFragment using a fragment transaction
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, paymentFragment)
            transaction.addToBackStack(null) // Optional: Add to back stack for navigation
            transaction.commit()

            val bottomNavigationView =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.menu.findItem(R.id.menu_payment).isChecked = true

        }

        if (hasUserRated()) {
            btnRateDorm.isClickable = false
        }

        btnRateDorm.setOnClickListener {
            showRatingDialog()
        }





        // Now you can use btnpay as needed.
        fetchTenantData(tvDormName, imgDormProfile, tvDueDate, tvEndContractDate, tvDuePayment)
        getPaymentHistoryFromFirestore()

        return view


    }


    private fun fetchTenantData(tvDormName: TextView, imgDormProfile: ImageView, tvDueDate: TextView, tvEndContractDate: TextView, tvDuePayment:TextView) {
        // Reference to the "tenant" collection
        val tenantCollection = firestore.collection("tenant")

        // Current user's UID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Fetch all documents in the "tenant" collection
        tenantCollection.get()
            .addOnSuccessListener { documents ->
                for (document in documents) {
                    // Access data from each document
                    val tenantId = document.getString("tenantId")
                    val dormitoryId = document.getString("dormitoryId")
                    // Retrieve the timestamp as a Date object
                    val acceptedDateTimestamp = document.getTimestamp("acceptedDate")
                    val contractEndDate = document.getTimestamp("contractEndDate")

// Check if acceptedDateTimestamp is not null
                    acceptedDateTimestamp?.let {
                        // Add one month to the accepted date
                        val nextMonthDate = Calendar.getInstance()
                        nextMonthDate.time = it.toDate()
                        nextMonthDate.add(Calendar.MONTH, 1)

                        // Create a SimpleDateFormat with the desired pattern
                        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

                        // Format the timestamp to a string
                        val nextMonthDateString = dateFormat.format(nextMonthDate.time)

                        // Set the formatted string to the TextView
                        tvDueDate.text = nextMonthDateString
                    }

                    contractEndDate?.let {
                        // Create a SimpleDateFormat with the desired pattern
                        val dateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.getDefault())

                        // Format the timestamp to a string
                        val endDateString = dateFormat.format(it.toDate())

                        // Set the formatted string to the TextView for contractEndDate
                        // If you have a different TextView for contractEndDate, replace tvDueDate with the appropriate TextView
                        tvEndContractDate.text = endDateString
                    }





                    // Check if the current user's UID matches the tenantId
                    if (currentUserId == tenantId) {
                        fetchDormInfo(dormitoryId, tvDormName, imgDormProfile, tvDuePayment)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }

    private fun fetchDormInfo(dormitoryId: String?, tvDormName: TextView, imgDormProfile: ImageView, tvDuePayment:TextView) {
        // Check if dormitoryId is not null
        dormitoryId?.let {
            // Reference to the "dormitories" collection
            val dormitoryCollection = firestore.collection("dormitories")


            // Query for the specific dormitoryId
            dormitoryCollection.document(dormitoryId).get()
                .addOnSuccessListener { dormDocument ->
                    // Access data from the dormitory document
                    val dormName = dormDocument.getString("dormName")
                    val imageUrl = dormDocument.get("images") as? List<String>
                    val price = dormDocument.getString("price")
                    val previousPrice = dormDocument.getString("previousPrice")

                    // Show dormName as a toast message
                    dormName?.let {
                        // Set the dormName to the TextView
                        tvDormName.text = dormName
                        if(previousPrice != "0"){
                            if (previousPrice != price){
                                val alertDialogBuilder = AlertDialog.Builder(requireContext())
                                alertDialogBuilder.setTitle("Dormitory Price Update!")

                                val message = "$dormName has updated its price.\n\nPrevious:  Php $previousPrice\nCurrent:  Php $price"
                                alertDialogBuilder.setMessage(message)

                                alertDialogBuilder.setPositiveButton("Okay") { dialog, which ->
                                    // Update previousPrice to the current price in the Firestore document
                                    dormitoryId?.let {
                                        val dormitoryDocRef = firestore.collection("dormitories").document(it)
                                        dormitoryDocRef
                                            .update("previousPrice", price)
                                            .addOnSuccessListener {
                                                // Successfully updated previousPrice
                                                // You can add any additional logic here if needed
                                            }
                                            .addOnFailureListener { e ->
                                                // Handle the failure to update previousPrice
                                                // You might want to log the error or show an error message to the user
                                            }
                                    }
                                }

                                val alertDialog = alertDialogBuilder.create()
                                alertDialog.show()
                            }
                        }



                        tvDuePayment.text = price


                        // Load the first image into imgDormProfile using Picasso
                        imageUrl?.get(0)?.let { firstImageUrl ->
                            Picasso.get().load(firstImageUrl).into(imgDormProfile)
                        }
                    }

                }
                .addOnFailureListener { exception ->
                    // Handle errors
                }
        }
    }



    private fun showToast(message: String) {
        // Display a toast message
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }

    private fun showRatingDialog() {
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.dialog_rate, null)
        val ratingBar: RatingBar = dialogView.findViewById(R.id.ratingBar)

        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Rate Dorm")
        builder.setView(dialogView)

        // Set up the buttons
        builder.setPositiveButton("Rate") { dialog, which ->
            // Disable the button immediately after the user clicks "Rate"
            btnRateDorm.isClickable = false

            // Retrieve dormitoryId from the "tenant" collection
            val tenantCollection = firestore.collection("tenant")
            val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

            // Fetch all documents in the "tenant" collection
            tenantCollection.document(currentUserId!!)
                .get()
                .addOnSuccessListener { documentSnapshot ->
                    val dormitoryId = documentSnapshot.getString("dormitoryId")

                    // Check if dormitoryId is not null
                    dormitoryId?.let {
                        // Reference to the "dormitories" collection
                        val dormitoryCollection = firestore.collection("dormitories")

                        // Query for the specific dormitoryId
                        dormitoryCollection.document(it).get()
                            .addOnSuccessListener { dormDocument ->
                                val numOfRatings = dormDocument.getDouble("numOfRatings") ?: 0.0
                                val numOfStars = dormDocument.getDouble("numOfStars") ?: 0.0
                                var updatedRatings: Double = 0.0

                                // Update the numOfRatings and numOfStars based on the new rating
                                val rating = ratingBar.rating.toInt() // Convert to integer
                                val newNumOfRatings = numOfRatings + 1
                                val newNumOfStars = numOfStars + rating

                                // You can do something with the newNumOfRatings and newNumOfStars, like updating the Firestore document
                                updatedRatings = newNumOfStars/newNumOfRatings
                                updatedRatings = String.format("%.2f", updatedRatings).toDouble()


                                val updatedValues = mapOf(
                                    "numOfRatings" to newNumOfRatings,
                                    "numOfStars" to newNumOfStars,
                                    "ratings" to updatedRatings
                                )

                                // Update the specific document
                                dormitoryCollection.document(dormitoryId).update(updatedValues)
                                    .addOnSuccessListener {
                                        // Update successful, now update the hasRated field
                                        updateHasRatedInFirestore(true)
                                        showSuccessDialog()
                                    }
                                    .addOnFailureListener { exception ->
                                        // Handle errors
                                    }
                            }
                            .addOnFailureListener { exception ->
                                // Handle errors
                            }
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                }
        }

        builder.setNegativeButton("Cancel") { dialog, which ->
            // Handle the cancel button click (if needed)
        }

        val dialog = builder.create()
        dialog.show()
    }


    private fun updateHasRatedInFirestore(hasRated: Boolean) {
        val tenantCollection = firestore.collection("tenant")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Update the hasRated field in Firestore
        currentUserId?.let {
            tenantCollection.document(it)
                .update("hasRated", hasRated)
                .addOnSuccessListener {
                    // Update successful
                }
                .addOnFailureListener { exception ->
                    // Handle update failure
                }
        }
    }

    private fun hasUserRated(): Boolean {
        val tenantCollection = firestore.collection("tenant")
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Check if the user is logged in
        currentUserId?.let {
            // Retrieve the hasRated field from Firestore asynchronously
            tenantCollection.document(it).get()
                .addOnSuccessListener { documentSnapshot ->
                    // If the document exists and has the hasRated field, return its value
                    if (documentSnapshot.exists()) {
                        val hasRated = documentSnapshot.getBoolean("hasRated") ?: false
                        // Disable the button if the user has already rated
                        btnRateDorm.isClickable = !hasRated
                    }
                }
                .addOnFailureListener { exception ->
                    // Handle errors
                }
        }

        return false
    }





    private fun getPaymentHistoryFromFirestore() {
        // Reference to the "tenant" collection
        val tenantCollection = firestore.collection("tenant")

        // Current user's UID
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid

        // Fetch the tenant document for the current user
        tenantCollection.document(currentUserId!!)
            .collection("payment_history")
            .get()
            .addOnSuccessListener { paymentHistoryDocuments ->
                paymentHistoryList2.clear()

                for (document in paymentHistoryDocuments) {
                    val paymentDate = document.getString("paymentDate") ?: ""
                    val paymentId = document.getString("paymentId") ?: ""
                    val status = document.getString("status") ?: ""

                    // Create a PaymentHistoryItem and add it to the list
                    val paymentHistoryItem = PaymentHistoryItem(paymentDate, paymentId, status)
                    paymentHistoryList2.add(paymentHistoryItem)
                }

                // Update the RecyclerView with the fetched data
                paymentHistoryAdapter.updateRecyclerView(paymentHistoryList2)
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }

    private fun showSuccessDialog() {
        val successDialog = AlertDialog.Builder(requireContext())
            .setTitle("Success!")
            .setMessage("Thanks for your rating! Your feedback helps improve dormitory services.")
            .setPositiveButton("OK") { dialog, which ->
                // Handle the OK button click if needed
            }
            .create()

        successDialog.show()
    }




}
