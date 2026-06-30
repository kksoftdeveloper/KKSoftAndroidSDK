package com.appmb.sdk.mbauth.data.datasource

import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource

interface MbCommonDataSource : MbCoreCommonDataSource {
  fun getSdkVersion(): String
  suspend fun getOtpVerifiedToken(): String
}