package com.renju_note.isoo.util

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import androidx.recyclerview.widget.RecyclerView
import com.renju_note.isoo.RenjuEditApplication
import com.renju_note.isoo.data.StorageElement
import com.renju_note.isoo.databinding.ItemStorageListBinding
import com.renju_note.isoo.databinding.PopupMenuStorageBinding
import io.realm.kotlin.ext.query
import io.realm.kotlin.query.Sort

class StorageRVAdapter(private val context : Context) : RecyclerView.Adapter<StorageRVAdapter.ViewHolder>() {

    private lateinit var storageList : List<StorageElement>

    interface StoragePopupMenuListener {
        fun load(pos : Int, storageList : List<StorageElement>)
        fun delete(pos : Int, element : StorageElement)
    }

    private var popupMenuListener : StoragePopupMenuListener? = null

    fun setOnStoragePopupMenuListener(listener : StoragePopupMenuListener) {
        popupMenuListener = listener
    }

    init {
        updateDataList()
    }

    inner class ViewHolder(private val binding : ItemStorageListBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(pos : Int) {
            if(pos >= storageList.size) {
                binding.itemStoragePreviewBg.visibility = View.INVISIBLE
                binding.itemStoragePreview.visibility = View.INVISIBLE
                binding.itemStorageTitleTv.visibility = View.INVISIBLE
                binding.itemStorageDateTv.visibility = View.INVISIBLE
                binding.itemStorageMenuBtn.visibility = View.INVISIBLE
            } else {
                binding.itemStoragePreviewBg.visibility = View.VISIBLE
                binding.itemStoragePreview.visibility = View.VISIBLE
                binding.itemStorageTitleTv.visibility = View.VISIBLE
                binding.itemStorageDateTv.visibility = View.VISIBLE
                binding.itemStorageMenuBtn.visibility = View.VISIBLE

                binding.itemStorageTitleTv.text = storageList[pos].title
                binding.itemStorageDateTv.text = storageList[pos].date
                binding.itemStoragePreview.putStone(storageList[pos].sequence)

                binding.itemStorageMenuBtn.setOnClickListener {
                    val popupBinding = PopupMenuStorageBinding.inflate(LayoutInflater.from(context))
                    val popupWindow = PopupWindow(
                        popupBinding.root,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        ViewGroup.LayoutParams.WRAP_CONTENT,
                        true
                    )
                    popupBinding.storagePopupLoadBtn.setOnClickListener {
                        popupWindow.dismiss()
                        popupMenuListener?.load(pos, storageList)
                    }
                    popupBinding.storagePopupDeleteBtn.setOnClickListener {
                        popupWindow.dismiss()
                        popupMenuListener?.delete(pos, storageList[pos])
                    }
                    popupWindow.elevation = 50f
                    popupWindow.showAsDropDown(binding.itemStorageMenuBtn)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemStorageListBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
    }

    override fun getItemCount(): Int {
        return if(storageList.size < 15) 15
        else storageList.size
    }

    fun updateDataList() {
        val realm = RenjuEditApplication.realm
        val results = realm.query<StorageElement>()
            .sort("date", Sort.DESCENDING)
            .find()
        storageList = results
    }

}