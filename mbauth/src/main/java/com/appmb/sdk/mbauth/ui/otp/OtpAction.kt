package com.appmb.sdk.mbauth.ui.otp

sealed interface OtpAction {
  data class OTPCompletion(val otpValues: List<String>, val otpType: String) : OtpAction
  data class OTPIncompletion(val otpValues: List<String>, val otpType: String): OtpAction
  data class VerifyOtp(val otp: String, val otpType: String) : OtpAction
  data class RequestOtp(val phone: String, val otpType: String) : OtpAction
}