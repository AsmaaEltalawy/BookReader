package com.example.bookreader.ui.theme.mvi.signupmvi

sealed class SignUpState {
    data object Idle : SignUpState()
    data object Loading : SignUpState()
    data object Success : SignUpState()
    data class Error(val message: String) : SignUpState()
}