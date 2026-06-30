package com.appmb.sdk.mbauth.ui.components

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.TextUnitType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomCheckbox
import com.appmb.sdk.mbcoreui.common.CustomFont

@Composable
fun TermsAndConditionsText() {
  val context = LocalContext.current
  val fullText = "Tôi đồng ý với điều khoản và chính sách bảo mật"
  val dkText = "điều khoản"
  val dqText = "chính sách bảo mật"
  val link1 = "https://kksoft.vn/dieu-khoan"
  val link2 = "https://kksoft.vn/chinh-sach-bao-mat"

  val annotatedString = buildAnnotatedString {

      append("Tôi đồng ý với ")




    pushStringAnnotation(tag = "LINK1", annotation = link1)

    withStyle(style = SpanStyle(color = colorResource(R.color.blue_text_link_color), textDecoration = TextDecoration.Underline)) {
      append(dkText)
    }
    pop()

    append(" và ")

    pushStringAnnotation(tag = "LINK2", annotation = link2)
    withStyle(style = SpanStyle(color = colorResource(R.color.blue_text_link_color), textDecoration = TextDecoration.Underline)) {
      append(dqText)
    }
    pop()
  }

  ClickableText(
    text = annotatedString,
    style = TextStyle(
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = TextUnit(10f, TextUnitType.Sp),
        textAlign = TextAlign.Left,
        color = colorResource(R.color.dark_gray_title)
      ),
    onClick = { offset ->
      annotatedString.getStringAnnotations("LINK1", start = offset, end = offset)
        .firstOrNull()?.let {
          context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link1)))
        }
      annotatedString.getStringAnnotations("LINK2", start = offset, end = offset)
        .firstOrNull()?.let {
          context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse(link2)))
        }
    }
  )
}


@Preview
@Composable
fun TermsAndConditionsTextPreview() {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .fillMaxWidth()
//      .padding(top = 4.dp)
  ) {
    CustomCheckbox(
      checked = true,
      onCheckedChange = {
//        termsChecked = it
//        authViewModel.onAcceptTermsChange(it)
      },
      modifier = Modifier.size(16.dp)
    )
    Spacer(
      modifier = Modifier.width(width = 4.dp)
    )
    TermsAndConditionsText()
  }
}