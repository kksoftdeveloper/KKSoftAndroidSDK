package com.appmb.sdk.mbcore.di.module

import com.appmb.sdk.mbcore.BuildConfig
import com.appmb.sdk.mbcore.di.MbSdkQualifiers
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.event.MixpanelAnalyticsProvider
import org.koin.core.qualifier.named
import org.koin.dsl.module

val mixpanelModule = module {
  single<AnalyticsProvider> {
    MixpanelAnalyticsProvider(
      context = get(),
      token = BuildConfig.ANALYTICS_EVENT_TOKEN
    )
  }
}