package com.appmb.sdk.mbauth.ui.linkaccount

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.model.LinkAccountType
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.model.localization
import com.appmb.sdk.mbcoreui.AlertFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.common.SocialButtonView
import com.appmb.sdk.mbcoreui.utils.ObserveAsEvents
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import org.koin.androidx.compose.koinViewModel

@Composable
fun LinkAccountScreen(
  activity: Activity,
  repeatableReminder: () -> Unit,
  navigateToRequestOtp: (String) -> Unit = {},
  handleGoogleSignIn: ((GoogleSignInAccount) -> Unit) -> Unit,
) {
  val viewModel: LinkAccountViewModel = koinViewModel()
  val context = LocalContext.current

  ObserveAsEvents(flow = viewModel.events) { event ->
    when (event) {
      is LinkAccountEvent.LinkSuccess -> {
        val intent = Intent().apply {
          putExtra("authResult", event.authResult)
        }
        activity.setResult(Activity.RESULT_OK, intent)
        activity.finish()
      }

      is LinkAccountEvent.Error -> {

      }
    }
  }


  AlertFrameContainer(
    buttonEnabledState = false,
    isLoading = viewModel.state.isLoading,
    onClose = repeatableReminder
  ) {
    BasicText(
      text = stringResource(R.string.link_account_title).uppercase(),
      style = TextStyle(
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
        color = colorResource(R.color.brown)
      ),
    )
    BasicText(
      text = stringResource(R.string.link_account_subtitle),
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
    // Social Buttons
    Row(
      horizontalArrangement = Arrangement.spacedBy(6.dp),
      modifier = Modifier
        .fillMaxWidth()
        .padding(top = 16.dp)
        .padding(horizontal = 16.dp),
    ) {
      // Google button
      SocialButtonView(
        text = stringResource(R.string.google),
        iconResId = R.drawable.ic_google,
        onClick = {
          handleGoogleSignIn.invoke { account ->
            viewModel.onAction(
              LinkAccountAction.LinkSocialAccount(
                context = context,
                linkAccountType = LinkAccountType.GOOGLE,
                googleSignInAccount = account
              )
            )
          }
        },
        modifier = Modifier.weight(1f)
      )

      // Facebook button
      SocialButtonView(
        text = stringResource(R.string.facebook),
        iconResId = R.drawable.ic_facebook,
        onClick = {
          viewModel.onAction(
            LinkAccountAction.LinkSocialAccount(
              context = context,
              linkAccountType = LinkAccountType.FACEBOOK
            )
          )
        },
        modifier = Modifier.weight(1f)
      )

      // Phone
      SocialButtonView(
        text = stringResource(R.string.link_account_with_phone),
        iconResId = R.drawable.ic_phone_white,
        onClick = {
          navigateToRequestOtp.invoke(MbAuthParams.OTP_TYPE_PARAM_LINK_PHONE_ACCOUNT)
        },
        modifier = Modifier.weight(1f)
      )
    }
    if (viewModel.state.error != null) {
      BasicText(
        modifier = Modifier
          .padding(top = 8.dp),
        text = viewModel.state.error?.localization(context) ?: stringResource(R.string.auth_unknown_error),
        style = TextStyle(
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 10.sp,
          color = colorResource(R.color.red_error),
          textAlign = TextAlign.Center
        ),
      )
    }
  }
}