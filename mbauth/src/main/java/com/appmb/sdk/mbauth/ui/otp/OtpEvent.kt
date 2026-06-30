package com.appmb.sdk.mbauth.ui.otp

import com.appmb.sdk.mbauth.data.dto.response.RequestOtpData
import com.appmb.sdk.mbauth.data.dto.response.VerifyOtpData

sealed interface OtpEvent {
  data class Error(val errorCode: Int) : OtpEvent
  data class LockOTPVerification(val errorCode: Int) : OtpEvent
  data class RequestedOTP(val requestOtpData: RequestOtpData) : OtpEvent
  data class VerifiedOTP(val verifiedOTPData: VerifyOtpData) : OtpEvent
}