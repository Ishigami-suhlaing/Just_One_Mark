package com.example.justonemark.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.justonemark.data.entities.SentenceEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface SentenceDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAllSentences(sentences: List<SentenceEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSentence(sentence: SentenceEntity)

    @Query("SELECT * FROM sentences WHERE chapterOwnerId = :chapterId")
    fun getSentencesByChapter(chapterId: Int): Flow<List<SentenceEntity>>

    @Query("SELECT * FROM sentences")
    fun getAllSentences(): Flow<List<SentenceEntity>>

    @Query("SELECT COUNT(*) FROM sentences")
    suspend fun getCount(): Int
}
