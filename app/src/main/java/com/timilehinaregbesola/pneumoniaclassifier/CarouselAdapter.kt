package com.timilehinaregbesola.pneumoniaclassifier

import android.graphics.Bitmap
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.github.islamkhsh.CardSliderAdapter
import com.timilehinaregbesola.pneumoniaclassifier.databinding.ImageDisplayCardBinding

class CarouselAdapter(private val images: ArrayList<Bitmap>) : CardSliderAdapter<CarouselAdapter.CarouselViewHolder>() {

    override fun getItemCount() = images.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CarouselViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = ImageDisplayCardBinding.inflate(inflater, parent, false)
        return CarouselViewHolder(binding)
    }

    override fun bindVH(holder: CarouselViewHolder, position: Int) {
        with(holder) {
            with(images[position]) {
                binding.imgSelected.setImageBitmap(this)
            }
        }
    }

    class CarouselViewHolder(val binding: ImageDisplayCardBinding) : RecyclerView.ViewHolder(binding.root)
}
