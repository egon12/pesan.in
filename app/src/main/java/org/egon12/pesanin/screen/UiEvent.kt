package org.egon12.pesanin.screen

sealed class UiEvent {
    data class ShowSnackbar(val message: String) : UiEvent()
}
