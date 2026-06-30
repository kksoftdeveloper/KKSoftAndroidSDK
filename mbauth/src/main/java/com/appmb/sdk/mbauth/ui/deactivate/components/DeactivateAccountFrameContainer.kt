package com.appmb.sdk.mbauth.ui.deactivate.components

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicText
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomFont


@Composable
fun DeactivateAccountFrameContainer(
  onDismissListener: () -> Unit,
  buttonEnabledState: Boolean = true,
  onButtonClick: (() -> Unit)? = null,
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
          .fillMaxWidth(0.7f)
      }.aspectRatio(0.9f)
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
            painter = painterResource(id = com.appmb.sdk.mbauth.R.drawable.frame_background_server_list),
            contentScale = ContentScale.FillBounds
          ),
      ) {
        val boxHeight = constraints.maxHeight
        val headerTitleOffsetY = if (isTablet) 0.035f else 0.025f
        val bodyOffsetY = if (isTablet) 0.1f else 0.06f
        BasicText(
          text = stringResource(R.string.account_deletion_title).uppercase(),
          style = TextStyle(
            color = colorResource(R.color.brown),
            fontFamily = CustomFont.fsClanPro,
            fontSize = 14.sp,
            fontWeight = FontWeight(1000),
            textAlign = TextAlign.Center
          ),
          modifier = Modifier
            .fillMaxWidth()
            .align(Alignment.TopCenter)
            .offset(y = (headerTitleOffsetY * boxHeight).dp)
        )
        Box(
          modifier = Modifier
            .offset(y = (bodyOffsetY * boxHeight).dp)
            .fillMaxHeight(0.65f)
            .fillMaxWidth(0.8f)
            .align(Alignment.TopCenter)
//            .background(Color.Red)
          ,
          contentAlignment = Alignment.TopCenter,
          content = content,
        )
      }
      Box(
        modifier = Modifier
          .width(124.dp)
          .height(52.dp)
          .align(Alignment.BottomCenter)
          .offset(y = 10.dp)
          .clickable { onButtonClick?.invoke() },
        contentAlignment = Alignment.Center
      ) {
        val buttonBackgroundRes = if (buttonEnabledState) R.drawable.button_enabled else R.drawable.button_disabled
        Image(
          painter = painterResource(id = buttonBackgroundRes),
          contentDescription = "Button Background",
          modifier = Modifier.fillMaxSize()
        )
        Button(
          onClick = { onButtonClick?.invoke() },
          enabled = buttonEnabledState,
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
          ),
          modifier = Modifier.align(Alignment.Center),
        ) {
          BasicText(
            modifier = Modifier.padding(bottom = 8.dp),
            text = stringResource(R.string.common_accept),
            style = TextStyle(
              color = colorResource(R.color.white),
              fontFamily = CustomFont.fsClanPro,
              fontSize = 14.sp,
              fontWeight = FontWeight(1000)
            )
          )
        }
      }

      Image(
        painterResource(R.drawable.ic_close),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
          .padding(horizontal = 24.dp, vertical = 16.dp)
          .size(40.dp)
          .align(Alignment.TopEnd)
          .clickable {
            onDismissListener.invoke()
          }
      )
    }
  }
}

@Preview
@Composable
fun previewDeactivateContainer(
  onDismissListener: () -> Unit = {},
  onButtonClick: (() -> Unit)? = null,
) {
  DeactivateAccountFrameContainer(
    onDismissListener = onDismissListener,
    onButtonClick = onButtonClick
  ) {
    // Content can be added here for preview
    BasicText(
      text = "Content goes here",
      style = TextStyle(
        color = Color.White,
        fontFamily = CustomFont.fsClanPro,
        fontSize = 14.sp
      )
    )
  }
}