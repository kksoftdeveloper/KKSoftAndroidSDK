package com.appmb.sdk.mbcore.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.emptyPreferences
import com.appmb.sdk.mbcore.utils.SecurityUtil
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map
import kotlinx.serialization.json.Json
import kotlinx.serialization.serializer
import java.io.IOException

class DataStoreManager(
  val dataStore: DataStore<Preferences>,
  val json: Json,
  val securityUtil: SecurityUtil = SecurityUtil(),
  val keyAlias: String = "data-store",
  val ivToStringSeparator: String = ":iv:",
  val bytesToStringSeperator: String = "|",
) {

  fun <T> getPreference(
    key: Preferences.Key<T>,
    defaultValue: T? = null
  ): Flow<T?> =
    dataStore.data
      .catch { exception ->
        if (exception is IOException) {
          emit(emptyPreferences())
        } else {
          throw exception
        }
      }
      .map { preferences ->
        preferences[key] ?: defaultValue
      }

  suspend fun <T> putPreference(
    key: Preferences.Key<T>,
    value: T?
  ) {
    dataStore.edit { preferences ->
      if (value == null) {
        preferences.remove(key)
      } else {
        preferences[key] = value
      }
    }
  }

  suspend inline fun <reified T> putSecurePreference(
    key: Preferences.Key<String>,
    value: T,
  ) {
    dataStore.edit { preferences ->
      val serializedInput = json.encodeToString(serializer(), value)
      val (iv, secureByteArray) = securityUtil.encryptData(keyAlias, serializedInput)
      val secureString =
        iv.joinToString(bytesToStringSeperator) + ivToStringSeparator + secureByteArray.joinToString(
          bytesToStringSeperator
        )
      preferences[key] = secureString
    }
  }

  inline fun <reified T> getSecurePreference(
    key: Preferences.Key<String>,
    defaultValue: T,
  ): Flow<T> = dataStore.data.catch { exception ->
    if (exception is IOException) {
      emit(emptyPreferences())
    } else {
      throw exception
    }
  }.map { preferences ->
    val secureString = preferences[key] ?: return@map defaultValue
    val (ivString, encryptedString) = secureString.split(ivToStringSeparator, limit = 2)
    val iv = ivString.split(bytesToStringSeperator).map { it.toByte() }.toByteArray()
    val encryptedData =
      encryptedString.split(bytesToStringSeperator).map { it.toByte() }.toByteArray()
    val decryptedValue = securityUtil.decryptData(keyAlias, iv, encryptedData)
    val deserializer = json.serializersModule.serializer<T>()
    json.decodeFromString(deserializer, decryptedValue)
  }


  suspend fun <T> removePreference(key: Preferences.Key<T>) {
    dataStore.edit {
      it.remove(key)
    }
  }

  suspend fun clearAllPreference() {
    dataStore.edit { preferences ->
      preferences.clear()
    }
  }
}

