package com.appmb.sdk.mbauth.ui.linkaccount

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.LoginResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class LinkAccountViewModel internal constructor() : ViewModel() {

  var state by mutableStateOf(LinkAccountState())
    private set

  private val eventChannel = Channel<LinkAccountEvent>()
  val events = eventChannel.receiveAsFlow()

  fun onAction(action: LinkAccountAction) {
    when (action) {
      is LinkAccountAction.LinkSocialAccount -> {
        onLinkSocialAccount(action)
      }
    }
  }

  private fun onLinkSocialAccount(action: LinkAccountAction.LinkSocialAccount) {
    viewModelScope.launch {
      val authParams = MbAuthParams.buildLinkSocialAccount(
        context = action.context,
        linkAccountType = action.linkAccountType,
        googleAccount = action.googleSignInAccount
      )
      state = state.copy(
        isLoading = true,
        error = null
      )
      MbAuth.linkSocialAccount(authParams) { result ->
        when (result) {
          is LoginResult.Success -> {
            state = state.copy(
              isLoading = false,
              error = null,
              authResult = AuthResult.LinkAccount(result.data)
            )
            viewModelScope.launch {
              eventChannel.send(
                LinkAccountEvent.LinkSuccess(AuthResult.LinkAccount(result.data))
              )
            }
          }

          is LoginResult.Error -> {
            val errorCode = result.status ?: AuthErrorCodeResponse.UnknownError.code
            state = state.copy(
              isLoading = false,
              error = errorCode
            )
            viewModelScope.launch {
              eventChannel.send(
                LinkAccountEvent.Error(errorCode)
              )
            }
          }

          else -> {

          }
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
  }
}