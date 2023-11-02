package com.example.kotlindormify

data class Users(
    var email: String? = null,
    var phoneNumber: String? = null,
    var password: String? = null,
    var role: Int = 0, // Default value for role
    var fcmToken: String? = null, // FCM token field
    var username: String? = null
)
