package com.example.justonemark.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "sentences",
    foreignKeys = [ForeignKey(
        entity = ChapterEntity::class,
        parentColumns = ["chapter"],
        childColumns = ["chapterOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class SentenceEntity(
    @PrimaryKey val sentenceId: Int,
    val chapter: String,
    val section: Int,
    val sentence: String,
    val chapterOwnerId: Int // FK to ChapterEntity
)
