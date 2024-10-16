package com.example.bookreader.ui.theme.views.fragments

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.example.bookreader.R
import com.example.bookreader.data.models.User
import com.example.bookreader.databinding.FragmentLoginBinding
import com.example.bookreader.ui.theme.mvi.loginmvi.SignInIntent
import com.example.bookreader.ui.theme.mvi.loginmvi.SignInState
import com.example.bookreader.ui.theme.viewmodels.SignInViewModel
import com.example.bookreader.ui.theme.views.activities.MainActivity

import com.example.bookreader.utils.Constant
import com.example.bookreader.utils.isValidEmail
import com.example.bookreader.utils.isValidPassword
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class LoginFragment : Fragment() {
    private var _binding: FragmentLoginBinding? = null
    private val binding get() = _binding!!
    private val viewModel: SignInViewModel by viewModels()
    private var sharedPreferences: SharedPreferences? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater, container, false)
        sharedPreferences =
            requireActivity().getSharedPreferences(Constant.REGISTER_INFO, Context.MODE_PRIVATE)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handelState()
        handelLoginClick()
        handelForgetPasswordClick()
    }

    private fun handelForgetPasswordClick() {
        binding.loginFragmentForgetPassword.setOnClickListener {
            findNavController().navigate(R.id.action_loginFragment_to_forgetPasswordFragment)
        }
    }

    private fun handelLoginClick() {
        binding.loginFragmentSigninBtn.setOnClickListener {
            lifecycleScope.launch {
                if(validateFields()){
                    viewModel.channel.send(
                        SignInIntent.SignIn(
                            binding.loginFragmentEmail.text.trim().toString(),
                            binding.loginFragmentPassword.text.trim().toString()
                        )
                    )
                }
            }
        }
    }

    private fun handelState() {
        lifecycleScope.launch {
            viewModel.state.collect { state ->
                when (state) {
                    is SignInState.Error -> handelError(state.message)
                    is SignInState.Idle -> {}
                    is SignInState.Loading -> handelLoading()
                    is SignInState.Success -> handelSuccess(state.user)
                    is SignInState.EmailOrPasswordInCorrect -> handelSignInOrPasswordInCorrect()
                }
            }
        }
    }

    private fun handelSignInOrPasswordInCorrect() {
        binding.loginFragmentLoadingProgress.visibility = View.GONE
        binding.loginFragmentSigninBtn.text = getString(R.string.login)
        binding.emailOrPasswordIncorrect.visibility = View.VISIBLE
    }

    private fun handelSuccess(user:User) {
        binding.loginFragmentLoadingProgress.visibility = View.GONE
        binding.loginFragmentSigninBtn.text = getString(R.string.login)
        val editor = sharedPreferences?.edit()
        editor?.apply {
            putString(Constant.USER_EMAIL, user.email)
            putString(Constant.USER_NAME, user.name)
            putBoolean(Constant.USER_IS_REGISTERED, true)
        }?.commit()
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun handelLoading() {
        binding.emailOrPasswordIncorrect.visibility = View.GONE
        binding.loginFragmentSigninBtn.text = ""
        binding.loginFragmentLoadingProgress.visibility = View.VISIBLE
    }

    private fun handelError(message: String) {
        binding.loginFragmentLoadingProgress.visibility = View.GONE
        binding.loginFragmentSigninBtn.text = getString(R.string.login)
        Snackbar.make(binding.root, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG)
            .show()
    }


    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private var emailError = false
    private var passwordError = false

    private fun validateFields(): Boolean {
        if (binding.loginFragmentEmail.text.trim()
                .isEmpty() || !isValidEmail(binding.loginFragmentEmail.text.trim().toString())
        ) {
            emailError = true
            binding.loginFragmentEmail.error = getString(R.string.field_not_valid)
        } else {
            emailError = false
        }

        if (binding.loginFragmentPassword.text.trim()
                .isEmpty() || !isValidPassword(binding.loginFragmentPassword.text.trim().toString())
        ) {
            passwordError = true
            binding.loginFragmentPassword.error = getString(R.string.field_not_valid)
        } else {
            passwordError = false
        }
        return !emailError && !passwordError
    }
}