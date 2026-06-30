package com.appmb.sdk.mbauth.ui.login

import android.content.Context
import com.google.android.gms.auth.api.signin.GoogleSignInAccount

sealed class AuthIntent {
  data class Login(val username: String, val password: String) : AuthIntent()
  data class LoginByPhone(val phone: String, val password: String) : AuthIntent()
  data class LoginOAuth(val token: String) : AuthIntent()
  data class LoginOtp(val phone: String) : AuthIntent()
  data class LoginByGoogle(val context: Context, val account: GoogleSignInAccount) : AuthIntent()
  data class LoginByFacebook(val context: Context) : AuthIntent()
  object LoginByGuest : AuthIntent()
  object GetGameInfo : AuthIntent()

  object Logout : AuthIntent()

  object GetAuthData : AuthIntent()
  object CheckIsLoggedIn : AuthIntent()
  object ResetAuthState : AuthIntent()

  // Register
  data class Register(
    val phoneNumber: String? = null,
    val password: String,
  ) : AuthIntent()

  // LinkAccount
  data class LinkPhoneAccount(val phoneNumber: String, val password: String) : AuthIntent()

  // Reset password
  data class ResetPassword(val phoneNumber: String, val password: String) : AuthIntent()
}