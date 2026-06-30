package com.appmb.sdk.mbtracking.di

import android.content.Context
import com.appmb.sdk.mbcore.config.TrackingConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbtracking.TrackingProvider
import com.appmb.sdk.mbtracking.TrackingProviderConfig
import com.appmb.sdk.mbtracking.TrackingSdk
import com.appmb.sdk.mbtracking.providers.AdjustTrackingProvider
import com.appmb.sdk.mbtracking.providers.FacebookTrackingProvider
import com.appmb.sdk.mbtracking.providers.FirebaseTrackingProvider
import com.appmb.sdk.mbtracking.providers.TikTokTrackingProvider
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import org.koin.dsl.module

fun trackingModule(context: Context, config: TrackingConfig) = module {
  single { FirebaseTrackingProvider() }
  single { AdjustTrackingProvider() }
  single { TikTokTrackingProvider() }
  single { FacebookTrackingProvider() }

  single {
    val providers = mutableListOf<TrackingProvider>()
    if (config.enableFirebase) providers += get<FirebaseTrackingProvider>()
    if (config.enableAdjust) providers += get<AdjustTrackingProvider>()
    if (config.enableTikTok) providers += get<TikTokTrackingProvider>()
    if (config.enableMeTa) providers += get<FacebookTrackingProvider>()

    val adidScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    TrackingSdk(
      context = context,
      providers = providers,
      config = TrackingProviderConfig(
        adjustAppToken = config.adjustToken,
        tiktokAppId = config.tiktokAppID,
        facebookAppId = config.facebookAppID,
        facebookClientToken = config.facebookClientToken,
        facebookDisplayName = config.facebookDisplayName,
        firebaseAppId = config.firebaseAppID,
        gidClientID = config.gidClientID,
        appFlyersId = config.appFlyersId,
        appFlyersDevKey = config.appFlyersDevKey
      ),
      onAdidReady = { adid ->
        adidScope.launch {
          get<MbCoreCommonDataSource>().saveAdid(adid)
        }
      }
    ).apply { initialize() }
  }
}


