package org.egon12.pesanin.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.launch
import org.egon12.pesanin.screen.Screen
import javax.inject.Inject

@HiltViewModel
class MainViewModel @Inject constructor() : ViewModel() {
    val events = MutableSharedFlow<UiEvent>()

    fun emit(event: UiEvent) {
        viewModelScope.launch {
            events.emit(event)
        }
    }

    fun back() {
        emit(UiEvent.NavigateBack)
    }

    fun navigate(screen: Screen) {
        emit(UiEvent.Navigate(screen))
    }
}

sealed class UiEvent {
    data class Snackbar(val msg: String) : UiEvent()
    data class Navigate(val screen: Screen) : UiEvent()
    object NavigateBack: UiEvent()

    object Idle:UiEvent()
}