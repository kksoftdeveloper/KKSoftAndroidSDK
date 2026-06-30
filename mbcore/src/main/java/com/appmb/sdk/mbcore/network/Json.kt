package com.appmb.sdk.mbcore.network

import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.json.Json

@OptIn(ExperimentalSerializationApi::class)
val json = Json {
  ignoreUnknownKeys = true
  useAlternativeNames = false
  coerceInputValues = true
  prettyPrint = true
  isLenient = true
  encodeDefaults = true
  explicitNulls = false
  allowSpecialFloatingPointValues = true
}

fun parseJsonToMapGson(json: String): Map<String, Any?> {
  val gson = Gson()
  val type = object : TypeToken<Map<String, Any?>>() {}.type
  return gson.fromJson(json, type)
}