package com.example.bookreader.utils

val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.com$")
fun isValidEmail(email: String): Boolean {
    return email.matches(emailRegex)
}

val passwordRegex = Regex("^.{8,}$")
fun isValidPassword(password: String): Boolean {
    return password.matches(passwordRegex)
}