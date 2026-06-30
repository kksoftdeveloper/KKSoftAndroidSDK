package com.appmb.sdk.mbauth.ui.deactivate

import android.app.Activity
import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.ui.deactivate.components.DeactivateAccountFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomCheckbox
import com.appmb.sdk.mbcoreui.common.CustomFont
import org.koin.androidx.compose.koinViewModel

@Composable
fun DeactivateAccountView(
  handleGoogleSignOut: () -> Unit,
) {
  val deactivateAccountViewModel: DeactivateAccountViewModel = koinViewModel()
  val state = deactivateAccountViewModel.deactivateAccountResult.collectAsState()
  val activity = LocalContext.current as? Activity

  var isAcceptedConditionChecked by rememberSaveable { mutableStateOf(false) }

  state.value?.let { result ->
    handleGoogleSignOut.invoke()
    val intent = Intent().apply {
      putExtra("authResult", result)
    }

    activity?.setResult(Activity.RESULT_OK, intent)
    activity?.finish()
  }

  DeactivateAccountFrameContainer(
    onDismissListener = {
      activity?.finish()
    },
    buttonEnabledState = isAcceptedConditionChecked,
    onButtonClick = {
      deactivateAccountViewModel.deactivate()
    }
  ) {
    LazyColumn(
      modifier = Modifier
        .padding(horizontal = 24.dp),
      verticalArrangement = Arrangement.spacedBy(8.dp),
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_guide),
        )
      }
      // Topic 1
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_notice),
          isTitle = true
        )
      }
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_warning_1),
        )
      }
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_warning_2),
        )
      }
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_warning_3),
        )
      }
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_warning_4),
        )
      }
      // Topic 2
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_cancel_notice),
          isTitle = true
        )
      }
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_cancel_info_1),
        )
      }
      item {
        MessageTextView(
          text = stringResource(R.string.account_deletion_cancel_info_2),
        )
      }
      // Accept condition deactivate
      item {
        Row(
          verticalAlignment = Alignment.CenterVertically,
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        ) {
          CustomCheckbox(
            checked = isAcceptedConditionChecked,
            onCheckedChange = {
              isAcceptedConditionChecked = it
            },
            modifier = Modifier.size(16.dp)
          )
          MessageTextView(
            text = stringResource(R.string.account_deletion_agreement),
            modifier = Modifier.padding(start = 8.dp)
          )
        }
      }
    }
  }
}

@Composable
private fun MessageTextView(
  text: String,
  modifier: Modifier = Modifier,
  isTitle: Boolean = false,
) {
  var textColor = colorResource(R.color.dark_gray_title)
  val textAlign = TextAlign.Start
  if (isTitle) {
    textColor = colorResource(R.color.brown)
  }
  BasicText(
    text = text,
    style = TextStyle(
      color = textColor,
      fontFamily = CustomFont.fzPoppinsFont,
      fontSize = 12.sp,
      textAlign = textAlign
    ),
    modifier = modifier
  )
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=portrait")
@Composable
fun DeactivateContainerPreview() {
  MaterialTheme {
    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      DeactivateAccountFrameContainer(
        onDismissListener = {},
        onButtonClick = {},
      ) {
      }
    }
  }
}