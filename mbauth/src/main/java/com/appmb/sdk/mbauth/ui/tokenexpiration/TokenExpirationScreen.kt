package com.appmb.sdk.mbauth.ui.tokenexpiration

import android.app.Activity
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
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
import com.appmb.sdk.mbcoreui.utils.ObserveAsEvents
import org.koin.androidx.compose.koinViewModel

@Composable
fun TokenExpirationScreen(
  goToLoginScreen: () -> Unit,
) {
  val viewModel: TokenExpirationViewModel = koinViewModel()
  val activity = LocalContext.current as? Activity
  val state = viewModel.state

//  state.value?.let { result ->
//    handleGoogleSignOut.invoke()
//    val intent = Intent().apply {
//      putExtra("authResult", result)
//    }
//
//    activity?.setResult(Activity.RESULT_OK, intent)
//    activity?.finish()
//  }

  ObserveAsEvents(flow = viewModel.events) { event ->
    when (event) {
      is TokenExpirationEvent.OpenLoginScreen -> {
        goToLoginScreen()
      }

      is TokenExpirationEvent.Error -> {

      }
    }
  }

  AlertFrameContainer(
    actionLabel = stringResource(R.string.confirm),
    buttonEnabledState = true,
    onAction = {
      viewModel.onAction(TokenExpirationAction.OnClickConfirmLogin)
    },
  ) {
    BasicText(
      text = stringResource(R.string.confirm_expiration).uppercase(),
      style = TextStyle(
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
        color = colorResource(R.color.brown)
      ),
    )
    BasicText(
      text = stringResource(R.string.confirm_expiration_message),
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
fun TokenExpirationScreenPreview() {
  TokenExpirationScreen({

  })
}