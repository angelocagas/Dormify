package com.example.kotlindormify.landlord

import android.Manifest
import android.app.Activity
import android.app.Activity.RESULT_OK
import android.app.ProgressDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.Dormitories
import com.example.kotlindormify.R
import com.example.kotlindormify.databinding.LandlordAddDormitoryFragmentBinding
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.google.zxing.BarcodeFormat
import com.google.zxing.EncodeHintType
import com.google.zxing.qrcode.QRCodeWriter
import com.google.zxing.qrcode.decoder.ErrorCorrectionLevel
import java.io.ByteArrayOutputStream
import java.io.IOException
import java.util.Hashtable
import java.util.UUID


class LandlordAddDormitoryFragment : Fragment(), OnMapReadyCallback {
    private lateinit var binding: LandlordAddDormitoryFragmentBinding
    private lateinit var database: FirebaseDatabase
    private lateinit var dormitoriesRef: DatabaseReference
    private lateinit var googleMap: GoogleMap
    private lateinit var selectedLocation: LatLng
    private lateinit var storage: FirebaseStorage
    private lateinit var storageRef: StorageReference
    private lateinit var firestore: FirebaseFirestore
    private lateinit var dormitoriesCollection: CollectionReference

    private val REQUEST_CODE_SELECT_IMAGES = 1

    private lateinit var recyclerView: RecyclerView
    private var selectedImageUris: MutableList<Uri> = mutableListOf()
    private lateinit var adapter: ImagePreviewAdapter


    private val firestorePath = "dormitories"

    private var isMapExpanded = false // Initially, the map is not expanded

    private val DEFAULT_LATITUDE = 14.998027206214473 // Replace with your desired default latitude
    private val DEFAULT_LONGITUDE = 120.65611294250105 // Replace with your desired default longitude

    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val DEFAULT_ZOOM_LEVEL = 15f

    private val PICK_IMAGE_REQUEST = 1
    private lateinit var selectedImageUri: Uri
    private var isImageSelected = false

    private val PICK_PERMIT_IMAGE_REQUEST = 2
    private lateinit var selectedPermitImageUri: Uri
    private var isPermitImageSelected = false
    private var progressDialog: ProgressDialog? = null
    private var imageIndex = 1


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = LandlordAddDormitoryFragmentBinding.inflate(inflater, container, false)
        database = FirebaseDatabase.getInstance()
        dormitoriesRef = database.reference.child("dormitories")
        storage = FirebaseStorage.getInstance()
        storageRef = storage.reference.child("dormitory_images")

        firestore = FirebaseFirestore.getInstance()
        dormitoriesCollection = firestore.collection(firestorePath)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.ivMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)



        binding.btnPinLocation.setOnClickListener {
            if (::selectedLocation.isInitialized) {
                // Update the Latitude and Longitude EditText fields
                binding.etLatitude.setText(selectedLocation.latitude.toString())
                binding.etLongitude.setText(selectedLocation.longitude.toString())
                binding.tvPinSuccess.visibility = View.VISIBLE
                binding.tvnoPin.visibility = View.GONE
            } else {
                // Handle the case where the selected location is not initialized
                Toast.makeText(requireContext(), "Select a location on the map first", Toast.LENGTH_SHORT).show()
            }
        }
        val layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        binding.recyclerView.layoutManager = layoutManager

        recyclerView = binding.recyclerView



        binding.btnAddImage.setOnClickListener {
            val intent = Intent(Intent.ACTION_GET_CONTENT)
            intent.type = "image/*"
            intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true) // Allow multiple image selection
            startActivityForResult(intent, REQUEST_CODE_SELECT_IMAGES)
        }

        binding.btnAddPermitImage.setOnClickListener {
            val intent = Intent()
            intent.type = "image/*"
            intent.action = Intent.ACTION_GET_CONTENT
            startActivityForResult(Intent.createChooser(intent, "Select Business Permit Photo"), PICK_PERMIT_IMAGE_REQUEST)
        }
        var selectedRentalTerm = "Per Month"
        binding.radioRentalTerms.setOnCheckedChangeListener { _, checkedId ->
            // Check which radio button was selected
            when (checkedId) {
                R.id.radioButtonMonth -> selectedRentalTerm = "Per Month"
                R.id.radioButtonSemester -> selectedRentalTerm = "Per Semester"
                R.id.radioButtonYear -> selectedRentalTerm = "Per Year"
                // Add more cases for other radio buttons if needed
            }
        }

        var selectedBathroom = "Separate (Private)"
        binding.radioBathroom.setOnCheckedChangeListener { _, checkedId ->
            // Check which radio button was selected
            when (checkedId) {
                R.id.radioShared -> selectedBathroom = binding.radioShared.text.toString()
                R.id.radioSeparate -> selectedBathroom = binding.radioSeparate.text.toString()
                // Add more cases for other radio buttons if needed
            }
        }

        var selectedElectric = "Included"
        binding.radioElectricity.setOnCheckedChangeListener { _, checkedId ->
            // Check which radio button was selected
            when (checkedId) {
                R.id.radioIncluded -> selectedElectric = binding.radioIncluded.text.toString()
                R.id.radioExcluded -> selectedElectric = binding.radioExcluded.text.toString()
                // Add more cases for other radio buttons if needed
            }
        }

        var selectedWater = "Included"
        binding.radioWater.setOnCheckedChangeListener { _, checkedId ->
            // Check which radio button was selected
            when (checkedId) {
                R.id.radioWaterIncluded -> selectedWater =
                    binding.radioWaterIncluded.text.toString()

                R.id.radioWaterExcluded -> selectedWater =
                    binding.radioWaterExcluded.text.toString()
                // Add more cases for other radio buttons if needed
            }
        }

        val selectedPaymentOptions = mutableListOf<String>()

        binding.checkBoxCash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPaymentOptions.add("Cash")
            } else {
                selectedPaymentOptions.remove("Cash")
            }
        }

        binding.checkBoxGcash.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedPaymentOptions.add("Gcash")
            } else {
                selectedPaymentOptions.remove("Gcash")
            }
        }



        binding.btnSubmit.setOnClickListener {
            val dormName = binding.etDormName.text.toString()
            val numOfRooms = binding.etNumOfRooms.text.toString()
            val description = binding.etDescription.text.toString()
            val price = binding.etPrice.text.toString()
            val address = binding.etAddress.text.toString()
            val phoneNumber = binding.etPhoneNumber.text.toString()
            val email = binding.etEmail.text.toString()
            val username = binding.etusername.text.toString()
            val cbAgreement = binding.cbAgreement
            var amenitiesString = binding.etAmenities.text.toString()
            var amenitiesList = amenitiesString.split(",").map { it.trim() }.toMutableList()


            // Check if an image has been selected
            if (!isPermitImageSelected) {
                Toast.makeText(requireContext(), "Please select both Dormitory Image and Business Permit Image", Toast.LENGTH_SHORT).show()
                return@setOnClickListener // Prevent further execution of the click listener
            }



            if (listOf(dormName, numOfRooms, price, address, phoneNumber, email, username, description, amenitiesString, selectedRentalTerm, selectedBathroom, selectedElectric, selectedWater,).all { it.isNotEmpty() } && selectedPaymentOptions.isNotEmpty() && amenitiesList.isNotEmpty()) {
                if (cbAgreement.isChecked) {
                    showLoadingDialog()
                    val activity = requireActivity() as LandlordDashboardActivity
                    val userEmail = activity.userEmail

                    if (userEmail != null) {
                        fetchCurrentUserUid(userEmail) { userId ->
                            if (binding.etLatitude.text.isNotEmpty() && binding.etLongitude.text.isNotEmpty()) {
                                if (userId != null) {
                                    generateUniqueDormId { newDormId ->
                                        val dormitory = Dormitories(
                                            newDormId,
                                            dormName,
                                            numOfRooms.toInt(),
                                            price,
                                            address,
                                            phoneNumber,
                                            email,
                                            username,
                                            description,
                                            userId,
                                            selectedLocation.latitude,
                                            selectedLocation.longitude,
                                            emptyList(),
                                            selectedRentalTerm,
                                            selectedBathroom,
                                            selectedElectric,
                                            selectedWater,
                                            selectedPaymentOptions,
                                            amenitiesList
                                        )

                                        // Add the dormitory information to Firestore
                                        val dormitoryDocRef =
                                            dormitoriesCollection.document(newDormId)
                                        dormitoryDocRef.set(dormitory)
                                            .addOnCompleteListener { task ->
                                                if (task.isSuccessful) {
                                                    // Successfully added dormitory, now add rooms
                                                    addRoomsToDormitory(
                                                        newDormId,
                                                        numOfRooms.toInt()
                                                    )
                                                    uploadImages(newDormId)
                                                    uploadPermitImage(newDormId)

                                                    val qrCodeText = "Dormitory Info:\n\n" +
                                                            "Name: $dormName\n" +
                                                            "Address: $address\n" +
                                                            "Description: $description\n" +
                                                            "Number of Rooms: $numOfRooms\n" +
                                                            "Price: ₱$price" +
                                                            "Rental Term: $selectedRentalTerm\n\n" +
                                                            "Contact \nPhone Number: $phoneNumber\n" +
                                                            "Name: $username\n"


                                                    val qrCodeBitmap = generateQRCode(qrCodeText)

                                                    // Upload the QR code image to Firebase Storage
                                                    val qrCodeImageRef =
                                                        storageRef.child("dormitory_qr/$newDormId-qr_code.jpg")
                                                    val baos = ByteArrayOutputStream()
                                                    qrCodeBitmap.compress(
                                                        Bitmap.CompressFormat.JPEG,
                                                        100,
                                                        baos
                                                    )
                                                    val data = baos.toByteArray()

                                                    qrCodeImageRef.putBytes(data)
                                                        .addOnSuccessListener { _ ->
                                                            // QR code image uploaded successfully
                                                            // Now, you can get the download URL of the QR code image
                                                            qrCodeImageRef.downloadUrl
                                                                .addOnSuccessListener { downloadUrl ->
                                                                    val qrCodeImageUrl =
                                                                        downloadUrl.toString()

                                                                    // Store the QR code image URL in Firestore
                                                                    dormitoryDocRef.update(
                                                                        "qrCodeImageUrl", qrCodeImageUrl,
                                                                        "dormCreatedTimestamp",
                                                                        FieldValue.serverTimestamp()
                                                                    )
                                                                        .addOnSuccessListener { _ ->
                                                                            // Dormitory information updated with QR code image URL
                                                                            // Continue with other operations and show success dialog
                                                                            val successDialog =
                                                                                AlertDialog.Builder(
                                                                                    requireContext()
                                                                                )
                                                                                    .setTitle("Success!")
                                                                                    .setMessage("You've added a dormitory.")
                                                                                    .setPositiveButton(
                                                                                        "OK"
                                                                                    ) { _, _ ->
                                                                                        requireActivity().supportFragmentManager.popBackStack()
                                                                                    }
                                                                                    .create()
                                                                            progressDialog?.dismiss()

                                                                            successDialog.show()
                                                                        }
                                                                        .addOnFailureListener { e ->
                                                                            // Handle failure to update dormitory information with QR code image URL
                                                                        }
                                                                }
                                                                .addOnFailureListener { e ->
                                                                    // Handle failure to get the download URL of the QR code image
                                                                }
                                                        }
                                                        .addOnFailureListener { e ->
                                                            // Handle failure to upload QR code image
                                                        }
                                                } else {
                                                    Toast.makeText(
                                                        requireContext(),
                                                        "Failed to add dormitory. Please try again.",
                                                        Toast.LENGTH_SHORT
                                                    ).show()
                                                    progressDialog?.dismiss()
                                                }
                                            }
                                    }
                                } else {
                                    Toast.makeText(
                                        requireContext(),
                                        "User not found with the specified email. Please try again.",
                                        Toast.LENGTH_SHORT
                                    ).show()
                                    progressDialog?.dismiss()
                                }
                            } else {
                                Toast.makeText(requireContext(), "Pin the dormitory location on the map first to continue (Long press on the map)", Toast.LENGTH_SHORT).show()
                                progressDialog?.dismiss()
                            }
                        }
                    }
                } else {
                    Toast.makeText(
                        requireContext(),
                        "Please agree to the terms and conditions",
                        Toast.LENGTH_SHORT
                    ).show()
                    progressDialog?.dismiss()
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "Please Fill Out All Fields",
                    Toast.LENGTH_SHORT
                ).show()
                progressDialog?.dismiss()
            }
        }





        binding.btnCancel.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.imageView.setOnClickListener {
            requireActivity().supportFragmentManager.popBackStack()
        }

        return binding.root
    }

    private fun generateUniqueDormId(callback: (String) -> Unit) {
        dormitoriesCollection
            .orderBy("dormId", Query.Direction.DESCENDING)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                val maxDormId = documents.documents.firstOrNull()
                    ?.get("dormId")
                    ?.toString()
                    ?.substring(4)
                    ?.toIntOrNull() ?: 0

                val newDormId = String.format("dorm%03d", maxDormId + 1)
                callback(newDormId)
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur during the query
                // You can log or display an error message here
            }
    }


    private fun fetchCurrentUserUid(email: String, callback: (String?) -> Unit) {
        val usersRef = firestore.collection("users")
        usersRef
            .whereEqualTo("email", email)
            .limit(1)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val userId = documents.documents[0].id
                    callback(userId)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                // Handle any errors that occur during the query
                // You can log or display an error message here
            }
    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        } else {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        val initialLocation = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(initialLocation, DEFAULT_ZOOM_LEVEL))

        // Enable zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Enable compass control
        googleMap.uiSettings.isCompassEnabled = true

        val markerOptions = MarkerOptions()
            .position(initialLocation)
            .title("Dormitory Location")
        googleMap.addMarker(markerOptions)

        googleMap.setOnMapLongClickListener { latLng ->
            googleMap.clear() // Clear previous markers
            googleMap.addMarker(MarkerOptions().position(latLng).title("Dormitory Location"))

            selectedLocation = latLng // Define selectedLocation as a LatLng property in your class
        }
    }

    // Function to add rooms to the dormitory
    private fun addRoomsToDormitory(dormitoryId: String, numOfRooms: Int) {
        // Reference to the specific dormitory document in the Firestore collection
        val dormitoryDocRef = firestore.collection("dormitories").document(dormitoryId)

        // Initialize a batch write to efficiently write multiple documents
        val batch = firestore.batch()

        for (roomNumber in 1..numOfRooms) {
            // Create a new room document with its properties
            val roomData = hashMapOf(
                "roomNumber" to roomNumber,
                "availability" to "available", // By default, all rooms are available
                "tenantId" to "", // Use an empty string to indicate no tenant assigned
                "tenantName" to "Vacant" // Use "Vacant" to indicate not occupied
                // Add other room details as needed
            )

            // Reference to the room document within the dormitory's subcollection
            val roomDocRef = dormitoryDocRef.collection("rooms").document("room_$roomNumber")

            // Add the set operation for the room document to the batch
            batch.set(roomDocRef, roomData)
        }

        // Commit the batch write to Firestore
        batch.commit()
            .addOnSuccessListener {
                // Batch write was successful
            }
            .addOnFailureListener { e ->
                // Handle the error if the batch write fails
            }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == REQUEST_CODE_SELECT_IMAGES && resultCode == Activity.RESULT_OK) {
            if (data != null && data.clipData != null) {
                // Multiple images selected
                val clipData = data.clipData!!
                for (i in 0 until clipData.itemCount) {
                    val imageUri = clipData.getItemAt(i).uri // Get the URI of each selected image
                    selectedImageUris.add(imageUri) // Add the URI to the list in the order they were selected
                    binding.btnAddImage.isClickable = false
                    binding.textView4.text = "Dormitory Images selected"
                    binding.ivSelectedImage.setImageResource(R.drawable.check_icon)
                }

            } else if (data != null && data.data != null) {
                // Single image selected
                val imageUri = data.data!! // Get the URI of the selected image
                selectedImageUris.add(imageUri) // Add the URI to the list
                binding.ivSelectedImage.setImageResource(R.drawable.check_icon)
                binding.btnAddImage.isClickable = false
                binding.textView4.text = "Dormitory Images selected"
            }
        }





        if (requestCode == PICK_PERMIT_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.data != null) {
            selectedPermitImageUri = data.data!!
            try {
                val bitmap = MediaStore.Images.Media.getBitmap(requireActivity().contentResolver, selectedPermitImageUri)
                binding.ivSelectedPermitImage.setImageBitmap(bitmap)
                binding.ivSelectedPermitImage.visibility = View.VISIBLE

                isPermitImageSelected = true
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }

    // Function to upload the selected image to Firebase Storage
    private fun uploadImages(dormitoryId: String) {
        val uploadedImageUrls = mutableListOf<String>()

        if (selectedImageUris.isNotEmpty()) {
            val uploadTasks = mutableListOf<Task<Uri>>()

            for (imageUri in selectedImageUris) {
                val fileName = "${dormitoryId}_${System.currentTimeMillis()}.jpg"
                val imageRef = storageRef.child("$dormitoryId/$fileName")

                val uploadTask = imageRef.putFile(imageUri)
                    .continueWithTask { task ->
                        if (!task.isSuccessful) {
                            task.exception?.let {
                                throw it
                            }
                        }
                        imageRef.downloadUrl
                    }

                uploadTasks.add(uploadTask)
            }

            Tasks.whenAllComplete(uploadTasks)
                .addOnSuccessListener { taskList ->
                    for (task in taskList) {
                        if (task.isSuccessful) {
                            val imageUrl = (task.result as Uri).toString()
                            uploadedImageUrls.add(imageUrl)
                        }
                    }

                    // All images are uploaded, now you have the URLs in the order of selection
                    storeImageUrlsInFirestore(dormitoryId, uploadedImageUrls)
                }
                .addOnFailureListener { exception ->
                    // Handle error
                }
        } else {
            // Handle the case when no images are selected
        }
    }







    // Function to store the image URL in the Realtime Database
    private fun storeImageUrlsInFirestore(dormitoryId: String, imageUrls: List<String>) {
        val dormitoryDocRef = dormitoriesCollection.document(dormitoryId)

        // Update the "images" field in the Firestore document with the list of image URLs
        dormitoryDocRef.update("images", imageUrls)
            .addOnSuccessListener {
                // Image URLs updated successfully
            }
            .addOnFailureListener { e ->
                // Handle image URLs update failure
            }
    }



    private fun uploadPermitImage(dormitoryId: String) {
        if (::selectedPermitImageUri.isInitialized) {
            val imageRef = storageRef.child("business_permits/$dormitoryId-business_permit.jpg")

            imageRef.putFile(selectedPermitImageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // Image uploaded successfully, get the download URL
                    imageRef.downloadUrl.addOnCompleteListener { downloadUrlTask ->
                        if (downloadUrlTask.isSuccessful) {
                            val permitImageUrl = downloadUrlTask.result.toString()

                            // Now, store the permit image URL in Firestore
                            storePermitImageUrlInFirestore(dormitoryId, permitImageUrl)
                        } else {
                            // Handle error while getting the permit image URL
                        }
                    }
                }
                .addOnFailureListener { e ->
                    // Handle permit image upload failure
                }
        }
    }

// Update the storeImageUrlInFirestore method to store permit image URL:

    private fun storePermitImageUrlInFirestore(dormitoryId: String, permitImageUrl: String) {
        val dormitoryDocRef = dormitoriesCollection.document(dormitoryId)

        // Update the "permitImage" field in the Firestore document
        dormitoryDocRef.update("permitImage", permitImageUrl)
            .addOnSuccessListener {
                // Permit Image URL updated successfully
            }
            .addOnFailureListener { e ->
                // Handle permit image URL update failure
            }
    }


    private fun generateQRCode(text: String): Bitmap {
        val hints: Hashtable<EncodeHintType, Any> = Hashtable()
        hints[EncodeHintType.CHARACTER_SET] = "UTF-8"
        hints[EncodeHintType.ERROR_CORRECTION] = ErrorCorrectionLevel.H
        val qrCodeWriter = QRCodeWriter()
        val bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 512, 512, hints)
        val width = bitMatrix.width
        val height = bitMatrix.height
        val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.RGB_565)
        for (x in 0 until width) {
            for (y in 0 until height) {
                bmp.setPixel(x, y, if (bitMatrix[x, y]) Color.BLACK else Color.WHITE)
            }
        }
        return bmp
    }

    private fun showLoadingDialog() {
        progressDialog = ProgressDialog(requireContext())
        progressDialog?.setMessage("Creating your dormitory ...") // Set the message you want to display
        progressDialog?.setCancelable(false) // Prevents user from dismissing the dialog by tapping outside
        progressDialog?.show()
    }







}