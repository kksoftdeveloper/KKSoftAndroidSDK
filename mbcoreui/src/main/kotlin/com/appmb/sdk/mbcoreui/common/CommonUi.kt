package com.appmb.sdk.mbcoreui.common

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicText
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeOptions
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.R

@Composable
fun RequiredFieldLabel(
  text: String,
  modifier: Modifier = Modifier,
  style: TextStyle = TextStyle(
    color = colorResource(R.color.black),
    fontFamily = CustomFont.fzPoppinsFont,
    fontSize = 12.sp,
  ),
) {
  val requiredColor = colorResource(R.color.red_error)
  BasicText(
    text = buildAnnotatedString {
      append(text)
      withStyle(SpanStyle(color = requiredColor)) {
        append(" *")
      }
    },
    style = style,
    modifier = modifier
  )
}

@Composable
fun TextInputField(
  textFieldValue: String,
  onValueChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String? = null
) {
  BasicTextField(
    value = textFieldValue,
    onValueChange = { value ->
      onValueChange.invoke(value)
    },
    modifier = modifier
      .fillMaxWidth()
      .padding(top = 4.dp)
      .heightIn(min = 32.dp)
      .background(
        color = colorResource(R.color.input_background_color),
        shape = RoundedCornerShape(8.dp)
      )
      .padding(horizontal = 16.dp, vertical = 10.dp), // inner padding
    textStyle = TextStyle(
      fontFamily = CustomFont.fzPoppinsFont,
      fontSize = 12.sp,
      color = Color.White
    ),
    cursorBrush = SolidColor(Color.White),
    singleLine = true,
    decorationBox = { innerTextField ->
      Box(
        modifier = Modifier.fillMaxWidth(),
        contentAlignment = Alignment.CenterStart,
      ) {
        if (textFieldValue.isEmpty()) {
          placeholder?.let {
            BasicText(
              text = placeholder,
              style = TextStyle(
                color = Color.Gray,
                fontSize = 12.sp,
                fontFamily = CustomFont.fzPoppinsFont,
              )
            )
          }
        }
        innerTextField()
      }
    }
  )
}

@Composable
fun PasswordInputWithToggle(
  password: String,
  onPasswordChange: (String) -> Unit,
  modifier: Modifier = Modifier,
  placeholder: String? = null
) {
  var isPasswordVisible by rememberSaveable { mutableStateOf(false) }
  Row(
    modifier = modifier
      .fillMaxWidth()
      .background(
        color = colorResource(R.color.input_background_color),
        shape = RoundedCornerShape(8.dp)
      )
      .padding(start = 12.dp),
    verticalAlignment = Alignment.CenterVertically
  ) {
    BasicTextField(
      value = password,
      onValueChange = { value ->
        onPasswordChange(value)
      },
      modifier = Modifier
        .weight(1f)
        .heightIn(min = 36.dp)
        .padding(horizontal = 4.dp, vertical = 0.dp),
      textStyle = TextStyle(
        fontFamily = CustomFont.fzPoppinsFont,
        fontSize = 12.sp,
        color = Color.White,
        fontWeight = FontWeight(600)
      ),
      cursorBrush = SolidColor(Color.White),
      singleLine = true,
      visualTransformation = if (isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
      decorationBox = { innerTextField ->
        Box(
          modifier = Modifier.fillMaxWidth(),
          contentAlignment = Alignment.CenterStart,
        ) {
          if (password.isEmpty()) {
            placeholder?.let {
              BasicText(
                text = placeholder,
                style = TextStyle(
                  color = Color.Gray,
                  fontSize = 12.sp,
                  fontFamily = CustomFont.fzPoppinsFont,
                )
              )
            }
          }
          innerTextField()
        }
      }
    )

    IconButton(
      modifier = Modifier
        .padding(8.dp)
        .size(16.dp),
      onClick = { isPasswordVisible = !isPasswordVisible }) {
      Icon(
        painter = if (isPasswordVisible) painterResource(R.drawable.ic_eye_close)
        else painterResource(R.drawable.ic_eye_open),
        contentDescription = if (isPasswordVisible) "Hide password" else "Show password",
        tint = Color.White,
        modifier = Modifier.fillMaxSize()
      )
    }
  }
}

@Composable
fun SocialButtonView(
  iconResId: Int,
  onClick: () -> Unit,
  modifier: Modifier = Modifier,
  text: String? = null,
) {
  Row(
    modifier = modifier
      .height(32.dp)
      .background(
        color = colorResource(R.color.brown_button_background),
        shape = RoundedCornerShape(8.dp)
      )
      .clickable {
        onClick.invoke()
      },
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically,
  ) {
    Image(
      painter = painterResource(iconResId),
      contentDescription = null,
      modifier = Modifier.size(12.dp)
    )
    text?.let {
      BasicText(
        text = text,
        style = TextStyle(
          fontFamily = CustomFont.fzPoppinsFont,
          color = Color.White,
          fontSize = 10.sp,
          textAlign = TextAlign.Center
        ),
        modifier = Modifier.padding(start = 10.dp, top = 1.dp)
      )
    }
  }
}

@Composable
fun CustomCheckbox(
  checked: Boolean,
  onCheckedChange: (Boolean) -> Unit,
  modifier: Modifier = Modifier,
  size: Dp = 16.dp,
) {
  val checkboxColor = colorResource(R.color.blue_checkbox_color)
  Box(
    modifier = modifier
      .size(size)
      .clip(RoundedCornerShape(4.dp))
      .background(if (checked) checkboxColor else Color.Transparent)
      .border(
        width = if (checked) 0.dp else 2.dp,
        color = checkboxColor,
        shape = RoundedCornerShape(4.dp)
      )
      .clickable { onCheckedChange(!checked) },
    contentAlignment = Alignment.Center
  ) {
    if (checked) {
      Icon(
        imageVector = Icons.Default.Check,
        contentDescription = "Checked",
        tint = Color.White,
        modifier = Modifier.size(size * 0.8f)
      )
    }
  }
}

@Preview
@Composable
fun CheckboxPreview() {
  CustomCheckbox(checked = false, onCheckedChange = {})
}

@Preview
@Composable
fun SocialButtonViewPreview() {
  SocialButtonView(
    text = stringResource(R.string.play_as_guest),
    iconResId = R.drawable.ic_play_now,
    onClick = {
//      authViewModel.dispatch(AuthIntent.LoginByGuest)
    }
  )
}

@Preview
@Composable
fun PasswordInputWithTogglePreview() {
  PasswordInputWithToggle(
    password = "",
    placeholder = "Hello",
    onPasswordChange = {}
  )
}
