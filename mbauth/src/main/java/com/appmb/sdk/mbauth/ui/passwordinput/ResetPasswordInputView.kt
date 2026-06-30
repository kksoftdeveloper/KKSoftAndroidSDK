package com.appmb.sdk.mbauth.ui.passwordinput

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.appmb.sdk.mbauth.ui.login.AuthIntent
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.ui.login.AuthResult.ResetPassword
import com.appmb.sdk.mbauth.ui.login.AuthViewModel
import com.appmb.sdk.mbcoreui.R

class ResetPasswordInputView(
  private val activity: Activity,
  private val authViewModel: AuthViewModel,
  phoneNumber: String,
  onClose: () -> Unit,
  private val navigateToLogin: () -> Unit
) : PasswordInputView(phoneNumber, onClose) {

  override fun onCompletePasswordInput(
    phoneNumber: String,
    password: String,
  ) {
    authViewModel.dispatch(
      AuthIntent.ResetPassword(
        phoneNumber = phoneNumber,
        password = password
      )
    )
  }

  override fun getComponentLabel(): Map<String, String> = mapOf(
    KEY_TITLE to activity.getString(R.string.reset_password),
  )

  @Composable
  override fun OnRender() {

  }

  override fun handleSetPasswordState(authResult: AuthResult?) {
    if (authResult is ResetPassword) {
      navigateToLogin()
    }
  }
}