package com.appmb.sdk.mbcore.di.module

import com.appmb.sdk.mbcore.di.MbSdkQualifiers
import com.appmb.sdk.mbcore.network.NetworkClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val eventModule = module {
  single<NetworkClient>(named(MbSdkQualifiers.Event.NETWORK_CLIENT)) {
    NetworkClient(
      get(
        named(
          MbSdkQualifiers.Event.BASE_URL
        )
      ),
      get(),
      get(),
      get()
    )
  }
}
