package com.example.justonemark.data.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "chapters")
data class ChapterEntity(
    @PrimaryKey val chapter: Int,
    val chapterName: String,
    val chapterPhoto: String
)
