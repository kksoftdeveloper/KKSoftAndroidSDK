package com.appmb.sdk.mbauth.ui.otp.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.R


@Composable
fun OtpCountdownTimer(
  modifier: Modifier = Modifier,
  timeLeft: Int = 60, // in seconds
) {
  if (timeLeft > 0) {
    Row(
      modifier = modifier.fillMaxWidth(),
      horizontalArrangement = Arrangement.Center,
      verticalAlignment = Alignment.CenterVertically
    ) {
      BasicText(
        text = stringResource(R.string.otp_resend_timer),
        style = TextStyle(
          color = colorResource(R.color.gray_text_color),
          fontSize = 12.sp,
          fontWeight = FontWeight(400),
        ),
      )
      BasicText(
        text = "${timeLeft.toString()}s",
        style = TextStyle(
          color = colorResource(R.color.gray_text_color),
          fontSize = 12.sp,
          fontWeight = FontWeight(700),
        ),
        modifier = Modifier.padding(start = 4.dp)
      )
    }
  }
  Spacer(modifier = Modifier)
}