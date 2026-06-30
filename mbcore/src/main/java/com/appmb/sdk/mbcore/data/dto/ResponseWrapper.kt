package com.appmb.sdk.mbcore.data.dto

import android.annotation.SuppressLint
import kotlinx.serialization.Serializable

@SuppressLint("UnsafeOptInUsageError")
@Serializable
class ResponseWrapper<T>(
  val status: Int? = null,
  val message: String? = null,
  val data: T? = null,
) {
  fun isSuccessResponse() = (status ?: 0) > 0
}