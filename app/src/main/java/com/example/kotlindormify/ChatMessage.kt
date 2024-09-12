package com.example.kotlindormify

import com.google.firebase.Timestamp

data class ChatMessage(
    val sender: String = "",
    val receiver: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now() // Initialize with the current timestamp
)

