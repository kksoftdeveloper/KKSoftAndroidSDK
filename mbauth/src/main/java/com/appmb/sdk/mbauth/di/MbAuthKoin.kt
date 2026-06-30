package com.appmb.sdk.mbauth.di

import android.util.Log
import com.appmb.sdk.mbauth.core.auth.MbAuthManager
import com.appmb.sdk.mbauth.core.auth.MbAuthManagerImpl
import com.appmb.sdk.mbauth.core.auth.MbAuthManagerProxy
import com.appmb.sdk.mbauth.core.game.MbGameManager
import com.appmb.sdk.mbauth.core.game.MbGameManagerImpl
import com.appmb.sdk.mbauth.core.provider.MbAuthProviderFactory
import com.appmb.sdk.mbauth.core.server.MbServerManager
import com.appmb.sdk.mbauth.core.server.MbServerManagerImpl
import com.appmb.sdk.mbauth.data.datasource.MbAuthDataSource
import com.appmb.sdk.mbauth.data.datasource.MbAuthRemoteDataSource
import com.appmb.sdk.mbauth.data.datasource.MbCommonDataSource
import com.appmb.sdk.mbauth.data.datasource.MbCommonLocalDataSource
import com.appmb.sdk.mbauth.data.datasource.MbServerDataSource
import com.appmb.sdk.mbauth.data.datasource.MbServerRemoteDataSource
import com.appmb.sdk.mbauth.data.repo.MbAuthRepositoryImpl
import com.appmb.sdk.mbauth.data.repo.MbServerRepositoryImpl
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.domain.MbServerRepository
import com.appmb.sdk.mbauth.network.MbAuthApi
import com.appmb.sdk.mbauth.network.impl.KtorMbAuthApiImpl
import com.appmb.sdk.mbauth.ui.deactivate.DeactivateAccountViewModel
import com.appmb.sdk.mbauth.ui.linkaccount.LinkAccountViewModel
import com.appmb.sdk.mbauth.ui.login.AuthViewModel
import com.appmb.sdk.mbauth.ui.logout.LogoutViewModel
import com.appmb.sdk.mbauth.ui.otp.OtpInputViewModel
import com.appmb.sdk.mbauth.ui.passwordinput.PasswordInputViewModel
import com.appmb.sdk.mbauth.ui.phoneinput.PhoneInputViewModel
import com.appmb.sdk.mbauth.ui.server.ServerViewModel
import com.appmb.sdk.mbauth.ui.tokenexpiration.TokenExpirationViewModel
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.config.MbConstant
import com.appmb.sdk.mbcore.data.datasource.game.MbGameDataSource
import com.appmb.sdk.mbcore.data.datasource.game.MbGameRemoteDataSource
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.facebook.FacebookSdk
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal object MbAuthKoin {

  fun loadModule() {
    MbSdk.loadModule(module {
      single<MbCommonDataSource> { MbCommonLocalDataSource(get(), get()) }
      single<MbAuthApi> { KtorMbAuthApiImpl(mixpanel = get<AnalyticsProvider>()) }
      single<MbAuthDataSource> { MbAuthRemoteDataSource(get()) }
      single<MbAuthRepository> { MbAuthRepositoryImpl(get(), get(), get(), get(), get()) }
      single<MbAuthProviderFactory> { MbAuthProviderFactory(get()) }
      single<MbAuthManager> { MbAuthManagerProxy(MbAuthManagerImpl(get(), get<AnalyticsProvider>())) }
      // Server
      single<MbServerManager> { MbServerManagerImpl(get(), get<AnalyticsProvider>(), get()) }
      single<MbServerDataSource> { MbServerRemoteDataSource(get()) }
//      single<MbGameDataSource> { MbGameRemoteDataSource(get()) }
      single<MbServerRepository> { MbServerRepositoryImpl(get(), get()) }
      //Game
      single<MbGameManager> { MbGameManagerImpl(get(), get()) }

      // ViewModel
      viewModel { PhoneInputViewModel() }
      viewModel { (phoneNumber: String, otpType: String, otpLength: Int) ->
        OtpInputViewModel(
          phoneNumber,
          otpType,
          otpLength
        )
      }
      viewModel { PasswordInputViewModel() }
      viewModel { (gameId: Int?) -> AuthViewModel(gameId, get(), get(), get()) }
      viewModel { LogoutViewModel(get()) }
      viewModel { LinkAccountViewModel() }
      viewModel { ServerViewModel() }
      viewModel { DeactivateAccountViewModel(get()) }
      viewModel { TokenExpirationViewModel(get()) }
    })
  }
}