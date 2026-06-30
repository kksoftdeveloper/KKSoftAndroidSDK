package com.appmb.sdk.mbcoreui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.ExperimentalAnimationApi
import androidx.compose.animation.core.FastOutLinearInEasing
import androidx.compose.animation.core.LinearOutSlowInEasing
import androidx.compose.animation.core.TargetBasedAnimation
import androidx.compose.animation.core.VectorConverter
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.SemanticsPropertyKey
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.common.CustomFont
import kotlinx.coroutines.delay
import java.util.UUID


@Stable
class Toast(
  val id: UUID = UUID.randomUUID(),
  val message: String,
  val iconRes: Int? = null,
  val iconColor: Color? = null,
  val duration: Long = defaultDuration,
  val position: Position = Position.Bottom,
  val actionLabel: String = "",
  val onActionClick: ((UUID) -> Unit)? = null,
) {
  companion object {
    internal const val limitedToast = 10
    internal const val enterDuration = 200
    internal const val exitDuration = 200

    const val defaultDuration = 4000L
  }

  enum class Position {
    Center,
    Bottom
  }
}

@Stable
class ToastHostState {

  internal val listToasts = mutableStateListOf<Toast>()

  fun showToast(
    id: UUID = UUID.randomUUID(),
    message: String,
    iconRes: Int? = null,
    iconColor: Color? = null,
    duration: Long = Toast.defaultDuration,
    position: Toast.Position = Toast.Position.Bottom,
    actionLabel: String = "",
    onActionClick: ((UUID) -> Unit)? = null,
  ) {
    listToasts.add(
      Toast(
        id = id,
        message = message,
        iconRes = iconRes,
        iconColor = iconColor,
        duration = duration,
        position = position,
        actionLabel = if (position == Toast.Position.Bottom) actionLabel else "",
        onActionClick = onActionClick
      )
    )
  }

  fun showToast(toast: Toast) {
    listToasts.add(toast)
  }

  fun dismiss(id: UUID) {
    listToasts.removeIf { it.id == id }
  }
}

@Composable
fun Toast(
  modifier: Modifier,
  toastState: ToastHostState,
  position: Toast.Position = Toast.Position.Bottom,
) {
  Box(modifier = modifier) {
    toastState.listToasts.forEach { item ->
      key(item.id) {
        var visible by remember { mutableStateOf(false) }
        if (item.actionLabel.isEmpty()) {
          val anim = remember {
            TargetBasedAnimation(
              animationSpec = tween(item.duration.toInt()),
              typeConverter = Float.VectorConverter,
              initialValue = item.duration.toFloat(),
              targetValue = 0f
            )
          }
          LaunchedEffect(anim) {
            val startTime = withFrameNanos { it }
            do {
              visible = anim.getValueFromNanos(withFrameNanos { it } - startTime) > 0
              if (!visible) {
                delay(Toast.exitDuration.toLong())
                toastState.dismiss(item.id)
              }
            } while (visible)
          }
        } else {
          LaunchedEffect(visible) {
            visible = true
          }
        }
        if (item.position == position) {
          when (item.position) {
            Toast.Position.Bottom -> BottomToastContent(
              modifier = Modifier.align(Alignment.BottomCenter),
              visible = visible,
              toast = item
            )

            Toast.Position.Center -> CenterToastContent(
              modifier = Modifier.align(Alignment.Center),
              visible = visible,
              toast = item
            )
          }
        }
      }
    }
    if (toastState.listToasts.size >= Toast.limitedToast) {
      toastState.dismiss(toastState.listToasts[0].id)
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun BottomToastContent(
  modifier: Modifier,
  visible: Boolean,
  toast: Toast,
) {
  AnimatedVisibility(
    modifier = modifier,
    visible = visible,
    enter = slideInVertically(
      initialOffsetY = { it },
      animationSpec = tween(
        durationMillis = Toast.enterDuration,
        easing = LinearOutSlowInEasing
      )
    ),
    exit = fadeOut(
      targetAlpha = 0f,
      animationSpec = tween(
        durationMillis = 0,
        easing = FastOutLinearInEasing
      )
    )
  ) {
    Row(
      modifier = Modifier
        .fillMaxWidth()
        .background(Color.LightGray, RoundedCornerShape(8.dp))
        .padding(horizontal = 16.dp, vertical = 12.dp),
      verticalAlignment = Alignment.CenterVertically
    ) {
      toast.iconRes?.let {
        Icon(
          modifier = Modifier
            .padding(end = 8.dp)
            .size(24.dp)
            .semantics {
              set(
                SemanticsPropertyKey(ToastSemanticKey.Companion.IconSize.value),
                if (toast.iconRes != 0) 24.dp else 0.dp
              )
            },
          painter = painterResource(id = it),
          color = toast.iconColor
        )
      }
      BasicText(
        text = toast.message,
        style = TextStyle(
          color = colorResource(R.color.white),
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 13.sp,
        ),
        maxLines = 2,
        modifier = Modifier.weight(1f),
      )
      if (toast.actionLabel.isNotEmpty()) {
        Spacer(
          Modifier
            .padding(horizontal = 16.dp)
            .width(1.dp)
            .height(20.dp)
            .background(colorResource(R.color.white).copy(alpha = 0.5f))
            .padding(12.dp)
        )
        BasicText(
          text = toast.actionLabel,
          style = TextStyle(
            color = colorResource(R.color.white),
            fontFamily = CustomFont.fzPoppinsFont,
            fontSize = 14.sp,
          ),
          maxLines = 2,
          modifier = Modifier
            .weight(1f)
            .clickable {
              toast.onActionClick?.invoke(toast.id)
            }
        )
      }
    }
  }
}

@OptIn(ExperimentalAnimationApi::class)
@Composable
internal fun CenterToastContent(
  modifier: Modifier,
  visible: Boolean,
  toast: Toast,
) {
  AnimatedVisibility(
    modifier = modifier,
    visible = visible,
    enter = fadeIn(
      animationSpec = tween(
        durationMillis = Toast.enterDuration,
        easing = LinearOutSlowInEasing
      )
    ),
    exit = fadeOut(
      animationSpec = tween(
        durationMillis = Toast.exitDuration,
        easing = FastOutLinearInEasing
      )
    )
  ) {
    Column(
      modifier = Modifier
        .width(184.dp)
        .background(Color.LightGray, RoundedCornerShape(8.dp))
        .padding(horizontal = 16.dp, vertical = 12.dp)
        .semantics {
          set(SemanticsPropertyKey(ToastSemanticKey.Companion.TextPadding.value), 16.dp)
        },
      horizontalAlignment = Alignment.CenterHorizontally
    ) {
      toast.iconRes?.let {
        Icon(
          modifier = Modifier
            .size(36.dp)
            .semantics {
              set(
                SemanticsPropertyKey(ToastSemanticKey.Companion.IconSize.value),
                if (toast.iconRes != 0) 36.dp else 0.dp
              )
            },
          painter = painterResource(id = it),
          color = toast.iconColor
        )
      }
      BasicText(
        text = toast.message,
        style = TextStyle(
          color = colorResource(R.color.white),
          fontFamily = CustomFont.fzPoppinsFont,
          fontSize = 13.sp,
          textAlign = TextAlign.Center
        ),
        maxLines = 2,
        modifier = Modifier
          .weight(1f)
          .padding(16.dp),
      )
    }
  }
}

@Composable
internal fun Icon(modifier: Modifier, painter: Painter, color: Color?) {
  color?.let {
    androidx.compose.material3.Icon(
      modifier = modifier,
      painter = painter,
      tint = it,
      contentDescription = ""
    )
  } ?: Image(modifier = modifier, painter = painter, contentDescription = "")
}

class ToastSemanticKey(val value: String) {
  companion object {
    val TextColor by lazy { ToastSemanticKey("TextColor") }
    val TextPadding by lazy { ToastSemanticKey("TextPadding") }
    val TextStyle by lazy { ToastSemanticKey("TextStyle") }

    val IconSize by lazy { ToastSemanticKey("IconSize") }
  }
}