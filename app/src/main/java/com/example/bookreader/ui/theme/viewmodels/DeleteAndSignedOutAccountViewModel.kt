package com.example.bookreader.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreader.di.SessionProvider
import com.example.bookreader.ui.theme.mvi.deleteandsignoutmvi.DeleteAccountState
import com.example.bookreader.ui.theme.mvi.deleteandsignoutmvi.DeleteAndSignOutAccountIntent
import com.example.bookreader.ui.theme.mvi.deleteandsignoutmvi.SignOutAccountState
import com.example.bookreader.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.core.Transaction
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

class DeleteAndSignedOutAccountViewModel @Inject constructor(
    private val auth:FirebaseAuth,
    private val firestore:FirebaseFirestore
) :ViewModel() {
    var channel:Channel<DeleteAndSignOutAccountIntent> = Channel()
    private var _deleteAccountState = MutableSharedFlow<DeleteAccountState>(replay = 0)
    var deleteAccountState = _deleteAccountState.asSharedFlow()
    private var _signOutState = MutableSharedFlow<SignOutAccountState>(replay = 0)
    var signOutState = _signOutState.asSharedFlow()

    init {
        handelIntent()
    }

    private fun handelIntent() {
        viewModelScope.launch {
            channel.consumeAsFlow().collect{intent->
                when(intent){
                    DeleteAndSignOutAccountIntent.DeleteAccount -> handelDeleteAccount()
                    DeleteAndSignOutAccountIntent.SignOutAccount -> handelSignOutAccount()
                }
            }
        }
    }

    private fun handelSignOutAccount() {
        try {
            viewModelScope.launch {
                _signOutState.emit(SignOutAccountState.Loading)
                auth.signOut()
                _signOutState.emit(SignOutAccountState.Success)
            }
        }catch (ex:Exception){
           viewModelScope.launch {
               _signOutState.emit(SignOutAccountState.Error(ex.message.toString()))
           }
        }
    }

    private fun handelDeleteAccount() {
        try {

            val user = auth.currentUser
            user?.delete()?.addOnSuccessListener{
                viewModelScope.launch {
                    _deleteAccountState.emit(DeleteAccountState.Loading)
                    firestore.collection(Constant.USER_COLLECTION).document(SessionProvider.userId!!).delete()
                    _deleteAccountState.emit(DeleteAccountState.Success)
                }
            }?.addOnFailureListener{
                viewModelScope.launch {
                    _deleteAccountState.emit(DeleteAccountState.Error(it.message.toString()))
                }
            }
        }catch (ex:Exception){
            viewModelScope.launch {
                _deleteAccountState.emit(DeleteAccountState.Error(ex.message.toString()))
            }
        }
    }
}