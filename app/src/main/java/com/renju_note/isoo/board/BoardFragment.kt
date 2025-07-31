package com.renju_note.isoo.board

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Rect
import android.graphics.drawable.GradientDrawable
import android.media.MediaScannerConnection
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.provider.OpenableColumns
import android.text.Editable
import android.text.TextWatcher
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupWindow
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.core.graphics.createBitmap
import androidx.core.graphics.drawable.toDrawable
import androidx.core.graphics.toColorInt
import androidx.core.net.toUri
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import com.renju_note.isoo.R
import com.renju_note.isoo.RenjuEditApplication
import com.renju_note.isoo.board.Stone
import com.renju_note.isoo.storage.StorageElement
import com.renju_note.isoo.databinding.FragmentBoardBinding
import com.renju_note.isoo.databinding.PopupMenuDrawingModeBinding
import com.renju_note.isoo.databinding.PopupMenuStoneModeBinding
import com.renju_note.isoo.dialog.ConfirmDialog
import com.renju_note.isoo.dialog.PutTextDialog
import com.renju_note.isoo.storage.StorageFragment
import com.renju_note.isoo.util.LoadingAsync
import io.realm.kotlin.UpdatePolicy
import kotlinx.coroutines.launch
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream
import java.io.ObjectInputStream
import java.io.ObjectOutputStream
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Date

class BoardFragment : Fragment() {

    private lateinit var binding : FragmentBoardBinding

    // for drawing mode
    enum class DrawingMode {
        LINE, AREA, ARROW
    }
    private var drawingMode = DrawingMode.LINE
    private var points = ArrayList<Pair<Int, Int>>()

    enum class EditMode {
        PUT_STONE, ADD_TEXT, DRAW
    }
    private var editMode = EditMode.PUT_STONE

    private var isStartIndexOn = false

    private lateinit var toast : Toast

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentBoardBinding.inflate(inflater, container, false)
        setTextSize()
        binding.boardToolbar.inflateMenu(R.menu.board_toolbar)
        binding.boardToolbar.title = resources.getString(R.string.app_name)
        toolbarItemSelected()

        boardClicked()
        buttonsClicked()
        editingTextArea()

        binding.boardBoard.updateBoardStatus(RenjuEditApplication.Companion.settings.boardColorSetting, RenjuEditApplication.Companion.settings.boardDisplaySetting)

        toast = Toast(requireContext())
        toast.duration = Toast.LENGTH_SHORT

        return binding.root
    }

    private fun buttonsClicked() {
        binding.boardMenuBtn.setOnClickListener {
            popupMenu()
        }

        binding.boardUndoAllBtn.setOnClickListener {
            val before = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
            if(RenjuEditApplication.Companion.boardManager.undoAll()) {
                val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                updateBoard(before, after)
            }
        }

        binding.boardUndoBtn.setOnClickListener {
            val before = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
            if(RenjuEditApplication.Companion.boardManager.undo()) {
                val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                updateBoard(before, after)
            }
        }

        binding.boardRedoBtn.setOnClickListener {
            val before = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
            if(RenjuEditApplication.Companion.boardManager.redo()) {
                val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                updateBoard(before, after)
            }
        }

        binding.boardRedoAllBtn.setOnClickListener {
            val before = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
            while (true) {
                if (!RenjuEditApplication.Companion.boardManager.redo()) break
            }
            val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
            updateBoard(before, after)
        }

        binding.boardModeBtn.setOnClickListener {
            toast.cancel()
            binding.boardBoard.deleteAllPoints()
            points.clear()
            editMode = when(editMode) {
                EditMode.PUT_STONE -> {
                    if(RenjuEditApplication.Companion.settings.modeSetting.canUseTextMode) {
                        binding.boardModeBtn.setImageResource(R.drawable.board_text_mode)
                        toast = Toast.makeText(requireContext(), requireContext().getText(R.string.text_mode), Toast.LENGTH_SHORT)
                        EditMode.ADD_TEXT
                    } else if(RenjuEditApplication.Companion.settings.modeSetting.canUseDrawMode) {
                        binding.boardModeBtn.setImageResource(R.drawable.board_drawing_mode)
                        toast = Toast.makeText(requireContext(), requireContext().getText(R.string.drawing_mode), Toast.LENGTH_SHORT)
                        EditMode.DRAW
                    } else {
                        toast = Toast.makeText(requireContext(), requireContext().getText(R.string.stone_mode), Toast.LENGTH_SHORT)
                        EditMode.PUT_STONE
                    }
                }
                EditMode.ADD_TEXT -> {
                    if(RenjuEditApplication.Companion.settings.modeSetting.canUseDrawMode) {
                        binding.boardModeBtn.setImageResource(R.drawable.board_drawing_mode)
                        toast = Toast.makeText(requireContext(), requireContext().getText(R.string.drawing_mode), Toast.LENGTH_SHORT)
                        EditMode.DRAW
                    } else {
                        binding.boardModeBtn.setImageResource(R.drawable.board_stone_mode)
                        toast = Toast.makeText(requireContext(), requireContext().getText(R.string.stone_mode), Toast.LENGTH_SHORT)
                        EditMode.PUT_STONE
                    }
                }
                EditMode.DRAW -> {
                    binding.boardModeBtn.setImageResource(R.drawable.board_stone_mode)
                    toast = Toast.makeText(requireContext(), requireContext().getText(R.string.stone_mode), Toast.LENGTH_SHORT)
                    EditMode.PUT_STONE
                }
            }
            toast.show()
        }
    }

    private fun popupMenu() {
        if (editMode != EditMode.DRAW) {
            val popupBinding = PopupMenuStoneModeBinding.inflate(layoutInflater)
            val popupWindow = PopupWindow(
                popupBinding.root,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupBinding.popupStoneModeDeleteBtn.setOnClickListener {
                delete()
            }
            if(isStartIndexOn) {
                popupBinding.popupStoneModeIndexHereBtn.setImageResource(R.drawable.board_index_here_on)
            } else {
                popupBinding.popupStoneModeIndexHereBtn.setImageResource(R.drawable.board_index_here_off)
            }
            popupBinding.popupStoneModeIndexHereBtn.setOnClickListener {
                toast.cancel()
                if(!isStartIndexOn) {
                    if(RenjuEditApplication.Companion.boardManager.getNowIndex() == 1) RenjuEditApplication.Companion.settings.boardDisplaySetting.startPoint = 0
                    else RenjuEditApplication.Companion.settings.boardDisplaySetting.startPoint = RenjuEditApplication.Companion.boardManager.getNowIndex() - 2
                    binding.boardBoard.updateBoardStatus(RenjuEditApplication.Companion.settings.boardColorSetting, RenjuEditApplication.Companion.settings.boardDisplaySetting)
                    RenjuEditApplication.Companion.settings.save(RenjuEditApplication.Companion.pref)
                    isStartIndexOn = true
                    popupBinding.popupStoneModeIndexHereBtn.setImageResource(R.drawable.board_index_here_on)
                    toast = Toast.makeText(requireContext(), requireContext().getText(R.string.index_here_on), Toast.LENGTH_SHORT)
                } else {
                    RenjuEditApplication.Companion.settings.boardDisplaySetting.startPoint = 0
                    binding.boardBoard.updateBoardStatus(RenjuEditApplication.Companion.settings.boardColorSetting, RenjuEditApplication.Companion.settings.boardDisplaySetting)
                    RenjuEditApplication.Companion.settings.save(RenjuEditApplication.Companion.pref)
                    isStartIndexOn = false
                    popupBinding.popupStoneModeIndexHereBtn.setImageResource(R.drawable.board_index_here_off)
                    toast = Toast.makeText(requireContext(), requireContext().getText(R.string.index_here_off), Toast.LENGTH_SHORT)
                }
                toast.show()
            }

            popupWindow.showAsDropDown(binding.boardMenuBtn)
        } else {
            val popupBinding = PopupMenuDrawingModeBinding.inflate(layoutInflater)
            val popupWindow = PopupWindow(
                popupBinding.root,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                true
            )

            popupBinding.popupDrawingModeDeleteBtn.setOnClickListener {
                val confirmDialog = ConfirmDialog(
                    requireContext(),
                    resources.getString(R.string.delete_drawing_element_confirm)
                )
                confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                confirmDialog.setOnResponseListener(object : ConfirmDialog.OnResponseListener {
                    override fun confirm() {
                        confirmDialog.dismiss()
                        binding.boardBoard.deleteLastDrawingElement()
                    }

                    override fun refuse() {
                        confirmDialog.dismiss()
                    }
                })
                confirmDialog.show()
            }
            when(drawingMode) {
                DrawingMode.LINE -> {
                    popupBinding.popupDrawingModeLineBtn.setBackgroundResource(R.drawable.mode_select)
                    popupBinding.popupDrawingModeAreaBtn.background = null
                    popupBinding.popupDrawingModeArrowBtn.background = null
                }
                DrawingMode.AREA -> {
                    popupBinding.popupDrawingModeLineBtn.background = null
                    popupBinding.popupDrawingModeAreaBtn.setBackgroundResource(R.drawable.mode_select)
                    popupBinding.popupDrawingModeArrowBtn.background = null
                }
                DrawingMode.ARROW -> {
                    popupBinding.popupDrawingModeLineBtn.background = null
                    popupBinding.popupDrawingModeAreaBtn.background = null
                    popupBinding.popupDrawingModeArrowBtn.setBackgroundResource(R.drawable.mode_select)
                }
            }
            popupBinding.popupDrawingModeLineBtn.setOnClickListener {
                drawingMode = DrawingMode.LINE
                popupBinding.popupDrawingModeLineBtn.setBackgroundResource(R.drawable.mode_select)
                popupBinding.popupDrawingModeAreaBtn.background = null
                popupBinding.popupDrawingModeArrowBtn.background = null
            }
            popupBinding.popupDrawingModeAreaBtn.setOnClickListener {
                drawingMode = DrawingMode.AREA
                popupBinding.popupDrawingModeLineBtn.background = null
                popupBinding.popupDrawingModeAreaBtn.setBackgroundResource(R.drawable.mode_select)
                popupBinding.popupDrawingModeArrowBtn.background = null
            }
            popupBinding.popupDrawingModeArrowBtn.setOnClickListener {
                drawingMode = DrawingMode.ARROW
                popupBinding.popupDrawingModeLineBtn.background = null
                popupBinding.popupDrawingModeAreaBtn.background = null
                popupBinding.popupDrawingModeArrowBtn.setBackgroundResource(R.drawable.mode_select)
            }

            popupWindow.showAsDropDown(binding.boardMenuBtn)
        }
    }

    private fun delete() {
        if(RenjuEditApplication.Companion.boardManager.getNowIndex() != 1) {
            val confirmDialog =
                ConfirmDialog(requireContext(), resources.getString(R.string.delete_confirm))
            confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
            confirmDialog.setOnResponseListener(object : ConfirmDialog.OnResponseListener {
                override fun confirm() {
                    confirmDialog.dismiss()
                    val before = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                    RenjuEditApplication.Companion.boardManager.deleteBranch()
                    val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                    updateBoard(before, after)
                }

                override fun refuse() {
                    confirmDialog.dismiss()
                }
            })
            confirmDialog.show()
        }
    }

    private fun boardClicked() {
        binding.boardBoard.setOnBoardTouchListener(object : BoardLayout.OnBoardTouchListener() {
            override fun getCoordinates(x: Int, y: Int) {
                if(editMode != EditMode.DRAW) {
                    val before = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                    if (editMode == EditMode.ADD_TEXT) {
                        val putTextDialog = PutTextDialog(requireContext())
                        putTextDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        putTextDialog.setOnResponseListener(object :
                            PutTextDialog.OnResponseListener {
                            override fun cancel() {
                                putTextDialog.dismiss()
                            }

                            override fun ok(text: String) {
                                putTextDialog.dismiss()
                                if (RenjuEditApplication.Companion.boardManager.addNewChild(x, y, text)) {
                                    val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                                    updateBoard(before, after)
                                }
                            }
                        })
                        putTextDialog.show()
                    } else {
                        if (RenjuEditApplication.Companion.boardManager.putStone(x, y)) {
                            val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                            updateBoard(before, after)
                        }
                    }
                } else {
                    when(drawingMode) {
                        DrawingMode.LINE -> {
                            if(points.isEmpty()) {
                                points.add(Pair(x, y))
                                binding.boardBoard.addPoint(binding.boardBoard.Point(x, y, RenjuEditApplication.Companion.settings.boardColorSetting.drawLineColor))
                            } else {
                                binding.boardBoard.deleteAllPoints()
                                binding.boardBoard.addLine(binding.boardBoard.Line(
                                    points[0].first, points[0].second, x, y))
                                points.clear()
                            }
                        }
                        DrawingMode.AREA -> {
                            if(points.isEmpty()) {
                                points.add(Pair(x, y))
                                binding.boardBoard.addPoint(binding.boardBoard.Point(x, y, RenjuEditApplication.Companion.settings.boardColorSetting.drawAreaColor))
                            } else {
                                binding.boardBoard.deleteAllPoints()
                                binding.boardBoard.addArea(binding.boardBoard.Area(
                                    points[0].first, points[0].second, x, y))
                                points.clear()
                            }
                        }
                        DrawingMode.ARROW -> {
                            if(points.isEmpty()) {
                                points.add(Pair(x, y))
                                binding.boardBoard.addPoint(binding.boardBoard.Point(x, y, RenjuEditApplication.Companion.settings.boardColorSetting.drawArrowColor))
                            } else {
                                binding.boardBoard.deleteAllPoints()
                                binding.boardBoard.addArrow(binding.boardBoard.Arrow(
                                    points[0].first, points[0].second, x, y))
                                points.clear()
                            }
                        }
                    }
                }
            }
        })
    }

    private fun editingTextArea() {
        val activityRootView = requireActivity().window.decorView.rootView
        val rect1 = Rect()
        activityRootView.getWindowVisibleDisplayFrame(rect1)
        val initialHeight = rect1.bottom
        activityRootView.viewTreeObserver.addOnGlobalLayoutListener {
            val rect2 = Rect()
            activityRootView.getWindowVisibleDisplayFrame(rect2)
            binding.boardTextAreaEt.isCursorVisible = rect2.bottom < initialHeight
        }

        binding.boardTextAreaEt.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                // save boxText to seqTree
                RenjuEditApplication.Companion.boardManager.setNowTextBoxString(s.toString())
            }
        })
        updateTextAreaStatus()
    }

    fun updateTextAreaStatus() {
        binding.boardTextAreaEt.isVisible = RenjuEditApplication.Companion.settings.textAreaSetting.isVisible
        binding.boardTextAreaEt.background = makeTextAreaDrawable(RenjuEditApplication.Companion.settings.textAreaSetting.backgroundColor, RenjuEditApplication.Companion.settings.textAreaSetting.strokeColor)
        binding.boardTextAreaEt.setTextColor(
            blendColors(
                "#FFFFFF",
                RenjuEditApplication.Companion.settings.textAreaSetting.textColor
            ).toColorInt())
    }

    fun updateMode() {
        binding.boardModeBtn.setImageResource(R.drawable.board_stone_mode)
        editMode = EditMode.PUT_STONE
    }

    fun updateBoard() {
        binding.boardBoard.updateBoardStatus(RenjuEditApplication.Companion.settings.boardColorSetting, RenjuEditApplication.Companion.settings.boardDisplaySetting)
    }

    private fun makeTextAreaDrawable(backgroundColor : String, strokeColor : String) : GradientDrawable {
        val drawable1 = GradientDrawable()
        drawable1.setColor(backgroundColor.toColorInt())
        drawable1.setStroke(3, strokeColor.toColorInt())
        drawable1.shape = GradientDrawable.RECTANGLE
        return drawable1
    }

    private fun blendColors(baseColor: String, blendColor: String): String {
        val base = baseColor.toColorInt()
        val blend = blendColor.toColorInt()
        val alpha = Color.alpha(blend) / 255f
        val red = (1 - alpha) * Color.red(base) + alpha * Color.red(blend)
        val green = (1 - alpha) * Color.green(base) + alpha * Color.green(blend)
        val blue = (1 - alpha) * Color.blue(base) + alpha * Color.blue(blend)
        val alphaInt = (alpha * 255).toInt()
        val colorInt = Color.argb(alphaInt, red.toInt(), green.toInt(), blue.toInt())
        return String.format("#%06X", 0xFFFFFF and colorInt)
    }

    private fun updateBoard(before : ArrayList<Stone>, after : ArrayList<Stone>) {
        binding.boardTextAreaEt.setText(RenjuEditApplication.Companion.boardManager.getNowTextBoxString())
        binding.boardSequenceTv.text = (RenjuEditApplication.Companion.boardManager.getNowIndex() - 1).toString()

        val addStone = ArrayList<BoardLayout.StoneView>()
        val deleteStone = ArrayList<String>()
        val childs = ArrayList<BoardLayout.Child>()

        // update child nodes view
        after.forEach { stone ->
            if(stone.type == Stone.Type.CHILD) {
                val child = binding.boardBoard.Child(stone.text, stone.x, stone.y)
                childs.add(child)
            }
        }

        val beforeSequence = RenjuEditApplication.Companion.boardManager.getSequence(before)
        val afterSequence = RenjuEditApplication.Companion.boardManager.getSequence(after)
        var isAdd = true // after board has more stones than before
        val changeSequence = if(beforeSequence.size > afterSequence.size) {
            isAdd = false
            beforeSequence.removeAll(afterSequence.toSet())
            beforeSequence
        } else {
            afterSequence.removeAll(beforeSequence.toSet())
            afterSequence
        }

        if(isAdd) {
            changeSequence.forEach { stone ->
                val stoneView = when (stone.type) {
                    Stone.Type.BLACK -> binding.boardBoard.StoneView(
                        BoardLayout.StoneViewType.BLACK, stone.text.toInt(),
                        stone.x, stone.y)

                    Stone.Type.WHITE -> binding.boardBoard.StoneView(
                        BoardLayout.StoneViewType.WHITE, stone.text.toInt(),
                        stone.x, stone.y)

                    Stone.Type.CHILD -> binding.boardBoard.StoneView(
                        BoardLayout.StoneViewType.BLACK, -1,
                        stone.x, stone.y)
                }
                addStone.add(stoneView)
            }
        } else {
            changeSequence.forEach { stone ->
                deleteStone.add(binding.boardBoard.getStoneID(stone.x, stone.y))
            }
        }

        binding.boardBoard.placeStones(addStone, deleteStone, childs)
    }

    private fun toolbarItemSelected() {
        binding.boardToolbar.setOnMenuItemClickListener { item ->
            if (item != null) {
                when (item.itemId) {
                    R.id.action_capture -> {
                        val confirmDialog = ConfirmDialog(
                            requireContext(),
                            resources.getString(R.string.capture_confirm)
                        )
                        confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        confirmDialog.setOnResponseListener(object : ConfirmDialog.OnResponseListener {
                            override fun confirm() {
                                confirmDialog.dismiss()
                                captureBoard()
                            }
                            override fun refuse() { confirmDialog.dismiss() }
                        })
                        confirmDialog.show()
                    }
                    R.id.save_board -> {
                        saveNowEditingFile()
                    }
                    R.id.new_board -> {
                        val confirmDialog = ConfirmDialog(
                            requireContext(),
                            resources.getString(R.string.new_file_confirm)
                        )
                        confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        confirmDialog.setOnResponseListener(object : ConfirmDialog.OnResponseListener {
                            override fun confirm() {
                                confirmDialog.dismiss()
                                RenjuEditApplication.Companion.boardManager.loadNodes(SeqTree())
                                binding.boardBoard.removeAllStones()
                                binding.boardBoard.deleteAllDrawingElements()
                                points.clear()
                                binding.boardSequenceTv.text = "0"
                                binding.boardTextAreaEt.text = null
                                RenjuEditApplication.Companion.editingFile = null
                                binding.boardEditingFileNameTv.text = requireContext().getString(R.string.new_file)
                            }
                            override fun refuse() { confirmDialog.dismiss() }
                        })
                        confirmDialog.show()
                    }
                    R.id.save_board_as -> {
                        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "application/*"
                        intent.putExtra(Intent.EXTRA_TITLE, "write_your_file_name")
                        startActivityResultSave.launch(intent)
                    }
                    R.id.load_board -> {
                        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT)
                        intent.addCategory(Intent.CATEGORY_OPENABLE)
                        intent.type = "application/*"
                        startActivityResultLoad.launch(intent)
                    }
                    R.id.about -> {
                        val dialog = Dialog(requireContext())
                        dialog.setContentView(R.layout.dialog_about)
                        dialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                        dialog.show()
                    }
                }
            }
            true
        }
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                captureBoard()
            } else {
                Toast.makeText(context, "Permission denied to write to external storage.", Toast.LENGTH_SHORT).show()
            }
        }

    private fun captureBoard() {
        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(Manifest.permission.WRITE_EXTERNAL_STORAGE)
            return
        }

        val layout = binding.boardContainerCl
        val width = layout.width
        val height = layout.height

        val bitmap = createBitmap(width, height)
        val canvas = Canvas(bitmap)
        layout.draw(canvas)

        val currentDateTime = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyyMMddHHmmss")
        val dateTimeString = currentDateTime.format(formatter)

        // Save the bitmap to the gallery
        val contentValues = ContentValues().apply {
            put(MediaStore.Images.Media.DISPLAY_NAME, "renju_edit$dateTimeString")
            put(MediaStore.Images.Media.MIME_TYPE, "image/jpeg")
            put(MediaStore.Images.Media.WIDTH, width)
            put(MediaStore.Images.Media.HEIGHT, height)
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { // over Android 10
            val imageUri = requireActivity().contentResolver.insert(
                MediaStore.Images.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                ), contentValues)
            imageUri?.let { uri ->
                requireActivity().contentResolver.openOutputStream(uri)?.use { outputStream ->
                    bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                }
            }
            MediaScannerConnection.scanFile(requireContext(), arrayOf(imageUri?.path), arrayOf("image/jpeg"), null)
            Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
        } else { // under Android 10
            try {
                val time = System.currentTimeMillis()
                @SuppressLint("SimpleDateFormat") val day = SimpleDateFormat("yyyyMMddhhmmssSSS")
                val output = day.format(Date(time))
                val folder = Environment.getExternalStorageDirectory().absolutePath + "/DCIM/Screenshots"
                val file = folder + File.separator + output + ".jpeg"
                val folderPath = File(folder)
                if (!folderPath.exists()) {
                    val success = folderPath.mkdirs()
                    if (!success) println("failed mkdirs")
                }
                val outputStream: OutputStream = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.JPEG, 100, outputStream)
                outputStream.flush()
                outputStream.close()
                MediaScannerConnection.scanFile(requireContext(), arrayOf("file://$file".toUri().path), arrayOf("image/jpeg"), null)
                Toast.makeText(context, "Image saved to gallery", Toast.LENGTH_SHORT).show()
            } catch (_: IOException) {
                Toast.makeText(requireContext(), "Failed", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun setNowEditingFile(uri : Uri) {
        fun getFileNameFromUri(context: Context, uri: Uri): String {
            var fileName = ""
            val cursor = context.contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                if (it.moveToFirst()) {
                    val displayNameIndex = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    fileName = it.getString(displayNameIndex)
                }
                cursor.close()
            }
            return fileName
        }

        val newStorageElement = StorageElement.Companion.create(
            title = getFileNameFromUri(requireContext(), uri),
            uri = uri,
            seq = RenjuEditApplication.Companion.boardManager.getSequence(RenjuEditApplication.Companion.boardManager.getNowBoardStatus())
        )
        RenjuEditApplication.Companion.editingFile = newStorageElement
        binding.boardEditingFileNameTv.text = RenjuEditApplication.Companion.editingFile?.title

        lifecycleScope.launch {
            RenjuEditApplication.Companion.realm.write {
                copyToRealm(newStorageElement, UpdatePolicy.ALL)
            }

            val storageFragment = requireActivity().supportFragmentManager.findFragmentByTag("f1") as StorageFragment?
            storageFragment?.updateRV()
        }
    }

    private fun saveNowEditingFile() {
        if(RenjuEditApplication.Companion.editingFile != null) {
            save(RenjuEditApplication.Companion.editingFile!!.getParsedUri())
        } else {
            val intent = Intent(Intent.ACTION_CREATE_DOCUMENT)
            intent.addCategory(Intent.CATEGORY_OPENABLE)
            intent.type = "application/*"
            intent.putExtra(Intent.EXTRA_TITLE, "write_your_file_name")
            startActivityResultSave.launch(intent)
        }
    }

    private fun save(uri : Uri?) {
        val loadingAsync = LoadingAsync(requireContext())
        loadingAsync.setProcess(object : LoadingAsync.ProcessListener {
            override fun process() {
                val outputStream: OutputStream = requireActivity().contentResolver.openOutputStream(uri!!)!!
                val os = ObjectOutputStream(outputStream)
                os.writeObject(RenjuEditApplication.Companion.boardManager.getSeqTree())
                os.close()
                outputStream.close()
            }

            override fun whenFinished() {
                Toast.makeText(context, "Save", Toast.LENGTH_SHORT).show()
                setNowEditingFile(uri!!)
            }

            override fun whenFailed() {
                Toast.makeText(context, "Failed to save!", Toast.LENGTH_SHORT).show()
            }
        })

        loadingAsync.run()
    }

    private var startActivityResultSave = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                this.requireActivity().contentResolver.takePersistableUriPermission(result.data!!.data!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
                this.requireActivity().contentResolver.takePersistableUriPermission(result.data!!.data!!,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                save(result.data!!.data)
            }
        }
    }

    fun load(uri : Uri?) {
        binding.boardBoard.deleteAllDrawingElements()
        points.clear()
        binding.boardBoard.removeAllStones()
        val before = ArrayList<Stone>()

        val loadingAsync = LoadingAsync(requireContext())
        loadingAsync.setProcess(object : LoadingAsync.ProcessListener {
            override fun process() {
                val inputStream: InputStream = requireActivity().contentResolver.openInputStream(uri!!)!!
                val ois = ObjectInputStream(inputStream)
                val tree : Any = ois.readObject() as SeqTree
                RenjuEditApplication.Companion.boardManager.loadNodes(tree)
                ois.close()
                inputStream.close()
            }

            override fun whenFinished() {
                Toast.makeText(context, "Load", Toast.LENGTH_SHORT).show()
                val after = RenjuEditApplication.Companion.boardManager.getNowBoardStatus()
                updateBoard(before, after)
                setNowEditingFile(uri!!)
            }

            override fun whenFailed() {
                Toast.makeText(context, "Failed to load!", Toast.LENGTH_SHORT).show()
            }
        })

        loadingAsync.run()
    }

    private var startActivityResultLoad = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            if (result.data != null) {
                this.requireActivity().contentResolver.takePersistableUriPermission(result.data!!.data!!,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION)
                this.requireActivity().contentResolver.takePersistableUriPermission(result.data!!.data!!,
                    Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                load(result.data!!.data)
            }
        }
    }

    private fun setTextSize() {
        binding.boardButtonContainerLl.addOnLayoutChangeListener { _, _, _, _, _, _, _, _, _ ->
            val density = requireContext().resources.displayMetrics.density
            val textSize : Int = (binding.boardBoard.width / 22.8 / density + 0.5).toInt()
            binding.boardTextAreaEt.setTextSize(TypedValue.COMPLEX_UNIT_DIP, (binding.boardBoard.width / 27.4 / density + 0.5).toInt().toFloat())
            binding.boardUndoBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
            binding.boardUndoAllBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
            binding.boardRedoBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
            binding.boardRedoAllBtn.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
            binding.boardSequenceTv.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize.toFloat())
        }
    }

}