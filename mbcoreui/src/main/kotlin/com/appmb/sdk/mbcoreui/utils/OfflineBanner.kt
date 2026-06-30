package com.appmb.sdk.mbcoreui.utils

// OfflineBanner.kt
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.R

@Composable
fun OfflineBanner(visible: Boolean, modifier: Modifier = Modifier) {
  AnimatedVisibility(visible = visible, modifier = modifier) {
    Surface(tonalElevation = 4.dp, color = MaterialTheme.colorScheme.errorContainer) {
      Row(
        Modifier
          .fillMaxWidth()
          .padding(horizontal = 16.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          stringResource(id = R.string.offline_banner_message),
          color = MaterialTheme.colorScheme.onErrorContainer,
          style = TextStyle(
            fontSize = 12.sp,
            fontFamily = CustomFont.fzPoppinsSemiBoldFont,
            color = Color.White,
            platformStyle = PlatformTextStyle(includeFontPadding = false)
          )
        )
      }
    }
  }
}

@Composable
@Preview(showBackground = true)
private fun ServerItemViewPreview() {
  OfflineBanner(
    visible = true,
    modifier = Modifier.fillMaxWidth()
  )
}
