package com.appmb.sdk.mbauth.ui.phoneinput

sealed interface RequestOtpState {
  object Idle : RequestOtpState
  object Loading : RequestOtpState
  data class Success(val phone: String, val timeToRetry: Int) : RequestOtpState
  data class Error(val code: Int, val message: String) : RequestOtpState
}