package com.example.kotlindormify.landlord

import com.google.firebase.Timestamp

data class Conversation(
    val title: String,
    val lastMessage: String,
    val Tenant: String,
    val username: String,
    val userPic: String,
    val lastMessageTimestamp: Timestamp

)

