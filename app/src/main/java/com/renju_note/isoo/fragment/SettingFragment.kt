package com.renju_note.isoo.fragment

import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renju_note.isoo.R
import com.renju_note.isoo.RenjuEditApplication.Companion.pref
import com.renju_note.isoo.RenjuEditApplication.Companion.settings
import com.renju_note.isoo.databinding.FragmentSettingBinding
import com.renju_note.isoo.dialog.ColorPickerDialog
import com.renju_note.isoo.dialog.ConfirmDialog
import com.renju_note.isoo.util.SettingColorRVAdapter
import com.renju_note.isoo.util.SettingDisplayRVAdapter
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt

class SettingFragment : Fragment() {

    private lateinit var binding : FragmentSettingBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentSettingBinding.inflate(inflater, container, false)

        displaySettingInit()
        colorSettingInit()

        binding.settingRollbackBtn.setOnClickListener {
            val confirmDialog = ConfirmDialog(requireContext(), resources.getString(R.string.default_setting_warning))
            confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            confirmDialog.setOnResponseListener(object : ConfirmDialog.OnResponseListener {
                override fun confirm() {
                    confirmDialog.dismiss()
                    settings.setDefaultSetting()
                    settings.save(pref)
                    update()
                    displaySettingInit()
                    colorSettingInit()
                }
                override fun refuse() { confirmDialog.dismiss() }
            })
            confirmDialog.show()
        }

        return binding.root
    }

    private fun update() {
        val boardFragment = requireActivity().supportFragmentManager.findFragmentByTag("f0") as BoardFragment
        boardFragment.updateBoard()
        boardFragment.updateTextAreaStatus()
    }

    private fun update(isUpdateMode : Boolean) {
        val boardFragment = requireActivity().supportFragmentManager.findFragmentByTag("f0") as BoardFragment
        boardFragment.updateBoard()
        boardFragment.updateTextAreaStatus()
        if(isUpdateMode) boardFragment.updateMode()
    }

    private fun colorSettingInit() {
        binding.settingColorRv.layoutManager = LinearLayoutManager(requireContext())
        binding.settingColorRv.adapter = SettingColorRVAdapter(requireContext())
        if(binding.settingColorRv.itemDecorationCount == 0)
            binding.settingColorRv.addItemDecoration(DividerItemDecoration(requireContext(), 1))
        (binding.settingColorRv.adapter as SettingColorRVAdapter).setOnItemClickListener(object : SettingColorRVAdapter.OnItemClickListener {
            override fun onItemClick(position: Int) {
                when(position) {
                    0 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.boardColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.boardColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    1 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.lineColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.lineColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    2 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.textColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.textColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    3 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.nodeColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.nodeColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    4 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.lastStoneStrokeColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.lastStoneStrokeColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    5 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.textAreaSetting.backgroundColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.textAreaSetting.backgroundColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    6 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.textAreaSetting.strokeColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.textAreaSetting.strokeColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    7 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.textAreaSetting.textColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.textAreaSetting.textColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    8 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.drawLineColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.drawLineColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    9 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.drawAreaColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.drawAreaColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                    10 -> {
                        val colorPickerDialog = ColorPickerDialog(requireContext(), requireActivity(), settings.boardColorSetting.drawArrowColor)
                        colorPickerDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        colorPickerDialog.show()
                        colorPickerDialog.setOnApplyColorListener(object : ColorPickerDialog.OnApplyColorListener {
                            override fun onApplyColor(color: String) {
                                settings.boardColorSetting.drawArrowColor = color
                                binding.settingColorRv.getChildAt(position).findViewById<View>(R.id.item_color_preview).background =
                                    makePreviewDrawable(color)
                                settings.save(pref)
                                update()
                            }
                        })
                    }
                }
            }
        })
    }

    private fun displaySettingInit() {
        binding.settingDisplayRv.layoutManager = LinearLayoutManager(requireContext())
        binding.settingDisplayRv.adapter = SettingDisplayRVAdapter(requireContext())
        if(binding.settingDisplayRv.itemDecorationCount == 0)
            binding.settingDisplayRv.addItemDecoration(DividerItemDecoration(requireContext(), 1))
        (binding.settingDisplayRv.adapter as SettingDisplayRVAdapter).setOnItemCheckListener(object : SettingDisplayRVAdapter.OnItemCheckListener {
            override fun onItemCheck(position: Int, isCheck: Boolean) {
                var isUpdateMode = false
                when(position) {
                    0 -> {
                        settings.textAreaSetting.isVisible = isCheck
                        settings.save(pref)
                    }
                    1 -> {
                        settings.boardDisplaySetting.sequenceVisible = isCheck
                        settings.save(pref)
                    }
                    2 -> {
                        settings.boardDisplaySetting.nextNodeVisible = isCheck
                        settings.save(pref)
                    }
                    3 -> {
                        settings.modeSetting.canUseTextMode = isCheck
                        settings.save(pref)
                        isUpdateMode = true
                    }
                    4 -> {
                        settings.modeSetting.canUseDrawMode = isCheck
                        settings.save(pref)
                        isUpdateMode = true
                    }
                }
                update(isUpdateMode)
            }
        })
    }

    private fun makePreviewDrawable(color : String) : GradientDrawable {
        val drawable1 = GradientDrawable()
        drawable1.setColor(color.toColorInt())
        drawable1.setStroke(3, "#666666".toColorInt())
        drawable1.shape = GradientDrawable.RECTANGLE
        return drawable1
    }

}