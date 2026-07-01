package com.appmb.sdk.mbauth.ui.phoneinput

import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.ui.components.TermsAndConditionsText
import com.appmb.sdk.mbauth.ui.frame.MbAuthFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomCheckbox
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.common.TextInputField
import org.koin.androidx.compose.koinViewModel

@Composable
fun PhoneInputView(
  otpType: String,
  stepLabel: String? = null,
  showAgeConfirmation: Boolean = otpType == MbAuthParams.OTP_TYPE_PARAM_REGISTRATION,
  forcedIsUnder16: Boolean? = null,
  navigateToVerifyOtp: (String, Int, Boolean) -> Unit,
  onClose: () -> Unit
) {
  val viewModel: PhoneInputViewModel = koinViewModel()
  val uiState by viewModel.uiState.collectAsState()

  val requestOtpState = viewModel.requestOtpState.collectAsState()

  var phoneNumber by rememberSaveable { mutableStateOf("") }
  var acceptTerms by rememberSaveable { mutableStateOf(false) }
  var confirmedAge16OrOlder by rememberSaveable { mutableStateOf(true) }
  val isRegistration = otpType == MbAuthParams.OTP_TYPE_PARAM_REGISTRATION
  val shouldShowAgeConfirmation = isRegistration && showAgeConfirmation

  // Display api request error
  var apiError by rememberSaveable { mutableStateOf("") }

  when (val state = requestOtpState.value) {
    is RequestOtpState.Error -> {
      apiError = stringResource(R.string.phone_input_with_unknown_error)
      /*when (state.code) {
        -20 -> stringResource(R.string.error_message_phone_already_linked)
        -80 -> stringResource(R.string.error_message_send_otp_too_much)
        else -> stringResource(R.string.phone_input_with_unknown_error)
      }*/
    }

    is RequestOtpState.Success -> {
      viewModel.dispatchUiEvent(PhoneInputIntent.ResetState)
      navigateToVerifyOtp(
        state.phone,
        state.timeToRetry,
        forcedIsUnder16 ?: (isRegistration && !confirmedAge16OrOlder)
      )
    }

    else -> Unit
  }

  MbAuthFrameContainer(
    buttonLabel = stringResource(R.string.receive_otp_code),
    buttonEnabledState = uiState.canRequestOTP,
    isLoading = uiState.isLoading,
    onButtonClick = {
      viewModel.dispatchUiEvent(PhoneInputIntent.RequestOtp(phoneNumber, otpType))
    },
    onCloseButtonClick = {
      onClose()
    }
  ) {
    Text(
      text = stepLabel ?: stringResource(R.string.step_1),
      color = colorResource(R.color.gray_text_color),
      fontFamily = CustomFont.fsClanPro,
      fontSize = 15.sp,
    )
    Text(
      text = stringResource(R.string.enter_phone_number).uppercase(),
      color = colorResource(R.color.brown),
      fontFamily = CustomFont.fsClanPro,
      fontSize = 16.sp,
      modifier = Modifier.padding(top = 4.dp)
    )
    BasicText(
      text = stringResource(R.string.we_will_send_you_a_verification_code),
      style = TextStyle(
        color = colorResource(R.color.brown),
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
      ),
      modifier = Modifier.padding(top = 6.dp, bottom = 12.dp)
    )
    BasicText(
      text = stringResource(R.string.phone_number_label),
      style = TextStyle(
        color = colorResource(R.color.black),
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 8.dp)
    )
    TextInputField(
      textFieldValue = phoneNumber,
      onValueChange = { value ->
        if (value.length <= 10 && value.all { it.isDigit() }) {
          phoneNumber = value
          apiError = ""
          viewModel.onPhoneChange(value)
        }
      },
      placeholder = stringResource(R.string.phone_number_hint),
      modifier = Modifier.padding(bottom = 8.dp)
    )
    Row(
      verticalAlignment = Alignment.CenterVertically,
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
    ) {
      CustomCheckbox(
        checked = acceptTerms,
        onCheckedChange = {
          acceptTerms = it
          viewModel.onAcceptTermsChange(it)
        },
        modifier = Modifier.size(16.dp)
      )
      Spacer(
        modifier = Modifier.width(width = 4.dp)
      )
      TermsAndConditionsText()
    }
    if (shouldShowAgeConfirmation) {
      Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 8.dp)
      ) {
        CustomCheckbox(
          checked = confirmedAge16OrOlder,
          onCheckedChange = {
            confirmedAge16OrOlder = it
            viewModel.onConfirmedAge16OrOlderChange(it)
          },
          modifier = Modifier.size(16.dp)
        )
        Spacer(
          modifier = Modifier.width(width = 4.dp)
        )
        BasicText(
          text = stringResource(R.string.confirm_age_16_or_older),
          style = TextStyle(
            color = colorResource(R.color.black),
            fontFamily = CustomFont.fzPoppinsFont,
            fontSize = 10.sp,
          ),
          modifier = Modifier.weight(1f)
        )
      }
    }
    BasicText(
      text = uiState.phoneError ?: apiError,
      style = TextStyle(
        color = MaterialTheme.colorScheme.error,
        fontSize = 12.sp,
        fontFamily = CustomFont.fzPoppinsFont,
        textAlign = TextAlign.Center
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp),
    )
  }
}

@Preview
@Composable
fun previewPhoneInputView() {
  PhoneInputView(
    otpType = "SMS",
    navigateToVerifyOtp = { _, _, _ -> },
    onClose = {}
  )
}
