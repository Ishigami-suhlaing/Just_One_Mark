package com.example.justonemark.data.models

data class ChapterData(
    val chapter: Int,
    val chapterName: String,
    val chapterPhoto: String,
    val content: List<SentenceData>
)

data class SentenceData(
    val sentenceId: Int,
    val chapter: String,
    val section: Int,
    val sentence: String,
    val questions: List<QuestionData>
)

data class QuestionData(
    val questionId: Int,
    val question_type: String,
    val question: String,
    val answer: Any, // Can be String or Boolean
    val options: List<Any>?, // Can be null or List of String/Boolean
    val practiceCount: Int,
    val important: Boolean,
    val mistake: Boolean
)