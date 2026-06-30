@file:OptIn(ExperimentalTextApi::class)

package com.appmb.sdk.mbpayment.ui

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.res.Configuration
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.text.BasicText
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.pullrefresh.PullRefreshIndicator
import androidx.compose.material.pullrefresh.pullRefresh
import androidx.compose.material.pullrefresh.rememberPullRefreshState
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshotFlow
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
import androidx.compose.ui.text.ExperimentalTextApi
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appmb.sdk.mbauth.AuthActivity
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbauth.model.SdkParams
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.worker.TimerWorker
import com.appmb.sdk.mbcoreui.common.CustomFont

import com.appmb.sdk.mbcoreui.utils.CommonUtils.roundScalePercent
import com.appmb.sdk.mbcoreui.utils.ObserveAsEvents
import com.appmb.sdk.mbpayment.MbPayment
import com.appmb.sdk.mbpayment.MbPayment.EXTRA_PURCHASE_RESULT
import com.appmb.sdk.mbpayment.R
import com.appmb.sdk.mbpayment.data.dto.PurchaseStatus
import com.appmb.sdk.mbpayment.model.PurchaseResult
import com.appmb.sdk.mbpayment.ui.components.OutlinedText
import com.appmb.sdk.mbpayment.ui.components.ProductItemView
import com.appmb.sdk.mbpayment.ui.components.PurchaseResultPopupView
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun ProductListScreen() {
  val viewModel: ProductListViewModel = koinViewModel()
  val state = viewModel.state

  val activity = LocalContext.current as Activity
  val coroutineScope = rememberCoroutineScope()

  // Store pending purchase info for guest users who need to link account
  var pendingPurchase by remember { mutableStateOf<Pair<String, String>?>(null) }
  
  // Track if link account dialog is currently showing to prevent duplicate dialogs
  var isLinkAccountDialogShowing by remember { mutableStateOf(false) }

  // Launcher for link account result
  val linkAccountLauncher = rememberLauncherForActivityResult(
    contract = ActivityResultContracts.StartActivityForResult()
  ) { result ->
    // Reset dialog showing state when dialog is dismissed
    isLinkAccountDialogShowing = false
    
    if (result.resultCode == Activity.RESULT_OK) {
      val authResult: AuthResult? = result.data?.getParcelableExtra("authResult")
      when (authResult) {
        is AuthResult.LinkAccount -> {
          // Link account successful, refresh products and proceed with purchase
          coroutineScope.launch {
            viewModel.onAction(ProductListAction.RefreshProducts)
            // Wait for products to refresh (products will now have supportedUser = true)
            delay(500) // Small delay to ensure products are refreshed
            pendingPurchase?.let { (productId, price) ->
              viewModel.onAction(
                ProductListAction.BuyProduct(
                  activity = activity,
                  productId = productId,
                  price = price,
                )
              )
              pendingPurchase = null
            }
          }
        }
        is AuthResult.RepeatableRemindLinkAccount -> {
          // User dismissed the link account screen, clear pending purchase
          pendingPurchase = null
        }
        else -> {
          // Other results, clear pending purchase
          pendingPurchase = null
        }
      }
    } else {
      // User cancelled, clear pending purchase
      pendingPurchase = null
    }
  }

  // Function to show link account dialog
  val showLinkAccountDialog: () -> Unit = {
    if (!isLinkAccountDialogShowing) {
      isLinkAccountDialogShowing = true
      val intent = Intent(activity, AuthActivity::class.java).apply {
        putExtra(AuthActivity.EXTRA_DATA, SdkParams.LinkAccount)
      }
      linkAccountLauncher.launch(intent)
    }
  }

  // Register broadcast receiver for timer countdown (shows link account dialog every 5 mins)
  DisposableEffect(Unit) {
    val broadcastManager = LocalBroadcastManager.getInstance(activity)
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == TimerWorker.ACTION_TIMER_DONE) {
          // Only show dialog if it's not already showing
          if (!isLinkAccountDialogShowing) {
            showLinkAccountDialog()
          }
        }
      }
    }
    val filter = IntentFilter(TimerWorker.ACTION_TIMER_DONE)
    broadcastManager.registerReceiver(receiver, filter)
    
    onDispose {
      broadcastManager.unregisterReceiver(receiver)
    }
  }

  val listState = rememberLazyListState()

  val pullRefreshState = rememberPullRefreshState(
    refreshing = state.isRefreshing,
    onRefresh = {
      viewModel.onAction(ProductListAction.RefreshProducts)
    },
  )

  ObserveAsEvents(flow = viewModel.events) { event ->
    if (event is ProductListEvent.TokenExpired) {
      val resultIntent = Intent(MbAuth.ACTION_TOKEN_EXPIRATION)
      LocalBroadcastManager.getInstance(activity).sendBroadcast(resultIntent)
      return@ObserveAsEvents
    }
    if (event !is ProductListEvent.PurchasedIdle) {
      Log.d("ProductListScreen", "Purchase List Event ${event.javaClass.simpleName}")
      val resultIntent = Intent(MbPayment.ACTION_PURCHASE_DONE).apply {
        putExtra(EXTRA_PURCHASE_RESULT, event.toPurchaseResult())
      }
      LocalBroadcastManager.getInstance(activity).sendBroadcast(resultIntent)
    } else {
      Log.d("ProductListScreen", "Purchase List Event Idle")
    }
  }

  Box(
    modifier = Modifier
      .pullRefresh(pullRefreshState)
      .fillMaxSize(),
    contentAlignment = Alignment.Center
  ) {
    val configuration = LocalConfiguration.current
    val screenWidthDp = configuration.screenWidthDp.dp
    val screenHeightDp = configuration.screenHeightDp.dp
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE
    val standardWidth = 400.dp
    var scalePercent = 1f
    var isTablet = false
    val modifier = if (isLandscape) {
      if (screenHeightDp < 600.dp) {
        scalePercent = screenHeightDp / standardWidth
        Modifier
          .fillMaxHeight()
          .fillMaxWidth(0.4f)
      } else {
        isTablet = true
        Modifier
          .fillMaxHeight(0.8f)
          .fillMaxWidth(0.3f)
      }
    } else {
      if (screenWidthDp < 600.dp) {
        scalePercent = (screenWidthDp / standardWidth).roundScalePercent()
        Modifier
          .fillMaxWidth()
          .fillMaxHeight(0.7f)
      } else {
        isTablet = true
        Modifier
          .fillMaxWidth(0.7f)
          .fillMaxHeight(0.5f)
      }
    }
    BoxWithConstraints(
      modifier = modifier
//        .background(Color.Blue.copy(0.5f))
    ) {
      val boxHeight = constraints.maxHeight
      val listProductsOffsetY = if (isTablet) 0.08f else 0.0555f  // 11.5% from the top
      val headerTitleOffsetY = if (isTablet) 0.0352f else 0.024f
      Image(
        painter = painterResource(id = R.drawable.product_container_frame),
        contentDescription = null,
        contentScale = ContentScale.FillBounds,
        modifier = Modifier
          .fillMaxSize()
//          .fillMaxHeight(0.7f)
      )
      BasicText(
        text = stringResource(R.string.knb_store).uppercase(),
        style = TextStyle(
          color = colorResource(R.color.brown),
          fontFamily = CustomFont.fsClanPro,
          fontSize = 13.sp, // if(isLandscape) 14.sp else 16.sp,
          fontWeight = FontWeight(1000)
        ),
        modifier = Modifier
          .align(Alignment.TopCenter)
          .offset(y = (headerTitleOffsetY * boxHeight).dp)
      )
      // Close Button
      IconButton(
        onClick = {
          val resultIntent = Intent(MbPayment.ACTION_PURCHASE_DONE).apply {
            putExtra(EXTRA_PURCHASE_RESULT, PurchaseResult.ClosedProductList)
          }
          LocalBroadcastManager.getInstance(activity).sendBroadcast(resultIntent)
          activity?.finish()
        },
        modifier = Modifier
          .align(Alignment.TopEnd)
          .offset(y = (0.02 * boxHeight).dp)
      ) {
        Image(
          painter = painterResource(id = R.drawable.ic_close),
          contentDescription = "Close",
        )
      }

      if (!state.isGoogleBillingAvailable) {
        OutlinedText(
          text = stringResource(R.string.error_message_require_google_play_services),
          fontSize = 20.sp,
          modifier = Modifier
            .align(Alignment.Center)
            .padding(16.dp)
        )
      } else {
        LazyColumn(
          state = listState,
          verticalArrangement = Arrangement.spacedBy(12.dp),
          modifier = Modifier
            .padding(horizontal = if(!isLandscape && isTablet) 44.dp else 32.dp)
            .fillMaxHeight(0.767f)
            .offset(y = (listProductsOffsetY * boxHeight).dp)
            .scale(if (scalePercent >= 1f) 1f else scalePercent)
//            .background(Color.LightGray.copy(0.3f))
        ) {
          items(state.products) { product ->
            if (product.supportedUser) {
              // Non-guest users can purchase directly
              ProductItemView(item = product) {
                activity ?: return@ProductItemView
                viewModel.onAction(
                  ProductListAction.BuyProduct(
                    activity = activity,
                    productId = product.productId,
                    price = product.price,
                  )
                )
              }
            } else {
              // Guest users need to link account before purchasing
              ProductItemView(item = product) {
                activity ?: return@ProductItemView
                // Store the pending purchase info
                pendingPurchase = Pair(product.productId, product.price)
                // Launch link account screen
                showLinkAccountDialog()
              }
            }
          }
          if (state.isLoadingMore) {
            item {
              Box(
                modifier = Modifier
                  .fillMaxWidth()
                  .padding(16.dp)
//                  .background(Color.Magenta.copy(alpha = 0.3f))
                ,
                contentAlignment = Alignment.Center
              ) {
                CircularProgressIndicator(
                  modifier = Modifier.size(24.dp)
                )
              }
            }
          }
        }
      }
      if (state.isLoading) {
        CircularProgressIndicator(
          modifier = Modifier
            .size(24.dp)
            .align(Alignment.Center)
        )
      }
      if (state.purchasedStatus !is PurchaseStatus.Idle) {
        PurchaseResultPopupView(
          purchaseStatus = state.purchasedStatus,
          onActionButtonClick = {
            viewModel.onAction(action = ProductListAction.ResetPurchaseStatusState)
          }
        )
      }
      PullRefreshIndicator(
        refreshing = state.isRefreshing,
        state = pullRefreshState,
        modifier = Modifier
          .padding(horizontal = 8.dp)
//          .fillMaxHeight(0.54f)
          .offset(y = (listProductsOffsetY * boxHeight).dp)
          .align(Alignment.TopCenter),
        scale = false,
        backgroundColor = Color.White,
        contentColor = colorResource(id = R.color.black)
      )
    }
    LaunchedEffect(listState, state.products.size, state.hasMoreProducts) {
      snapshotFlow { listState.layoutInfo }
        .collect { layoutInfo ->
          val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
          val totalItems = layoutInfo.totalItemsCount
          val allItemsVisible = layoutInfo.visibleItemsInfo.size == totalItems

          val shouldLoadMore =
            state.products.isNotEmpty() &&
                state.hasMoreProducts &&
                !state.isLoadingMore &&
                (
                    (lastVisible >= state.products.lastIndex - 2) ||
                        allItemsVisible
                    )

          if (shouldLoadMore) {
            viewModel.onAction(ProductListAction.LoadMoreProducts)
          }
        }
    }
  }
}