package com.example.bookreader.ui.theme.mvi.loginmvi

import com.example.bookreader.data.models.User

sealed class SignInState {
    data object Idle : SignInState()
    data object EmailOrPasswordInCorrect: SignInState()
    data object Loading : SignInState()
    data class Success(val user:User): SignInState()
    data class Error(val message: String) : SignInState()
}