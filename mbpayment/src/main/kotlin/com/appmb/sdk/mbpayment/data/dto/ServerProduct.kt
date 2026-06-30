package com.appmb.sdk.mbpayment.data.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ServerProduct(
  @SerialName("id") val id: Int,
  @SerialName("sku") val sku: String,
  @SerialName("price") val price: Double
)

@Serializable
data class Pagination(
  @SerialName("page") val page: Int,
  @SerialName("limit") val limit: Int,
  @SerialName("totalPages") val totalPages: Int,
  @SerialName("total") val total: Int
)

@Serializable
data class ProductListData(
  @SerialName("content") val content: List<ServerProduct>,
  @SerialName("pagination") val pagination: Pagination
)

@Serializable
data class ProductListResponse(
  @SerialName("status") val status: Int,
  @SerialName("message") val message: String,
  @SerialName("data") val data: ProductListData
)
