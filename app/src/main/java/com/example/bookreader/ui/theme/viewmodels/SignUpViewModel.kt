package com.example.bookreader.ui.theme.viewmodels

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.bookreader.data.models.User
import com.example.bookreader.di.SessionProvider
import com.example.bookreader.ui.theme.mvi.signupmvi.SignupIntent
import com.example.bookreader.ui.theme.mvi.signupmvi.SignUpState
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
class SignUpViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore:FirebaseFirestore
):ViewModel() {
    var channel:Channel<SignupIntent> = Channel(Channel.UNLIMITED)
    private val _state:MutableSharedFlow<SignUpState> = MutableSharedFlow(0)
    var state = _state.asSharedFlow()
    private val usersDb = firestore.collection(Constant.USER_COLLECTION)
    init {
        handelIntent()
    }

    private fun handelIntent() {
        viewModelScope.launch {
            channel.consumeAsFlow().collect{intent->
                when(intent){
                    is SignupIntent.Signup -> handelSignup(intent.name , intent.email,intent.password)
                }
            }
        }
    }

    private fun handelSignup(name:String, email: String, password: String) {
        try {
            viewModelScope.launch(Dispatchers.IO) {
                signup(name, email,password)
            }

        }catch (ex:Exception){
            viewModelScope.launch {
                _state.emit(SignUpState.Error(ex.message.toString()))
            }
        }
    }

    private suspend fun signup(name:String,email: String, password: String) {
        try {
            _state.emit(SignUpState.Loading)
            auth.createUserWithEmailAndPassword(email.trim(), password.trim())
                .addOnCompleteListener { task ->
                    if (task.isSuccessful) {
                        val user = auth.currentUser
                        SessionProvider.userId = user?.uid
                        usersDb.document(SessionProvider.userId!!)
                            .set(User(
                                id = SessionProvider.userId!!,
                                email = email,
                                name = name
                            )).addOnSuccessListener{
                                viewModelScope.launch {
                                    _state.emit(SignUpState.Success)
                                }
                            }
                    } else {
                        viewModelScope.launch {
                            _state.emit(SignUpState.Error(task.exception?.message.toString()))
                        }
                    }
                }
        }catch (ex:Exception){
            throw ex
        }

    }
}