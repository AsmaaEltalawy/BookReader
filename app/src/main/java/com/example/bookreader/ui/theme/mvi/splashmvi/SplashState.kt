package com.example.bookreader.ui.theme.mvi.splashmvi

sealed class SplashState {
    data object Idle:SplashState()
    data class UserIsSigned(val isFound:Boolean):SplashState()
    data class Error(val message:String):SplashState()
}