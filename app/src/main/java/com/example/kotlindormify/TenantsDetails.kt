package com.example.kotlindormify

data class TenantsDetails(
    var username: String? = null,
    var age: String? = null,
    var sex: String? = null,
    var address: String? = null,
    var phoneNumber: String? = null,
    var email: String? = null,
    var password: String? = null,
    val profile: String? = null,
    val identification: String? = null,
    var role: Int = 0, // Default value for role

    //emergency contact
    var ecUsername: String? = null,
    var ecAddress: String? = null,
    var ecPhoneNumber: String? = null,
    var ecEmail: String? = null,

    var fcmToken: String? = null // FCM token field

)
