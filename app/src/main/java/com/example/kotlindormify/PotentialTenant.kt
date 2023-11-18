package com.example.kotlindormify

import com.google.firebase.firestore.FieldValue
import java.lang.reflect.Field
import java.sql.Timestamp

data class PotentialTenant(
    var address: String? = null,
    var age: String? = null,
    var email: String? = null,
    var emergencyAddress: String? = null,
    var emergencyEmail: String? = null,
    var emergencyFullName: String? = null,
    var emergencyPhoneNumber: String? = null,
    var gender: String? = null,
    var phoneNumber: String? = null,
    var requesterFullName: String? = null,
    var timestamp: String? = null

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
        null
    )
}
