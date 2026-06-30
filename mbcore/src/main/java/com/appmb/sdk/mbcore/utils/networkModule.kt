package com.appmb.sdk.mbcore.utils

// module
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module
import org.koin.android.ext.koin.androidContext

val networkMonitorModule = module {
  single<NetworkMonitor> { AndroidNetworkMonitor(androidContext()) }
  viewModel { ConnectivityViewModel(get()) }
}
