package com.appmb.sdk.mbcore.di

import androidx.datastore.preferences.preferencesDataStore
import com.appmb.sdk.mbcore.config.MbConstant.DATASTORE_NAME
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonLocalDataSource
import com.appmb.sdk.mbcore.data.datasource.auth.MbCoreAuthDataSource
import com.appmb.sdk.mbcore.data.datasource.auth.MbCoreAuthRemoteDataSource
import com.appmb.sdk.mbcore.data.datasource.facebookapp.FacebookApp
import com.appmb.sdk.mbcore.data.datasource.facebookapp.FacebookAppImpl
import com.appmb.sdk.mbcore.data.datasource.game.MbGameDataSource
import com.appmb.sdk.mbcore.data.datasource.game.MbGameRemoteDataSource
import com.appmb.sdk.mbcore.data.datasource.gapp.GApp
import com.appmb.sdk.mbcore.data.datasource.gapp.GAppImpl
import com.appmb.sdk.mbcore.data.repo.MbCoreAuthRepositoryImpl
import com.appmb.sdk.mbcore.data.repo.MbGameRepositoryImpl
import com.appmb.sdk.mbcore.datastore.DataStoreManager
import com.appmb.sdk.mbcore.di.module.authModule
import com.appmb.sdk.mbcore.di.module.eventModule
import com.appmb.sdk.mbcore.di.module.mixpanelModule
import com.appmb.sdk.mbcore.di.module.paymentModule
import com.appmb.sdk.mbcore.domain.auth.MbCoreAuthRepository
import com.appmb.sdk.mbcore.domain.game.MbGameRepository
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbcore.network.api.auth.KtorCoreAuthApiImpl
import com.appmb.sdk.mbcore.network.api.auth.MbCoreAuthApi
import com.appmb.sdk.mbcore.network.api.game.KtorMbGameApiImpl
import com.appmb.sdk.mbcore.network.api.game.MbGameApi
import com.appmb.sdk.mbcore.network.json
import com.appmb.sdk.mbcore.platform.MbDeviceInfo
import com.appmb.sdk.mbcore.platform.MbDeviceInfoImpl
import com.appmb.sdk.mbcore.utils.networkMonitorModule
import kotlinx.serialization.json.Json
import org.koin.dsl.module

val commonModule = module {
  single<Json> { json }
  single {
    preferencesDataStore(name = DATASTORE_NAME).getValue(
      get(), String::javaClass
    )
  }
  single<DataStoreManager?> { DataStoreManager(dataStore = get(), json = get()) }
  single<MbDeviceInfo> { MbDeviceInfoImpl(get()) }
  single<FacebookApp> { FacebookAppImpl(get()) }
  single<GApp> { GAppImpl(get()) }
  single<MbCoreCommonDataSource> { MbCoreCommonLocalDataSource(get(), get(), get(), get()) }

  single<MbGameApi> { KtorMbGameApiImpl(mixpanel = get()) }
  single<MbGameDataSource> { MbGameRemoteDataSource(get()) }
  single<MbGameRepository> { MbGameRepositoryImpl(get(), get(), get(), get<AnalyticsProvider>()) }
  //
  single<MbCoreAuthApi> { KtorCoreAuthApiImpl(mixpanel = get<AnalyticsProvider>()) }
  single<MbCoreAuthDataSource> { MbCoreAuthRemoteDataSource(get()) }
  single<MbCoreAuthRepository> { MbCoreAuthRepositoryImpl(get(), get(), get()) }
}

val coreModule = commonModule + authModule + paymentModule + eventModule + mixpanelModule + networkMonitorModule