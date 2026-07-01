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
    const val NAME = "VerifyOTP/{phone}/{timeToRetry}/{otpType}/{isUnder16}"
  }
}

data class RegisterPersonalInfo(val phone: String, val isUnder16: Boolean) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "RegisterPersonalInfo/{phone}/{isUnder16}"
  }
}

data class RegisterGuardianInfo(val phone: String) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "RegisterGuardianInfo/{phone}"
  }
}

data class RegisterGuardianOTP(val phone: String, val guardianPhone: String, val timeToRetry: Int) :
  AuthScreen(name = NAME) {
  companion object {
    const val NAME = "RegisterGuardianOTP/{phone}/{guardianPhone}/{timeToRetry}"
  }
}

data class Register(val phone: String, val isUnder16: Boolean) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "Register/{phone}/{isUnder16}"
  }
}

data class CompleteProfilePhone(val isUnder16: Boolean) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "CompleteProfilePhone/{isUnder16}"
  }
}

data class CompleteProfileVerifyOTP(val phone: String, val timeToRetry: Int, val isUnder16: Boolean) :
  AuthScreen(name = NAME) {
  companion object {
    const val NAME = "CompleteProfileVerifyOTP/{phone}/{timeToRetry}/{isUnder16}"
  }
}

data class CompleteProfilePersonalInfo(
  val isUnder16: Boolean,
  val requiresUserPhoneVerification: Boolean,
) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "CompleteProfilePersonalInfo/{isUnder16}/{requiresUserPhoneVerification}"
  }
}

data class CompleteProfileGuardianInfo(val requiresUserPhoneVerification: Boolean) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "CompleteProfileGuardianInfo/{requiresUserPhoneVerification}"
  }
}

data class CompleteProfileGuardianOTP(
  val guardianPhone: String,
  val timeToRetry: Int,
  val requiresUserPhoneVerification: Boolean,
) : AuthScreen(name = NAME) {
  companion object {
    const val NAME = "CompleteProfileGuardianOTP/{guardianPhone}/{timeToRetry}/{requiresUserPhoneVerification}"
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
