package com.appmb.sdk.mbauth.ui.server.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcore.data.dto.response.MbServer
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbauth.R as AuthR


@Composable
fun ServerItemView(
  server: MbServer,
  isSelected: Boolean,
  onClick: () -> Unit,
) {
  val serverStatusInfo = remember { getItemStatusInfo(server.status.orEmpty()) }
  val selectionColor = colorResource(serverStatusInfo.textColorStatusResId)
  val shape = remember { RoundedCornerShape(16.dp) }
  Surface(
    shape = shape,
    color = colorResource(R.color.background_server_item),
    modifier = Modifier
      .fillMaxWidth()
      .border(
        width = 1.dp,
        color = Color.Black.copy(alpha = 0.1f),
        shape = shape
      )
      .drawBehind {
        if (isSelected) {
          val strokeWidth = 3.dp.toPx()
          drawRoundRect(
            color = selectionColor,
            size = size,
            cornerRadius = CornerRadius(16.dp.toPx(), 16.dp.toPx()),
            style = Stroke(width = strokeWidth)
          )
        }
      }
      .clip(shape)
      .clickable(onClick = onClick)
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 12.dp)
        .background(shape = shape, color = Color.Transparent),
      verticalAlignment = Alignment.CenterVertically
    ) {
      Image(
        painter = painterResource(id = serverStatusInfo.iconResId),
        contentDescription = null,
        modifier = Modifier.size(16.dp)
      )
      BasicText(
        text = server.serverName.orEmpty(),
        style = TextStyle(
          fontWeight = FontWeight(500),
          fontFamily = CustomFont.fzPoppinsSemiBoldFont,
          fontSize = 13.sp,
          color = colorResource(serverStatusInfo.textColorNameResId)
        ),
        modifier = Modifier
          .weight(1f)
          .padding(start = 8.dp)
      )
      BasicText(
        text = stringResource(serverStatusInfo.statusResId),
        style = TextStyle(
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 11.sp,
          fontWeight = FontWeight(400),
          color = colorResource(serverStatusInfo.textColorStatusResId),
        ),
        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
      )
    }
  }
}


fun getItemStatusInfo(serverStatus: String): ServerStatusInfo {
  return when (serverStatus) {
    MbServer.SERVER_GOOD -> ServerStatusInfo(
      iconResId = AuthR.drawable.ic_status_good_2,
      textColorNameResId = R.color.brown,
      statusResId = R.string.status_good,
      textColorStatusResId = R.color.color_server_good
    )

    MbServer.SERVER_NEW -> ServerStatusInfo(
      iconResId = AuthR.drawable.ic_status_new_2,
      textColorNameResId = R.color.brown,
      statusResId = R.string.status_new,
      textColorStatusResId = R.color.color_server_new
    )

    MbServer.SERVER_FULL -> ServerStatusInfo(
      iconResId = AuthR.drawable.ic_status_full,
      textColorNameResId = R.color.brown,
      statusResId = R.string.status_full,
      textColorStatusResId = R.color.color_server_full
    )

    MbServer.SERVER_MAINTENANCE -> ServerStatusInfo(
      iconResId = AuthR.drawable.ic_status_down,
      textColorNameResId = R.color.gray_text_disabled,
      statusResId = R.string.status_good,
      textColorStatusResId = R.color.color_server_maintenance
    )

    else -> ServerStatusInfo(
      iconResId = AuthR.drawable.ic_status_down,
      textColorNameResId = R.color.gray_text_disabled,
      statusResId = R.string.status_offline,
      textColorStatusResId = R.color.color_server_offline
    )
  }
}

@Composable
@Preview(showBackground = true)
private fun ServerItemViewPreview() {
  ServerItemView(
    server = MbServer(
      "", "Server",
      status = MbServer.SERVER_GOOD
    ), isSelected = true
  ) { }
}