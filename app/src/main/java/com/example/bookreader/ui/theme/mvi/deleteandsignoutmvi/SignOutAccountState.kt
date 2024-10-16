package com.example.bookreader.ui.theme.mvi.deleteandsignoutmvi

sealed class SignOutAccountState {
    data object Idle : SignOutAccountState()
    data object Loading : SignOutAccountState()
    data object Success : SignOutAccountState()
    data class Error(val message: String) : SignOutAccountState()
}