package com.appmb.sdk.mbcoreui.utils

import java.text.DecimalFormat

object CommonUtils {
  fun formatCurrency(value: String): String {
    val formatter = DecimalFormat("#,##0.###")
    return formatter.format(value.toDouble())
  }

  fun Float.roundScalePercent() =
    if (this >= 1.0f) {
      1.0f
    } else {
      val step = 0.1f
      val rounded = (this / step).toInt() * step
      rounded
    }

  fun extractNumberAndText(input: String): Pair<Int, String> {
    val regex = Regex("""^\s*([\d,]+)\s+(.+)$""")
    val matchResult = regex.find(input)

    return matchResult?.let {
      val number = it.groupValues[1].replace(",", "").toInt()
      val text = it.groupValues[2]
      number to text
    } ?: (0 to "")
  }
}