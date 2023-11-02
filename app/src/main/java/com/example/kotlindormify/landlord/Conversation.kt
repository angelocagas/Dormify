package com.example.kotlindormify.landlord

import com.google.firebase.Timestamp

data class Conversation(
    val title: String,
    val lastMessage: String,
    val timestamp: Timestamp = Timestamp.now()
)

