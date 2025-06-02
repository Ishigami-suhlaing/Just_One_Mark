package com.example.justonemark

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.GridLayoutManager
import com.example.justonemark.data.db.AppDatabase
import com.example.justonemark.data.entities.ChapterEntity
import com.example.justonemark.data.entities.QuestionEntity
import com.example.justonemark.data.entities.SentenceEntity
import com.example.justonemark.data.models.ChapterData
import com.example.justonemark.databinding.ActivityHomeBinding
import com.example.justonemark.ui.chapter.ChapterAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch
import java.io.IOException

class HomeActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHomeBinding
    private lateinit var chapterAdapter: ChapterAdapter
    private lateinit var database: AppDatabase

    override fun onCreate(savedInstanceState: Bundle?) {
        binding = ActivityHomeBinding.inflate(layoutInflater)
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(binding.root)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        database = AppDatabase.getInstance(this)
        setupRecyclerView()

        // Initialize database and parse JSON data
        lifecycleScope.launch {
            Log.d("HomeActivity", "Starting data initialization")

            // FOR DEBUGGING: Clear database and force re-insertion
            try {
                database.clearAllTables()
                Log.d("HomeActivity", "Database cleared for fresh start")
            } catch (e: Exception) {
                Log.e("HomeActivity", "Error clearing database", e)
            }

            // Parse and insert data FIRST, then load chapters
            parseAndInsertDataFromJson()

            // Only load chapters after insertion is complete
            Log.d("HomeActivity", "Now loading chapters...")
            loadChaptersOnce()
        }
    }

    private fun setupRecyclerView() {
        chapterAdapter = ChapterAdapter { chapter ->
            onChapterClick(chapter)
        }

        binding.chapterRv.apply {
            adapter = chapterAdapter
            layoutManager = GridLayoutManager(this@HomeActivity, 2)
        }
        Log.d("HomeActivity", "RecyclerView setup complete")
    }

    private fun onChapterClick(chapter: ChapterEntity) {
        Toast.makeText(this, "Clicked: ${chapter.chapterName}", Toast.LENGTH_SHORT).show()
        // TODO: Navigate to chapter detail screen
    }

    private suspend fun loadChaptersOnce() {
        Log.d("HomeActivity", "Loading chapters from database")
        database.chapterDao().getAllChapters().collect { chapters ->
            Log.d("HomeActivity", "Loaded ${chapters.size} chapters")
            chapterAdapter.submitList(chapters)
        }
    }

    private suspend fun loadChapters() {
        Log.d("HomeActivity", "Loading chapters from database")
        database.chapterDao().getAllChapters().collect { chapters ->
            Log.d("HomeActivity", "Loaded ${chapters.size} chapters")
            chapterAdapter.submitList(chapters)
        }
    }

    private suspend fun parseAndInsertDataFromJson() {
        try {
            Log.d("HomeActivity", "parseAndInsertDataFromJson() started")

            // Check if data already exists
            val existingChapterCount = database.chapterDao().getCount()
            Log.d("HomeActivity", "Existing chapter count: $existingChapterCount")

            if (existingChapterCount > 0) {
                Log.d("HomeActivity", "Data already exists, skipping insertion")
                return // Data already exists, no need to insert again
            }

            Log.d("HomeActivity", "Reading JSON from assets")

            // Test if assets folder is accessible
            val assetList = assets.list("")
            Log.d("HomeActivity", "Assets folder contents: ${assetList?.joinToString()}")

            // Read JSON from assets with proper BOM handling
            val inputStream = assets.open("dataj.json")
            val bytes = inputStream.readBytes()
            inputStream.close()

            // Remove BOM if present and convert to string
            val jsonString = when {
                bytes.size >= 3 && bytes[0] == 0xEF.toByte() && bytes[1] == 0xBB.toByte() && bytes[2] == 0xBF.toByte() -> {
                    // UTF-8 BOM
                    String(bytes.sliceArray(3 until bytes.size), Charsets.UTF_8)
                }

                bytes.size >= 2 && bytes[0] == 0xFF.toByte() && bytes[1] == 0xFE.toByte() -> {
                    // UTF-16 LE BOM
                    String(bytes.sliceArray(2 until bytes.size), Charsets.UTF_16LE)
                }

                bytes.size >= 2 && bytes[0] == 0xFE.toByte() && bytes[1] == 0xFF.toByte() -> {
                    // UTF-16 BE BOM
                    String(bytes.sliceArray(2 until bytes.size), Charsets.UTF_16BE)
                }

                else -> {
                    // No BOM, assume UTF-8
                    String(bytes, Charsets.UTF_8)
                }
            }

            Log.d("HomeActivity", "JSON length: ${jsonString.length}")
            Log.d("HomeActivity", "JSON preview: ${jsonString.take(200)}...")

            if (jsonString.isBlank() || jsonString.trim() == "[]") {
                Log.e("HomeActivity", "JSON file is empty or contains empty array")
                return
            }

            // Parse JSON to data models
            Log.d("HomeActivity", "Starting JSON parsing")
            val gson = Gson()
            val listType = object : TypeToken<List<ChapterData>>() {}.type
            val chaptersData: List<ChapterData> = gson.fromJson(jsonString, listType)
            Log.d("HomeActivity", "Parsed ${chaptersData.size} chapters from JSON")

            if (chaptersData.isEmpty()) {
                Log.e("HomeActivity", "No chapters found in JSON data")
                return
            }

            // Convert and insert data
            chaptersData.forEachIndexed { index, chapterData ->
                Log.d("HomeActivity", "Processing chapter $index: ${chapterData.chapterName}")

                // Insert chapter
                val chapterEntity = ChapterEntity(
                    chapter = chapterData.chapter,
                    chapterName = chapterData.chapterName,
                    chapterPhoto = chapterData.chapterPhoto
                )

                try {
                    database.chapterDao().insertChapter(chapterEntity)
                    Log.d(
                        "HomeActivity",
                        "Successfully inserted chapter: ${chapterData.chapterName}"
                    )
                } catch (e: Exception) {
                    Log.e("HomeActivity", "Failed to insert chapter: ${chapterData.chapterName}", e)
                    return
                }

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

                try {
                    database.sentenceDao().insertAllSentences(sentenceEntities)
                    Log.d(
                        "HomeActivity",
                        "Inserted ${sentenceEntities.size} sentences for chapter ${chapterData.chapter}"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "HomeActivity",
                        "Failed to insert sentences for chapter ${chapterData.chapter}",
                        e
                    )
                }

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

                try {
                    database.questionDao().insertAllQuestions(questionEntities)
                    Log.d(
                        "HomeActivity",
                        "Inserted ${questionEntities.size} questions for chapter ${chapterData.chapter}"
                    )
                } catch (e: Exception) {
                    Log.e(
                        "HomeActivity",
                        "Failed to insert questions for chapter ${chapterData.chapter}",
                        e
                    )
                }
            }

            Log.d("HomeActivity", "Data insertion completed successfully")

            // Verify insertion
            val finalCount = database.chapterDao().getCount()
            Log.d("HomeActivity", "Final chapter count after insertion: $finalCount")

        } catch (e: IOException) {
            Log.e("HomeActivity", "IOException during data parsing", e)
            e.printStackTrace()
        } catch (e: Exception) {
            Log.e("HomeActivity", "Exception during data parsing", e)
            e.printStackTrace()
        }
    }
}
