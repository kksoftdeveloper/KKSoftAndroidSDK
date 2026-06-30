package com.appmb.sdk.mbauth.ui.login

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.ui.components.ErrorMessageView
import com.appmb.sdk.mbauth.ui.components.TermsAndConditionsText
import com.appmb.sdk.mbauth.ui.frame.MbAuthFrameContainer
import com.appmb.sdk.mbcore.model.localization
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomCheckbox
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.common.PasswordInputWithToggle
import com.appmb.sdk.mbcoreui.common.SocialButtonView
import com.appmb.sdk.mbcoreui.common.TextInputField
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun LoginScreen(
  gameId: Int,
  navigateToChooseServer: () -> Unit,
  navigateToRequestOtp: (String) -> Unit = {},
  handleGoogleSignIn: ((GoogleSignInAccount) -> Unit) -> Unit,
) {
  val authViewModel: AuthViewModel = koinViewModel(parameters = { parametersOf(gameId) })

  var phoneNumber by rememberSaveable { mutableStateOf("") }
  var password by rememberSaveable { mutableStateOf("") }
  var termsChecked by rememberSaveable { mutableStateOf(false) }

  val uiState by authViewModel.uiState.collectAsState()
  val authResultState = authViewModel.authResult.collectAsState()
  val loginState = authViewModel.loginState.collectAsState()
  val activity = LocalContext.current as? Activity
  val context = LocalContext.current

//  val toastHostState = remember { ToastHostState() }
  LaunchedEffect(loginState.value) {
    loginState.value.let {
      when (loginState.value) {
        is LoginState.Error -> {
//          toastHostState.showToast(message = state.message)
          authViewModel.dispatch(AuthIntent.ResetAuthState)
        }

        is LoginState.UnknownServer -> {
          navigateToChooseServer.invoke()
        }

        else -> Unit
      }
    }
  }

  LaunchedEffect(authResultState.value) {
    authResultState.value?.let { result ->
      val intent = Intent().apply {
        putExtra("authResult", result)
      }

      when (result) {
        is AuthResult.AuthSuccess -> {
          val authResult: AuthResult? = intent.getParcelableExtra<AuthResult?>("authResult")
          if (authResult is AuthResult.AuthSuccess) {
            authResult.user.userBlocked?.let { isBlocked ->
              if (isBlocked) {
                val resultIntent = Intent(MbAuth.ACTION_USER_BLOCKED)
                LocalBroadcastManager.getInstance(context).sendBroadcast(resultIntent)
              }
            }
            activity?.setResult(Activity.RESULT_OK, intent)
            activity?.finish()
          }
        }

        is AuthResult.Failure -> {
          val msg = result.msg.localization(context)
//          toastHostState.showToast(message = msg)
        }

        else -> {
          // ignore
        }
      }
    }
  }

  MbAuthFrameContainer(
    buttonLabel = stringResource(R.string.login),
    buttonEnabledState = uiState.canLogin,
    isLoading = uiState.isLoading,
    onButtonClick = {
      authViewModel.dispatch(AuthIntent.LoginByPhone(phoneNumber, password))
    },
    onCloseButtonClick = null
  ) {
    BasicText(
      text = stringResource(R.string.login).uppercase(),
      style = TextStyle(
        color = colorResource(R.color.brown),
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
      )
    )
    BasicText(
      text = stringResource(R.string.phone_number_label),
      style = TextStyle(
        color = colorResource(R.color.black),
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 2.dp)
    )
    TextInputField(
      textFieldValue = phoneNumber,
      onValueChange = { value ->
        // Accept only digits (0–9)
        if (value.length <= 10 && value.all { it.isDigit() }) {
          phoneNumber = value
          authViewModel.onPhoneChange(phoneNumber)
        }
      },
      placeholder = stringResource(R.string.phone_number_hint)
    )

    Spacer(modifier = Modifier.height(4.dp))
    // Password field
    BasicText(
      text = stringResource(R.string.password_label),
      style = TextStyle(
        color = colorResource(R.color.black),
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp)
    )
    PasswordInputWithToggle(
      password = password,
      onPasswordChange = { value ->
        password = value
        authViewModel.onPasswordChange(password)
      },
      placeholder = stringResource(R.string.enter_password_hint),
      modifier = Modifier.padding(top = 2.dp)
    )

    Spacer(modifier = Modifier.height(4.dp))
    // Forgot password
    BasicText(
      text = stringResource(R.string.forgot_password),
      style = TextStyle(
        textDecoration = TextDecoration.Underline,
        fontWeight = FontWeight(700),
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 10.sp,
        color = colorResource(R.color.blue_text_link_color),
        textAlign = TextAlign.Center
      ),
      modifier = Modifier
        .padding(vertical = 2.dp)
        .background(Color.Transparent)
        .clickable {
          // Handle forgot password flow
          navigateToRequestOtp.invoke(MbAuthParams.OTP_TYPE_PARAM_FORGOT_PASSWORD)
        }
        .padding(vertical = 2.dp)
        .align(Alignment.CenterHorizontally)
    )
    // Terms and conditions
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp)
    ) {
      CustomCheckbox(
        checked = termsChecked,
        onCheckedChange = {
          termsChecked = it
          authViewModel.onAcceptTermsChange(it)
        },
        modifier = Modifier.size(16.dp)
      )
      Spacer(
        modifier = Modifier.width(width = 4.dp)
      )
      TermsAndConditionsText()
    }
    Spacer(modifier = Modifier.height(4.dp))
    // Divider with text
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier.fillMaxWidth()
    ) {
      Divider(
        modifier = Modifier.weight(1f),
        thickness = 1.dp,
        color = Color.Gray
      )
      BasicText(
        text = stringResource(R.string.or_continue_with),
        style = TextStyle(
          fontSize = 10.sp,
          fontFamily = CustomFont.fzPoppinsFont,
          color = colorResource(R.color.dark_gray_title)
        ),
        modifier = Modifier.padding(horizontal = 8.dp),
      )
      Divider(
        modifier = Modifier.weight(1f),
        thickness = 1.dp,
        color = Color.Gray
      )
    }
    Spacer(modifier = Modifier.height(4.dp))
    // Social Buttons
    Row(
      horizontalArrangement = Arrangement.spacedBy(4.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp),
    ) {
      // Google button
      if (uiState.isGoogleEnabled) {
        SocialButtonView(
          iconResId = R.drawable.ic_google,
          onClick = {
            if (!uiState.isLoading) {
              handleGoogleSignIn.invoke { account ->
                authViewModel.dispatch(AuthIntent.LoginByGoogle(context, account))
              }
            }
          },
          modifier = Modifier.weight(0.3f)
        )
      }

//      // Facebook button
      if (uiState.isFacebookEnabled) {
        SocialButtonView(
          iconResId = R.drawable.ic_facebook,
          onClick = {
            if (!uiState.isLoading) {
              authViewModel.dispatch(AuthIntent.LoginByFacebook(context))
            }
          },
          modifier = Modifier.weight(0.3f)
        )
      }

      // Play now
      SocialButtonView(
        text = stringResource(R.string.play_as_guest),
        iconResId = R.drawable.ic_play_now,
        onClick = {
          if (!uiState.isLoading) {
            authViewModel.dispatch(AuthIntent.LoginByGuest)
          }
        },
        modifier = Modifier.weight(1f)
      )
    }

    Row(

    ) {
      BasicText(
        text = stringResource(R.string.dont_have_account),
        style = TextStyle(
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 10.sp,
          color = colorResource(R.color.dark_gray_title),
          textAlign = TextAlign.Center
        ),
        modifier = Modifier
          .padding(vertical = 8.dp)
      )

      BasicText(
        text = stringResource(R.string.sign_up_now),
        style = TextStyle(
          textDecoration = TextDecoration.Underline,
          fontWeight = FontWeight(700),
          fontSize = 10.sp,
          fontFamily = CustomFont.fzPoppinsFont,
          color = colorResource(R.color.blue_text_link_color),
          textAlign = TextAlign.Center
        ),
        modifier = Modifier
          .padding(vertical = 8.dp, horizontal = 4.dp)
          .background(Color.Transparent)
          .clickable {
            navigateToRequestOtp.invoke(MbAuthParams.OTP_TYPE_PARAM_REGISTRATION)
          }
      )
    }

    authViewModel.uiState.value.errorCode?.let {
      ErrorMessageView(
        text = it.localization(context)
      )
    }
  }
}

@Preview
@Composable
fun LoginScreenPreview() {
  LoginScreen(
    gameId = 1,
    navigateToChooseServer = { },
    handleGoogleSignIn = { }
  )
}