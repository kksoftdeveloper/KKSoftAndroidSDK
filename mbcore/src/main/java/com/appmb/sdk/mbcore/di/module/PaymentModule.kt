package com.appmb.sdk.mbcore.di.module

import com.appmb.sdk.mbcore.di.MbSdkQualifiers
import com.appmb.sdk.mbcore.network.NetworkClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val paymentModule = module {
  single<NetworkClient>(named(MbSdkQualifiers.Payment.NETWORK_CLIENT)) {
    NetworkClient(
      get(
        named(
          MbSdkQualifiers.Payment.BASE_URL
        )
      ),
      get(),
      get(),
      get()
    )
  }
}
