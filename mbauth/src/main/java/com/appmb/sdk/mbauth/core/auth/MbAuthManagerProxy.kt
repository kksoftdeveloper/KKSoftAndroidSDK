package com.appmb.sdk.mbauth.core.auth

import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.LoginResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow

/**
 * Interface for managing authentication operations.
 * Extends the MbAuthProvider interface to provide additional methods for OTP requests, verification, logout, and session refresh.
 */
internal class MbAuthManagerProxy(val mbAuthManager: MbAuthManager) :
  MbAuthManager by mbAuthManager {

  fun validateCommon(authParams: MbAuthParams): Boolean {
    return MbSdk.getConfig().isValid()
  }

  fun validateGameId(authParams: MbAuthParams): Boolean {
    return !(authParams.gameId == null || authParams.gameId == 0)
  }

  fun validateAppVersion(authParams: MbAuthParams): Boolean {
    return !(authParams.appVersion == null || authParams.appVersion?.isBlank() == true)
  }

  fun validatePackageName(authParams: MbAuthParams): Boolean {
    return !(authParams.appPackageName == null || authParams.appPackageName?.isBlank() == true)
  }

  override suspend fun login(authParams: MbAuthParams): Flow<LoginResult> {
    if (MbSdk.getConfig().isValid().not()) {
      return flow {
        emit(LoginResult.Error.from(authErrorCode = AuthErrorCodeResponse.SDKNotInitialized))
      }
    }

    val validGameId = validateGameId(authParams)
    val validAppVersion = validateAppVersion(authParams)
    val validPackageName = validatePackageName(authParams)
    if(!validGameId || !validAppVersion || !validPackageName) {
      return flow {
        emit(LoginResult.Error.from(authErrorCode = AuthErrorCodeResponse.MatchError))
      }
    }
    return mbAuthManager.login(authParams)
  }
}