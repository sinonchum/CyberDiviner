package com.cyberdiviner.ui.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.cyberdiviner.data.dao.DivinationDao
import com.cyberdiviner.data.model.DivinationReading
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@HiltViewModel
class ArchiveViewModel @Inject constructor(
    divinationDao: DivinationDao
) : ViewModel() {

    val readings: StateFlow<List<DivinationReading>> = divinationDao.getAll()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )
}
