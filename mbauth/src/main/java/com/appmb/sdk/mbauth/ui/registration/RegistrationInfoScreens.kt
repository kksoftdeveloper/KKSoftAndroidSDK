package com.appmb.sdk.mbauth.ui.registration

import androidx.compose.foundation.clickable
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.Alignment
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.toSize
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
  var birthDateError by rememberSaveable { mutableStateOf<String?>(null) }
  var gender by rememberSaveable { mutableStateOf("") }
  var address by rememberSaveable { mutableStateOf("") }
  val invalidDayError = stringResource(R.string.birth_date_invalid_day)
  val invalidMonthError = stringResource(R.string.birth_date_invalid_month)
  val canContinue = fullName.isNotBlank() &&
    birthDate.isCompleteBirthDate() &&
    birthDateError == null

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
    BirthDateField(
      label = stringResource(R.string.birth_date_label),
      value = birthDate,
      error = birthDateError,
      onValueChange = { value ->
        val result = formatBirthDateInput(
          previousValue = birthDate,
          newValue = value,
          invalidDayError = invalidDayError,
          invalidMonthError = invalidMonthError,
        )
        birthDate = result.value
        birthDateError = result.error
      }
    )
    GenderDropdownField(
      label = stringResource(R.string.gender_label),
      value = gender,
      placeholder = stringResource(R.string.gender_hint),
      options = listOf(
        stringResource(R.string.gender_male),
        stringResource(R.string.gender_female),
        stringResource(R.string.gender_other),
      ),
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
  var birthDateError by rememberSaveable { mutableStateOf<String?>(null) }
  var phone by rememberSaveable { mutableStateOf("") }
  var address by rememberSaveable { mutableStateOf("") }
  val invalidDayError = stringResource(R.string.birth_date_invalid_day)
  val invalidMonthError = stringResource(R.string.birth_date_invalid_month)
  val invalidPhoneError = stringResource(R.string.phone_number_invalid)
  val phoneError = if (phone.isNotBlank() && !phone.isValidVietnamPhoneNumber()) invalidPhoneError else null
  val canContinue = fullName.isNotBlank() &&
    birthDate.isCompleteBirthDate() &&
    birthDateError == null &&
    phone.isValidVietnamPhoneNumber()

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
    BirthDateField(
      label = stringResource(R.string.guardian_birth_date_label),
      value = birthDate,
      error = birthDateError,
      onValueChange = { value ->
        val result = formatBirthDateInput(
          previousValue = birthDate,
          newValue = value,
          invalidDayError = invalidDayError,
          invalidMonthError = invalidMonthError,
        )
        birthDate = result.value
        birthDateError = result.error
      }
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
    if (!phoneError.isNullOrBlank()) {
      BasicText(
        text = phoneError,
        style = TextStyle(
          color = MaterialTheme.colorScheme.error,
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 10.sp,
        ),
        modifier = Modifier
          .fillMaxWidth()
          .padding(top = 2.dp)
      )
    }
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

@Composable
private fun GenderDropdownField(
  label: String,
  value: String,
  placeholder: String,
  options: List<String>,
  onValueChange: (String) -> Unit,
) {
  var expanded by rememberSaveable { mutableStateOf(false) }
  var fieldSize by remember { mutableStateOf(Size.Zero) }
  val density = LocalDensity.current

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
  Box(
    modifier = Modifier
      .fillMaxWidth()
      .onGloballyPositioned { coordinates ->
        fieldSize = coordinates.size.toSize()
      }
      .clickable { expanded = true }
  ) {
    Box(
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 4.dp)
        .heightIn(min = 32.dp)
        .background(
          color = colorResource(R.color.input_background_color),
          shape = RoundedCornerShape(8.dp)
        )
        .padding(horizontal = 16.dp, vertical = 10.dp),
      contentAlignment = Alignment.CenterStart,
    ) {
      BasicText(
        text = value.ifBlank { placeholder },
        style = TextStyle(
          color = if (value.isBlank()) Color.Gray else Color.White,
          fontSize = 12.sp,
          fontFamily = CustomFont.fzPoppinsFont,
        )
      )
    }
    Icon(
      imageVector = Icons.Default.KeyboardArrowDown,
      contentDescription = null,
      tint = colorResource(R.color.white),
      modifier = Modifier
        .align(Alignment.CenterEnd)
        .padding(end = 12.dp, top = 4.dp)
    )
    DropdownMenu(
      expanded = expanded,
      onDismissRequest = { expanded = false },
      offset = DpOffset(0.dp, 0.dp),
      modifier = Modifier.width(with(density) { fieldSize.width.toDp() })
    ) {
      options.forEach { option ->
        DropdownMenuItem(
          text = {
            Text(
              text = option,
              fontFamily = CustomFont.fzPoppinsFont,
              fontSize = 12.sp,
            )
          },
          onClick = {
            onValueChange(option)
            expanded = false
          }
        )
      }
    }
  }
}

@Composable
private fun BirthDateField(
  label: String,
  value: String,
  error: String?,
  onValueChange: (String) -> Unit,
) {
  RegistrationField(
    label = label,
    value = value,
    placeholder = stringResource(R.string.birth_date_hint),
    onValueChange = onValueChange
  )
  if (!error.isNullOrBlank()) {
    BasicText(
      text = error,
      style = TextStyle(
        color = MaterialTheme.colorScheme.error,
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 10.sp,
      ),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 2.dp)
    )
  }
}

private data class BirthDateInputResult(
  val value: String,
  val error: String? = null,
)

private fun formatBirthDateInput(
  previousValue: String,
  newValue: String,
  invalidDayError: String,
  invalidMonthError: String,
): BirthDateInputResult {
  if (newValue.length < previousValue.length) {
    val sanitizedValue = newValue.filter { it.isDigit() || it == '/' }.take(10)
    return BirthDateInputResult(
      value = sanitizedValue,
      error = validateBirthDate(sanitizedValue, invalidDayError, invalidMonthError)
    )
  }

  if (newValue.any { !it.isDigit() && it != '/' }) {
    return BirthDateInputResult(previousValue)
  }

  val digits = newValue.filter { it.isDigit() }.take(8)
  val validationError = validateBirthDateDigits(
    digits = digits,
    invalidDayError = invalidDayError,
    invalidMonthError = invalidMonthError
  )
  if (validationError != null) {
    return BirthDateInputResult(previousValue, validationError)
  }

  return BirthDateInputResult(formatBirthDateDigits(digits))
}

private fun validateBirthDate(value: String, invalidDayError: String, invalidMonthError: String): String? {
  val digits = value.filter { it.isDigit() }
  return validateBirthDateDigits(digits, invalidDayError, invalidMonthError)
}

private fun validateBirthDateDigits(
  digits: String,
  invalidDayError: String,
  invalidMonthError: String,
): String? {
  if (digits.length == 1 && (digits[0].digitToIntOrNull() ?: 0) > 3) {
    return invalidDayError
  }

  val day = if (digits.length >= 2) digits.substring(0, 2).toIntOrNull() else null
  if (day != null && day !in 1..31) {
    return invalidDayError
  }

  if (digits.length == 3 && (digits[2].digitToIntOrNull() ?: 0) > 1) {
    return invalidMonthError
  }

  val month = if (digits.length >= 4) digits.substring(2, 4).toIntOrNull() else null
  if (month != null && month !in 1..12) {
    return invalidMonthError
  }

  if (day != null && month != null) {
    val year = if (digits.length >= 8) digits.substring(4, 8).toIntOrNull() else null
    val maxDay = maxDayOfMonth(month, year)
    if (day > maxDay) {
      return invalidDayError
    }
  }

  return null
}

private fun formatBirthDateDigits(digits: String): String =
  buildString {
    digits.forEachIndexed { index, char ->
      if (index == 2 || index == 4) {
        append('/')
      }
      append(char)
    }
    if (digits.length == 2 || digits.length == 4) {
      append('/')
    }
  }

private fun String.isCompleteBirthDate(): Boolean =
  length == 10 && validateBirthDate(this, invalidDayError = "", invalidMonthError = "") == null

private fun maxDayOfMonth(month: Int, year: Int?): Int =
  when (month) {
    2 -> if (year == null || isLeapYear(year)) 29 else 28
    4, 6, 9, 11 -> 30
    else -> 31
  }

private fun isLeapYear(year: Int): Boolean =
  year % 400 == 0 || (year % 4 == 0 && year % 100 != 0)

private fun String.isValidVietnamPhoneNumber(): Boolean =
  Regex("""^(?:\+?84|0)([35789])\d{8}$""").matches(this)
