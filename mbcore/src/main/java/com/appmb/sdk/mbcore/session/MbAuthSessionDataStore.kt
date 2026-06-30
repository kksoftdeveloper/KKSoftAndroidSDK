package com.appmb.sdk.mbcore.session

import androidx.datastore.preferences.core.stringPreferencesKey
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.error.NetworkError
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.model.isNullOrEmpty
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.launch

class MbAuthSessionDataStore : MbAuthSession {

  val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
  val sessionKey = stringPreferencesKey("secured_data")
  val otpVerifiedTokenKey = stringPreferencesKey("otp_verified_token")

  override fun setResult(result: LoginResult) {
    when (result) {
      is LoginResult.Error -> clear()
      is LoginResult.Success -> save(result.data)
      is LoginResult.UnknownServerConfiguration -> save(result.data)
    }
  }

  override fun save(mbAuthData: MbAuthData) {
    coroutineScope.launch {
      MbSdk.getDataStoreManager().putSecurePreference(
        sessionKey,
        mbAuthData
      )
    }
  }

  override fun clear() {
    coroutineScope.launch {
      MbSdk.getDataStoreManager().removePreference(sessionKey)
      MbSdk.getDataStoreManager().removePreference(otpVerifiedTokenKey)
    }
  }

  override fun isAuthenticated(): Flow<Boolean> = flow {
    val data = getSessionData().firstOrNull()
    emit(data.isNullOrEmpty().not())
  }

  override fun getSessionData(): Flow<MbAuthData?> {
    return MbSdk.getDataStoreManager().getSecurePreference(
      sessionKey,
      MbAuthData()
    )
  }

  override suspend fun getLatestSessionData(): Either<NetworkError, MbAuthData> {
    val session = getSessionData().firstOrNull() ?: return NetworkError.DataNullError.left()
    val serverId = session.serverId.orEmpty()
    if (serverId.isEmpty()) {
      return NetworkError.DataNullError.left()
    }
    return session.right()
  }

  override fun saveOtpVerifiedToken(otpVerifiedToken: String) {
    coroutineScope.launch {
      MbSdk.getDataStoreManager().putSecurePreference(
        otpVerifiedTokenKey,
        otpVerifiedToken
      )
    }
  }

  override suspend fun getOtpVerifiedToken(): Flow<String> {
    return MbSdk.getDataStoreManager().getSecurePreference(
      otpVerifiedTokenKey,
      "",
    )
  }

  companion object {
    fun default(): MbAuthSession = MbAuthSessionDataStore()
  }
}