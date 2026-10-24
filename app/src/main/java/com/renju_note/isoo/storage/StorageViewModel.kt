package com.renju_note.isoo.storage

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.renju_note.isoo.RenjuEditApplication
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class StorageViewModel : ViewModel() {

    private val storageDao = RenjuEditApplication.database.storageDao()

    // Room re-emits whenever the table changes, so saves from the board show up here directly
    val storageList : StateFlow<List<StorageElement>> = storageDao.observeAll()
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), emptyList())

    fun deleteElement(element : StorageElement) {
        viewModelScope.launch {
            storageDao.delete(element)
        }
    }

}
