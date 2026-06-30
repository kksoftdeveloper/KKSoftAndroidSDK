@file:OptIn(ExperimentalComposeUiApi::class)

package com.appmb.sdk.mbauth.ui.otp

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.toMutableStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.ui.frame.MbAuthFrameContainer
import com.appmb.sdk.mbauth.ui.otp.OtpState.Companion.RETRY_TIME
import com.appmb.sdk.mbauth.ui.otp.components.OtpCountdownTimer
import com.appmb.sdk.mbauth.ui.otp.components.OtpInput
import com.appmb.sdk.mbcore.model.localization
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun OtpInputScreen(
  otpLength: Int = 6,
  otpType: String,
  phoneNumber: String,
  timeToRetry: Int,
  onOtpVerifiedSuccess: () -> Unit,
  onClose: () -> Unit
) {
  val viewModel: OtpInputViewModel =
    koinViewModel(parameters = { parametersOf(phoneNumber, otpType, otpLength) })
  val otpState = viewModel.otpState

  ObserveAsEvents(flow = viewModel.events) { event ->
    when (event) {
      is OtpEvent.VerifiedOTP -> {
        onOtpVerifiedSuccess()
      }

      is OtpEvent.RequestedOTP -> {
        viewModel.startCountdownRetryTime(seconds =  event.requestOtpData.retryAfterSeconds ?: RETRY_TIME)
        viewModel.startCountdownExpiredTime(seconds = event.requestOtpData.expiresInSeconds ?: OtpState.EXPIRED_TIME)
      }

      is OtpEvent.LockOTPVerification -> {
        viewModel.startCountdownUnlockTime(seconds = OtpState.LOCK_TIME)
      }

      else -> { }
    }
  }

  MbAuthFrameContainer(
    buttonLabel = stringResource(R.string.common_continue),
    buttonEnabledState = otpState.canVerifyOTP && !otpState.isLocked,
    isLoading = otpState.isLoading,
    onButtonClick = {
      viewModel.onAction(
        action = OtpAction.VerifyOtp(
          otp = otpState.otpValues.joinToString(""),
          otpType = otpType
        )
      )
    },
    onCloseButtonClick = {
      onClose()
    },
  ) {
    Text(
      text = stringResource(R.string.step_2),
      color = colorResource(R.color.gray_text_color),
      fontFamily = CustomFont.fsClanPro,
      fontSize = 15.sp,
    )
    Text(
      text = stringResource(R.string.enter_verification_code).uppercase(),
      color = colorResource(R.color.brown),
      fontFamily = CustomFont.fsClanPro,
      fontSize = 16.sp,
      modifier = Modifier.padding(top = 4.dp)
    )
    BasicText(
      text = stringResource(R.string.send_otp_notification, phoneNumber),
      style = TextStyle(
        color = colorResource(R.color.brown),
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
        textAlign = TextAlign.Center,
      ),
      modifier = Modifier
        .padding(top = 6.dp)
        .fillMaxWidth()
    )
    OtpInput(
      otpValues = otpState.otpValues.toMutableStateList(),
      enabled = otpState.isEnableOTPInput && !otpState.isLocked,
      focusRequesters = otpState.focusRequesters,
      otpLength = otpLength,
      onOtpComplete = { result ->
        viewModel.onAction(action = OtpAction.OTPCompletion(result, otpType = otpType))
      },
      onOtpIncomplete = { otpValues ->
        viewModel.onAction(action = OtpAction.OTPIncompletion(otpValues, otpType = otpType))
      },
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)
    )
    Row(
      modifier = Modifier
        .padding(top = 8.dp)
        .fillMaxWidth(),
      verticalAlignment = Alignment.CenterVertically,
      horizontalArrangement = Arrangement.Center,
    ) {
      if (otpState.errorCode != null) {
        Image(
          painterResource(R.drawable.ic_error),
          contentDescription = null,
          modifier = Modifier.size(16.dp)
        )
        BasicText(
          text = otpState.errorCode.localization(context = LocalContext.current),
          style = TextStyle(
            fontSize = 12.sp,
            color = MaterialTheme.colorScheme.error,
            fontWeight = FontWeight(400),
            fontFamily = CustomFont.fzPoppinsFont,
          ),
          modifier = Modifier.padding(start = 2.dp)
        )
      } else {
        if(otpState.expiredTime == 0) {
          Image(
            painterResource(R.drawable.ic_error),
            contentDescription = null,
            modifier = Modifier.size(16.dp)
          )
          BasicText(
            text = stringResource(R.string.auth_otp_expired),
            style = TextStyle(
              fontSize = 12.sp,
              color = MaterialTheme.colorScheme.error,
              fontWeight = FontWeight(400),
              fontFamily = CustomFont.fzPoppinsFont,
            ),
            modifier = Modifier.padding(start = 2.dp)
          )
        }
      }
    }
    if (otpState.timeToRetry > 0) {
      OtpCountdownTimer(
        modifier = Modifier.padding(top = 8.dp),
        timeLeft = otpState.timeToRetry // timeToResendRemaining Seconds
      )
    }
    if (otpState.canResend && !otpState.isLocked) {
      BasicText(
        text = stringResource(R.string.resend_otp),
        style = TextStyle(
          textDecoration = TextDecoration.Underline,
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 12.sp,
          fontWeight = FontWeight(700),
        ),
        modifier = Modifier
          .padding(top = 8.dp)
          .background(Color.Transparent)
          .clickable {
            viewModel.onAction(OtpAction.RequestOtp(phone = phoneNumber, otpType = otpType))
          }
      )
    }
  }
}