package com.appmb.sdk.mbcore.utils

// ConnectivityViewModel.kt
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class ConnectivityViewModel(monitor: NetworkMonitor) : ViewModel() {
  val isOnline: StateFlow<Boolean> =
    monitor.isConnected.stateIn(
      scope = viewModelScope,
      started = SharingStarted.WhileSubscribed(5_000),
      initialValue = true // or false if you prefer
    )
}
