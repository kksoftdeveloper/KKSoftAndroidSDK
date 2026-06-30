package com.appmb.sdk.mbauth.ui.passwordinput

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.appmb.sdk.mbauth.ui.login.AuthIntent
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.ui.login.AuthViewModel
import com.appmb.sdk.mbauth.ui.login.RegisterState
import com.appmb.sdk.mbcoreui.R

class SignUpPasswordInputScreen(
  private val activity: Activity,
  private val authViewModel: AuthViewModel,
  phoneNumber: String,
  onClose: () -> Unit,
  private val navigateToChooseServer: () -> Unit,
) : PasswordInputView(phoneNumber, onClose) {

  override fun onCompletePasswordInput(
    phoneNumber: String,
    password: String,
  ) {
    authViewModel.dispatch(
      AuthIntent.Register(
        phoneNumber = phoneNumber,
        password = password
      )
    )
  }

  override fun getComponentLabel(): Map<String, String> = mapOf(
    KEY_TITLE to activity.getString(R.string.set_up_your_password),
  )

  @Composable
  override fun OnRender() {
    val registerState by authViewModel.registerState.collectAsState()
    LaunchedEffect(registerState) {
      if (registerState is RegisterState.UnknownServer) {
        navigateToChooseServer.invoke()
      }
    }
  }

  override fun handleSetPasswordState(authResult: AuthResult?) {
    // Handle back to client app when register success
    authResult?.let {
      val intent = Intent().apply {
        putExtra("authResult", authResult)
      }
      activity.setResult(Activity.RESULT_OK, intent)
      activity.finish()
    }
  }
}