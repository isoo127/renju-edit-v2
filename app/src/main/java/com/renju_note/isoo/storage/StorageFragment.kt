package com.renju_note.isoo.storage

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.toDrawable
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import com.renju_note.isoo.R
import com.renju_note.isoo.board.BoardFragment
import com.renju_note.isoo.databinding.FragmentStorageBinding
import com.renju_note.isoo.dialog.ConfirmDialog
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

class StorageFragment : Fragment() {

    private lateinit var binding : FragmentStorageBinding
    private lateinit var storageAdapter : StorageRVAdapter
    private val viewModel : StorageViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentStorageBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        observeViewModel()
    }

    private fun setupRecyclerView() {
        val listener = object : StorageRVAdapter.StoragePopupMenuListener {
            override fun onLoad(element: StorageElement) {
                val boardFragment = requireActivity().supportFragmentManager.findFragmentByTag("f0") as? BoardFragment
                val confirmDialog = ConfirmDialog(requireContext(), resources.getString(R.string.storage_load_confirm))
                confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                confirmDialog.setOnResponseListener(object : ConfirmDialog.OnResponseListener {
                    override fun confirm() {
                        confirmDialog.dismiss()
                        boardFragment?.load(element.getParsedUri())
                    }
                    override fun refuse() { confirmDialog.dismiss() }
                })
                confirmDialog.show()
            }

            override fun onDelete(element: StorageElement) {
                val confirmDialog = ConfirmDialog(requireContext(), resources.getString(R.string.storage_delete_confirm))
                confirmDialog.window?.setBackgroundDrawable(Color.TRANSPARENT.toDrawable())
                confirmDialog.setOnResponseListener(object : ConfirmDialog.OnResponseListener {
                    override fun confirm() {
                        viewModel.deleteElement(element)
                        confirmDialog.dismiss()
                    }
                    override fun refuse() { confirmDialog.dismiss() }
                })
                confirmDialog.show()
            }
        }
        storageAdapter = StorageRVAdapter(requireContext(), listener)
        binding.storageListRv.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = storageAdapter
            if (itemDecorationCount == 0) {
                addItemDecoration(DividerItemDecoration(requireContext(), 1))
            }
        }
    }

    private fun observeViewModel() {
        viewModel.storageList
            .flowWithLifecycle(viewLifecycleOwner.lifecycle)
            .onEach { list ->
                storageAdapter.submitList(list) {
                    if (list.isNotEmpty()) {
                        binding.storageListRv.scrollToPosition(0)
                    }
                }
            }
            .launchIn(viewLifecycleOwner.lifecycleScope)
    }

}