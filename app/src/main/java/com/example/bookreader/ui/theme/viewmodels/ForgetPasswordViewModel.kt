package com.example.bookreader.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreader.di.SessionProvider
import com.example.bookreader.ui.theme.mvi.forgetpasswordmvi.ForgetPasswordIntent
import com.example.bookreader.ui.theme.mvi.forgetpasswordmvi.ForgetPasswordState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ForgetPasswordViewModel @Inject constructor(
    private val auth:FirebaseAuth
):ViewModel() {

    var channel = Channel<ForgetPasswordIntent>(Channel.BUFFERED)
    private var _state = MutableSharedFlow<ForgetPasswordState>(0)
    var state = _state.asSharedFlow()

    init {
        handelIntent()
    }

    private fun handelIntent() {
        viewModelScope.launch {
            channel.consumeAsFlow().collect{intent->
                when(intent){
                    is ForgetPasswordIntent.ConfirmChangePassword -> handelConfirmChangePassword(intent.email)
                }
            }
        }
    }

    private fun handelConfirmChangePassword(email: String) {
        try {
            viewModelScope.launch {
                confirmPasswordChanges(email)
            }
        }catch (ex:Exception){
            viewModelScope.launch {
                _state.emit(ForgetPasswordState.Error(ex.message.toString()))
            }
        }

    }

    private suspend fun confirmPasswordChanges(email:String) {
        try {
            _state.emit(ForgetPasswordState.Loading)
            auth.sendPasswordResetEmail(email)
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        viewModelScope.launch {
                            _state.emit(ForgetPasswordState.Success)
                        }
                    } else {
                        viewModelScope.launch {
                            _state.emit(ForgetPasswordState.Error(task.exception?.message.toString()))
                        }
                    }
                }
        }catch (ex:Exception){
            throw ex
        }
    }
}