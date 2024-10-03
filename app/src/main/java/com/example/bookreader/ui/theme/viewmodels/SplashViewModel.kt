package com.example.bookreader.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreader.di.SessionProvider
import com.example.bookreader.ui.theme.mvi.splashmvi.SplashIntent
import com.example.bookreader.ui.theme.mvi.splashmvi.SplashState
import com.google.firebase.auth.FirebaseAuth
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SplashViewModel @Inject constructor(
    private val auth:FirebaseAuth
):ViewModel() {
    var channel:Channel<SplashIntent> = Channel()
    private var _state = MutableStateFlow<SplashState>(SplashState.Idle)
    var state = _state.asStateFlow()

    init {
        handelIntent()
    }

    private fun handelIntent() {
        viewModelScope.launch {
            channel.consumeAsFlow().collect{intent->
                when(intent){
                    SplashIntent.CheckUserIsSigned -> handelCheckUserIsSigned()
                }
            }
        }
    }

    private fun handelCheckUserIsSigned() {
        try {
            val user = auth.currentUser
            if(user!=null){
                SessionProvider.userId = user.uid
                _state.value = SplashState.UserIsSigned(true)
            }else{
                _state.value = SplashState.UserIsSigned(false)
            }
        }catch (ex:Exception){
            _state.value = SplashState.Error(ex.message.toString())
        }
    }
}