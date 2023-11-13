package com.example.kotlindormify.landlord

data class Dormitory(
    val dormName: String?,
    val dormPrice: String?,
    val dormitoryId: String,
    val dormRooms: Int?,
    val images: List<String>? = null,
    val landlordId: String?,
    val qrCodeImageUrl: String?,
    var latitude: Double? = 0.0, // Add latitude
    var longitude: Double? = 0.0,  // Add longitude
    var address: String?,
    var phoneNumber: String?,
    var email: String?,
    var username: String?,
    var description: String?,
    var permitImage: String?,
    var pendingRequestsCount: Int?,
    var rentalTerm: String? = null,
    var bathroom: String?,
    var electric: String?,
    var water: String?,
    var paymentOptions: List<String>?,
    var amenities: List<String>?

)
