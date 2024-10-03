package com.example.bookreader.ui.theme.mvi.forgetpasswordmvi

sealed class ForgetPasswordState {
    data object Idle : ForgetPasswordState()
    data object Loading : ForgetPasswordState()
    data object Success : ForgetPasswordState()
    data object ConfirmChangePasswordFailed : ForgetPasswordState()
    data class Error(val message: String) : ForgetPasswordState()
}