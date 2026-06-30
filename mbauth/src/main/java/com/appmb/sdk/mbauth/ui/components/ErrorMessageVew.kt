package com.appmb.sdk.mbauth.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont

@Composable
fun ErrorMessageView(
  text: String,
  modifier: Modifier = Modifier,
) {
  val textColor = colorResource(R.color.red_error)
  val iconResId = R.drawable.ic_error

  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = modifier
      .fillMaxWidth()
      .padding(vertical = 2.dp),
  ) {
    Image(
      painterResource(iconResId),
      contentDescription = null,
      contentScale = ContentScale.FillBounds,
      modifier = Modifier
        .size(14.dp)
        .padding(0.dp)
    )
    Spacer(
      modifier = Modifier.width(4.dp)
    )
    BasicText(
      text = text,
      style = TextStyle(
        lineHeight = 11.sp,
        color = textColor,
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 10.sp,
        fontWeight = FontWeight.Medium
      )
    )
  }
}
