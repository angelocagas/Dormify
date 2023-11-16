package com.example.kotlindormify.tenant3

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import com.example.kotlindormify.R
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class PaymentFragment : Fragment(R.layout.payment_fragment) {
    private val firestore = FirebaseFirestore.getInstance()
    private val PICK_RECEIPT_IMAGE_REQUEST = 1
    private lateinit var ivSelectedImage: ImageView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.payment_fragment, container, false)


        // Find the TextView with ID tvdormnae
        val tvDormName: TextView = view.findViewById(R.id.tvlandlordname)
        val imgDormProfile: ImageView = view.findViewById(R.id.landlordprofile)
        val tvDueDate: TextView = view.findViewById(R.id.tvduedate)
        val tvDuePayment: TextView = view.findViewById(R.id.tvduepayment)
        val tvLocation: TextView = view.findViewById(R.id.tvlocation)
        val tvPaymentMethods: TextView = view.findViewById(R.id.tvpaymentmehtods)
        val tvGcashNum: TextView = view.findViewById(R.id.tvGcashNum)
        val btnPay: Button = view.findViewById(R.id.payrentbtn)

        btnPay.setOnClickListener {
            showPaymentDialog()
        }



        // Now you can use btnpay as needed.
        fetchTenantData(tvDormName, imgDormProfile, tvDueDate, tvDuePayment, tvLocation, tvPaymentMethods, tvGcashNum)

        return view
    }

    private fun fetchTenantData(tvDormName: TextView, imgDormProfile: ImageView, tvDueDate: TextView, tvDuePayment:TextView, tvLocation: TextView, tvPaymentMethods: TextView, tvGcashNum: TextView) {
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
                        fetchDormInfo(dormitoryId, tvDormName, imgDormProfile, tvDuePayment, tvLocation, tvPaymentMethods, tvGcashNum)
                    }
                }
            }
            .addOnFailureListener { exception ->
                // Handle errors
            }
    }

    private fun fetchDormInfo(dormitoryId: String?, tvDormName: TextView, imgDormProfile: ImageView, tvDuePayment:TextView, tvLocation: TextView, tvPaymentMethods: TextView, tvGcashNum: TextView) {
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
                    val address = dormDocument.getString("address")
                    val paymentOptions = dormDocument.get("paymentOptions") as? List<String>
                    val gcashNum = dormDocument.getString("gcashNum")


                    // Show dormName as a toast message
                    dormName?.let {
                        // Set the dormName to the TextView
                        tvDormName.text = dormName
                        tvDuePayment.text = price
                        tvLocation.text = address

                        if (paymentOptions != null) {
                            // Do something with paymentOptions, it's a list

                            val paymentOptionsString = paymentOptions.joinToString("\n")
                            tvPaymentMethods.text = "$paymentOptionsString"
                        }

                        if (gcashNum != null){
                            tvGcashNum.visibility = View.VISIBLE
                            tvGcashNum.text = "Gcash Number: $gcashNum"
                        }
                        else{
                            tvGcashNum.visibility = View.GONE
                        }


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

    private fun showPaymentDialog() {
        val builder = AlertDialog.Builder(requireContext())
        val inflater = layoutInflater
        val dialogView = inflater.inflate(R.layout.payment_process, null)
        val btnAddReceipt: ConstraintLayout = dialogView.findViewById(R.id.btnAddReceipt)
        ivSelectedImage = dialogView.findViewById(R.id.ivSelectedImage)


        btnAddReceipt.setOnClickListener {
            // Open image picker
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Receipt Photo"), PICK_RECEIPT_IMAGE_REQUEST)
        }


        builder.setView(dialogView)
        builder.setTitle("Payment Process")

        builder.setPositiveButton("Upload") { _, _ ->
            // Handle positive button click (Upload)
            // Add your logic here to handle image upload

            // Display a success message in an AlertDialog
            val successDialogBuilder = AlertDialog.Builder(requireContext())
            successDialogBuilder.setTitle("Payment Proof Sent")
            successDialogBuilder.setMessage("Your payment proof has been successfully sent. Please wait for the landlord to confirm your payment.")
            successDialogBuilder.setPositiveButton("OK") { dialog, _ ->
                // Close the dialog if needed
                dialog.dismiss()
            }

            val successDialog: AlertDialog = successDialogBuilder.create()
            successDialog.show()
        }

        builder.setNegativeButton("Cancel") { dialog, _ ->
            // Handle negative button click (Cancel)
            dialog.dismiss()
        }

        val alertDialog: AlertDialog = builder.create()
        alertDialog.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == PICK_RECEIPT_IMAGE_REQUEST && resultCode == Activity.RESULT_OK) {
            // Handle the selected image here
            val selectedImageUri: Uri? = data?.data

            try {
                if (selectedImageUri != null) {
                    val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedImageUri)
                    ivSelectedImage.setImageBitmap(bitmap)

                    // Set a flag or perform additional logic as needed
                }
                else{
                    Toast.makeText(requireContext(), "null", Toast.LENGTH_SHORT).show()
                }
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

}

