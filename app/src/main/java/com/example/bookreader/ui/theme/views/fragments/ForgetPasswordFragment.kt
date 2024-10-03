package com.example.bookreader.ui.theme.views.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.navOptions
import com.example.bookreader.R
import com.example.bookreader.databinding.FragmentForgetPasswordBinding
import com.example.bookreader.ui.theme.mvi.forgetpasswordmvi.ForgetPasswordIntent
import com.example.bookreader.ui.theme.mvi.forgetpasswordmvi.ForgetPasswordState
import com.example.bookreader.ui.theme.viewmodels.ForgetPasswordViewModel
import com.example.bookreader.utils.isValidEmail
import com.example.bookreader.utils.isValidPassword
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

@AndroidEntryPoint
class ForgetPasswordFragment : Fragment() {
    private var _binding:FragmentForgetPasswordBinding?=null
    private val binding get() = _binding!!
    private val viewModel:ForgetPasswordViewModel by viewModels()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentForgetPasswordBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        handelState()
        handelConfirmChanges()
    }

    private fun handelConfirmChanges(){
        binding.forgetPasswordFragmentConfirmBtn.setOnClickListener {
            if(validateFields()){
                lifecycleScope.launch {
                    viewModel.channel.send(
                        ForgetPasswordIntent.ConfirmChangePassword(
                        binding.forgetPasswordFragmentEmail.text.trim().toString(),
                    ))
                }
            }
        }
    }

    private fun handelState(){
        lifecycleScope.launch {
            viewModel.state.collect{state->
                when(state){
                    is ForgetPasswordState.ConfirmChangePasswordFailed -> handelConfirmFailed()
                    is ForgetPasswordState.Error -> handelError(state.message)
                    is ForgetPasswordState.Idle -> {}
                    is ForgetPasswordState.Loading -> handelLoading()
                    is ForgetPasswordState.Success -> handelSuccess()
                }
            }
        }
    }

    private fun handelSuccess() {
        binding.forgetPasswordFragmentLoadingProgress.visibility = View.GONE
        binding.forgetPasswordFragmentConfirmBtn.text = getString(R.string.reseat_password)
        findNavController().navigate(
            R.id.action_forgetPasswordFragment_to_loginFragment,
            null,
            navOptions {
                popUpTo(R.id.forgetPasswordFragment) {
                    inclusive = true
                }
            }
        )

    }

    private fun handelLoading() {
        binding.forgetPasswordFragmentConfirmBtn.text = ""
        binding.forgetPasswordFragmentLoadingProgress.visibility = View.VISIBLE

    }

    private fun handelError(message: String) {
        binding.forgetPasswordFragmentLoadingProgress.visibility = View.GONE
        binding.forgetPasswordFragmentConfirmBtn.text = getString(R.string.reseat_password)
        Snackbar.make(binding.root, getString(R.string.something_went_wrong), Snackbar.LENGTH_LONG)
            .show()
    }

    private fun handelConfirmFailed() {
       binding.forgetPasswordFragmentLoadingProgress.visibility = View.GONE
        binding.forgetPasswordFragmentConfirmBtn.text = getString(R.string.reseat_password)
        Snackbar.make(binding.root,
            getString(R.string.changed_password_failed), Snackbar.LENGTH_LONG)
            .show()
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }


    private var emailError = false

    private fun validateFields(): Boolean {
        if (binding.forgetPasswordFragmentEmail.text.trim()
                .isEmpty() || !isValidEmail(binding.forgetPasswordFragmentEmail.text.trim().toString())
        ) {
            emailError = true
            binding.forgetPasswordFragmentEmail.error = getString(R.string.field_not_valid)
        } else {
            emailError = false
        }

        return !emailError
    }

}