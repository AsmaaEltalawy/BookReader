package com.example.bookreader.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.models.User
import com.example.bookreader.di.SessionProvider
import com.example.bookreader.ui.theme.mvi.loginmvi.SignInIntent
import com.example.bookreader.ui.theme.mvi.loginmvi.SignInState
import com.example.bookreader.utils.Constant
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.consumeAsFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class SignInViewModel @Inject constructor(
    private val auth:FirebaseAuth,
    private val firestore:FirebaseFirestore
):ViewModel() {

    var channel: Channel<SignInIntent> = Channel(Channel.UNLIMITED)
    private val _state:MutableSharedFlow<SignInState> = MutableSharedFlow(0)
    var state = _state.asSharedFlow()
    init {
        handelIntent()
    }

    private fun handelIntent() {
        viewModelScope.launch {
            channel.consumeAsFlow().collect{intent->
                when(intent){
                    is SignInIntent.SignIn -> handelSignIn(intent.email,intent.password)
                }
            }
        }
    }

    private fun handelSignIn(email: String, password: String) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                signIn(email,password)
            }

        }catch (ex:Exception){
          viewModelScope.launch {
              _state.emit(SignInState.Error(ex.message.toString()))
          }
        }
    }

    private suspend fun signIn(email: String, password: String) {
        try {
            _state.emit(SignInState.Loading)
            auth.signInWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        SessionProvider.userId = user?.uid
                        firestore.collection(Constant.USER_COLLECTION)
                            .document(SessionProvider.userId!!)
                            .get().addOnSuccessListener {
                                if(it.exists()){
                                   var  signedUser = it.toObject(User::class.java)!!
                                    viewModelScope.launch {
                                        _state.emit(SignInState.Success(signedUser))
                                    }
                                }
                            }

                    } else {
                       viewModelScope.launch {
                           _state.emit(SignInState.EmailOrPasswordInCorrect)
                       }
                    }
                }
        }catch (ex:Exception){
            throw ex
        }

    }
}