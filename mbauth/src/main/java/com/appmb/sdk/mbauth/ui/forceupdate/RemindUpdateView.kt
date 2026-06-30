package com.appmb.sdk.mbauth.ui.forceupdate

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcore.utils.VersionInfo
import com.appmb.sdk.mbcoreui.AlertFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont

@Composable
fun RemindUpdateView(
  onClose: () -> Unit
) {
  val context = LocalContext.current

  val forceUpdate = VersionInfo.isForceUpdate
  LaunchedEffect(Unit) {
    VersionInfo.alreadyDisplayRequireUpdatePopup = true
  }
  AlertFrameContainer(
    actionLabel = stringResource(R.string.update),
    buttonEnabledState = true,
    onClose = if (!forceUpdate) null else {
      onClose
    },
    onAction = {
      VersionInfo.openPlayStore(context)
    }
  ) {
    BasicText(
      text = stringResource(R.string.reminder_update_title).uppercase(),
      style = TextStyle(
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
        color = colorResource(R.color.brown)
      ),
    )
    BasicText(
      text = stringResource(R.string.reminder_update_mesage),
      style = TextStyle(
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
        color = colorResource(R.color.brown),
        textAlign = TextAlign.Center
      ),
      modifier = Modifier
        .padding(top = 16.dp)
        .padding(horizontal = 24.dp)
    )
  }
}