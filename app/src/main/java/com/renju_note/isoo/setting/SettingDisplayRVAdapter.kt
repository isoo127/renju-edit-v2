package com.renju_note.isoo.setting

import android.animation.ObjectAnimator
import android.content.Context
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renju_note.isoo.R
import com.renju_note.isoo.RenjuEditApplication.Companion.settings
import com.renju_note.isoo.databinding.ItemSettingCheckBinding

class SettingDisplayRVAdapter(val context : Context) : RecyclerView.Adapter<SettingDisplayRVAdapter.ViewHolder>() {

    interface OnItemCheckListener {
        fun onItemCheck(position : Int, isCheck : Boolean) { }
    }
    private var onItemClickListener : OnItemCheckListener? = null
    fun setOnItemCheckListener(listener : OnItemCheckListener) {
        onItemClickListener = listener
    }

    inner class ViewHolder(private val binding : ItemSettingCheckBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position : Int) {
            when(position) {
                0 -> {
                    binding.itemCheckText.text = context.getString(R.string.setting_text_box_visible)
                    binding.itemCheckCheckbox.isChecked = settings.textAreaSetting.isVisible
                }
                1 -> {
                    binding.itemCheckText.text = context.getString(R.string.setting_sequence_visible)
                    binding.itemCheckCheckbox.isChecked = settings.boardDisplaySetting.sequenceVisible
                }
                2 -> {
                    binding.itemCheckText.text = context.getString(R.string.setting_next_node_visible)
                    binding.itemCheckCheckbox.isChecked = settings.boardDisplaySetting.nextNodeVisible
                }
                3 -> {
                    binding.itemCheckText.text = context.getString(R.string.setting_text_mode_on)
                    binding.itemCheckCheckbox.isChecked = settings.modeSetting.canUseTextMode
                }
                4 -> {
                    binding.itemCheckText.text = context.getString(R.string.setting_draw_mode_on)
                    binding.itemCheckCheckbox.isChecked = settings.modeSetting.canUseDrawMode
                }
            }
            binding.itemCheckCheckbox.isClickable = false
            binding.itemCheckCheckbox.setOnCheckedChangeListener { _, b -> onItemClickListener?.onItemCheck(position, b) }
        }

        fun changeCheck() {
            binding.itemCheckCheckbox.isChecked = !binding.itemCheckCheckbox.isChecked
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingCheckBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(position)
        holder.itemView.setOnTouchListener { v, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    val anim = ObjectAnimator.ofFloat(v, View.ALPHA, 1f, 0.3f)
                    anim.duration = 200
                    anim.start()
                }
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> {
                    val anim = ObjectAnimator.ofFloat(v, View.ALPHA, 0.3f, 1f)
                    anim.duration = 200
                    anim.start()
                    if (event.action == MotionEvent.ACTION_UP) {
                        v.performClick()
                        holder.changeCheck()
                    }
                }
            }
            false
        }
        holder.itemView.setOnClickListener { }
    }

    override fun getItemCount(): Int = 5

}