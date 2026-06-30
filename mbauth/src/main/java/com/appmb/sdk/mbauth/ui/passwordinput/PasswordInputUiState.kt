package com.appmb.sdk.mbauth.ui.passwordinput

data class PasswordInputUiState(
  val password: String = "",
  val confirmPassword: String = "",
  val isLoading: Boolean = false,
  val error: String? = null
) {
  val isPasswordLengthValid: Boolean
    get() = password.isNotBlank() && password.length >= 8

//  val doesPasswordHaveDigit: Boolean
//    get() = Regex(".*\\d.*").containsMatchIn(password)

//  val doesPasswordHaveSpecialCharacter: Boolean
//    get() = Regex(".*[!@#\$%^&*(),.?\":{}|<>].*").containsMatchIn(password)

  val doesConfirmPasswordMatch: Boolean
    get() = confirmPassword.isNotBlank() && confirmPassword == password

  val canContinue: Boolean
    get() = password.isNotBlank() && confirmPassword.isNotBlank() &&
        isPasswordLengthValid /* && doesPasswordHaveDigit && doesPasswordHaveSpecialCharacter*/
        && doesConfirmPasswordMatch
}

