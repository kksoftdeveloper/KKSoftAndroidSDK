package com.appmb.sdk.mbauth.ui.server.components

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.AlertFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont

@Composable
fun MaintenancePopupView(
  modifier: Modifier = Modifier,
  onClose: (() -> Unit),
  onAction: (() -> Unit)? = null,
) {
  AlertFrameContainer(
    actionLabel = null,
    buttonEnabledState = true,
    onClose = null,
    onAction = null
  ) {
    BasicText(
      text = stringResource(R.string.maintenance).uppercase(),
      style = TextStyle(
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
        color = colorResource(R.color.brown)
      ),
    )

    val rawMessage = stringResource(R.string.maintenance_message)
    val highlightLabels = listOf(
      stringResource(R.string.fanpage_label),
      stringResource(R.string.customer_service_label)
    )

    // Build a spannable string with the highlight labels
    val styledMessage = buildAnnotatedString {
      append(rawMessage)
      highlightLabels.forEach { word ->
        val regex = Regex("\\b$word\\b")
        regex.findAll(rawMessage).forEach { matchResult ->
          addStyle(
            style = SpanStyle(
              color = colorResource(R.color.brown),
              fontFamily = CustomFont.fzPoppinsBoldFont,
              fontWeight = FontWeight.ExtraBold,
            ),
            start = matchResult.range.first,
            end = matchResult.range.last + 1
          )
        }
      }
    }
    BasicText(
      text = styledMessage,
      style = TextStyle(
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
        fontWeight = FontWeight(400),
        color = colorResource(R.color.brown),
        textAlign = TextAlign.Center
      ),
      modifier = Modifier
        .padding(top = 16.dp)
        .padding(horizontal = 24.dp)
    )
  }
}

@Composable
@Preview
fun ServerMaintenancePopupViewPreview() {
  MaintenancePopupView(
    onClose = {

    },
    onAction = null,
    modifier = Modifier.padding(16.dp)
  )
}