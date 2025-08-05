package com.example.stoki.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "brands")
data class Brand(
    @PrimaryKey(autoGenerate = true)
    val id: Int = 0,
    val name: String
)