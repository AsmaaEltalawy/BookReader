package com.example.bookreader.ui.theme.mvi.forgetpasswordmvi

sealed class ForgetPasswordIntent {
    data class ConfirmChangePassword(val email:String) : ForgetPasswordIntent()
}