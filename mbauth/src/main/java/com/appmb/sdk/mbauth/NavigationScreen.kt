package com.appmb.sdk.mbauth

abstract class AuthScreen(open val name: String)

object Authenticate : AuthScreen("Authenticate")

object LoginOAuth : AuthScreen("LoginOAuth")

object LoginOtp : AuthScreen("LoginOtp")

object Payment : AuthScreen("Payment")

object EventTracking : AuthScreen("EventTracking")

object SelectServer : AuthScreen("SelectServer")

data class RequestOTP(val otpType: String) : AuthScreen(name = NAME) {
  fun getNavPath() = NAME.replace("{otpType}", otpType)
  companion object {
    const val NAME = "RequestOTP/{otpType}"
  }
}

data class VerifyOTP(val phone: String, val timeToRetry: Int, val otpType: String) :
  AuthScreen(name = NAME) {
  companion object {
    const val NAME = "VerifyOTP/{phone}/{timeToRetry}/{otpType}"
  }
}

data class Register(val phone: String) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "Register/{phone}"
  }
}

//object GuestHome : AuthScreen("GuestHome")

data class SetLinkPhoneAccountPassword(val phone: String) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "SetLinkPhoneAccountPassword/{phone}"
  }
}

data class ResetPassword(val phone: String) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "ResetPassword/{phone}"
  }
}

object ReminderUpdate : AuthScreen("ReminderUpdate")

object Logout : AuthScreen("Logout")

object LinkAccount : AuthScreen("LinkAccount")

object ChooseServer : AuthScreen("ChooseServer")

object DeactivateAccount : AuthScreen("DeactivateAccount")

object TokenExpiration : AuthScreen("TokenExpiration")
object UserBlocked : AuthScreen("UserBlocked")
object ServerMaintenance : AuthScreen("ServerMaintenance")