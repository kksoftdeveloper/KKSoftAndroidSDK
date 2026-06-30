package com.appmb.sdk.mbauth.data.datasource

import android.content.Context
import com.appmb.sdk.mbauth.BuildConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.session.MbAuthSessionFactory.getSession
import kotlinx.coroutines.flow.firstOrNull

class MbCommonLocalDataSource(
  val context: Context,
//  val deviceInfo: MbDeviceInfo,
  val coreCommonDataSource: MbCoreCommonDataSource,
) : MbCommonDataSource, MbCoreCommonDataSource by coreCommonDataSource {

  override fun getSdkVersion(): String {
    return BuildConfig.LIBRARY_VERSION
  }

  override suspend fun getOtpVerifiedToken(): String =
    getSession().getOtpVerifiedToken().firstOrNull().orEmpty()
}