package com.renju_note.isoo.setting

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.renju_note.isoo.R
import com.renju_note.isoo.RenjuEditApplication.Companion.settings
import com.renju_note.isoo.databinding.ItemSettingColorBinding
import androidx.core.graphics.toColorInt

class SettingColorRVAdapter(val context : Context) : RecyclerView.Adapter<SettingColorRVAdapter.ViewHolder>() {

    interface OnItemClickListener {
        fun onItemClick(position : Int) { }
    }
    private var onItemClickListener : OnItemClickListener? = null
    fun setOnItemClickListener(listener : OnItemClickListener) {
        onItemClickListener = listener
    }

    inner class ViewHolder(private val binding : ItemSettingColorBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(position : Int) {
            when(position) {
                0 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_board_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.boardColor)
                }
                1 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_line_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.lineColor)
                }
                2 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_board_text_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.textColor)
                }
                3 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_board_node_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.nodeColor)
                }
                4 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_last_stone_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.lastStoneStrokeColor)
                }
                5 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_text_box_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.textAreaSetting.backgroundColor)
                }
                6 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_text_box_stroke_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.textAreaSetting.strokeColor)
                }
                7 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_text_box_text_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.textAreaSetting.textColor)
                }
                8 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_draw_line_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.drawLineColor)
                }
                9 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_draw_area_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.drawAreaColor)
                }
                10 -> {
                    binding.itemColorText.text = context.getString(R.string.setting_draw_arrow_color)
                    binding.itemColorPreview.background = makePreviewDrawable(settings.boardColorSetting.drawArrowColor)
                }
                else -> {
                    binding.itemColorText.text = context.getString(R.string.error)
                    binding.itemColorPreview.background = makePreviewDrawable("#FFFFFF")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val binding = ItemSettingColorBinding.inflate(LayoutInflater.from(parent.context), parent, false)
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
                        onItemClickListener?.onItemClick(position)
                    }
                }
            }
            false
        }
        holder.itemView.setOnClickListener { }
    }

    override fun getItemCount(): Int = 11

    private fun makePreviewDrawable(color : String) : GradientDrawable {
        val drawable1 = GradientDrawable()
        drawable1.setColor(color.toColorInt())
        drawable1.setStroke(3, "#666666".toColorInt())
        drawable1.shape = GradientDrawable.RECTANGLE
        return drawable1
    }

}