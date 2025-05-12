package com.example.prog7313_part2.data.local.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "categories")
data class Category(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    @ColumnInfo(name = "name")
    val name: String,

    @ColumnInfo(name = "description")
    val description: String = "undefined",

    @ColumnInfo(name = "priceLimit")
    val priceLimit: Double = 0.0,

    @ColumnInfo(name = "userId")
    val userId: Int
)