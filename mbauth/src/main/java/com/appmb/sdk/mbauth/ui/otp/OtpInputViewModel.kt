package com.appmb.sdk.mbauth.ui.otp

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import kotlinx.coroutines.Job
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

class OtpInputViewModel(
  private val phoneNumber: String,
  private val otpType: String,
  private val otpLength: Int,
) : ViewModel() {

  var otpState by mutableStateOf(
    OtpState(
      phoneNumber = phoneNumber,
      otpType = otpType,
      otpLength = otpLength,
      isEnableOTPInput = true,
      timeToRetry = OtpState.RETRY_TIME
    )
  )
    private set

  private val eventChannel = Channel<OtpEvent>()
  val events = eventChannel.receiveAsFlow()
  private var retryCountdownJob: Job? = null
  private var expiredCountdownJob: Job? = null
  private var unlockCountdownJob: Job? = null

  init {
//    sendRequestOtp(
//      actionData = OtpAction.RequestOtp(
//        phone = phoneNumber,
//        otpType = otpType
//      )
//    )
    startCountdownRetryTime(seconds = OtpState.RETRY_TIME)
    startCountdownExpiredTime(seconds = OtpState.EXPIRED_TIME)
  }
  fun startCountdownRetryTime(seconds: Int) {
    retryCountdownJob?.cancel()
    retryCountdownJob = viewModelScope.launch {
      var timeLeft = seconds
      otpState = otpState.copy(
        canResend = false
      )
      while (timeLeft > 0) {
        otpState = otpState.copy(timeToRetry = timeLeft)
        delay(1000)
        timeLeft--
      }
      otpState = otpState.copy(
        timeToRetry = 0,
        isEnableOTPInput = true,
        canResend = true
      )
    }
  }

  fun startCountdownExpiredTime(seconds: Int) {
    expiredCountdownJob?.cancel()
    expiredCountdownJob = viewModelScope.launch {
      var timeLeft = seconds
      while (timeLeft > 0) {
        otpState = otpState.copy(expiredTime = timeLeft)
        delay(1000)
        timeLeft--
      }
      otpState = otpState.copy(
        expiredTime = 0,
        isEnableOTPInput = true,
        canResend = true
      )
    }
  }

  fun startCountdownUnlockTime(seconds: Int) {
    unlockCountdownJob?.cancel()
    unlockCountdownJob = viewModelScope.launch {
      var timeLeft = seconds
      while (timeLeft > 0) {
        otpState = otpState.copy(timeToUnlock = timeLeft)
        delay(1000)
        timeLeft--
      }
      otpState = otpState.copy(
        timeToUnlock = 0,
        isLocked = false,
        isEnableOTPInput = true
      )
    }
  }

  fun onAction(action: OtpAction) {
    when (action) {
      is OtpAction.RequestOtp -> {
        sendRequestOtp(action)
      }

      is OtpAction.VerifyOtp -> {
        verifyOtp(action)
      }

      is OtpAction.OTPCompletion -> {
        otpState = otpState.copy(
          otpValues = action.otpValues,
          canVerifyOTP = true,
          errorCode = null,
        )
        if (!otpState.isLocked) {
          onAction(
            action = OtpAction.VerifyOtp(
              otp = otpState.otpValues.joinToString(""),
              otpType = otpType
            )
          )
        }
      }

      is OtpAction.OTPIncompletion -> {
        otpState = otpState.copy(
          otpValues = action.otpValues,
          canVerifyOTP = false,
          errorCode = null
        )
      }
    }
  }

  private fun sendRequestOtp(actionData: OtpAction.RequestOtp) {
    viewModelScope.launch {
      otpState = otpState.copy(
        isLoading = true,
        errorCode = null
      )
      MbAuth.requestOtp(
        MbAuthParams.buildRequestOtp(
          phone = actionData.phone,
          type = actionData.otpType,
        )
      ) { result ->
        when (result) {
          is RequestOtpResult.Error -> {
            if(result.code == AuthErrorCodeResponse.OTPRequestManyTime.code) {
              otpState = otpState.copy(
                isLoading = false,
                isLocked = true,
                canVerifyOTP = false,
                errorCode = result.code
              )
              viewModelScope.launch {
                eventChannel.send(OtpEvent.LockOTPVerification(errorCode = result.code))
              }
              return@requestOtp
            }
            otpState = otpState.copy(
              isLoading = false,
              canVerifyOTP = false,
              errorCode = result.code,
            )
            viewModelScope.launch {
              eventChannel.send(OtpEvent.Error(errorCode = result.code))
            }
          }

          is RequestOtpResult.Success -> {
            viewModelScope.launch {
              eventChannel.send(OtpEvent.RequestedOTP(requestOtpData = result.data))
            }
            otpState = otpState.copy(
              isLoading = false,
              timeToRetry = result.data.retryAfterSeconds ?: OtpState.RETRY_TIME,
              expiredTime = result.data.expiresInSeconds ?: OtpState.EXPIRED_TIME,
              errorCode = null
            )
          }
        }
      }
    }
  }

  private fun verifyOtp(actionData: OtpAction.VerifyOtp) {
    viewModelScope.launch {
      otpState = otpState.copy(
        isLoading = true,
        errorCode = null
      )
      MbAuth.verifyOtp(
        MbAuthParams.buildVerifyOtp(
          phone = phoneNumber,
          otp = actionData.otp,
          otpType = actionData.otpType
        )
      ) { result ->
        when (result) {
          is VerifyOtpResult.Error -> {
            otpState = otpState.copy(
              isLoading = false,
              errorCode = result.code,
              canVerifyOTP = false,
            )
            viewModelScope.launch {
              eventChannel.send(OtpEvent.Error(result.code))
            }
          }
          is VerifyOtpResult.Success -> {
            otpState = otpState.copy(
              isLoading = false,
              errorCode = null
            )
            viewModelScope.launch {
              eventChannel.send(OtpEvent.VerifiedOTP(verifiedOTPData = result.data))
            }
          }
        }
      }
    }
  }
}
