package com.appmb.sdk.mbpayment.ui.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.colorResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.PlatformTextStyle
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.appmb.sdk.mbcoreui.common.CustomFont
import com.appmb.sdk.mbpayment.R
import com.appmb.sdk.mbpayment.model.GoogleBillingProduct


// Package view
@Composable
fun ProductItemView(
  item: GoogleBillingProduct,
  onBuyClick: () -> Unit = {}
) {
//  val supportedMarketPlace: Boolean = true // item.supportedVNMarketPlace
//  val supportedUser: Boolean = item.supportedUser
  Surface(
    shape = RoundedCornerShape(12.dp),
    color = colorResource(R.color.background_package_item),
    modifier = Modifier
      .fillMaxWidth()
      .height(68.dp)
  ) {

    Box() {
      Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(start = 12.dp)
      ) {
        // Icon product
        Box(
          modifier = Modifier.size(56.dp)
        ) {
          Image(
            painter = painterResource(R.drawable.product_icon_frame),
            contentDescription = null,
            modifier = Modifier
              .size(52.dp)
              .align(Alignment.Center)
          )
          Image(
            painter = painterResource(id = R.drawable.icon_product),
            contentDescription = null,
            contentScale = ContentScale.Crop,
            modifier = Modifier
              .size(44.dp)
              .align(Alignment.Center)
          )
          OutlinedText(
            text = item.points,
            fontSize = 20.sp,
            fontFamily = CustomFont.dongleRegular,
            fillColor = Color.White,
            modifier = Modifier
              .align(Alignment.BottomEnd)
              .padding(vertical = 0.dp, horizontal = 6.dp)
              .offset(y = 4.dp)
          )
        }

        // Name
        Column(
          modifier = Modifier
            .padding(start = 4.dp)
            .weight(4f),
        ) {
          OutlinedText(
            text = item.name.uppercase(),
            fontSize = 18.sp,
            modifier = Modifier.offset(y = 8.dp)
          )

          Row(
            verticalAlignment = Alignment.Bottom
          ) {
            OutlinedText(
              text = item.points,
              fontSize = 36.sp,
              modifier = Modifier.offset(y = (-8).dp)
            )
            OutlinedText(
              text = item.pointUnit.uppercase(),
              fontSize = 28.sp,
              modifier = Modifier
                .padding(start = 4.dp)
                .offset(y = (-4).dp)
            )
          }
        }
      }
      /*

      if (!item.supportedUser) {
        Spacer(modifier = Modifier.height(4.dp))
        Row(
          modifier = Modifier
            .wrapContentWidth()
            .align(Alignment.BottomEnd)
            .padding(horizontal = 12.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.warning),
            contentDescription = null,
            modifier = Modifier
              .height(12.dp)
              .width(12.dp)
              .padding(end = 2.dp)

          )
          Text(
            text = stringResource(R.string.error_message_not_supported_user),
            style = TextStyle(
              fontSize = 12.sp,
              fontFamily = CustomFont.dongleRegular,
              color = Color.White,
              textAlign = TextAlign.Right,
              platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
          )
        }
      } else if(!supportedMarketPlace) {
        Row(
          modifier = Modifier
            .wrapContentWidth()
            .align(Alignment.BottomEnd)
            .padding(horizontal = 12.dp, vertical = 4.dp)
        ) {
          Image(
            painter = painterResource(id = R.drawable.warning),
            contentDescription = null,
            modifier = Modifier
              .height(12.dp)
              .width(12.dp)
              .padding(end = 2.dp)
          )
          Text(
            text = stringResource(R.string.error_message_not_supported_marketplace),
            style = TextStyle(
              fontSize = 12.sp,
              fontFamily = CustomFont.dongleRegular,
              color = Color.White,
              textAlign = TextAlign.Right,
              platformStyle = PlatformTextStyle(includeFontPadding = false)
            )
          )
        }
      }


    }
*/

      Box(
        modifier = Modifier
          .align(Alignment.BottomEnd)
//        .background(color = Color.Red.copy(alpha = 0.4f))
        ,
      ) {
        Button(
          onClick = {
//          if (supportedMarketPlace && supportedUser) {
            onBuyClick()
//          }
          },
          modifier = Modifier
            .fillMaxWidth(fraction = 0.4f)
            .height(48.dp)
            .align(alignment = Alignment.BottomEnd)
//          .background(Color.Yellow.copy(alpha = 0.5f))
          ,
          shape = RoundedCornerShape(24.dp),
          colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
        contentPadding = PaddingValues(horizontal = 4.dp)
        ) {
          Box {
            Image(
//            painter = if (supportedMarketPlace) painterResource(id = R.drawable.price_frame) else painterResource(
//              id = R.drawable.disable
//            )
              painter = painterResource(id = R.drawable.price_frame),
              contentDescription = null,
              contentScale = ContentScale.FillBounds,
              modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth()
//              .background(Color.Black.copy(0.5f))
            )
            OutlinedText(
              modifier = Modifier
                .align(Alignment.Center),
              text = item.formattedPrice,
              fontSize = 22.sp,
              fillColor = Color.White,
              strokeColor = colorResource(R.color.outlined_text_border_color)
            )
          }
        }
      }
    }
  }
}

@Preview
@Composable
fun ProductItemViewTextPreview() {
  ProductItemView(
    item = GoogleBillingProduct(
      productId = "test_product",
      name = "Test Productqưe asdsf fsdggqdgfdgửew qửeetrqw rư",
      points = "1000",
      pointUnit = "MB",
      formattedPrice = "2,699,000 đ",
      price = "đ20,000",
      currency = "VND",
      purchaseToken = "",
      supportedVNMarketPlace = true,
      supportedUser = false,
      productDetails = null,
    ),
    {

    }
  )
}
