package com.appmb.sdk.mbauth.ui.userblocked

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.ClickableText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalUriHandler
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.AlertFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont

@Composable
fun UserBlockedScreen(
  onClose: () -> Unit
) {
  val dkText = "điều khoản"
  val dqText = "chính sách bảo mật"

  val fanpage = "fan page"
  val CSKH = "CSKH"
  val link1 = "https://kksoft.vn/dieu-khoan"
  val link2 = "https://kksoft.vn/chinh-sach-bao-mat"
  val link3 = "https://www.facebook.com/profile.php?id=61574162151534"
  val link4 = "tel:+84398686854"

  val uriHandler = LocalUriHandler.current
  val context = LocalContext.current

  val annotatedString = buildAnnotatedString {
    append("Hiện tại, tài khoản của bạn bị khoá vì vi phạm ")

    pushStringAnnotation(tag = "LINK", annotation = link1)

    withStyle(
      style = SpanStyle(
        color = colorResource(R.color.blue_text_link_color),
        textDecoration = TextDecoration.Underline
      )
    ) {
      append(dkText)
    }
    pop()

    append(" và ")

    pushStringAnnotation(tag = "LINK", annotation = link2)
    withStyle(
      style = SpanStyle(
        color = colorResource(R.color.blue_text_link_color),
        textDecoration = TextDecoration.Underline
      )
    ) {
      append(dqText)
    }
    pop()

    append(". Quý đại hiệp vui lòng thông tin chi tiết đến ")

    pushStringAnnotation(tag = "LINK", annotation = link3)
    withStyle(
      style = SpanStyle(
        color = colorResource(R.color.blue_text_link_color),
        textDecoration = TextDecoration.Underline
      )
    ) {
      append(fanpage)
    }
    pop()
    append(" hoặc liên hệ ")

    pushStringAnnotation(tag = "LINK", annotation = link4)
    withStyle(
      style = SpanStyle(
        color = colorResource(R.color.blue_text_link_color),
        textDecoration = TextDecoration.Underline
      )
    ) {
      append(CSKH)
    }
    append(".")
  }

  AlertFrameContainer(
    onClose = {
      onClose()
    }
  ) {
    BasicText(
      text = stringResource(R.string.user_blocked_title).uppercase(),
      style = TextStyle(
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
        color = colorResource(R.color.brown)
      ),
    )
    ClickableText(
      text = annotatedString,
      style = TextStyle(fontFamily = CustomFont.fzPoppinsFont, fontSize = 12.sp, textAlign = TextAlign.Center),
      modifier = Modifier.padding(top = 16.dp).padding(horizontal = 24.dp),
      onClick = { offset ->
        annotatedString.getStringAnnotations(tag = "LINK", start = offset, end = offset)
          .firstOrNull()?.let { ann ->
            val uri = ann.item
            if (uri.startsWith("tel:")) {

              context.startActivity(Intent(Intent.ACTION_DIAL, Uri.parse(uri)))
            } else {
              uriHandler.openUri(uri)
            }
          }
      }
    )
  }
}