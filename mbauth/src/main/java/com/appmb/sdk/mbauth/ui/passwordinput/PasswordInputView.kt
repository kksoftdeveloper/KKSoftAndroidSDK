package com.appmb.sdk.mbauth.ui.passwordinput

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.ui.frame.MbAuthFrameContainer
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.ui.login.AuthViewModel
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.common.PasswordInputWithToggle
import com.appmb.sdk.mbcoreui.common.RequiredFieldLabel
import org.koin.androidx.compose.koinViewModel

abstract class PasswordInputView(
  private val phoneNumber: String,
  private val onClose: () -> Unit,
  private val stepLabel: String? = null,
) {

  abstract fun onCompletePasswordInput(phoneNumber: String, password: String)

  abstract fun getComponentLabel(): Map<String, String>

  // We will use this function to listen the result from the viewModel operation
  abstract fun handleSetPasswordState(authResult: AuthResult?)

  @Composable
  abstract fun OnRender()

  @Composable
  fun Content() {

    val viewModel: PasswordInputViewModel = koinViewModel()
    val uiState by viewModel.uiState.collectAsState()

    val authViewModel: AuthViewModel = koinViewModel()
    val authResultState by authViewModel.authResult.collectAsState()
    val authUiStyle by authViewModel.uiState.collectAsState()

    var password by rememberSaveable { mutableStateOf("") }
    var confirmPwd by rememberSaveable { mutableStateOf("") }

    LaunchedEffect(authResultState) {
      handleSetPasswordState(authResultState)
    }
    OnRender()
    MbAuthFrameContainer(
      buttonLabel = stringResource(R.string.common_accept),
      buttonEnabledState = uiState.canContinue,
      isLoading = authUiStyle.isLoading,
      onButtonClick = {
        onCompletePasswordInput(phoneNumber, password)
      },
      onCloseButtonClick = onClose,
    ) {
      Text(
        text = stepLabel ?: stringResource(R.string.step_3),
        color = colorResource(R.color.gray_text_color),
        fontFamily = CustomFont.fsClanPro,
        fontSize = 15.sp,
      )
      Text(
        text = getComponentLabel()[KEY_TITLE].orEmpty().uppercase(),
        color = colorResource(R.color.brown),
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
      )
      // Password field
      RequiredFieldLabel(
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
          viewModel.onPasswordChange(password)
        },
        placeholder = stringResource(R.string.enter_password_hint),
        modifier = Modifier.padding(top = 2.dp)
      )
      // Confirm password field
      RequiredFieldLabel(
        text = stringResource(R.string.confirm_pwd_label),
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
        password = confirmPwd,
        onPasswordChange = { value ->
          confirmPwd = value
          viewModel.onConfirmPasswordChange(confirmPwd)
        },
        placeholder = stringResource(R.string.reenter_password_hint),
        modifier = Modifier.padding(top = 2.dp)
      )

      // Password validation items
      // Length validation
      PasswordValidationItemView(
        text = stringResource(R.string.password_length_validation),
        isValid = uiState.isPasswordLengthValid,
        modifier = Modifier.padding(top = 4.dp)
      )
//      // Has at least 1 digit
//      PasswordValidationItemView(
//        text = stringResource(R.string.password_have_digit_validation),
//        isValid = uiState.doesPasswordHaveDigit,
//      )
//      // Has at least 1 special character
//      PasswordValidationItemView(
//        text = stringResource(R.string.password_have_special_character_validation),
//        isValid = uiState.doesPasswordHaveSpecialCharacter,
//      )
      // Confirm password must be matched
      PasswordValidationItemView(
        text = stringResource(R.string.password_must_be_match),
        isValid = uiState.doesConfirmPasswordMatch,
      )
    }
  }

  @Composable
  fun PasswordValidationItemView(
    text: String,
    isValid: Boolean,
    modifier: Modifier = Modifier,
  ) {
    val textColor = if (isValid) colorResource(R.color.dark_gray_title) else colorResource(R.color.red_error)
    val iconResId = if (isValid) R.drawable.ic_check_success else R.drawable.ic_error

    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = modifier
        .fillMaxWidth()
        .padding(vertical = 2.dp),
    ) {
      Image(
        painterResource(iconResId),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
          .size(14.dp)
          .padding(0.dp)
      )
      Spacer(
        modifier = Modifier.width(4.dp)
      )
      BasicText(
        text = text,
        style = TextStyle(
          lineHeight = 11.sp,
          color = textColor,
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 10.sp,
          fontWeight = FontWeight.Medium
        )
      )
    }
  }

  companion object {
    const val KEY_TITLE = "title"
  }
}
