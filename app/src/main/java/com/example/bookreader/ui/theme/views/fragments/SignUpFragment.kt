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
import com.example.bookreader.databinding.FragmentSignUpBinding
import com.example.bookreader.ui.theme.mvi.signupmvi.SignUpState
import com.example.bookreader.ui.theme.mvi.signupmvi.SignupIntent
import com.example.bookreader.ui.theme.viewmodels.SignUpViewModel
import com.example.bookreader.ui.theme.views.activities.MainActivity
import com.example.bookreader.utils.Constant
import com.example.bookreader.utils.isValidEmail
import com.example.bookreader.utils.isValidPassword
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    val binding get() = _binding!!
    private val viewModel by viewModels<SignUpViewModel>()
    private  var sharedPreferences:SharedPreferences? = null
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)
        sharedPreferences = requireActivity().getSharedPreferences(Constant.REGISTER_INFO,Context.MODE_PRIVATE )
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handelState()
        handelSignUp()
        navigateToLoginScreen()
    }

    private fun navigateToLoginScreen(){
        binding.signupFragmentIHaveAnAccount.setOnClickListener {
            findNavController().navigate(R.id.action_signUpFragment_to_loginFragment)
        }
    }
    private fun handelSignUp(){
        binding.signupFragmentSigupBtn.setOnClickListener {
            if (validateFields()) {
                lifecycleScope.launch {
                    viewModel.channel.send(
                        SignupIntent.Signup(
                            name = binding.signupFragmentUsername.text.trim().toString(),
                            email = binding.signupFragmentEmail.text.trim().toString(),
                            password = binding.signupFragmentPassword.text.trim().toString()
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
                    is SignUpState.Error -> handelError()
                    SignUpState.Idle -> {}
                    SignUpState.Loading -> handelLoading()
                    SignUpState.Success -> handelSuccess()
                }
            }
        }
    }

    private fun handelSuccess() {
        binding.signupFragmentSigupBtn.text = getString(R.string.sign_up)
        binding.signupFragmentLoginProgress.visibility = View.GONE
        val editor = sharedPreferences?.edit()
        editor?.apply{
            putString(Constant.USER_NAME, binding.signupFragmentUsername.text.toString())
            putString(Constant.USER_EMAIL, binding.signupFragmentEmail.text.toString())
            putBoolean(Constant.USER_IS_REGISTERED, true)
        }?.commit()
        val intent = Intent(requireContext(), MainActivity::class.java)
        startActivity(intent)
        requireActivity().finish()
    }

    private fun handelLoading() {
        binding.signupFragmentSigupBtn.text = ""
        binding.signupFragmentLoginProgress.visibility = View.VISIBLE
    }

    private fun handelError() {
        binding.signupFragmentLoginProgress.visibility = View.GONE
        binding.signupFragmentSigupBtn.text = getString(R.string.sign_up)
        Snackbar.make(binding.root, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    private var userNameError = false
    private var emailError = false
    private var passwordError = false

    private fun validateFields(): Boolean {
        if (binding.signupFragmentUsername.text.trim().isEmpty()) {
            userNameError = true
            binding.signupFragmentUsername.error = getString(R.string.field_not_valid)
        } else {
            userNameError = false
        }

        if (binding.signupFragmentEmail.text.trim()
                .isEmpty() || !isValidEmail(binding.signupFragmentEmail.text.trim().toString())
        ) {
            emailError = true
            binding.signupFragmentEmail.error = getString(R.string.field_not_valid)
        } else {
            emailError = false
        }

        if (binding.signupFragmentPassword.text.trim()
                .isEmpty() || !isValidPassword(binding.signupFragmentPassword.text.trim().toString())
        ) {
            passwordError = true
            binding.signupFragmentPassword.error = getString(R.string.field_not_valid)
        } else {
            passwordError = false
        }
        return !userNameError && !emailError && !passwordError
    }
}