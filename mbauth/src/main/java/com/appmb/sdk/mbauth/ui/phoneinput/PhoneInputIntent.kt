package com.appmb.sdk.mbauth.ui.phoneinput

sealed interface PhoneInputIntent {
  object ResetState : PhoneInputIntent
  data class RequestOtp(val phone: String, val otpType: String) : PhoneInputIntent
}