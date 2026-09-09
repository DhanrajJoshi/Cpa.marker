package com.example.data.model

data class ImageItem(
    val id: String,
    val prompt: String,
    val size: String,
    val quality: String,
    val imageUrl: String,
    val timestamp: Long,
    val filename: String
)
