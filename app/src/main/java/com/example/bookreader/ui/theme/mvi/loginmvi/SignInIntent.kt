package com.example.bookreader.ui.theme.mvi.loginmvi

sealed class SignInIntent {
    data class SignIn(val email: String, val password: String) : SignInIntent()
}