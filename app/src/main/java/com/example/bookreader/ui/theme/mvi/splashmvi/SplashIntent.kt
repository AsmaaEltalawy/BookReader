package com.example.bookreader.ui.theme.mvi.splashmvi

sealed class SplashIntent {
    data object CheckUserIsSigned:SplashIntent()
}