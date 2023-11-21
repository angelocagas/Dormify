package com.example.kotlindormify

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.text.SpannableString
import android.text.Spanned
import android.text.style.UnderlineSpan
import android.view.View
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import android.widget.Button // Import Button class
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.Navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.kotlindormify.landlord.Dormitory
import com.google.firebase.firestore.FirebaseFirestore
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.LatLngBounds
import com.google.android.material.bottomnavigation.BottomNavigationView
import android.location.Geocoder
import android.net.Uri
import android.view.ViewGroup
import android.widget.ImageView
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.constraintlayout.widget.ConstraintLayout
import com.example.kotlindormify.databinding.ActivityMainBinding


class HomeFragment : Fragment(R.layout.home_fragment), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private val DEFAULT_LATITUDE = 14.998027206214473 // Replace with your desired default latitude
    private val DEFAULT_LONGITUDE = 120.65611294250105 // Replace with your desired default longitude

    private val LOCATION_PERMISSION_REQUEST_CODE = 123
    private val DEFAULT_ZOOM_LEVEL = 35f

    private var isMapExpanded = false
    private var listExpanded = false
    private lateinit var allDormitoriesAdapter: AllDormitoriesAdapter

    private lateinit var allDormitoriesRecyclerView: RecyclerView
    private lateinit var allDormitoriesLayoutManager: LinearLayoutManager
    private lateinit var allDormitoriesLayoutManager2: GridLayoutManager

    private lateinit var tvSort: TextView



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        // Check and request location permission
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(android.Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }




        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        // Initialize the RecyclerViews and adapters
        allDormitoriesRecyclerView = view.findViewById<RecyclerView>(R.id.rvAllDormitories)
        val savedDormitoriesRecyclerView = view.findViewById<RecyclerView>(R.id.rvSavedDormitories)

        allDormitoriesLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        allDormitoriesRecyclerView.layoutManager = allDormitoriesLayoutManager

        allDormitoriesLayoutManager2 = GridLayoutManager(requireContext(), 2)

        val savedDormitoriesLayoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        savedDormitoriesRecyclerView.layoutManager = savedDormitoriesLayoutManager

        val allDormitoriesList: MutableList<Dormitory> = mutableListOf()
        val savedDormitoriesList: MutableList<Dormitory> = mutableListOf()

        allDormitoriesAdapter = AllDormitoriesAdapter(allDormitoriesList)
        val savedDormitoriesAdapter = AllDormitoriesAdapter(savedDormitoriesList)

        allDormitoriesRecyclerView.adapter = allDormitoriesAdapter
        savedDormitoriesRecyclerView.adapter = savedDormitoriesAdapter


        val tvExpandList = view.findViewById<TextView>(R.id.tvSeeAll)
        val btnSort = view.findViewById<ConstraintLayout>(R.id.clFilters)
        tvSort = view.findViewById<TextView>(R.id.tvAllDormitories2)





        btnSort.setOnClickListener {
            showSortOptionsDialog()
        }

        tvExpandList.setOnClickListener {
            if (!listExpanded) {
                // Set the layoutManager when the list is expanded
                allDormitoriesRecyclerView.layoutManager = allDormitoriesLayoutManager2

                // Add margins to the left and right
                val layoutParams = allDormitoriesRecyclerView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(50, 0, 0, 0)

                // Update the text and flag
                tvExpandList.text = "Collapse List"
                listExpanded = true
            } else {
                // If the list is collapsed, remove margins
                val layoutParams = allDormitoriesRecyclerView.layoutParams as ViewGroup.MarginLayoutParams
                layoutParams.setMargins(0, 0, 0, 0)

                // Set the original layoutManager
                allDormitoriesRecyclerView.layoutManager = allDormitoriesLayoutManager

                // Update the text and flag
                tvExpandList.text = "Expand List"
                listExpanded = false
            }
        }

        val activity = requireActivity() as DashboardActivity
        val currentUserEmail = activity.userEmail

        // Fetch saved dormitory IDs for the current user
        if (currentUserEmail != null) {
            fetchSavedDormitoryIds(currentUserEmail) { savedDormitoryIds ->
                // Query dormitory details based on saved IDs
                queryDormitoryDetails(savedDormitoryIds) { savedDormitories ->
                    // Add the retrieved saved dormitories to the list with reversing
                    savedDormitoriesList.clear()
                    savedDormitoriesList.addAll(savedDormitories)
                    savedDormitoriesAdapter.notifyDataSetChanged()
                }
            }
        }


        val tvSeeAllSaved = view.findViewById<TextView>(R.id.tvSeeAllSaved)
        val spannableString = SpannableString("View All Saved")
        spannableString.setSpan(UnderlineSpan(), 0, spannableString.length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
        tvSeeAllSaved.text = spannableString

        tvSeeAllSaved.setOnClickListener {
            val saveFragment = SavedFragment()
            val transaction = requireActivity().supportFragmentManager.beginTransaction()
            transaction.replace(R.id.fragment_container, saveFragment)
            transaction.addToBackStack(null) // Optional: Add to the back stack if needed
            transaction.commit()

            // Set the selected item in the bottom navigation view
            val bottomNavigationView = requireActivity().findViewById<BottomNavigationView>(R.id.bottom_navigation)
            bottomNavigationView.menu.findItem(R.id.menu_save).isChecked = true
        }




        // Query and display all dormitories
        queryAllDormitories { allDormitories ->
            allDormitoriesList.addAll(allDormitories)
            allDormitoriesAdapter.sortByDistanceAscending()
            allDormitoriesAdapter.notifyDataSetChanged()
        }



        // Initialize the map
        val mapFragment = childFragmentManager.findFragmentById(R.id.ivMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        // Find the button that will expand/shrink the map
        val toggleMapButton = view.findViewById<Button>(R.id.btnToggleMap)

        // Change to Button

        // Set an initial click listener for the button
        toggleMapButton.setOnClickListener {

            var condition = true

            if(condition){

                toggleMapHeight(toggleMapButton) // Pass the button as an argument
            }else{

            }
            condition = !condition
        }




    }


    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Enable location tracking
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            googleMap.isMyLocationEnabled = true
        }

        // Fetch all dormitory details
        queryAllDormitories { allDormitories ->
            val customMarkerIcon = BitmapDescriptorFactory.fromResource(R.drawable.logo_on_map)

            // Create a LatLngBounds builder
            val boundsBuilder = LatLngBounds.builder()

            // Add markers for dormitories and include their positions in the bounds
            // Add markers for dormitories and include their positions in the bounds
            for (dormitory in allDormitories) {
                    val dormitoryLocation = LatLng(
                        dormitory.latitude ?: DEFAULT_LATITUDE,
                        dormitory.longitude ?: DEFAULT_LONGITUDE
                    )

                    // Set the custom marker icon
                    val markerOptions = MarkerOptions()
                        .position(dormitoryLocation)
                        .title("${dormitory.dormName}")
                        .snippet("${dormitory.address}")
                        .icon(customMarkerIcon)

                    val marker = googleMap.addMarker(markerOptions)

                    // Include the dormitory's position in the bounds
                    boundsBuilder.include(dormitoryLocation)

                    // Add an info window for the marker

                googleMap.setOnMarkerClickListener { marker ->
                    // Show the info window for the clicked marker
                    marker.showInfoWindow()

                    // Center the camera on the clicked marker
                    val cameraUpdate = CameraUpdateFactory.newLatLng(marker.position)
                    googleMap.moveCamera(cameraUpdate)

                    true
                }

                // Add a click listener to the info window's title
                googleMap.setOnInfoWindowClickListener { clickedMarker ->
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


            }


            // Build the LatLngBounds object only if there are dormitories
            if (allDormitories.isNotEmpty()) {
                val defaultLocation = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, 14.0F)
                googleMap.moveCamera(cameraUpdate)
            } else {
                // Move the camera to the default latitude and longitude
                val defaultLocation = LatLng(DEFAULT_LATITUDE, DEFAULT_LONGITUDE)
                val cameraUpdate = CameraUpdateFactory.newLatLngZoom(defaultLocation, 14.0F)
                googleMap.moveCamera(cameraUpdate)

            }


            // Enable zoom controls
            googleMap.uiSettings.isZoomControlsEnabled = true

            // Enable compass control
            googleMap.uiSettings.isCompassEnabled = true

            googleMap.uiSettings.isMapToolbarEnabled = false
        }
    }




    private fun toggleMapHeight(button: Button) { // Accept the button as an argument
        // Find the map view by its ID
        val mapView = requireView().findViewById<View>(R.id.ivMapView)

        // Define initial and expanded heights for the map
        val initialHeight = resources.getDimension(R.dimen.initial_map_height).toInt()
        val expandedHeight = resources.getDimension(R.dimen.expanded_map_height).toInt()

        // Calculate the new height based on the current state
        val newHeight = if (isMapExpanded) initialHeight else expandedHeight

        // Update the map view's layout parameters
        val layoutParams = mapView.layoutParams
        layoutParams.height = newHeight
        mapView.layoutParams = layoutParams

        // Toggle the expanded state
        isMapExpanded = !isMapExpanded

        // Update the button text based on the new state
        val buttonDrawableId = if (isMapExpanded) R.drawable.dorm_maximize else R.drawable.dorm_minimize
        button.setBackgroundResource(buttonDrawableId)
    }

    private fun queryAllDormitories(callback: (List<Dormitory>) -> Unit) {
        // Query all dormitory details
        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        dormitoriesRef
            .get()
            .addOnSuccessListener { dormQuerySnapshot ->
                val allDormitories = mutableListOf<Dormitory>()
                for (dormitoryDocument in dormQuerySnapshot.documents) {
                    // Retrieve individual dormitory data
                    val dormName = dormitoryDocument.getString("dormName")
                    val dormPrice = dormitoryDocument.getString("price")
                    val dormitoryId = dormitoryDocument.getString("dormId") ?: ""
                    val numOfRooms = dormitoryDocument.getLong("numOfRooms")?.toInt()
                    val maxCapacity = dormitoryDocument.getLong("maxCapacity")?.toInt()
                    val imageUrl = dormitoryDocument.get("images") as? List<String>
                    val landlordId = dormitoryDocument.getString("landlordId")
                    val qrCodeImageUrl = dormitoryDocument.getString("qrCodeImageUrl")
                    val latitude = dormitoryDocument.getDouble("latitude")
                    val longitude = dormitoryDocument.getDouble("longitude")
                    val address = dormitoryDocument.getString("address")
                    val phoneNumber = dormitoryDocument.getString("phoneNumber")
                    val email = dormitoryDocument.getString("email")
                    val username = dormitoryDocument.getString("username")
                    val description = dormitoryDocument.getString("description")
                    val permitImage = dormitoryDocument.getString("permitImage")
                    val profileImage = dormitoryDocument.getString("profileImage")
                    val pendingRequestsCount = dormitoryDocument.getLong("pendingRequestsCount")?.toInt()
                    val rentalTerm = dormitoryDocument.getString("rentalTerm")
                    val bathroom = dormitoryDocument.getString("bathroom")
                    val electric = dormitoryDocument.getString("electric")
                    val water = dormitoryDocument.getString("water")
                    val paymentOptions = dormitoryDocument.get("paymentOptions") as? List<String>
                    val amenities = dormitoryDocument.get("amenities") as? List<String>
                    val amenities2 = dormitoryDocument.get("amenities2") as? List<String>
                    val genderRestriction = dormitoryDocument.getString("genderRestriction")


                    // Add other dormitory fields as needed

                    // Create a Dormitory object and add it to the list
                    val dormitory = Dormitory(dormName, dormPrice, dormitoryId, numOfRooms, maxCapacity, imageUrl, landlordId, qrCodeImageUrl, latitude, longitude, address, phoneNumber, email, username, description, permitImage, profileImage, pendingRequestsCount,rentalTerm, bathroom, electric, water, paymentOptions, amenities, amenities2, genderRestriction)
                    allDormitories.add(dormitory)
                }
                callback(allDormitories)
            }
            .addOnFailureListener { e ->
                // Handle the failure to query all dormitories
                callback(emptyList())
            }
    }

    private fun fetchSavedDormitoryIds(currentUserEmail: String, callback: (List<String>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        // Query user document by email
        firestore.collection("users")
            .whereEqualTo("email", currentUserEmail)
            .get()
            .addOnSuccessListener { querySnapshot ->
                if (!querySnapshot.isEmpty) {
                    val userDocument = querySnapshot.documents[0]
                    val savedDormitoryIds = userDocument["savedDormitories"] as? List<String> ?: emptyList()
                    callback(savedDormitoryIds)
                } else {
                    // No user found, return an empty list
                    callback(emptyList())
                }
            }
            .addOnFailureListener { e ->
                // Handle the failure to query the user document
                callback(emptyList())
            }
    }

    private fun queryDormitoryDetails(dormitoryIds: List<String>, callback: (List<Dormitory>) -> Unit) {
        val firestore = FirebaseFirestore.getInstance()
        val dormitoriesRef = firestore.collection("dormitories")

        // Create an empty list to store dormitory details
        val dormitoriesList: MutableList<Dormitory> = mutableListOf()

        // Query dormitory details for each saved dormitory ID
        for (dormitoryId in dormitoryIds) {
            dormitoriesRef.document(dormitoryId)
                .get()
                .addOnSuccessListener { dormitoryDocument ->
                    // Retrieve individual dormitory data
                    val dormName = dormitoryDocument.getString("dormName")
                    val dormPrice = dormitoryDocument.getString("price")
                    val dormitoryId = dormitoryDocument.getString("dormId") ?: ""
                    val numOfRooms = dormitoryDocument.getLong("numOfRooms")?.toInt()
                    val maxCapacity = dormitoryDocument.getLong("maxCapacity")?.toInt()
                    val imageUrl = dormitoryDocument.get("images") as? List<String>
                    val landlordId = dormitoryDocument.getString("landlordId")
                    val qrCodeImageUrl = dormitoryDocument.getString("qrCodeImageUrl")
                    val latitude = dormitoryDocument.getDouble("latitude")
                    val longitude = dormitoryDocument.getDouble("longitude")
                    val address = dormitoryDocument.getString("address")
                    val phoneNumber = dormitoryDocument.getString("phoneNumber")
                    val email = dormitoryDocument.getString("email")
                    val username = dormitoryDocument.getString("username")
                    val description = dormitoryDocument.getString("description")
                    val permitImage = dormitoryDocument.getString("permitImage")
                    val profileImage = dormitoryDocument.getString("profileImage")
                    val pendingRequestsCount = dormitoryDocument.getLong("pendingRequestsCount")?.toInt()
                    val rentalTerm = dormitoryDocument.getString("rentalTerm")
                    val bathroom = dormitoryDocument.getString("bathroom")
                    val electric = dormitoryDocument.getString("electric")
                    val water = dormitoryDocument.getString("water")
                    val paymentOptions = dormitoryDocument.get("paymentOptions") as? List<String>
                    val amenities = dormitoryDocument.get("amenities") as? List<String>
                    val amenities2 = dormitoryDocument.get("amenities2") as? List<String>
                    val genderRestriction = dormitoryDocument.getString("genderRestriction")
                    // Add other dormitory fields as needed



                    // Create a Dormitory object and add it to the list
                    val dormitory = Dormitory(dormName, dormPrice, dormitoryId, numOfRooms, maxCapacity, imageUrl, landlordId, qrCodeImageUrl, latitude, longitude, address, phoneNumber, email, username, description, permitImage, profileImage, pendingRequestsCount, rentalTerm, bathroom, electric, water, paymentOptions, amenities, amenities2, genderRestriction)
                    dormitoriesList.add(dormitory)


                    // Check if all dormitories have been retrieved
                    if (dormitoriesList.size == dormitoryIds.size) {
                        callback(dormitoriesList)
                    }

                }
                .addOnFailureListener { e ->
                    // Handle the failure to query dormitory details
                    callback(emptyList())
                }
        }
    }

    private var sortingOptions = arrayOf(
        "Name (Z - A ↓)",
        "Name (A - Z ↑)",
        "Price (Lowest - Highest ↑)",
        "Price (Highest to Lowest ↓)",
        "Distance (Closest to DHVSU)",
        "Distance (Farthest from DHVSU)"
    )

    private fun showSortOptionsDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Sort By:")
            .setItems(sortingOptions) { _, which ->
                when (which) {
                    0 -> {
                        allDormitoriesAdapter.sortByNameDescending()
                        tvSort.text = "Sorted by Name (Z - A)"
                        updateSortingOptionText(0)

                    }
                    1 -> {
                        allDormitoriesAdapter.sortByNameAscending()
                        tvSort.text = "Sorted by Name (A - Z)"
                        updateSortingOptionText(1)
                    }
                    2 -> {
                        allDormitoriesAdapter.sortByPriceAscending()
                        tvSort.text = "Sorted by Price (Lowest - Highest)"
                        updateSortingOptionText(2)
                    }
                    3 -> {
                        allDormitoriesAdapter.sortByPriceDescending()
                        tvSort.text = "Sorted by Price (Highest to Lowest)"
                        updateSortingOptionText(3)
                    }
                    4 -> {
                        allDormitoriesAdapter.sortByDistanceAscending()
                        tvSort.text = "Sorted by Distance (Closest to DHVSU)"
                        updateSortingOptionText(4)
                    }
                    5 -> {
                        allDormitoriesAdapter.sortByDistanceDescending()
                        tvSort.text = "Sorted by Distance (Farthest from DHVSU)"
                        updateSortingOptionText(5)
                    }

                }
            }
        val dialog = builder.create()
        dialog.show()
    }

    private fun updateSortingOptionText(selectedIndex: Int) {
        // Remove "(Selected)" from all options
        sortingOptions = sortingOptions.map { it.replace("  ✔", "") }.toTypedArray()

        // Update the text of the selected option
        sortingOptions[selectedIndex] += "  ✔"
        // Update the dialog with the modified options
    }







}