package com.appmb.sdk.mbauth.ui.registration

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.ui.frame.MbAuthFrameContainer
import com.appmb.sdk.mbauth.ui.phoneinput.PhoneInputIntent
import com.appmb.sdk.mbauth.ui.phoneinput.PhoneInputViewModel
import com.appmb.sdk.mbauth.ui.phoneinput.RequestOtpState
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.common.TextInputField
import org.koin.androidx.compose.koinViewModel

@Composable
fun PersonalInfoScreen(
  stepLabel: String,
  onContinue: () -> Unit,
  onClose: () -> Unit,
) {
  var fullName by rememberSaveable { mutableStateOf("") }
  var birthDate by rememberSaveable { mutableStateOf("") }
  var gender by rememberSaveable { mutableStateOf("") }
  var address by rememberSaveable { mutableStateOf("") }
  val canContinue = fullName.isNotBlank() &&
    birthDate.isNotBlank() &&
    gender.isNotBlank() &&
    address.isNotBlank()

  MbAuthFrameContainer(
    buttonLabel = stringResource(R.string.common_continue),
    buttonEnabledState = canContinue,
    onButtonClick = onContinue,
    onCloseButtonClick = onClose,
  ) {
    RegistrationTitle(
      stepLabel = stepLabel,
      title = stringResource(R.string.personal_info_title)
    )
    RegistrationField(
      label = stringResource(R.string.full_name_label),
      value = fullName,
      placeholder = stringResource(R.string.full_name_hint),
      onValueChange = { fullName = it }
    )
    RegistrationField(
      label = stringResource(R.string.birth_date_label),
      value = birthDate,
      placeholder = stringResource(R.string.birth_date_hint),
      onValueChange = { birthDate = it }
    )
    RegistrationField(
      label = stringResource(R.string.gender_label),
      value = gender,
      placeholder = stringResource(R.string.gender_hint),
      onValueChange = { gender = it }
    )
    RegistrationField(
      label = stringResource(R.string.address_label),
      value = address,
      placeholder = stringResource(R.string.address_hint),
      onValueChange = { address = it }
    )
  }
}

@Composable
fun GuardianInfoScreen(
  stepLabel: String,
  onContinue: (String, Int) -> Unit,
  onClose: () -> Unit,
) {
  val viewModel: PhoneInputViewModel = koinViewModel()
  val requestOtpState = viewModel.requestOtpState.collectAsState()
  var fullName by rememberSaveable { mutableStateOf("") }
  var birthDate by rememberSaveable { mutableStateOf("") }
  var phone by rememberSaveable { mutableStateOf("") }
  var address by rememberSaveable { mutableStateOf("") }
  val canContinue = fullName.isNotBlank() &&
    birthDate.isNotBlank() &&
    phone.isNotBlank() &&
    address.isNotBlank()

  when (val state = requestOtpState.value) {
    is RequestOtpState.Success -> {
      viewModel.dispatchUiEvent(PhoneInputIntent.ResetState)
      onContinue(state.phone, state.timeToRetry)
    }

    else -> Unit
  }

  MbAuthFrameContainer(
    buttonLabel = stringResource(R.string.receive_otp_code),
    buttonEnabledState = canContinue,
    isLoading = requestOtpState.value is RequestOtpState.Loading,
    onButtonClick = {
      viewModel.dispatchUiEvent(
        PhoneInputIntent.RequestOtp(
          phone = phone,
          otpType = MbAuthParams.OTP_TYPE_PARAM_REGISTRATION
        )
      )
    },
    onCloseButtonClick = onClose,
  ) {
    RegistrationTitle(
      stepLabel = stepLabel,
      title = stringResource(R.string.guardian_info_title)
    )
    RegistrationField(
      label = stringResource(R.string.guardian_full_name_label),
      value = fullName,
      placeholder = stringResource(R.string.full_name_hint),
      onValueChange = { fullName = it }
    )
    RegistrationField(
      label = stringResource(R.string.guardian_birth_date_label),
      value = birthDate,
      placeholder = stringResource(R.string.birth_date_hint),
      onValueChange = { birthDate = it }
    )
    RegistrationField(
      label = stringResource(R.string.guardian_phone_label),
      value = phone,
      placeholder = stringResource(R.string.phone_number_hint),
      onValueChange = { value ->
        if (value.length <= 10 && value.all { it.isDigit() }) {
          phone = value
        }
      }
    )
    RegistrationField(
      label = stringResource(R.string.address_label),
      value = address,
      placeholder = stringResource(R.string.address_hint),
      onValueChange = { address = it }
    )
  }
}

@Composable
private fun RegistrationTitle(
  stepLabel: String,
  title: String,
) {
  Text(
    text = stepLabel,
    color = colorResource(R.color.gray_text_color),
    fontFamily = CustomFont.fsClanPro,
    fontSize = 15.sp,
  )
  Text(
    text = title.uppercase(),
    color = colorResource(R.color.brown),
    fontFamily = CustomFont.fsClanPro,
    fontSize = 16.sp,
    modifier = Modifier.padding(top = 4.dp)
  )
}

@Composable
private fun RegistrationField(
  label: String,
  value: String,
  placeholder: String,
  onValueChange: (String) -> Unit,
) {
  BasicText(
    text = label,
    style = TextStyle(
      color = colorResource(R.color.black),
      fontFamily = CustomFont.fzPoppinsFont,
      fontSize = 12.sp,
    ),
    modifier = Modifier
      .fillMaxWidth()
      .padding(top = 4.dp)
  )
  TextInputField(
    textFieldValue = value,
    onValueChange = onValueChange,
    placeholder = placeholder,
  )
}
