package com.example.kotlindormify.tenant3

import com.google.firebase.Timestamp

data class Chat3Message(
    val sender: String = "",
    val text: String = "",
    val timestamp: Timestamp = Timestamp.now() // Initialize with the current timestamp
)

