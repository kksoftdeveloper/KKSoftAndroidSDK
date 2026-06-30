package com.appmb.sdk.mbauth.ui.logout

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.AlertFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import org.koin.androidx.compose.koinViewModel

@Composable
fun LogoutView(
  onClose: () -> Unit,
  handleGoogleSignOut: () -> Unit
) {
  val logoutViewModel: LogoutViewModel = koinViewModel()
  val state = logoutViewModel.logoutResult.collectAsState()
  val activity = LocalContext.current as? Activity

  state.value?.let { result ->
    handleGoogleSignOut.invoke()
    val intent = Intent().apply {
      putExtra("authResult", result)
    }

    activity?.setResult(Activity.RESULT_OK, intent)
    activity?.finish()
  }
  AlertFrameContainer(
    actionLabel = stringResource(R.string.common_accept),
    buttonEnabledState = true,
    onClose = onClose,
    onAction = {
      logoutViewModel.logout()
    },
  ) {
    BasicText(
      text = stringResource(R.string.confirm_logout).uppercase(),
      style = TextStyle(
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
        color = colorResource(R.color.brown)
      ),
    )
    BasicText(
      text = stringResource(R.string.confirm_logout_message),
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

@Preview
@Composable
fun previewLogoutView() {
  LogoutView(
    onClose = {},
    handleGoogleSignOut = {}
  )
}