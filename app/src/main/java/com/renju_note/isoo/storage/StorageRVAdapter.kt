package com.renju_note.isoo.storage

import android.content.Context
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.renju_note.isoo.databinding.ItemStorageListBinding
import com.renju_note.isoo.databinding.PopupMenuStorageBinding

class StorageRVAdapter(
    private val context : Context,
    private val listener : StoragePopupMenuListener
) : ListAdapter<StorageElement, StorageRVAdapter.ViewHolder>(StorageDiffCallback()) {

    class StorageDiffCallback : DiffUtil.ItemCallback<StorageElement>() {
        override fun areItemsTheSame(oldItem: StorageElement, newItem: StorageElement): Boolean {
            return oldItem.location == newItem.location
        }

        override fun areContentsTheSame(oldItem: StorageElement, newItem: StorageElement): Boolean {
            return oldItem == newItem
        }
    }

    interface StoragePopupMenuListener {
        fun onLoad(element : StorageElement)
        fun onDelete(element : StorageElement)
    }

    inner class ViewHolder(private val binding : ItemStorageListBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(element : StorageElement) {
            binding.itemStorageTitleTv.text = element.title
            binding.itemStorageDateTv.text = element.date
            binding.itemStoragePreview.putStone(element.sequence)

            binding.itemStorageMenuBtn.setOnClickListener {
                val popupBinding = PopupMenuStorageBinding.inflate(LayoutInflater.from(context))
                val popupWindow = PopupWindow(popupBinding.root, ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT, true)

                popupBinding.storagePopupLoadBtn.setOnClickListener {
                    popupWindow.dismiss()
                    currentElement()?.let { listener.onLoad(it) }
                }
                popupBinding.storagePopupDeleteBtn.setOnClickListener {
                    popupWindow.dismiss()
                    currentElement()?.let { listener.onDelete(it) }
                }
                popupWindow.elevation = 50f
                popupWindow.showAsDropDown(binding.itemStorageMenuBtn)
            }
        }

        // the list may have changed while the popup was open
        private fun currentElement() : StorageElement? =
            bindingAdapterPosition.takeIf { it != RecyclerView.NO_POSITION }?.let { getItem(it) }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStorageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

}