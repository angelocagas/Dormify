package com.example.kotlindormify

import com.google.firebase.firestore.FieldValue
import java.lang.reflect.Field
import java.sql.Timestamp

data class Dormitories(
    var dormId: String? = null,
    var dormName: String? = null,
    var numOfRooms: Int? = null,
    val maxCapacity: Int?,
    var previousPrice: String? = null,
    var price: String? = null,
    var gcashNum: String? = null,
    var address: String? = null,
    var phoneNumber: String? = null,
    var email: String? = null,
    var username: String? = null,
    var description: String? = null,
    var landlordId: String? = null,
    var latitude: Double? = null,
    var longitude: Double? = null,
    var images: List<String>? = null,
    var rentalTerm: String? = null,
    var bathroom: String? = null,
    var electric: String? = null,
    var water: String? = null,
    var paymentOptions: List<String>? = null,
    var amenities: List<String>? = null,
    var genderRestriction: String? = null,
    var numOfStars: Double = 0.0,
    var numOfRatings: Double = 0.0,
    var ratings: Double = 0.0,
    var status: String? = null

) {
    // No-argument constructor
    constructor() : this(
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null,
        null
    )
}
