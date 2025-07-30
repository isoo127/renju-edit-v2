package com.renju_note.isoo.dialog

import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import androidx.core.content.ContextCompat
import androidx.core.graphics.toColorInt
import com.renju_note.isoo.R
import com.renju_note.isoo.databinding.DialogColorPickerBinding
import com.skydoves.colorpickerview.listeners.ColorListener

class ColorPickerDialog(context : Context, private val activity : Activity, private val initialColor : String) : Dialog(context) {

    private lateinit var binding : DialogColorPickerBinding
    private var isKeyBoardUp = false
    private var returnColor = ""

    interface OnApplyColorListener {
        fun onApplyColor(color : String) { }
    }
    private var onApplyColorListener : OnApplyColorListener? = null
    fun setOnApplyColorListener(listener : OnApplyColorListener) {
        onApplyColorListener = listener
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = DialogColorPickerBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.colorPickerView.attachAlphaSlider(binding.colorPickerAlphaBar)
        binding.colorPickerAlphaBar.setSelectorDrawableRes(R.drawable.color_picker_selector)
        binding.colorPickerView.attachBrightnessSlider(binding.colorPickerBrightnessBar)
        binding.colorPickerBrightnessBar.setSelectorDrawableRes(R.drawable.color_picker_selector)

        binding.colorPickerView.setColorListener(object : ColorListener {
            override fun onColorSelected(color: Int, fromUser: Boolean) {
                returnColor = colorInt2String(color)
                if(!isKeyBoardUp)
                    binding.colorPickerCodeEt.setText(colorInt2String(color))
                binding.colorPickerPreview.background = makePreviewDrawable(colorInt2String(color))
            }
        })
        binding.colorPickerView.setInitialColor(initialColor.toColorInt())
        binding.colorPickerView.setSelectorDrawable(ContextCompat.getDrawable(context, R.drawable.color_picker_selector))

        colorTextChanged()
        binding.colorPickerPreview.background = makePreviewDrawable(initialColor)

        binding.colorPickerApplyBtn.setOnClickListener {
            onApplyColorListener?.onApplyColor(returnColor)
            dismiss()
        }
    }

    private fun colorTextChanged() {
        val activityRootView = activity.window.decorView.rootView
        val rect1 = Rect()
        activityRootView?.getWindowVisibleDisplayFrame(rect1)
        val initialHeight = rect1.bottom
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect2 = Rect()
            activityRootView.getWindowVisibleDisplayFrame(rect2)
            isKeyBoardUp = rect2.bottom < initialHeight
            binding.colorPickerCodeEt.isCursorVisible = isKeyBoardUp
            binding.colorPickerView.isEnabled = !isKeyBoardUp
        }

        binding.colorPickerCodeEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {}
            override fun afterTextChanged(p0: Editable?) {
                try {
                    if(isKeyBoardUp) {
                        val color = p0.toString().toColorInt()
                        binding.colorPickerView.setInitialColor(color)
                    }
                } catch (_ : Exception) { }
            }
        })
        binding.colorPickerCodeEt.setText(initialColor)
    }

    private fun colorInt2String(color : Int) : String {
        val alpha = color ushr 24 and 0xFF
        val red = color shr 16 and 0xFF
        val green = color shr 8 and 0xFF
        val blue = color and 0xFF
        return String.format("#%02X%02X%02X%02X", alpha, red, green, blue)
    }

    private fun makePreviewDrawable(color : String) : GradientDrawable {
        val drawable1 = GradientDrawable()
        drawable1.setColor(color.toColorInt())
        drawable1.setStroke(3, "#666666".toColorInt())
        drawable1.shape = GradientDrawable.RECTANGLE
        return drawable1
    }

}