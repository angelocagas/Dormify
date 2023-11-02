import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.app.ActivityCompat
import androidx.fragment.app.Fragment
import com.example.kotlindormify.R
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.material.floatingactionbutton.FloatingActionButton

class MapFragment : Fragment(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var locationCallback: LocationCallback
    private lateinit var refreshButton: FloatingActionButton
    private var currentLocationMarker: Marker? = null // To keep track of the current location marker

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val rootView = inflater.inflate(R.layout.map_fragment, container, false)

        val mapFragment =
            childFragmentManager.findFragmentById(R.id.ivMapView) as SupportMapFragment
        mapFragment.getMapAsync(this)

        refreshButton = rootView.findViewById(R.id.refreshButton)
        refreshButton.setOnClickListener {
            // Handle the refresh button click to update the location
            updateCurrentLocation()
        }

        return rootView
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // Initialize FusedLocationProviderClient
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(requireContext())

        // Create a LocationRequest
        val locationRequest = LocationRequest.create()
            .setInterval(10000) // Update location every 10 seconds
            .setFastestInterval(5000) // Fastest update interval
            .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)

        // Create a LocationCallback
        locationCallback = object : LocationCallback() {
            override fun onLocationResult(locationResult: LocationResult?) {
                locationResult ?: return
                for (location in locationResult.locations) {
                    // Update the map with the new location and remove the previous marker
                    currentLocationMarker?.remove()
                    updateMapLocation(location)
                }
            }
        }

        // Request location updates if permission is granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            fusedLocationClient.requestLocationUpdates(locationRequest, locationCallback, null)
        } else {
            // Request location permission if not granted
            ActivityCompat.requestPermissions(
                requireActivity(),
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }

        // Enable zoom controls
        googleMap.uiSettings.isZoomControlsEnabled = true

        // Enable compass control
        googleMap.uiSettings.isCompassEnabled = true

        // Add a marker at the user's current location using the default marker icon


        // Set an OnClickListener for the current location button
        googleMap.setOnMyLocationButtonClickListener {
            updateCurrentLocation()
            true
        }
    }


    private fun updateMapLocation(location: Location) {
        // Get the current location's latitude and longitude
        val latitude = location.latitude
        val longitude = location.longitude

        // Create a LatLng object
        val userLocation = LatLng(latitude, longitude)

        // Move the camera to the user's current location
        googleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))

        // Add a marker at the user's current location using the default marker icon
        currentLocationMarker = googleMap.addMarker(
            MarkerOptions().position(userLocation).title("My Location")
        )
    }

    private fun updateCurrentLocation() {
        // Check if the location permission is granted
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            // Request the last known location and update the map
            fusedLocationClient.lastLocation.addOnSuccessListener { location ->
                location?.let {
                    // Remove the previous marker and add the new one
                    currentLocationMarker?.remove()
                    updateMapLocation(location)
                }
            }
        }
    }

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 123
    }

    override fun onDestroy() {
        super.onDestroy()
        // Stop location updates when the fragment is destroyed
        fusedLocationClient.removeLocationUpdates(locationCallback)
    }
}
