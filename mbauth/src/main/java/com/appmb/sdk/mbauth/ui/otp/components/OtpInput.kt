@file:OptIn(ExperimentalComposeUiApi::class)

package com.appmb.sdk.mbauth.ui.otp.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.LocalTextStyle
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.input.key.KeyEventType
import androidx.compose.ui.input.key.key
import androidx.compose.ui.input.key.onKeyEvent
import androidx.compose.ui.input.key.type
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.ui.otp.OtpState
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull


@Composable
fun OtpInput(
  otpValues: SnapshotStateList<String>,
  focusRequesters: List<FocusRequester>,
  enabled: Boolean,
  onOtpComplete: (List<String>) -> Unit,
  onOtpIncomplete: (List<String>) -> Unit,
  modifier: Modifier = Modifier,
  otpLength: Int = OtpState.OTP_LENGTH
) {
  LaunchedEffect(focusRequesters) {
    snapshotFlow { focusRequesters.firstOrNull() }
      .filterNotNull()
      .firstOrNull()
    delay(100)
  }
  Box(
    modifier = modifier.fillMaxWidth(),
    contentAlignment = Alignment.Center
  ) {
    Row(
      horizontalArrangement = Arrangement.spacedBy(8.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      otpValues.forEachIndexed { index, value ->
        Box(
          modifier = Modifier
            .weight(1f)
            .height(44.dp)
            .focusRequester(focusRequesters[index])
        ) {
          BasicTextField(
            value = value,
            enabled = enabled,
            onValueChange = { newValue ->
              if (newValue.all { it.isDigit() }) {
                if (newValue.length <= 1) {
                  otpValues[index] = newValue
                  if (newValue.isNotEmpty()) {
                    if (index < otpLength - 1) {
                      focusRequesters[index + 1].requestFocus()
                    }
                  }
                } else {
                  val givenString = otpValues[index]
                  val nextChar = newValue.find { it !in givenString }
                  if (nextChar != null) {
                    otpValues[index] = nextChar.toString()
                    if (index < otpLength - 1) {
                      focusRequesters[index + 1].requestFocus()
                    }
                  }
                }
                val finalOtp = otpValues.joinToString(separator = "")
                if (finalOtp.trim().length == otpLength) {
                  onOtpComplete(otpValues)
                } else {
                  onOtpIncomplete(otpValues)
                }
              }
            },
            keyboardOptions = KeyboardOptions.Default.copy(keyboardType = KeyboardType.Number),
            singleLine = true,
            textStyle = LocalTextStyle.current.copy(
              textAlign = TextAlign.Center,
              fontSize = 14.sp,
              fontFamily = CustomFont.fzPoppinsFont,
              fontWeight = FontWeight(600),
              color = Color.White
            ),
            cursorBrush = SolidColor(Color.White),
            modifier = Modifier
              .fillMaxSize()
              .focusRequester(focusRequesters[index])
              .onKeyEvent { keyEvent ->
                if (keyEvent.type == KeyEventType.KeyUp) {
                  if (keyEvent.key == Key.Backspace) {
                    if (otpValues[index].isEmpty() && index > 0) {
                      focusRequesters[index - 1].requestFocus()
                    } else {
                      otpValues[index] = ""
                    }
                  }
                  true
                } else {
                  false
                }
              },
            decorationBox = { innerTextField ->
              Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                  .fillMaxSize()
                  .background(
                    color = colorResource(R.color.input_background_color),
                    shape = RoundedCornerShape(8.dp)
                  )
              ) {
                innerTextField()
              }
            }
          )
        }
      }
    }
  }
}
