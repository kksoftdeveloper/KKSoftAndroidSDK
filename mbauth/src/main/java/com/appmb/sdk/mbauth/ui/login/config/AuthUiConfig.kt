package com.appmb.sdk.mbauth.ui.login.config

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.R

data class AuthUiConfig(
  var backgroundColor: Color = Color.White,
  var inputTextColor: Color = Color.Black,
  var errorTextColor: Color = Color.Black,
  var focusedIndicatorColor: Color = Color.Black,
  var cursorColor: Color = Color.Black,
  var outlinedButtonBorderColor: Color = Color.Black,
  var checkedBoxColor: Color = Color.Black,
  var loginButtonColors: Color = Color.Black,
  var buttonTextColor: Color = Color.Black,
  var tintIconColor: Color = Color.Black,
  var dividerColor: Color = Color.Gray,
  var checkboxCheckedColor: Color = Color.Black,
  @StringRes var welcomeText: Int = R.string.welcome_back,
  @StringRes var subtitleText: Int = R.string.login_to_continue,
  @StringRes var loginButtonText: Int = R.string.login,
  @StringRes var phoneLabelText: Int = R.string.phone_number_label,
  @StringRes var phonePlaceholderText: Int = R.string.phone_number_hint,
  @StringRes var passwordLabelText: Int = R.string.password_label,
  @StringRes var passwordPlaceholderText: Int = R.string.enter_password_label,
  @StringRes var noAccountText: Int = R.string.dont_have_account,
  @StringRes var signUpText: Int = R.string.sign_up_now,
  @StringRes var guestButtonText: Int = R.string.play_as_guest,
  @StringRes var facebookButtonText: Int = R.string.continue_with_facebook,
  @StringRes var googleButtonText: Int = R.string.continue_with_google,

  @DrawableRes val googleIconRes: Int = R.drawable.ic_google,
  @DrawableRes val facebookIconRes: Int = R.drawable.ic_facebook,
  @DrawableRes val guestIconRes: Int = R.drawable.ic_play_now,
  var inputTextStyle: TextStyle = TextStyle(fontSize = 14.sp),
)