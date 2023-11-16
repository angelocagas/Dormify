package com.example.kotlindormify.tenant3

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import com.example.kotlindormify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DashboardPaymentTenantFragment : Fragment() {
    private val firestore = FirebaseFirestore.getInstance()

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
        val tvDueDate: TextView = view.findViewById(R.id.tvduedate)
        val tvDuePayment: TextView = view.findViewById(R.id.tvduepayment)


        // Now you can use btnpay as needed.
        fetchTenantData(tvDormName, imgDormProfile, tvDueDate, tvDuePayment)

        return view
    }

    private fun fetchTenantData(tvDormName: TextView, imgDormProfile: ImageView, tvDueDate: TextView, tvDuePayment:TextView) {
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

                    // Show dormName as a toast message
                    dormName?.let {
                        // Set the dormName to the TextView
                        tvDormName.text = dormName
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
}
