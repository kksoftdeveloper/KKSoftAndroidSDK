package com.appmb.sdk.mbcore.domain

import android.util.Log
import arrow.core.Either
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.di.IsolatedKoinContext
import com.appmb.sdk.mbcore.domain.game.MbGameRepository
import org.koin.core.component.KoinComponent

object MbInitialize : KoinComponent {

  val mbGameRepository: MbGameRepository by lazy {
    IsolatedKoinContext.koin.get<MbGameRepository>()
  }

  suspend fun initSdk() {
    when (val result = mbGameRepository.fetchGameInfo()) {
      is Either.Left -> Log.e("MbInitialize", "Failed to fetch game info: ${result.value}")
      is Either.Right -> Log.i("MbInitialize", "Success to get game info")
    }
  }
}
