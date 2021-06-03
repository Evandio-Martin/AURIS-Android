package com.bangkit.auris.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bangkit.auris.utils.DataDictionary
import com.bumptech.glide.Glide
import com.bangkit.auris.databinding.ItemDictionaryBinding

class DataAdapter : RecyclerView.Adapter<DataAdapter.ViewHolder>() {

    private var listDataLibrary = ArrayList<DataDictionary>()

    fun setDataLibrary(dictionary: List<DataDictionary>?){
        if (dictionary == null) return
        this.listDataLibrary.clear()
        this.listDataLibrary.addAll(dictionary)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemLibraryBinding = ItemDictionaryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(itemLibraryBinding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val library = listDataLibrary[position]
        holder.bind(library)
    }

    override fun getItemCount(): Int = listDataLibrary.size

    class ViewHolder(private val binding: ItemDictionaryBinding): RecyclerView.ViewHolder(binding.root) {
        fun bind(dictionary: DataDictionary){
            with(binding){
                tvTitle.text = dictionary.title
                Glide.with(itemView.context)
                    .load(dictionary.image)
                    .into(ivLibrary)
            }
        }

    }
}