package com.appmb.sdk.mbcore.di.module

import com.appmb.sdk.mbcore.BuildConfig
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.di.MbSdkQualifiers
import com.appmb.sdk.mbcore.network.NetworkClient
import org.koin.core.qualifier.named
import org.koin.dsl.module

val authModule = module {
  single<String>(named(MbSdkQualifiers.Common.BASE_URL)) {
    get<MbSdkConfig>().getBaseUrl()?.takeIf { it.isNotBlank() } ?: BuildConfig.BASE_URL
  }
  single<NetworkClient>(named(MbSdkQualifiers.Auth.NETWORK_CLIENT)) {
    NetworkClient(
      get(
        named(
          MbSdkQualifiers.Auth.BASE_URL
        )
      ),
      get(),
      get(),
      get()
    )
  }
}
