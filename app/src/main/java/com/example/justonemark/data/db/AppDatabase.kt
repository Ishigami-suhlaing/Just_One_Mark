package com.example.justonemark.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.justonemark.data.dao.ChapterDao
import com.example.justonemark.data.dao.QuestionDao
import com.example.justonemark.data.dao.SentenceDao
import com.example.justonemark.data.entities.ChapterEntity
import com.example.justonemark.data.entities.QuestionEntity
import com.example.justonemark.data.entities.SentenceEntity

@Database(
    entities = [ChapterEntity::class, SentenceEntity::class, QuestionEntity::class],
    version = 2
)
@TypeConverters(Converters::class)
abstract class AppDatabase: RoomDatabase() {
    abstract fun chapterDao(): ChapterDao
    abstract fun sentenceDao(): SentenceDao
    abstract fun questionDao(): QuestionDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "learning_app_db"
                ).fallbackToDestructiveMigration().build().also { INSTANCE = it }
            }
        }
    }

}
