package com.timilehinaregbesola.pneumoniaclassifier

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.timilehinaregbesola.pneumoniaclassifier.databinding.RecogItemBinding

class RecognitionAdapter(private val ctx: Context) :
    ListAdapter<GalleryUploadViewModel.Recognition, RecognitionViewHolder>(RecognitionDiffUtil()) {

    /**
     * Inflating the ViewHolder with recognition_item layout and data binding
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecognitionViewHolder {
        val inflater = LayoutInflater.from(ctx)
        val binding = RecogItemBinding.inflate(inflater, parent, false)
        return RecognitionViewHolder(binding)
    }

    // Binding the data fields to the RecognitionViewHolder
    override fun onBindViewHolder(holder: RecognitionViewHolder, position: Int) {
        holder.bindTo(getItem(position))
    }

    private class RecognitionDiffUtil : DiffUtil.ItemCallback<GalleryUploadViewModel.Recognition>() {
        override fun areItemsTheSame(oldItem: GalleryUploadViewModel.Recognition, newItem: GalleryUploadViewModel.Recognition): Boolean {
            return oldItem.label == newItem.label
        }

        override fun areContentsTheSame(oldItem: GalleryUploadViewModel.Recognition, newItem: GalleryUploadViewModel.Recognition): Boolean {
            return oldItem.confidence == newItem.confidence
        }
    }
}

class RecognitionViewHolder(private val binding: RecogItemBinding) :
    RecyclerView.ViewHolder(binding.root) {

    // Binding all the fields to the view - to see which UI element is bind to which field, check
    // out layout/recognition_item.xml
    fun bindTo(recognition: GalleryUploadViewModel.Recognition) {
        binding.recognitionItem = recognition
        binding.executePendingBindings()
    }
}
