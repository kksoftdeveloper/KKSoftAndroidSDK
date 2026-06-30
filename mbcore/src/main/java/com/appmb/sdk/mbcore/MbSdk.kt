package com.appmb.sdk.mbcore

import android.content.Context
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.data.dto.response.MbRefreshTokenResponse
import com.appmb.sdk.mbcore.datastore.DataStoreManager
import com.appmb.sdk.mbcore.di.IsolatedKoinContext
import com.appmb.sdk.mbcore.di.MbSdkQualifiers
import com.appmb.sdk.mbcore.di.coreModule
import com.appmb.sdk.mbcore.domain.MbInitialize
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.network.NetworkClient
import com.appmb.sdk.mbcore.platform.MbDeviceInfo
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import com.mixpanel.android.mpmetrics.MixpanelAPI
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext
import org.koin.core.context.startKoin
import org.koin.core.module.Module
import org.koin.core.qualifier.named
import org.koin.dsl.module

object MbSdk {
  private var initialized = false
  private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  fun init(context: Context, block: () -> MbSdkConfig) {
    MixpanelAPI.getInstance(context, BuildConfig.ANALYTICS_EVENT_TOKEN, true)
    val dynamicModule = module {
      single {
        context
      }
      single {
        block.invoke()
      }
    }
    val allModule = mutableListOf(dynamicModule) + coreModule
    startKoin {
      androidContext(context)
      modules(allModule)
    }
    initialized = true
    initApplicationSdk()
  }

  private fun initApplicationSdk() {
    ensureInitSdk()
    coroutineScope.launch {
      getDeviceInfo().initDeviceId()
      MbInitialize.initSdk()
    }
  }

  fun getDeviceInfo(): MbDeviceInfo {
    ensureInitSdk()
    return IsolatedKoinContext.koin.get()
  }

  fun loadModule(module: Module) {
    ensureInitSdk()
    IsolatedKoinContext.koin.loadModules(mutableListOf(module))
  }

  private fun ensureInitSdk() {
    if (!initialized) {
      throw IllegalStateException("MbSdk is not initialized. Please call MbSdk.init() first.")
    }
  }

  fun getKoin() = IsolatedKoinContext.koin

  fun getContext(): Context {
    ensureInitSdk()
    return IsolatedKoinContext.koin.get()
  }

  fun getAuthNetwork(): NetworkClient {
    ensureInitSdk()
    return IsolatedKoinContext.koin.get(named(MbSdkQualifiers.Auth.NETWORK_CLIENT))
  }

  fun getPaymentNetwork(): NetworkClient {
    ensureInitSdk()
    return IsolatedKoinContext.koin.get(named(MbSdkQualifiers.Payment.NETWORK_CLIENT))
  }

  fun getCoreNetwork(): NetworkClient {
    ensureInitSdk()

    return IsolatedKoinContext.koin.get(named(MbSdkQualifiers.Auth.NETWORK_CLIENT))
  }

  fun getDataStoreManager(): DataStoreManager {
    ensureInitSdk()
    return IsolatedKoinContext.koin.get()
  }

  fun getConfig(): MbSdkConfig {
    ensureInitSdk()
    return IsolatedKoinContext.koin.get()
  }

  fun getCurrentSessionData(): MbAuthData? =
    runBlocking {
      getSession()
        .getSessionData()
        .firstOrNull()
    }

  fun onClear() {
    coroutineScope.cancel()
  }

  suspend fun updateAuthenticationToken(result: MbRefreshTokenResponse) {
    result.refreshToken ?: return
    result.accessToken ?: return
    getAuthNetwork().invalidateAuthTokens()
    val currentSession = getSession().getSessionData().firstOrNull()
    currentSession?.let { session ->
      getSession().save(result.updateMbAuthData(session))
      getAuthNetwork().saveToken(
        accessToken = session.accessToken.orEmpty(),
        refreshToken = session.refreshToken.orEmpty()
      )
    }
  }
}