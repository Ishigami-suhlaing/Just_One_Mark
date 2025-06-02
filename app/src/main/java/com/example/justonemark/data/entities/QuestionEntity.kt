package com.example.justonemark.data.entities

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey

@Entity(
    tableName = "questions",
    foreignKeys = [ForeignKey(
        entity = SentenceEntity::class,
        parentColumns = ["sentenceId"],
        childColumns = ["sentenceOwnerId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class QuestionEntity(
    @PrimaryKey val questionId: Int,
    val questionType: String,
    val question: String,
    val answer: String,
    val options: List<String>, // requires TypeConverter
    val practiceCount: Int,
    val important: Boolean,
    val mistake: Boolean,
    val sentenceOwnerId: Int // FK to SentenceEntity
)
