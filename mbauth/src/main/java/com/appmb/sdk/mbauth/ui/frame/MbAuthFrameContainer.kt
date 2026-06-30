package com.appmb.sdk.mbauth.ui.frame

import android.app.Activity
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.Configuration
import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imeNestedScroll
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbauth.R
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbcoreui.utils.CommonUtils.roundScalePercent
import com.appmb.sdk.mbtracking.TrackingSdk
import org.koin.java.KoinJavaComponent
import com.appmb.sdk.mbcoreui.R as CoreUiR

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun MbAuthFrameContainer(
  buttonLabel: String,
  buttonEnabledState: Boolean,
  onButtonClick: () -> Unit,
  isLoading: Boolean = false,
  onCloseButtonClick: (() -> Unit)?,
  content: @Composable ColumnScope.() -> Unit = {},
) {
  val activity = LocalContext.current as? Activity
  val configuration = LocalConfiguration.current
  var logoClickCount by remember { mutableStateOf(0) }
  var showTrackingDialog by remember { mutableStateOf(false) }

  // Get Adjust ID (using callback)
  var adjustId by remember { mutableStateOf<String?>(null) }
  LaunchedEffect(Unit) {
    runCatching {
      KoinJavaComponent.getKoin().get<TrackingSdk>().getAdjustId { id ->
        adjustId = id
      }
    }
  }
  val screenWidthDp = configuration.screenWidthDp.dp
  val screenHeightDp = configuration.screenHeightDp.dp
  val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
  var shouldScaleContent = false
  val standardWidth = 400.dp
  var scalePercent = 1f
  var isTablet = false
  var containerModifier = Modifier
    .fillMaxSize()
    .background(
      Color.Black.copy(alpha = 0.5f)
    )
  if (!isLandscape) {
    containerModifier = containerModifier
      .imePadding()
      .imeNestedScroll()
  }
  Box(
    modifier = containerModifier,
    contentAlignment = Alignment.Center

  ) {
    val modifier = if (isLandscape) {
      if (screenHeightDp < 600.dp) {
        shouldScaleContent = true
        scalePercent = screenHeightDp / standardWidth
        Modifier
          .fillMaxHeight()
          .aspectRatio(0.72f)
      } else {
        isTablet = true
        Modifier
          .fillMaxHeight(0.9f)
          .aspectRatio(0.74f)
      }
    } else {
      if (screenWidthDp < 600.dp) {
        scalePercent = (screenWidthDp / standardWidth).roundScalePercent()
        Modifier
          .fillMaxWidth()
      } else {
        isTablet = true
        Modifier
          .fillMaxWidth()
      }
        .aspectRatio(0.8f)
    }
    // Outer Box which contains all component
    Box(
      modifier = modifier
        .fillMaxWidth()
        .align(Alignment.Center),
      contentAlignment = Alignment.Center
    ) {
      // Box contains content view and button
      Box(
        modifier = Modifier
          .align(Alignment.Center)
          .fillMaxWidth()
          .aspectRatio(1f)
      ) {
        // Box contains content view
        Box(
          modifier = Modifier
            .fillMaxWidth()
            .scale(if (shouldScaleContent) 0.98f else 1.0f)
            .aspectRatio(0.8f),
          contentAlignment = Alignment.Center
        ) {
          Image(
            painter = painterResource(R.drawable.frame_body),
            contentDescription = null,
            modifier = Modifier.fillMaxSize()
          )
          Box(
            modifier = Modifier
              .fillMaxSize(0.85f),
            contentAlignment = Alignment.Center,
          ) {
            val paddingHorizontal = if (isTablet) 64.dp else {
              if (scalePercent >= 1f) 48.dp
              else 32.dp * scalePercent
            }

//            val paddingTop = if (scalePercent >= 1f) 44.dp else 32.dp * scalePercent
            Column(
              modifier = Modifier
                .scale(if (scalePercent >= 1f) 1f else scalePercent)
                .fillMaxSize()
                .padding(horizontal = paddingHorizontal),
//                .padding(top = paddingTop),
              verticalArrangement = Arrangement.Center,
              horizontalAlignment = Alignment.CenterHorizontally,
              content = content
            )
            if (isLoading) {
              CircularProgressIndicator(
                modifier = Modifier.align(Alignment.Center)
              )
            }
          }
        }
      }

      Box(
        modifier = Modifier
          .width(160.dp)
          .height(52.dp)
          .scale(if (shouldScaleContent) 0.8f else 1.0f)
          .align(Alignment.BottomCenter)
          .offset(y = if (isLandscape) 0.dp else (16).dp)
          .clickable { onButtonClick.invoke() },
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
          onClick = onButtonClick,
          enabled = buttonEnabledState, // Controls whether the button is clickable
          colors = ButtonDefaults.buttonColors(
            containerColor = Color.Transparent,
            disabledContainerColor = Color.Transparent,
          ),
        ) {
          Text(
            modifier = Modifier.padding(bottom = 8.dp),
            text = buttonLabel,
            color = colorResource(CoreUiR.color.white),
            fontFamily = CustomFont.fsClanPro,
            fontSize = 14.sp,
            fontWeight = FontWeight(1000)
          )
        }
      }
      Image(
        painterResource(R.drawable.ic_logo),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
          .fillMaxWidth(0.7f)
          .aspectRatio(2.0f)
          .scale(if (shouldScaleContent) 0.8f else 1.0f)
          .align(Alignment.TopCenter)
          .offset(y = if (isLandscape) (-8).dp else (-32).dp)
          .clickable {
            logoClickCount++
            if (logoClickCount >= 7) {
              showTrackingDialog = true
              logoClickCount = 0 // Reset counter
            }
          }
          .offset(y = if (isLandscape) (-20).dp else (-48).dp)
      )
      if (onCloseButtonClick != null) {
        Image(
          painterResource(CoreUiR.drawable.ic_close),
          contentDescription = null,
          contentScale = ContentScale.FillBounds,
          modifier = Modifier
            .padding(
              vertical = if (isLandscape) 28.dp else 12.dp,
              horizontal = if (isLandscape) 12.dp else 20.dp
            )
            .size(if (isLandscape) 36.dp else 56.dp)
            .scale(if (shouldScaleContent) 0.9f else 1.0f)
            .align(Alignment.TopEnd)
            .clickable {
              onCloseButtonClick()
            }
        )
      }
    }
    
    if (showTrackingDialog) {
      TrackingIdsDialog(
        adjustId = adjustId,
        onDismiss = { showTrackingDialog = false }
      )
    }
//    toastHostState?.let {
//      Toast(
//        modifier = Modifier
//          .padding(horizontal = 16.dp)
//          .windowInsetsPadding(WindowInsets.navigationBars)
//          .padding(bottom = 16.dp)
//          .align(Alignment.BottomCenter)
//          .fillMaxWidth(if (shouldScaleContent) 0.5f else 1f),
//        toastState = toastHostState,
//        position = Toast.Position.Bottom
//      )
//    }
  }
}

@Composable
fun TrackingIdsDialog(
  adjustId: String?,
  onDismiss: () -> Unit
) {
  val context = LocalContext.current

  fun copyToClipboard(text: String, label: String) {
    val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clip = ClipData.newPlainText(label, text)
    clipboard.setPrimaryClip(clip)
    Toast.makeText(context, "$label copied to clipboard", Toast.LENGTH_SHORT).show()
  }

  AlertDialog(
    onDismissRequest = {},
    title = {
      Text(
        text = "Tracking IDs",
        fontFamily = CustomFont.fsClanPro,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp
      )
    },
    text = {
      Column(
        modifier = Modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(16.dp)
      ) {
        Column {
          Text(
            text = "Adjust ID",
            fontFamily = CustomFont.fsClanPro,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 4.dp)
          )
          Text(
            text = "Use this ID in Adjust Dashboard to test events:",
            fontFamily = CustomFont.fsClanPro,
            fontSize = 12.sp,
            color = Color.Gray,
            modifier = Modifier.padding(bottom = 8.dp)
          )
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(
              text = adjustId ?: "Not available (Adjust may not be initialized)",
              fontFamily = CustomFont.fsClanPro,
              fontSize = 14.sp,
              fontWeight = FontWeight.Bold,
              color = if (adjustId != null) colorResource(
                CoreUiR.color.blue_text_link_color
              ) else Color.Gray,
              modifier = Modifier
                .weight(1f)
                .padding(12.dp)
                .background(
                  Color.LightGray.copy(alpha = 0.2f),
                  RoundedCornerShape(8.dp)
                )
                .padding(12.dp)
            )
            if (adjustId != null) {
              Button(
                onClick = { copyToClipboard(adjustId, "Adjust ID") },
                colors = ButtonDefaults.buttonColors(
                  containerColor = colorResource(CoreUiR.color.blue_text_link_color)
                ),
                modifier = Modifier.height(40.dp)
              ) {
                Text(
                  text = "Copy",
                  fontFamily = CustomFont.fsClanPro,
                  fontSize = 12.sp,
                  fontWeight = FontWeight(1000)
                )
              }
            }
          }
        }
      }
    },
    confirmButton = {
      Button(
        onClick = onDismiss,
        colors = ButtonDefaults.buttonColors(
          containerColor = colorResource(CoreUiR.color.brown)
        )
      ) {
        Text(
          text = "Close",
          fontFamily = CustomFont.fsClanPro,
          fontSize = 14.sp,
          fontWeight = FontWeight(1000)
        )
      }
    }
  )
}

@Preview(
  showBackground = true, device = "spec:width=800dp,height=1200dp,dpi=420",
)
@Composable
fun PreviewFrameContainer() {
  MaterialTheme {
    Box(
      modifier = Modifier
        .fillMaxSize(),
      contentAlignment = Alignment.Center
    ) {
      MbAuthFrameContainer(
        buttonLabel = stringResource(CoreUiR.string.receive_otp_code),
        buttonEnabledState = false,
        onButtonClick = {},
        onCloseButtonClick = {}
      ) {
        for (i in 0..15) {
          Text("$i",
            modifier = Modifier.fillMaxWidth()
              .padding(vertical = 8.dp)
              .background(Color.Blue.copy(alpha = 0.1f))
          )
        }
      }
    }
  }
}