package com.appmb.sdk.mbauth.ui.passwordinput

import android.app.Activity
import android.content.Intent
import androidx.compose.runtime.Composable
import com.appmb.sdk.mbauth.ui.login.AuthIntent
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.ui.login.AuthViewModel
import com.appmb.sdk.mbcoreui.R

class LinkPhoneAccountPasswordInputScreen(
  private val activity: Activity,
  private val authViewModel: AuthViewModel,
  phoneNumber: String,
  onClose: () -> Unit,
) : PasswordInputView(phoneNumber, onClose) {

  override fun onCompletePasswordInput(
    phoneNumber: String,
    password: String,
  ) {
    authViewModel.dispatch(
      AuthIntent.LinkPhoneAccount(
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

  }

  override fun handleSetPasswordState(authResult: AuthResult?) {
    // Handle Result back to client app
    authResult?.let {
      val intent = Intent().apply {
        putExtra("authResult", authResult)
      }
      activity.setResult(Activity.RESULT_OK, intent)
      activity.finish()
    }
  }
}