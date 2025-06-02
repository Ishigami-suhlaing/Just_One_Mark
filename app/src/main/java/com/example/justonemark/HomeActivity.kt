package com.example.justonemark

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.example.justonemark.data.db.AppDatabase
import com.example.justonemark.data.entities.ChapterEntity
import com.example.justonemark.data.entities.QuestionEntity
import com.example.justonemark.data.entities.SentenceEntity
import com.example.justonemark.data.models.ChapterData
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.IOException

class HomeActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_home)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Initialize database and parse JSON data
        lifecycleScope.launch {
            parseAndInsertDataFromJson()
        }
    }

    private suspend fun parseAndInsertDataFromJson() {
        try {
            // Read JSON from assets
            val jsonString = assets.open("dataj.json").bufferedReader().use { it.readText() }

            // Parse JSON to data models
            val gson = Gson()
            val listType = object : TypeToken<List<ChapterData>>() {}.type
            val chaptersData: List<ChapterData> = gson.fromJson(jsonString, listType)

            // Get database instance
            val database = AppDatabase.getInstance(this@HomeActivity)

            // Check if data already exists
            val existingChapterCount = database.chapterDao().getCount()
            if (existingChapterCount > 0) {
                return // Data already exists, no need to insert again
            }

            // Convert and insert data
            chaptersData.forEach { chapterData ->
                // Insert chapter
                val chapterEntity = ChapterEntity(
                    chapter = chapterData.chapter,
                    chapterName = chapterData.chapterName,
                    chapterPhoto = chapterData.chapterPhoto
                )
                database.chapterDao().insertChapter(chapterEntity)

                // Insert sentences
                val sentenceEntities = chapterData.content.map { sentenceData ->
                    SentenceEntity(
                        sentenceId = sentenceData.sentenceId,
                        chapter = sentenceData.chapter,
                        section = sentenceData.section,
                        sentence = sentenceData.sentence,
                        chapterOwnerId = chapterData.chapter
                    )
                }
                database.sentenceDao().insertAllSentences(sentenceEntities)

                // Insert questions
                val questionEntities = mutableListOf<QuestionEntity>()
                chapterData.content.forEach { sentenceData ->
                    sentenceData.questions.forEach { questionData ->
                        val optionsList = questionData.options?.map { it.toString() }
                        val questionEntity = QuestionEntity(
                            questionId = questionData.questionId,
                            questionType = questionData.question_type,
                            question = questionData.question,
                            answer = questionData.answer.toString(),
                            options = optionsList ?: emptyList(),
                            practiceCount = questionData.practiceCount,
                            important = questionData.important,
                            mistake = questionData.mistake,
                            sentenceOwnerId = sentenceData.sentenceId
                        )
                        questionEntities.add(questionEntity)
                    }
                }
                database.questionDao().insertAllQuestions(questionEntities)
            }

        } catch (e: IOException) {
            e.printStackTrace()
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
