package com.appmb.sdk.mbpayment.ui.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbpayment.R


@OptIn(ExperimentalTextApi::class)
@Composable
fun OutlinedText(
  text: String,
  modifier: Modifier = Modifier,
  fontSize: TextUnit = 20.sp,
  fillColor: Color = Color.White,
  strokeColor: Color = colorResource(R.color.outlined_text_border_color),
  strokeWidth: Float = 4f,
  fontFamily: FontFamily = CustomFont.dongleRegular,
) {
  Box(modifier = modifier) {
    // Stroke (outline) layer
    BasicText(
      text = text,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = TextStyle(
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = strokeColor,
        drawStyle = Stroke(width = strokeWidth),
        textAlign = TextAlign.Center,
        platformStyle = PlatformTextStyle(includeFontPadding = false),
      ),
    )

    // Fill layer
    BasicText(
      text = text,
      maxLines = 1,
      overflow = TextOverflow.Ellipsis,
      style = TextStyle(
        fontSize = fontSize,
        fontFamily = fontFamily,
        color = fillColor,
        textAlign = TextAlign.Center,
        platformStyle = PlatformTextStyle(includeFontPadding = false)
      )
    )
  }
}

@Preview
@Composable
fun OutlinedTextPreview() {
  OutlinedText(
    text = "1000",
  )
}