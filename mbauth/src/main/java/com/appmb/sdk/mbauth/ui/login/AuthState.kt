package com.appmb.sdk.mbauth.ui.login

import com.appmb.sdk.mbcore.data.dto.response.GameInfoData
import com.appmb.sdk.mbcore.model.MbAuthData

sealed interface LoginState {
  object Idle : LoginState
  object Loading : LoginState
  data class Error(val message: String) : LoginState
  object UnknownServer : LoginState
  data class Success(val mbAuthData: MbAuthData) : LoginState
}

sealed interface RegisterState {
  object Idle : RegisterState
  object Loading : RegisterState
  data class Error(val message: String) : RegisterState
  object UnknownServer : RegisterState
  data class Success(val mbAuthData: MbAuthData) : RegisterState
}


sealed interface AuthState {
  object Idle : AuthState
  data class ShowAuthData(val isShow: Boolean = false, val data: MbAuthData?) : AuthState
  data class ShowIsLoggedIn(val isLoggedIn: Boolean = false) : AuthState
  data class ShowLoginSocialState(val isSuccess: Boolean) : AuthState
  data class Logout(val isSuccess: Boolean = false) : AuthState
  object ShowSelectServerId : AuthState
}


sealed interface GetGameUuidState {
  object Idle : GetGameUuidState
  data class Success(val mbAuthData: MbAuthData, val mbCharacterId: String) : GetGameUuidState
  data class Error(val message: String) : GetGameUuidState
}

sealed interface GameInfoState {
  object Idle : GameInfoState
  data class Success(val gameInfo: GameInfoData) : GameInfoState
  data class Error(val message: String) : GameInfoState
}

