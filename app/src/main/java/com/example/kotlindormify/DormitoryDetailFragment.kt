package com.example.kotlindormify

import android.content.ContentValues
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.navigation.Navigation.findNavController
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.squareup.picasso.Picasso
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.bumptech.glide.Glide
import com.example.kotlindormify.landlord.LandlordDashboardActivity
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator


class DormitoryDetailFragment : Fragment(), OnMapReadyCallback {

    private lateinit var userId: String
    private lateinit var ivProfilePicture: ImageView
    private var googleMap: GoogleMap? = null

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dormitory_detail, container, false)
        val btnAddToSaved = view.findViewById<Button>(R.id.btnAddToSaved)
        val btnRemoveFromSaved = view.findViewById<Button>(R.id.btnRemoveFromSaved)
        val btnRent = view.findViewById<Button>(R.id.inquirebtn)
        ivProfilePicture = view.findViewById(R.id.ivProfilePicture)

        val viewPager: ViewPager2 = view.findViewById(R.id.viewPager)
        val tabLayout: TabLayout = view.findViewById(R.id.tabLayout)




        // Retrieve dormitory data from arguments
        val dormName = arguments?.getString("dormName")
        val dormPrice = arguments?.getString("dormPrice")
        val dormitoryId = arguments?.getString("dormitoryId")
        val imageUrls: ArrayList<String>? = arguments?.getStringArrayList("imageUrls")
        val landlordId = arguments?.getString("landlordId")
        val dormRooms = arguments?.getInt("dormRooms")
        val qrCodeImageUrl = arguments?.getString("qrCodeImageUrl")
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")
        val address = arguments?.getString("address")
        val description = arguments?.getString("description")
        val permitImage = arguments?.getString("permitImage")
        val water = arguments?.getString("water")
        val electric = arguments?.getString("electric")
        val rentalTerm = arguments?.getString("rentalTerm")
        val bathroom = arguments?.getString("bathroom")
        val genderRestriction = arguments?.getString("genderRestriction")


        val imageSliderAdapter = ImageSliderAdapter(requireContext(), imageUrls ?: emptyList())
        viewPager.adapter = imageSliderAdapter

        // Connect ViewPager2 with TabLayout (optional)
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            // Customize tab if needed
        }.attach()


        //landlord details
        val email = arguments?.getString("email")
        val phoneNumber = arguments?.getString("phoneNumber")
        val username = arguments?.getString("username")
        val amenities = arguments?.getStringArrayList("amenities")
        val paymentOptions = arguments?.getStringArrayList("paymentOptions")
        val amenities2 = arguments?.getStringArrayList("amenities2")


        val activity = requireActivity() as DashboardActivity
        val currentUserEmail = activity.userEmail


        // pupulate kona ang landlord details

        // Populate UI with dormitory details
        val dormNameTextView = view.findViewById<TextView>(R.id.textDormName)
        val dormPriceTextView = view.findViewById<TextView>(R.id.textDormPrice)
        val genderRestrictionTextview = view.findViewById<TextView>(R.id.tvGenderRestriction)
        //val dormImageView = view.findViewById<ImageView>(R.id.ivDormitoryImage)
        //  val recyclerView = view.findViewById<RecyclerView>(R.)
        val descriptionTextview = view.findViewById<TextView>(R.id.ViewContent)
        val addressTextView = view.findViewById<TextView>(R.id.textDormloc)
        val numOfRoomsTextView = view.findViewById<TextView>(R.id.Availableroomtxt)
        val permitImageview = view.findViewById<ImageView>(R.id.permitImage)
        val qrcodeImageView = view.findViewById<ImageView>(R.id.qrcodeimage)
        val backBtn = view.findViewById<Button>(R.id.backbtn)

        backBtn.setOnClickListener {
            val fragmentManager: FragmentManager = requireActivity().supportFragmentManager
            fragmentManager.popBackStack()
        }

        //new features for better application
        //Amenities & Features
        val AmenitiesListTextView = view.findViewById<TextView>(R.id.AmenitiesListtxt)
        val Amenities2ListTextView = view.findViewById<TextView>(R.id.Amenities2Listtxt)
        val perWhat = view.findViewById<TextView>(R.id.monthlytxt)

        //Room Information
        val RoomCapacityTextView = view.findViewById<TextView>(R.id.RoomCapacitytxt)
        val BathroomTextView = view.findViewById<TextView>(R.id.Bathroomtxt)

        //Payment Terms
        val PaymentMethodTextView = view.findViewById<TextView>(R.id.PaymentMethodtxt)
        val MinimumStayTextView = view.findViewById<TextView>(R.id.MinimumStaytxt)
        val ElectricityTextView = view.findViewById<TextView>(R.id.Electricitytxt)
        val WaterTextView = view.findViewById<TextView>(R.id.Watertxt)

        //Landlord Information
        val TvUserNameTextView = view.findViewById<TextView>(R.id.tvUserName)
        val ContactTextView = view.findViewById<TextView>(R.id.contactTxt)
        val EmailTextView = view.findViewById<TextView>(R.id.emailtxt)



        dormNameTextView.text = dormName
        dormPriceTextView.text = "₱ $dormPrice"
        descriptionTextview.text = description
        addressTextView.text = address

        numOfRoomsTextView.text = "$dormRooms"
        TvUserNameTextView.text = username
        ContactTextView.text = phoneNumber
        EmailTextView.text = email
        WaterTextView.text = water
        ElectricityTextView.text = electric
        BathroomTextView.text = bathroom
        MinimumStayTextView.text = rentalTerm
        perWhat.text = rentalTerm
        genderRestrictionTextview.text = genderRestriction




        if (amenities != null) {
            // Do something with amenities, it's a list

            val amenitiesString = amenities.joinToString("\n ✔ ")
            Amenities2ListTextView.text = " ✔ $amenitiesString"


        } else {
            Toast.makeText(requireContext(), "wala", Toast.LENGTH_SHORT).show()

        }



        if (paymentOptions != null) {
            // Do something with paymentOptions, it's a list

            val paymentOptionsString = paymentOptions.joinToString("\n")
            PaymentMethodTextView.text = "$paymentOptionsString"
        } else {
            Toast.makeText(requireContext(), "wala", Toast.LENGTH_SHORT).show()

        }

        // Initialize the map
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.ivMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Check if latitude and longitude are available
        if (latitude != null && longitude != null) {
            // Create a LatLng object for the dormitory's location
            val dormitoryLocation = LatLng(latitude, longitude)

            // Create a custom marker icon
            val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.logo_on_map)

            // Create marker options
            val markerOptions = MarkerOptions()
                .position(dormitoryLocation)
                .title(dormName) // Use dormName without the $ symbol
                .icon(customMarkerIcon)

            // Add a marker for the dormitory's location with the custom icon
            googleMap?.addMarker(markerOptions)

            // Move the camera to the dormitory's location
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(dormitoryLocation, 15f))
        } else {
            Toast.makeText(requireContext(), "wala yung lat long", Toast.LENGTH_SHORT).show()
        }

        // Initialize Firebase Firestore
        val firestore = FirebaseFirestore.getInstance()

        val currentUser = FirebaseAuth.getInstance().currentUser
        if (currentUser != null) {
            // Query Firestore to get user data based on the current user ID
            firestore.collection("potential_tenant_details")
                .whereEqualTo("userId", currentUser.uid)
                .get()
                .addOnSuccessListener { querySnapshot ->
                    // Check if any documents were found
                    if (!querySnapshot.isEmpty) {
                        // Get the first document (assuming there's only one document per user)
                        val documentSnapshot = querySnapshot.documents[0]

                        // Retrieve data from the document
                        val selectedGender = documentSnapshot.getString("gender")

                        if(selectedGender == "Male"){
                            //User is Male
                            if(genderRestriction == "Female Only"){
                                btnRent.setOnClickListener {
                                    AlertDialog.Builder(requireContext())
                                        .setMessage("Sorry, this dormitory is exclusively for female tenants only.")
                                        .setPositiveButton("OK") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .show()
                                    btnRent.isClickable = false
                                    btnRent.setBackgroundColor(resources.getColor(R.color.notClickable))
                                }
                            }
                            else{
                                btnRent.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("dormitoryId", dormitoryId)
                                    bundle.putString("dormName", dormName)


                                    val requestRentFragment = RequestRentFragment()
                                    requestRentFragment.arguments = bundle
                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                    transaction.replace(R.id.fragment_container, requestRentFragment)
                                    transaction.addToBackStack(null) // Optional: Add to the back stack if needed
                                    transaction.commit()
                                }
                            }
                        }
                        else if(selectedGender == "Female"){
                            //User is Female
                            if(genderRestriction == "Male Only"){
                                btnRent.setOnClickListener {
                                    AlertDialog.Builder(requireContext())
                                        .setMessage("Sorry, this dormitory is exclusively for male tenants only.")
                                        .setPositiveButton("OK") { dialog, _ ->
                                            dialog.dismiss()
                                        }
                                        .show()
                                    btnRent.isClickable = false
                                    btnRent.setBackgroundColor(resources.getColor(R.color.notClickable))
                                }
                            }
                            else{
                                btnRent.setOnClickListener {
                                    val bundle = Bundle()
                                    bundle.putString("dormitoryId", dormitoryId)
                                    bundle.putString("dormName", dormName)


                                    val requestRentFragment = RequestRentFragment()
                                    requestRentFragment.arguments = bundle
                                    val transaction = requireActivity().supportFragmentManager.beginTransaction()
                                    transaction.replace(R.id.fragment_container, requestRentFragment)
                                    transaction.addToBackStack(null) // Optional: Add to the back stack if needed
                                    transaction.commit()
                                }
                            }

                        }



                    } else {
                        // Handle the case where no data is found for the current user
                        Toast.makeText(requireContext(), "User data not found", Toast.LENGTH_SHORT).show()
                    }
                }
                .addOnFailureListener { e ->
                    // Handle the failure to retrieve user data
                    Toast.makeText(requireContext(), "Failed to retrieve user data", Toast.LENGTH_SHORT).show()
                }
        }

















        // Load and display the dormitory image using Picasso or Glide
        /* if (imageUrls != null) {
             Picasso.get().load(imageUrls[0]).into(dormImageView)
         } else {
             dormImageView.setImageResource(R.drawable.dormify_logo)
         }

         */

        if (permitImage != null) {
            Picasso.get().load(permitImage).into(permitImageview)
        } else {
            permitImageview.setImageResource(R.drawable.dormify_logo)
        }

        if (qrCodeImageUrl != null) {
            Picasso.get().load(qrCodeImageUrl).into(qrcodeImageView)
        } else {
            qrcodeImageView.setImageResource(R.drawable.dormify_logo)
        }





        // Check if the dormitory is already saved for the current user
        if (currentUserEmail != null && dormitoryId != null) {
            checkIfDormitoryIsSaved(currentUserEmail, dormitoryId) { isSaved ->
                if (isSaved) {
                    // The dormitory is saved, change the button text to "Saved"
                    btnAddToSaved.visibility = View.GONE
                    btnRemoveFromSaved.visibility = View.VISIBLE
                } else {
                    btnRemoveFromSaved.visibility = View.GONE
                    btnAddToSaved.visibility = View.VISIBLE
                }
            }
        }

        // Set a click listener for the button to save or unsave the dormitory
        btnAddToSaved.setOnClickListener {
            if (currentUserEmail != null && dormitoryId != null) {
                checkIfDormitoryIsSaved(currentUserEmail, dormitoryId) { isSaved ->
                    if (isSaved) {
                        // The dormitory is saved, you can implement code to unsave it here
                        // For now, let's show a toast message
                        Toast.makeText(
                            requireContext(),
                            "Dormitory is already saved.",
                            Toast.LENGTH_SHORT
                        ).show()
                    } else {
                        // The dormitory is not saved, add it to saved dormitories
                        addDormitoryToSaved(currentUserEmail, dormitoryId) {
                            // Update the button text to "Saved" and disable the button
                            btnAddToSaved.visibility = View.GONE
                            btnRemoveFromSaved.visibility = View.VISIBLE
                            // Show a success message
                            Toast.makeText(
                                requireContext(),
                                "Dormitory added to saved.",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }

        btnRemoveFromSaved.setOnClickListener {
            if (currentUserEmail != null && dormitoryId != null) {
                removeDormitoryFromSaved(currentUserEmail, dormitoryId) {
                    // Update button visibility
                    btnRemoveFromSaved.visibility = View.GONE
                    btnAddToSaved.visibility = View.VISIBLE
                    // Show a success message
                    Toast.makeText(
                        requireContext(),
                        "Dormitory removed from saved.",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
        }

        val btnChatLandlord = view.findViewById<Button>(R.id.btnChatLandlord)
        btnChatLandlord.setOnClickListener {
            val bundle = Bundle()
            bundle.putString("dormName", dormName)
            bundle.putString("landlordId", landlordId)

            var chatFragment = ChatFragment()
            chatFragment.arguments = bundle


// Replace the current fragment with ChatFragment using a fragment transaction
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, chatFragment)
            transaction.addToBackStack(null) // Optional: Add to back stack for navigation
            transaction.commit()

            val bottomNavigationView =
                requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.menu.findItem(R.id.menu_chat).isChecked = true

        }
        initMap()
        if (landlordId != null) {
            checkLandlordProfilePicture(landlordId)
        }





        return view
    }

    private fun checkIfDormitoryIsSaved(
        userEmail: String,
        dormitoryId: String,
        callback: (Boolean) -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        // Query user document by email
        firestore.collection("users")
            .whereEqualTo("email", userEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val savedDormitories =
                        userDocument["savedDormitories"] as? List<String> ?: emptyList()
                    // Check if the dormitoryId is in the list of saved dormitories
                    val isSaved = savedDormitories.contains(dormitoryId)
                    callback(isSaved)
                } else {
                    // No user found or user document doesn't have savedDormitories field
                    callback(false)
                }
            }
            .addOnFailureListener { e ->
                // Handle the failure to query the user document
                callback(false)
            }
    }

    private fun addDormitoryToSaved(userEmail: String, dormitoryId: String, callback: () -> Unit) {
        val firestore = FirebaseFirestore.getInstance()

        fetchUserIdFromFirestore(userEmail) { userId ->
            if (userId != null) {
                // Update the user's document with the dormitory ID
                firestore.collection("users")
                    .document(userId)
                    .update("savedDormitories", FieldValue.arrayUnion(dormitoryId))
                    .addOnSuccessListener {
                        callback()
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure to add dormitory to saved
                    }
            } else {
                // Handle the case where the user is not found
                // You can choose to show an error message here
            }
        }
    }

    private fun fetchUserIdFromFirestore(email: String, callback: (String?) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .whereEqualTo("email", email)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userId = querySnapshot.documents[0].id
                    callback(userId)
                } else {
                    callback(null)
                }
            }
            .addOnFailureListener { e ->
                callback(null)
            }
    }

    private fun removeDormitoryFromSaved(
        userEmail: String,
        dormitoryId: String,
        callback: () -> Unit
    ) {
        val firestore = FirebaseFirestore.getInstance()
        // Fetch the user's ID based on their email
        fetchUserIdFromFirestore(userEmail) { userId ->
            if (userId != null) {
                // Update the user's document to remove the dormitory ID
                firestore.collection("users")
                    .document(userId)
                    .update("savedDormitories", FieldValue.arrayRemove(dormitoryId))
                    .addOnSuccessListener {
                        callback()
                    }
                    .addOnFailureListener { e ->
                        // Handle the failure to remove the dormitory from saved
                    }
            } else {
                // Handle the case where the user is not found
                // You can choose to show an error message here
            }
        }
    }

    private fun initMap() {
        val mapFragment =
            childFragmentManager.findFragmentById(R.id.ivMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    private fun addDormitoryMarker(latitude: Double?, longitude: Double?, dormName: String?, address: String) {
        if (latitude != null && longitude != null) {
            // Create a LatLng object for the dormitory's location
            val dormitoryLocation = LatLng(latitude, longitude)

            // Create a custom marker icon
            val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.logo_on_map)

            // Create marker options
            val markerOptions = MarkerOptions()
                .position(dormitoryLocation)
                .title(dormName) // Use dormName without the $ symbol
                .icon(customMarkerIcon)
                .snippet(address)
                .anchor(0.5f, 1.0f)
// Add a marker for the dormitory's location with the custom icon
            val marker = googleMap?.addMarker(markerOptions)

            googleMap?.setOnMarkerClickListener { marker ->
                // Show the info window for the clicked marker
                marker.showInfoWindow()

                true
            }

            googleMap?.setOnInfoWindowClickListener { clickedMarker ->
                val dormitoryLocation = clickedMarker.position // Get the LatLng of the clicked marker
                val dormitoryName = clickedMarker.title ?: "Dormitory Name" // Use the dormitory's name as the label, or provide a default name
                val dormitorySnippet = clickedMarker.snippet ?: "Dormitory Snippet" // Use the marker's snippet as the address, or provide a default snippet

                // Create an Intent to open the Google Maps app with a pinned marker at the specified location
                val gmmIntentUri = Uri.parse("geo:${dormitoryLocation.latitude},${dormitoryLocation.longitude}?z=15&q=${dormitoryLocation.latitude},${dormitoryLocation.longitude}(${Uri.encode(dormitoryName)})")

                val mapIntent = Intent(Intent.ACTION_VIEW, gmmIntentUri)
                mapIntent.setPackage("com.google.android.apps.maps") // Specify the Google Maps package

                // Check if the Google Maps app is installed
                if (mapIntent.resolveActivity(requireContext().packageManager) != null) {
                    startActivity(mapIntent)
                } else {
                    // If the Google Maps app is not installed, handle it accordingly
                    // You can open a web-based map or prompt the user to install the app.
                    // Example: Open a web-based map
                    val webMapIntent = Intent(Intent.ACTION_VIEW, Uri.parse("https://www.google.com/maps/search/?api=1&query=${dormitoryLocation.latitude},${dormitoryLocation.longitude}"))
                    startActivity(webMapIntent)
                }
            }

            // Move the camera to the dormitory's location
            googleMap?.moveCamera(CameraUpdateFactory.newLatLngZoom(dormitoryLocation, 15f))

            googleMap?.uiSettings?.isMapToolbarEnabled = false

            // Show the info window without the need for clicking the marker
            marker?.showInfoWindow()
        } else {
            Toast.makeText(
                requireContext(),
                "Latitude and longitude not available",
                Toast.LENGTH_SHORT
            ).show()
        }
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap?.isMyLocationEnabled = true

        }

        // Call the addDormitoryMarker function to pin the location
        val latitude = arguments?.getDouble("latitude")
        val longitude = arguments?.getDouble("longitude")
        val dormName = arguments?.getString("dormName")
        val address = arguments?.getString("address")
        if (address != null) {
            addDormitoryMarker(latitude, longitude, dormName, address)
        }

        // Enable zoom controls
        googleMap!!.uiSettings.isZoomControlsEnabled = true

        // Enable compass control
        googleMap!!.uiSettings.isCompassEnabled = true

        // Enable location tracking if needed
        // You can add your location tracking code here
    }

    private fun checkLandlordProfilePicture(landlordId: String) {
        val firestore = FirebaseFirestore.getInstance()
        firestore.collection("users")
            .document(landlordId)
            .get()
            .addOnSuccessListener { documentSnapshot ->
                if (documentSnapshot.exists()) {
                    val profileImageUrl = documentSnapshot.getString("profileImageUrl")
                    if (!profileImageUrl.isNullOrEmpty()) {
                        // Load and display the profile picture using Picasso or Glide
                        Picasso.get().load(profileImageUrl).into(ivProfilePicture)
                    } else {
                        // Handle the case where there's no profile image URL
                        // You can set a default image or hide the ImageView
                    }
                } else {
                    // Handle the case where the landlord document doesn't exist
                }
            }
            .addOnFailureListener { e ->
                // Handle the failure to fetch landlord profile picture
            }
    }


}
