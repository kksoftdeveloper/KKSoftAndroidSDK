package com.appmb.sdk.mbcoreui

import android.content.res.Configuration
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.paint
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.common.CustomFont

@Composable
fun AlertFrameContainer(
  actionLabel: String? = null,
  resourceBgId: Int = R.drawable.alert_frame_body,
  buttonEnabledState: Boolean = true,
  isLoading: Boolean = false,
  toastHostState: ToastHostState? = null,
  onClose: (() -> Unit)? = null,
  onAction: (() -> Unit)? = null,
  content: @Composable ColumnScope.() -> Unit = {},
) {
  val configuration = LocalConfiguration.current
  val screenWidthDp = configuration.screenWidthDp.dp
  val screenHeightDp = configuration.screenHeightDp.dp
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  Box(
    modifier = Modifier
      .fillMaxSize()
      .background(
        Color.Black.copy(alpha = 0.5f)
      ),
  ) {
    val modifier = if (isLandscape) {
      if (screenHeightDp < 600.dp) {
        Modifier
          .fillMaxHeight(0.7f)
          .aspectRatio(1.4f)
      } else {
        Modifier
          .fillMaxHeight(0.6f)
          .aspectRatio(1.5f)
          .scale(0.6f)
      }
    } else {
      if (screenWidthDp < 600.dp) {
        Modifier
          .fillMaxWidth()
          .aspectRatio(1.4f)
      } else {
        Modifier
          .fillMaxWidth(0.5f)
          .aspectRatio(1.4f)
      }
    }
    // Outer Box which contains all component
    Box(
      modifier = modifier
        .align(Alignment.Center),
      contentAlignment = Alignment.Center
    ) {
      // Box contains content view
      Box(
        modifier = Modifier
//          .padding(top = 16.dp)
//          .padding(horizontal = 16.dp)
          .fillMaxWidth()
          .aspectRatio(1.7f)
          .align(Alignment.Center)
          .paint(
            painter = painterResource(id = resourceBgId),
            contentScale = ContentScale.Crop
          ),
        contentAlignment = Alignment.Center
      ) {
        Box(
          modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = 32.dp)
            .padding(top = 32.dp, bottom = 24.dp),
          contentAlignment = Alignment.Center,
        ) {
          Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
            content = content
          )
        }
        if (isLoading) {
          CircularProgressIndicator(
            modifier = Modifier.align(Alignment.Center)
          )
        }
      }
      actionLabel?.let {
        Box(
          modifier = Modifier
            .padding(bottom = 6.dp)
            .width(124.dp)
            .height(48.dp)
            .align(Alignment.BottomCenter)
            .clickable { onAction?.invoke() },
          contentAlignment = Alignment.Center
        ) {
          // Image background
          val buttonBackgroundRes =
            if (buttonEnabledState) R.drawable.button_enabled else R.drawable.button_disabled
          Image(
            painter = painterResource(id = buttonBackgroundRes),
            contentDescription = "Button Background",
            modifier = Modifier.fillMaxSize()
          )
          Button(
            onClick = { onAction?.invoke() },
            enabled = buttonEnabledState, // Controls whether the button is clickable
            colors = ButtonDefaults.buttonColors(
              containerColor = Color.Transparent,
            ),
          ) {
            Text(
              modifier = Modifier.padding(bottom = 8.dp),
              text = actionLabel,
              color = colorResource(R.color.white),
              fontFamily = CustomFont.fsClanPro,
              fontSize = 14.sp,
              fontWeight = FontWeight(1000)
            )
          }
        }
      }
      if (onClose != null) {
        Image(
          painterResource(R.drawable.ic_close),
          contentDescription = null,
          contentScale = ContentScale.FillBounds,
          modifier = Modifier
            .padding(end = 22.dp)
            .padding(top = 32.dp)
            .size(36.dp)
            .align(Alignment.TopEnd)
            .clickable {
              onClose()
            }
        )
      }
//      toastHostState?.let {
//        Toast(
//          modifier = Modifier
//            .padding(horizontal = 16.dp)
//            .windowInsetsPadding(WindowInsets.navigationBars)
//            .padding(bottom = 16.dp)
//            .align(Alignment.BottomCenter)
//            .fillMaxWidth(if (isLandscape) 0.5f else 1f),
//          toastState = toastHostState,
//          position = Toast.Position.Bottom
//        )
//      }
    }
  }
}

@Preview(device = "spec:width=1600dp,height=891dp")
@Composable
fun AlertFrameContainerPreview() {
  MaterialTheme {
    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      AlertFrameContainer(
        actionLabel = stringResource(R.string.update),
        onAction = {

        },
        onClose = {
          
        }
      ) {

      }
    }
  }
}