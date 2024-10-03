package com.example.bookreader.ui.theme.mvi.deleteandsignoutmvi

sealed class DeleteAndSignOutAccountIntent {
    data object DeleteAccount : DeleteAndSignOutAccountIntent()
    data object SignOutAccount : DeleteAndSignOutAccountIntent()
}