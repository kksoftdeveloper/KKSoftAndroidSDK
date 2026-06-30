package com.appmb.sdk.mbpayment

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbcore.utils.ConnectivityViewModel
import com.appmb.sdk.mbcoreui.theme.AppMbSampleTheme
import com.appmb.sdk.mbcoreui.utils.OfflineBanner
import com.appmb.sdk.mbpayment.ui.ProductListScreen

class PaymentActivity : ComponentActivity() {

  private var tokenExpiratoryReceiver: BroadcastReceiver? = null
  private var userBlockedReceiver: BroadcastReceiver? = null

  override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
		tokenExpiratoryReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == MbAuth.ACTION_TOKEN_EXPIRATION) {
          this@PaymentActivity.finish()
        }
      }
    }

    userBlockedReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (intent?.action == MbAuth.ACTION_USER_BLOCKED) {
          this@PaymentActivity.finish()
        }
      }
    }

    val tokenExpiratoryFilter = IntentFilter(MbAuth.ACTION_TOKEN_EXPIRATION)
    LocalBroadcastManager.getInstance(this).registerReceiver(tokenExpiratoryReceiver!!, tokenExpiratoryFilter)

    val userBlockedFilter = IntentFilter(MbAuth.ACTION_USER_BLOCKED)
    LocalBroadcastManager.getInstance(this).registerReceiver(userBlockedReceiver!!, userBlockedFilter)

    setContent {
      val connectivityVm: ConnectivityViewModel = org.koin.androidx.compose.koinViewModel()
      val lifecycle = LocalLifecycleOwner.current.lifecycle
      val online by remember(connectivityVm, lifecycle) {
        connectivityVm.isOnline.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
      }.collectAsState(initial = true)

      androidx.compose.material3.MaterialTheme {
        Box(Modifier.fillMaxSize()) {
          AppContent()
          OfflineBanner(
            visible = !online,
            modifier = Modifier
              .align(Alignment.TopCenter)
              .statusBarsPadding()
          )
        }}
    }
  }

  @Composable
  fun AppContent() {
    val navigationController = rememberNavController()
    AppMbSampleTheme {
      AppNavigator(
        navController = navigationController
      )
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    tokenExpiratoryReceiver?.let {
      LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
      tokenExpiratoryReceiver = null
    }
    userBlockedReceiver?.let {
      LocalBroadcastManager.getInstance(this).unregisterReceiver(it)
      userBlockedReceiver = null
    }
  }

  @Composable
  fun AppNavigator(
    navController: NavHostController
  ) {
    NavHost(
      navController, startDestination = BillingScreen.NAME
    ) {
      composable(route = BillingScreen.NAME) {
        ProductListScreen()
      }
    }
  }
}