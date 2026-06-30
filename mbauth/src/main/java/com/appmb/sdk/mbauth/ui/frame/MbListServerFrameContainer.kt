package com.appmb.sdk.mbauth.ui.frame

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.R
import com.appmb.sdk.mbcoreui.Toast
import com.appmb.sdk.mbcoreui.ToastHostState
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.R as CoreUiR

@Composable
fun MbListServersFrameContainer(
  buttonEnabledState: Boolean = true,
  onButtonClick: (() -> Unit)? = null,
  onClose: (() -> Unit)? = null,
  isEnableClose: Boolean = false,
  isLoading: Boolean = false,
  toastHostState: ToastHostState? = null,
  content: @Composable BoxScope.() -> Unit = {},
) {
  val configuration = LocalConfiguration.current
  val screenWidthDp = configuration.screenWidthDp.dp
  val screenHeightDp = configuration.screenHeightDp.dp
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  var isTablet = false
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Color.Black.copy(alpha = 0.5f)
      ),
    contentAlignment = Alignment.Center
  ) {
    val modifier = if (isLandscape) {
      if (screenHeightDp < 600.dp) {
        Modifier
          .fillMaxHeight(0.9f)
      } else {
        isTablet = true
        Modifier
          .fillMaxHeight(0.7f)
      }.aspectRatio(1f)
    } else {
      if (screenWidthDp < 600.dp) {
        Modifier
          .fillMaxWidth()
      } else {
        isTablet = true
        Modifier
          .fillMaxWidth(0.5f)
      }.aspectRatio(0.8f)
    }
    // Outer Box which contains all component
    Box(
      modifier = modifier,
      contentAlignment = Alignment.Center
    ) {
      // Box contains content view
      BoxWithConstraints(
        modifier = Modifier
          .fillMaxWidth()
          .fillMaxHeight()
          .align(Alignment.Center)
          .paint(
            painter = painterResource(id = R.drawable.frame_background_server_list),
            contentScale = ContentScale.FillBounds
          ),
      ) {
        val boxHeight = constraints.maxHeight
        val headerTitleOffsetY = if (isTablet) 0.045f else 0.024f
        val bodyOffsetY = if (isTablet) 0.1f else 0.06f
        BasicText(
          text = stringResource(CoreUiR.string.choose_server).uppercase(),
          style = TextStyle(
            color = colorResource(CoreUiR.color.brown),
            fontFamily = CustomFont.fsClanPro,
            fontSize = 16.sp,
            fontWeight = FontWeight(1000)
          ),
          modifier = Modifier
            .align(Alignment.TopCenter)
            .offset(y = (headerTitleOffsetY * boxHeight).dp)
            .padding(start = 8.dp)
        )
        Box(
          modifier = Modifier
            .offset(y = (bodyOffsetY * boxHeight).dp)
            .fillMaxHeight(0.7f)
            .fillMaxWidth(0.8f)
            .align(Alignment.TopCenter),
          contentAlignment = Alignment.TopCenter,
          content = content,
        )
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier
              .size(24.dp)
              .align(Alignment.Center)
          )
        }
      }
      Box(
        modifier = Modifier
          .width(124.dp)
          .height(56.dp)
          .align(Alignment.BottomCenter)
          .offset(y = 16.dp)
          .clickable { onButtonClick?.invoke() },
        contentAlignment = Alignment.Center
      ) {
        // Image background
        val buttonBackgroundRes =
          if (buttonEnabledState) CoreUiR.drawable.button_enabled else CoreUiR.drawable.button_disabled
        Image(
          painter = painterResource(id = buttonBackgroundRes),
          contentDescription = "Button Background",
          modifier = Modifier.fillMaxSize()
        )
        Button(
          onClick = { onButtonClick?.invoke() },
          enabled = buttonEnabledState, // Controls whether the button is clickable
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
          ),
        ) {
          BasicText(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(CoreUiR.string.access_game_button_label),
            style = TextStyle(
              color = colorResource(CoreUiR.color.white),
              fontFamily = CustomFont.fsClanPro,
              fontSize = 14.sp,
              fontWeight = FontWeight(1000)
            )
          )
        }
      }
      if (isEnableClose) {
        Image(
          painterResource(CoreUiR.drawable.ic_close),
          contentDescription = null,
          contentScale = ContentScale.FillBounds,
          modifier = Modifier
            .padding(end = 24.dp, top = 20.dp)
            .size(36.dp)
            .align(Alignment.TopEnd)
            .clickable {
              onClose?.invoke()
            }
        )
      }
      toastHostState?.let {
        Toast(
          modifier = Modifier
            .padding(horizontal = 16.dp)
            .windowInsetsPadding(WindowInsets.navigationBars)
            .padding(bottom = 16.dp)
            .align(Alignment.BottomCenter)
            .fillMaxWidth(if (isLandscape) 0.5f else 1f),
          toastState = toastHostState,
          position = Toast.Position.Bottom
        )
      }
    }
  }
}

@Preview(device = "id:small_phone")
@Composable
fun MbListServersFrameContainerPreview() {
  MaterialTheme {
    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      MbListServersFrameContainer(
        onButtonClick = {},
        isEnableClose = true,
      ) {
      }
    }
  }
}
