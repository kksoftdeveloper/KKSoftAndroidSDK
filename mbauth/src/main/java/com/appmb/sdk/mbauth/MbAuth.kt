package com.appmb.sdk.mbauth

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.activity.result.ActivityResultLauncher
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appmb.sdk.mbauth.core.auth.MbAuthManager
import com.appmb.sdk.mbauth.core.game.MbGameManager
import com.appmb.sdk.mbauth.core.server.MbServerManager
import com.appmb.sdk.mbauth.di.MbAuthKoin
import com.appmb.sdk.mbcore.model.server.GetListServerIdsResult
import com.appmb.sdk.mbauth.model.GetSessionResult
import com.appmb.sdk.mbauth.model.LogoutResult
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.RequestOtpResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.model.SdkParams
import com.appmb.sdk.mbauth.model.UpdateServerIdResult
import com.appmb.sdk.mbauth.model.VerifyOtpResult
import com.appmb.sdk.mbauth.model.game.GameInfoResult
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.worker.WorkerManager
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbauth.tracking.AuthTracking
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.isNullOrEmpty
import com.appmb.sdk.mbcore.utils.VersionInfo
import com.appmb.sdk.mbcore.error.NetworkError
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.launch
import org.koin.java.KoinJavaComponent

/**
 * Object for managing authentication operations and sessions.
 */
object MbAuth {
  const val ACTION_TOKEN_EXPIRATION: String = "ACTION_TOKEN_EXPIRATION"
  const val ACTION_USER_BLOCKED: String = "ACTION_USER_BLOCKED"
  const val ACTION_SERVER_MAINTENANCE: String = "ACTION_SERVER_MAINTENANCE"

  // Manager for handling authentication operations.
  private val mbAuthManager: MbAuthManager by lazy {
    MbSdk.getKoin().get<MbAuthManager>()
  }

  // Manager for handling server operations.
  private val mbServerManager: MbServerManager by lazy {
    MbSdk.getKoin().get<MbServerManager>()
  }

  private val mbGameManager: MbGameManager by lazy {
    MbSdk.getKoin().get<MbGameManager>()
  }

  private val mbCoreCommonDataSource: MbCoreCommonDataSource by lazy {
    MbSdk.getKoin().get<MbCoreCommonDataSource>()
  }

  val context: Context by KoinJavaComponent.inject(Context::class.java)

  fun init() {
    MbSdk.getConfig().setAuthSdkVersion(BuildConfig.LIBRARY_VERSION)
    MbAuthKoin.loadModule()
  }

  @JvmStatic
  fun startAuthenticationForResult(
    activity: Activity,
    requestCode: Int
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      AuthTracking.logOpenLoginForm()
      val intent = Intent(activity, AuthActivity::class.java)
      intent.putExtra(
        AuthActivity.EXTRA_DATA,
        SdkParams.Authenticate(gameId = mbGameManager.getGameId())
      )
      activity.startActivityForResult(intent, requestCode)
    }
  }

  @JvmStatic
  fun startCheckingForceUpdateForResult(
    activity: Activity,
    requestCode: Int
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val isForceUpdateRequired = mbCoreCommonDataSource.isForceUpdateRequired()
      if (isForceUpdateRequired) {
        val intent = Intent(activity, AuthActivity::class.java)
        intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.UpdateApp)
        activity.startActivityForResult(intent, requestCode)
        return@launch
      }
    }
  }

  suspend fun startAuthentication(
    context: Context,
    launcher: ActivityResultLauncher<Intent>,
    startAuthenticationSuccess: () -> Unit,
    onResult: (GetSessionResult) -> Unit,
  ) {
    Log.e("MbAuth", "inside startAuthentication")
    val intent = Intent(context, AuthActivity::class.java)
    startAuthenticationSuccess.invoke()
    if (VersionInfo.isOutDatedVersion() && !VersionInfo.alreadyDisplayRequireUpdatePopup) {
      intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.UpdateApp)
      launcher.launch(intent)
      return
    }

    val result = mbAuthManager.getSessionData().firstOrNull()
    result?.let { sessionData ->
      if (sessionData.isNullOrEmpty()) {
        intent.putExtra(
          AuthActivity.EXTRA_DATA,
          SdkParams.Authenticate(gameId = mbGameManager.getGameId())
        )
        launcher.launch(intent)
      } else {
        if (sessionData.gameUuid.isNullOrEmpty()) {
          intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.ChooseServer(isEnableClose = false))
          launcher.launch(intent)
          return
        }
        onResult.invoke(GetSessionResult.Success(sessionData))
      }
    }
  }

  @JvmStatic
  fun startTokenExpirationForResult(
    activity: Activity,
    requestCode: Int
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val intent = Intent(activity, AuthActivity::class.java)
      intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.TokenExpiration)
      activity.startActivityForResult(intent, requestCode)
    }
  }

  @JvmStatic
  fun startUserBlock(
    activity: Activity
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val intent = Intent(activity, AuthActivity::class.java)
      intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.UserBlocked)
      activity.startActivity(intent)
    }
  }

  @JvmStatic
  fun startServerMaintenance(
    activity: Activity
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val intent = Intent(activity, AuthActivity::class.java)
      intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.ServerMaintenance)
      activity.startActivity(intent)
    }
  }

  @JvmStatic
  fun startLogoutForResult(
    activity: Activity,
    requestCode: Int
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val intent = Intent(activity, AuthActivity::class.java)
      intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.Logout)
      activity.startActivityForResult(intent, requestCode)
    }
  }

  fun logout(context: Context, launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(context, AuthActivity::class.java).apply {
      putExtra(AuthActivity.EXTRA_DATA, SdkParams.Logout)
    }
    launcher.launch(intent)
  }

  @JvmStatic
  fun countDownForGuestAsync(callback: () -> Unit) {
    CoroutineScope(Dispatchers.Main).launch {
      WorkerManager.startCountDown()
      callback()
    }
  }

  @JvmStatic
  fun countDownForGuest() {
    CoroutineScope(Dispatchers.Main).launch {
      val isGuest = mbCoreCommonDataSource.isGuestUser()
      Log.d("MbAuth", "countDownForGuest called - isGuestUser: $isGuest")
      if (isGuest == true) {
        WorkerManager.startCountDown()
      } else {
        Log.d("MbAuth", "User is not guest, cancelling timer")
        WorkerManager.cancelTimer()
      }
    }
  }

  @JvmStatic
  fun startLinkAccountScreenForResult(activity: Activity, requestCode: Int) {
    val intent = Intent(activity, AuthActivity::class.java)
    intent.putExtra(AuthActivity.EXTRA_DATA, SdkParams.LinkAccount)
    activity.startActivityForResult(intent, requestCode)
  }


  /**
   * Authenticates the user with the provided authentication parameters.
   *
   * @param authParams The parameters required for authentication.
   * @param onResult The callback to be invoked with the authentication result.
   */
  suspend fun login(
    authParams: MbAuthParams,
    onResult: (LoginResult) -> Unit,
  ) {
    val result = mbAuthManager.login(authParams = authParams)
    result.collect { mbAuthResult ->
      // Set result to session manager and invoke callback
      onResult.invoke(mbAuthResult)
    }
  }

  /**
   * Registers a new user with the provided authentication parameters.
   *
   * @param authParams The parameters required for registration.
   * @param onResult The callback to be invoked with the registration result.
   */
  suspend fun register(
    authParams: MbAuthParams,
    onResult: (RegisterResult) -> Unit,
  ) {
    val result = mbAuthManager.register(authParams = authParams)
    result.collect { registerResult ->
      // invoke callback
      onResult.invoke(registerResult)
    }
  }

  /**
   * Logs out the user by clearing the session.
   *
   * @return A Flow emitting a boolean indicating whether the logout operation was successful.
   */
  suspend fun logout(onResult: (LogoutResult) -> Unit) {
    val result = mbAuthManager.logout()
    result.collect { mbAuthResult ->
      onResult.invoke(mbAuthResult)
    }
  }

  /**
   * Requests an OTP for the provided phone number.
   *
   * @param mbAuthParams The request OTP Params to request the OTP.
   *  @param onResult The callback to be invoked with the request OTP result.
   */
  suspend fun requestOtp(
    mbAuthParams: MbAuthParams,
    onResult: (RequestOtpResult) -> Unit,
  ) {
    val result = mbAuthManager.requestOtp(mbAuthParams)
    result.collect { requestOtpResult ->
      // invoke callback
      onResult.invoke(requestOtpResult)
    }
  }

  /**
   * Verifies the OTP for the provided phone number.
   *
   * @param mbAuthParams The verify OTP params to verify the OTP.
   *  @param onResult The callback to be invoked with the OTP verification result.
   */
  suspend fun verifyOtp(
    mbAuthParams: MbAuthParams,
    onResult: (VerifyOtpResult) -> Unit,
  ) {
    val result = mbAuthManager.verifyOtp(mbAuthParams)
    result.collect { verifyOtpResult ->
      // invoke callback
      onResult.invoke(verifyOtpResult)
    }
  }

  fun deactivateAccount(context: Context, launcher: ActivityResultLauncher<Intent>) {
    val intent = Intent(context, AuthActivity::class.java).apply {
      putExtra(AuthActivity.EXTRA_DATA, SdkParams.DeactivateAccount)
    }
    launcher.launch(intent)
  }

  @JvmStatic
  fun deactivateAccountForResult(
    activity: Activity,
    requestCode: Int
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val intent = Intent(activity, AuthActivity::class.java).apply {
        putExtra(AuthActivity.EXTRA_DATA, SdkParams.DeactivateAccount)
      }
      activity.startActivityForResult(intent, requestCode)
    }
  }

  @JvmStatic
  fun changeGameServerForResult(
    activity: Activity,
    requestCode: Int
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val intent = Intent(activity, AuthActivity::class.java).apply {
        putExtra(AuthActivity.EXTRA_DATA, SdkParams.ChooseServer(isEnableClose = true))
      }
      activity.startActivityForResult(intent, requestCode)
    }
  }

  /**
   * Checks if the user is authenticated.
   *
   * @return A boolean indicating whether the user is authenticated.
   */
  fun isAuthenticated(): Flow<Boolean> {
    return mbAuthManager.isAuthenticated()
  }

  suspend fun getSessionData(
    onResult: (GetSessionResult) -> Unit,
  ) {
    val result = mbAuthManager.getSessionData()
    result.collect { sessionData ->
      if (sessionData.isNullOrEmpty())
        onResult.invoke(GetSessionResult.Error.from(
          authErrorCode = AuthErrorCodeResponse.NotFound
        ))
      else
        onResult.invoke(GetSessionResult.Success(sessionData!!))
    }
  }

  @JvmStatic
  fun getSessionDatas(
    onResult: (AuthResult) -> Unit
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      val latest = mbAuthManager.getLatestSessionData()
      latest.fold(
        ifLeft = { error ->
          val status = (error as? NetworkError.ApiError)?.errorBody?.status
            ?: (error as? NetworkError.ApiError)?.errorBody?.status ?: AuthErrorCodeResponse.Unauthorized.code
          val msg = (error as? NetworkError.ApiError)?.errorBody?.message
            ?: AuthErrorCodeResponse.Unauthorized.description
          onResult.invoke(AuthResult.Failure(status = status, msg = msg))
        },
        ifRight = { data ->
          onResult.invoke(AuthResult.AuthSuccess(user = data))
        }
      )
    }
  }

  /**
   * Get list server ids for client selection
   *
   * @param onResult The callback to be invoked with the list of server ids
   */
  suspend fun getListServerIds(
    onResult: (GetListServerIdsResult) -> Unit,
  ) {
    mbServerManager.getListServerIds().collect {
      onResult.invoke(it)
    }
  }

  /**
   * Update server id for game client
   *
   * @param serverId The new server id
   * @param onResult The callback to be invoked with the update result
   */
  suspend fun getGameUuid(
    serverId: String,
    onResult: (UpdateServerIdResult) -> Unit,
  ) {
    mbServerManager.getGameUuid(serverId).collect {
      onResult.invoke(it)
    }
  }

  suspend fun getGameInfo(
    onResult: (GameInfoResult) -> Unit,
  ) {
    mbGameManager.getGameInfo().collect {
      onResult.invoke(it)
    }
  }

  /**
   * Refresh token operation
   *
   * @param onResult The callback to be invoked with the refresh token result
   */
  suspend fun refreshToken(
    onResult: (LoginResult) -> Unit,
  ) {
    mbAuthManager.refreshSession().collect {
      onResult.invoke(it)
    }
  }

  @JvmStatic
  fun refreshTokenForResult(
    activity: Activity,
    requestCode: Int,
    onResult: (AuthResult) -> Unit
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      mbAuthManager.refreshSession().collect { result ->
        when (result) {
          is LoginResult.Success -> {
            Log.d("MbAuth", "Token refreshed successfully: ${result.data.accessToken}")
            onResult(
              AuthResult.AuthSuccess(
                user = result.data
              )
            )
          }

          is LoginResult.Error -> {
            Log.e("MbAuth", "Error refreshing token: ${result.message}")
            onResult(
              AuthResult.Failure(
                status = result.status ?: 0,
                msg = result.message ?: "Unknown error occurred"
              )
            )
          }

          is LoginResult.UnknownServerConfiguration -> {
            Log.e("MbAuth", "Unknown server configuration, prompting user to select server")
            changeGameServerForResult(
              activity = activity,
              requestCode = requestCode
            )
          }

        }
      }
    }
  }

  /**
   * Link Guest user to social account
   *
   * @param authParams The parameters required for link social account
   * @param onResult The callback to be invoked with the link account result
   */
  suspend fun linkSocialAccount(
    authParams: MbAuthParams,
    onResult: (LoginResult) -> Unit,
  ) {
    mbAuthManager.linkAccount(authParams).collect {
      onResult.invoke(it)
    }
  }

  /**
   * Link Guest user to phone account
   *
   * @param authParams The parameters required for link phone account
   * @param onResult The callback to be invoked with the link account result
   */
  suspend fun linkPhoneAccount(
    authParams: MbAuthParams,
    onResult: (LoginResult) -> Unit,
  ) {
    mbAuthManager.linkAccount(authParams).collect {
      onResult.invoke(it)
    }
  }

  /**
   * Reset password
   *
   * @param authParams The parameters required for reset password
   * @param onResult The callback to be invoked with the reset password result
   */
  suspend fun resetPassword(
    authParams: MbAuthParams,
    onResult: (ResetPasswordResult) -> Unit,
  ) {
    mbAuthManager.resetPassword(authParams).collect {
      onResult.invoke(it)
    }
  }

  /**
   * Update server client ID - Java/Unity friendly static method
   * If called when unauthenticated, serverName will only be saved locally.
   * When authenticated, serverName (must be not null and not empty) will be used to:
   * 1. Fetch server list from API
   * 2. Find serverId by matching serverName
   * 3. Save serverId and serverName into local storage
   * 4. Call API characters/me. The successful response containing gameUId and characterId
   *    will be saved into local storage.
   *
   * @param serverName The server name to update
   * @param onResult The callback to be invoked with the update result
   */
  @JvmStatic
  fun updateServerClientId(
    serverName: String?,
    onResult: (UpdateServerIdResult) -> Unit,
  ) {
    CoroutineScope(Dispatchers.Main).launch {
      mbServerManager.updateServerClientId(serverName).collect {
        onResult.invoke(it)
      }
    }
  }

  fun releaseMbAuthSDK() {
    CoroutineScope(Dispatchers.Main).launch {
      WorkerManager.cancelTimer()
    }
    MbSdk.onClear()
  }

}