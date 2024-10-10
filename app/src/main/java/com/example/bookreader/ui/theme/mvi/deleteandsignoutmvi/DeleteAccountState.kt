package com.example.bookreader.ui.theme.mvi.deleteandsignoutmvi

sealed class DeleteAccountState {
    data object Idle : DeleteAccountState()
    data object Loading : DeleteAccountState()
    data object Success : DeleteAccountState()
    data class Error(val message: String) : DeleteAccountState()
}