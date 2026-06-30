package com.appmb.sdk.mbauth.core.provider

import android.content.Context
import android.util.Log
import androidx.activity.result.ActivityResultRegistryOwner
import arrow.core.Either
import arrow.core.left
import arrow.core.right
import com.appmb.sdk.mbauth.data.dto.response.LoginDataModel
import com.appmb.sdk.mbauth.data.dto.response.RegisterDataModel
import com.appmb.sdk.mbauth.domain.MbAuthRepository
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbcore.config.MbConstant
import com.appmb.sdk.mbcore.error.NetworkError
import com.facebook.CallbackManager
import com.facebook.FacebookCallback
import com.facebook.FacebookException
import com.facebook.FacebookSdk
import com.facebook.login.LoginManager
import com.facebook.login.LoginResult
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

internal class AuthenticationByFacebookProvider(
  val repository: MbAuthRepository,
) : MbAuthProvider {

  val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())

  override fun validate(authParams: MbAuthParams): Boolean {
    return authParams.context != null && FacebookSdk.isInitialized()
  }

  override suspend fun login(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    return loginFacebook(authParams.context).fold(
      ifLeft = {
        it.left()
      }, ifRight = {
        repository.loginByFacebook(it)
      }
    )

  }

  override suspend fun register(authParams: MbAuthParams): Either<NetworkError, RegisterDataModel> {
    return NetworkError.UnSupportOperationError("Not support this register method").left()
  }

  override suspend fun linkAccount(authParams: MbAuthParams): Either<NetworkError, LoginDataModel?> {
    return loginFacebook(authParams.context).fold(
      ifLeft = {
        it.left()
      }, ifRight = { facebookToken ->
        repository.linkSocialAccount(authParams, facebookToken)
      }
    )
  }

  private suspend fun loginFacebook(context: Context?): Either<NetworkError, String> {
    return suspendCoroutine { continuation ->
      context ?: return@suspendCoroutine continuation.resume(NetworkError.DataNullError.left())
      val callbackManager = CallbackManager.Factory.create()
      val loginManager = LoginManager.getInstance()
      loginManager.logOut()
      loginManager.registerCallback(callbackManager, object : FacebookCallback<LoginResult> {

        override fun onCancel() {
          // Handle the login cancellation.
          Log.d(MbConstant.TAG, "onCancel: ")
          continuation.resume(NetworkError.DataNullError.left())
        }

        override fun onError(error: FacebookException) {
          Log.d(MbConstant.TAG, "error: $error")
          continuation.resume(NetworkError.DataNullError.left())
        }

        override fun onSuccess(result: LoginResult) {
          Log.d(MbConstant.TAG, "result:token ${result.accessToken}")
          coroutineScope.launch {
            continuation.resume(result.accessToken.token.right())
          }
        }
      })

      loginManager.logInWithReadPermissions(
        context as ActivityResultRegistryOwner,
        callbackManager,
        listOf("public_profile")
      )
    }
  }

}
