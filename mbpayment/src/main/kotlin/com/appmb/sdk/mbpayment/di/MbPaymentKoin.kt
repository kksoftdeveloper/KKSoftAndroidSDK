package com.appmb.sdk.mbpayment.di

import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import com.appmb.sdk.mbtracking.di.TrackingLoader
import com.appmb.sdk.mbpayment.data.datasource.BillingDataSource
import com.appmb.sdk.mbpayment.data.datasource.MbPaymentDataSource
import com.appmb.sdk.mbpayment.data.repository.BillingRepositoryImpl
import com.appmb.sdk.mbpayment.data.repository.MbPaymentRepositoryImpl
import com.appmb.sdk.mbpayment.domain.BillingRepository
import com.appmb.sdk.mbpayment.domain.FetchListProductsUseCase
import com.appmb.sdk.mbpayment.domain.MbPaymentRepository
import com.appmb.sdk.mbpayment.network.KtorMbPaymentApiImpl
import com.appmb.sdk.mbpayment.network.MbPaymentApi
import com.appmb.sdk.mbpayment.ui.ProductListViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

internal object MbPaymentKoin {

  fun loadModule() {
    // Ensure Tracking SDK is loaded
    TrackingLoader.loadOnce()
    MbSdk.loadModule(module {
      single<MbPaymentApi> { KtorMbPaymentApiImpl(mixpanel = get<AnalyticsProvider>()) }
      single { BillingDataSource(get()) }
      single { MbPaymentDataSource(get()) }
      single<BillingRepository> { BillingRepositoryImpl(get(), get()) }
      single<MbPaymentRepository> { MbPaymentRepositoryImpl(get(), get(), get()) }
      factory { FetchListProductsUseCase(get()) }

      viewModel { ProductListViewModel(get(), get(), get(), get(), androidContext()) }
    })
  }
}