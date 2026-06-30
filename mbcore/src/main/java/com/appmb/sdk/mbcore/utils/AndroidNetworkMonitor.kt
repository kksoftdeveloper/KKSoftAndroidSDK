package com.appmb.sdk.mbcore.utils

import android.content.*
import android.net.*
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.distinctUntilChanged

// NetworkMonitor.kt
interface NetworkMonitor {
  val isConnected: kotlinx.coroutines.flow.Flow<Boolean>
}

class AndroidNetworkMonitor(private val context: Context) : NetworkMonitor {
  override val isConnected = callbackFlow {
    val cm = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun current(): Boolean =

      @Suppress("DEPRECATION")
      cm.activeNetworkInfo?.isConnected == true


    trySend(current())


    @Suppress("DEPRECATION")
    val receiver = object : BroadcastReceiver() {
      override fun onReceive(c: Context?, i: Intent?) {
        trySend(current())
      }
    }
    @Suppress("DEPRECATION")
    context.registerReceiver(receiver, IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION))
    awaitClose { runCatching { context.unregisterReceiver(receiver) } }

  }.distinctUntilChanged()
}
