package com.appmb.sdk.mbpayment.ui.components

import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.AlertFrameContainer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus

@Composable
fun PurchaseResultPopupView(
  purchaseStatus: PurchaseStatus,
  onActionButtonClick: () -> Unit,
) {
  val buttonLabel = when (purchaseStatus) {
    is PurchaseStatus.Success -> stringResource(R.string.common_close)
    PurchaseStatus.Error -> stringResource(R.string.common_retry)
    else -> stringResource(R.string.common_close)
  }
  val popupTitle = when (purchaseStatus) {
    is PurchaseStatus.Success -> stringResource(R.string.payment_purchase_title_successfully)
    PurchaseStatus.Error -> stringResource(R.string.payment_purchase_title_failed)
    else -> stringResource(R.string.payment_purchase_title_failed)
  }
  val popupMessage = when (purchaseStatus) {
    is PurchaseStatus.Success -> stringResource(
      R.string.payment_purchase_message_successfully,
      purchaseStatus.productName
    )

    PurchaseStatus.Error -> stringResource(R.string.payment_purchase_message_failed)
    PurchaseStatus.ProductUnavailableInGameServer -> stringResource(R.string.payment_purchase_unavailable_in_selected_server)
    else -> stringResource(R.string.payment_purchase_message_failed)
  }

  AlertFrameContainer(
    onClose = null,
    resourceBgId = R.drawable.alert_frame_body2,
    actionLabel = buttonLabel,
    buttonEnabledState = true,
//    forceClickButton = false,
    onAction = {
      onActionButtonClick.invoke()
    }
  ) {
    BasicText(
      text = popupTitle.uppercase(),
      style = TextStyle(
        fontFamily = CustomFont.fsClanPro,
        fontSize = 16.sp,
        color = colorResource(R.color.brown)
      ),
    )
    BasicText(
      text = popupMessage,
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