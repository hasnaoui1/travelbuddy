package com.example.travelbuddy.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.travelbuddy.R
import com.example.travelbuddy.database.Destination
import com.example.travelbuddy.databinding.ItemDestinationBinding
import java.io.File

class DestinationAdapter(
    private var destinations: List<Destination>,
    private val onItemClick: (Destination) -> Unit
) : RecyclerView.Adapter<DestinationAdapter.DestinationViewHolder>() {
    
    fun updateDestinations(newDestinations: List<Destination>) {
        destinations = newDestinations
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DestinationViewHolder {
        val binding = ItemDestinationBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return DestinationViewHolder(binding)
    }
    
    override fun onBindViewHolder(holder: DestinationViewHolder, position: Int) {
        holder.bind(destinations[position])
    }
    
    override fun getItemCount() = destinations.size
    
    inner class DestinationViewHolder(
        private val binding: ItemDestinationBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        
        init {
            binding.root.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClick(destinations[position])
                }
            }
        }
        
        fun bind(destination: Destination) {
            binding.textName.text = destination.name
            binding.textLocation.text = itemView.context.getString(
                R.string.location_format,
                destination.city,
                destination.country
            )
            
            destination.imagePath?.let { path ->
                binding.imageDestination.setImageURI(Uri.fromFile(File(path)))
            } ?: run {
                binding.imageDestination.setImageResource(R.drawable.placeholder_destination)
            }
        }
    }
}
