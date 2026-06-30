package com.appmb.sdk.mbauth.ui.otp

import androidx.compose.ui.focus.FocusRequester

data class OtpState(
  val phoneNumber: String,
  val otpValues: List<String> = List(OTP_LENGTH) { "" },
  val focusRequesters: List<FocusRequester> = List(OTP_LENGTH) { FocusRequester() },
  val requestOTPCount: Int = 0,
  val isOTPValid: Boolean = false,
  val isEnableOTPInput: Boolean = true,
  val isLocked: Boolean = false,
  val timeToUnlock: Int = LOCK_TIME, // seconds
  val timeToRetry: Int = RETRY_TIME, // seconds
  val expiredTime: Int = EXPIRED_TIME, // seconds
  val otpType: String = OTP_TYPE_SMS,
  val otpLength: Int = OTP_LENGTH,
  val canResend: Boolean = false,
  val canVerifyOTP: Boolean = false,
  val isLoading: Boolean = false,
  val errorCode: Int? = null // value of AuthErrorCodeResponse
) {
  companion object {
    const val OTP_TYPE_SMS = "SMS"
    const val OTP_LENGTH = 6
    const val MIN_TIME_TO_RETRY = 60 // seconds
    const val MAX_TIME_TO_RETRY = 300 // seconds
    const val EXPIRED_TIME = 300 // seconds
    const val RETRY_TIME = 60 // seconds
    const val LOCK_TIME = 60 // seconds
    const val MAX_REQUEST_OTP = 5
  }
}