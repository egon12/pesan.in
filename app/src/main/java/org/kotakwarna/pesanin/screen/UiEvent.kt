package org.kotakwarna.pesanin.screen

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}
