package com.example.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacts")
data class Contact(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String,
    val phoneNumber: String,
    val email: String = "",
    val bank: String = "",
    val pixKey: String = "",
    val notes: String = "",
    val createdAt: Long = System.currentTimeMillis()
)
