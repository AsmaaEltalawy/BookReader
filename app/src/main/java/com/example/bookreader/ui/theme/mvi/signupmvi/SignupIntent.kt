package com.example.bookreader.ui.theme.mvi.signupmvi

sealed class SignupIntent {
    data class Signup(val name:String, val email: String, val password: String) : SignupIntent()
}