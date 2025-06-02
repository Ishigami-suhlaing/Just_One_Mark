package com.example.justonemark.ui.chapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.justonemark.data.entities.ChapterEntity
import com.example.justonemark.databinding.ItemChapterBinding

class ChapterAdapter(
    private val onChapterClick: (ChapterEntity) -> Unit
) : ListAdapter<ChapterEntity, ChapterAdapter.ChapterViewHolder>(ChapterDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChapterViewHolder {
        val binding = ItemChapterBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ChapterViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ChapterViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class ChapterViewHolder(
        private val binding: ItemChapterBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(chapter: ChapterEntity) {
            binding.apply {
                chapterNumberTxt.text = "Chapter - ${chapter.chapter}"
                chapterNameTxt.text = chapter.chapterName

                // Set chapter image based on chapterPhoto
                val imageResource = when (chapter.chapterPhoto) {
                    "@drawable/p1" -> com.example.justonemark.R.drawable.p1
                    "@drawable/p2" -> com.example.justonemark.R.drawable.p2
                    "@drawable/p3" -> com.example.justonemark.R.drawable.p3
                    else -> com.example.justonemark.R.drawable.p1 // default
                }
                chapterImageView.setImageResource(imageResource)

                root.setOnClickListener {
                    onChapterClick(chapter)
                }
            }
        }
    }

    private class ChapterDiffCallback : DiffUtil.ItemCallback<ChapterEntity>() {
        override fun areItemsTheSame(oldItem: ChapterEntity, newItem: ChapterEntity): Boolean {
            return oldItem.chapter == newItem.chapter
        }

        override fun areContentsTheSame(oldItem: ChapterEntity, newItem: ChapterEntity): Boolean {
            return oldItem == newItem
        }
    }
}
