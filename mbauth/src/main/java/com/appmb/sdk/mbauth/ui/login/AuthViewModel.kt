package com.appmb.sdk.mbauth.ui.login

import android.os.Build
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.appmb.sdk.mbauth.MbAuth
import com.appmb.sdk.mbauth.core.auth.MbAuthManager
import com.appmb.sdk.mbauth.model.GetSessionResult
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegisterResult
import com.appmb.sdk.mbauth.model.ResetPasswordResult
import com.appmb.sdk.mbauth.worker.WorkerManager
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.model.AuthErrorCodeResponse
import com.appmb.sdk.mbcore.model.LoginResult
import com.appmb.sdk.mbcore.model.MbAuthData
import com.appmb.sdk.mbcore.model.localization
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class AuthViewModel internal constructor(
  private val gameId: Int?,
  private val mbAuthManager: MbAuthManager,
  private val mbConfig: MbSdkConfig,
  private val mbCoreCommonDataSource: MbCoreCommonDataSource
) : ViewModel() {

  private val _uiState = MutableStateFlow(UiState(gameId = gameId))
  val uiState: StateFlow<UiState> = _uiState.asStateFlow()

  val registerState = MutableStateFlow<RegisterState>(RegisterState.Idle)
  val loginState = MutableStateFlow<LoginState>(LoginState.Idle)
  val authState = MutableStateFlow<AuthState>(AuthState.Idle)
  val linkPhoneAccountState = MutableStateFlow<RegisterState>(RegisterState.Idle)
  val resetPasswordState = MutableStateFlow<RegisterState>(RegisterState.Idle)

  private val _authResult = MutableStateFlow<AuthResult?>(null)
  val authResult: StateFlow<AuthResult?> = _authResult.asStateFlow()

  init {
    checkSocialLoginAvailability()
  }

  private fun checkSocialLoginAvailability() {
    viewModelScope.launch {
      val remoteGoogleClientId = mbCoreCommonDataSource.getGoogleClientId()
      val remoteFacebookAppId = mbCoreCommonDataSource.getFacebookAppId()
      val remoteFacebookClientToken = mbCoreCommonDataSource.getFacebookClientToken()
      val trackingConfig = mbConfig.getTrackingConfig()

      val isGoogleEnabled = !trackingConfig?.gidClientID.isNullOrBlank() ||
          !mbConfig.getGoogleClientId().isNullOrBlank() ||
          !remoteGoogleClientId.isNullOrBlank()

      val isFacebookEnabled = (!trackingConfig?.facebookAppID.isNullOrBlank() && !trackingConfig?.facebookClientToken.isNullOrBlank()) ||
          (!mbConfig.getFacebookAppId().isNullOrBlank() && !mbConfig.getFacebookClientToken().isNullOrBlank()) ||
          (!remoteFacebookAppId.isNullOrBlank() && !remoteFacebookClientToken.isNullOrBlank())

      _uiState.update { 
        it.copy(
          isGoogleEnabled = isGoogleEnabled,
          isFacebookEnabled = isFacebookEnabled
        ) 
      }
    }
  }

  fun onPhoneChange(phone: String) {
    _uiState.update { it.copy(phone = phone) }
  }

  fun onPasswordChange(password: String) {
    _uiState.update { it.copy(password = password) }
  }

  fun onAcceptTermsChange(accepted: Boolean) {
    _uiState.update { it.copy(acceptTerms = accepted) }
  }

  fun dispatch(intent: AuthIntent) {
    when (intent) {
      is AuthIntent.Login -> {
        // handle login
      }

      is AuthIntent.LoginByPhone -> handleLoginByPhone(
        intent.phone, intent.password
      )

      is AuthIntent.LoginOAuth -> {
        // handle login with OAuth
      }

      is AuthIntent.LoginOtp -> {
        // handle login with OTP
      }

      AuthIntent.GetAuthData -> handleGetAuthData()
      AuthIntent.CheckIsLoggedIn -> checkIsLoggedIn()
      AuthIntent.ResetAuthState -> resetAuthState()
      AuthIntent.Logout -> handleLogout()
      is AuthIntent.LoginByGoogle -> handleLoginByGoogle(intent)
      AuthIntent.LoginByGuest -> handleLoginByGuest()
      is AuthIntent.LoginByFacebook -> handleLoginByFacebook(intent)
      is AuthIntent.Register -> handleRegister(intent)

      is AuthIntent.LinkPhoneAccount -> {
        handleLinkPhoneAccount(intent)
      }

      is AuthIntent.ResetPassword -> {
        handleResetPassword(intent)
      }

      else -> {}
    }
  }

  private fun handleLoginByFacebook(intent: AuthIntent.LoginByFacebook) {
    viewModelScope.launch {
      val authParams = MbAuthParams.buildLoginByFacebook(
        intent.context,
        gameId = gameId,
        osVersion = Build.VERSION.SDK_INT.toString(),
        appVersion = mbConfig.getAppVersionName(),
        appPackageName = mbConfig.getAppId()
      )
      _uiState.update { it.copy(isLoading = true) }
      MbAuth.login(authParams) { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is LoginResult.Error -> {
            _authResult.update {
              AuthResult.Failure(
                status = result.status ?: AuthErrorCodeResponse.UnknownError.code,
                msg = result.message.orEmpty()
              )
            }
            _uiState.update {
              it.copy(
                errorCode = result.status ?: AuthErrorCodeResponse.UnknownError.code
              )
            }
          }

          is LoginResult.UnknownServerConfiguration -> {
            loginState.update {
              LoginState.UnknownServer
            }
          }

          is LoginResult.Success -> {
            _authResult.update {
              AuthResult.AuthSuccess(result.data)
            }
          }
        }
      }
    }
  }

  private fun handleLoginByGoogle(intent: AuthIntent.LoginByGoogle) {
    val authParams = MbAuthParams.buildLoginByGoogle(
      context = intent.context,
      osVersion = Build.VERSION.SDK_INT.toString(),
      account = intent.account,
      gameId = gameId,
      appVersion = mbConfig.getAppVersionName(),
      appPackageName = mbConfig.getAppId()
    )
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      mbAuthManager.login(authParams).collect { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is LoginResult.Error -> {
            _authResult.update {
              AuthResult.Failure(
                status = result.status ?: AuthErrorCodeResponse.UnknownError.code,
                result.message.orEmpty()
              )
            }
            _uiState.update {
              it.copy(
                errorCode = result.status ?: AuthErrorCodeResponse.UnknownError.code
              )
            }
          }

          is LoginResult.Success -> {
            _authResult.update {
              AuthResult.AuthSuccess(result.data)
            }
          }

          is LoginResult.UnknownServerConfiguration -> {
            loginState.update {
              LoginState.UnknownServer
            }
          }
        }
      }
    }
  }

  private fun handleLoginByGuest() {
    val authParams = MbAuthParams.buildLoginByGuest(
      osVersion = Build.VERSION.SDK_INT.toString(),
      gameId = gameId,
      appVersion = mbConfig.getAppVersionName(),
      appPackageName = mbConfig.getAppId()
    )
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      MbAuth.login(authParams) { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is LoginResult.Error -> {
            _authResult.update {
              AuthResult.Failure(
                status = result.status ?: AuthErrorCodeResponse.UnknownError.code,
                result.message.orEmpty()
              )
            }
            _uiState.update {
              it.copy(
                errorCode = result.status ?: AuthErrorCodeResponse.UnknownError.code
              )
            }
          }

          is LoginResult.Success -> {
            startCountDownTimer()
            _authResult.update {
              AuthResult.AuthSuccess(result.data)
            }
          }

          is LoginResult.UnknownServerConfiguration -> {
            loginState.update {
              LoginState.UnknownServer
            }
          }
        }
      }
    }
  }

  private fun startCountDownTimer() {
    viewModelScope.launch {
      WorkerManager.cancelTimer()
      WorkerManager.startCountDown()
    }
  }

  private fun handleLogout() {
    viewModelScope.launch {
      MbAuth.logout {
        authState.update {
          AuthState.Logout(true)
        }
      }
    }
  }

  private fun resetAuthState() {
    loginState.update {
      LoginState.Idle
    }
    authState.update {
      AuthState.Idle
    }
    registerState.update {
      RegisterState.Idle
    }
    linkPhoneAccountState.update {
      RegisterState.Idle
    }
  }

  private fun checkIsLoggedIn() {
    viewModelScope.launch {
      MbAuth.isAuthenticated().collect { isAuthenticated ->
        authState.update {
          AuthState.ShowIsLoggedIn(isAuthenticated)
        }
      }
    }
  }

  private fun handleGetAuthData() {
    viewModelScope.launch {
      MbAuth.getSessionData { result ->
        when (result) {
          is GetSessionResult.Success -> {
            _authResult.update {
              AuthResult.AuthSuccess(result.data)
            }
          }

          is GetSessionResult.Error -> {
            _authResult.update {
              AuthResult.Failure(
                result.code ?: AuthErrorCodeResponse.UnknownError.code,
                result.message.orEmpty()
              )
            }
          }
        }
      }
    }
  }

  fun handleLoginByPhone(phone: String, password: String) {
    val authParams = MbAuthParams.buildLoginByPhone(
      phone = phone,
      password = password,
      gameId = gameId,
      osVersion = Build.VERSION.SDK_INT.toString(),
      appVersion = mbConfig.getAppVersionName(),
      appPackageName = mbConfig.getAppId()
    )
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      MbAuth.login(authParams) { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is LoginResult.Success -> {
            loginState.update {
              LoginState.Success(result.data)
            }
            _authResult.update {
              AuthResult.AuthSuccess(result.data)
            }
          }

          is LoginResult.Error -> {
            loginState.update {
              LoginState.Error(result.message.orEmpty())
            }
            _authResult.update {
              AuthResult.Failure(
                status = result.status ?: AuthErrorCodeResponse.UnknownError.code,
                result.message.orEmpty()
              )
            }
            _uiState.update {
              it.copy(
                errorCode = result.status ?: AuthErrorCodeResponse.UnknownError.code
              )
            }
          }

          is LoginResult.UnknownServerConfiguration -> {
            loginState.update {
              LoginState.UnknownServer
            }
          }
        }
      }
    }
  }

  private fun handleRegister(intent: AuthIntent.Register) {
    // Handle register
    val authParams = MbAuthParams.buildRegister(
      phone = intent.phoneNumber,
      password = intent.password,
    )
    viewModelScope.launch {
      _uiState.update { it.copy(isLoading = true) }
      MbAuth.register(authParams) { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is RegisterResult.Success -> {
            _authResult.update {
              AuthResult.RegisterSuccess(result.data)
            }
            registerState.update {
              RegisterState.Success(result.data)
            }
          }

          is RegisterResult.Error -> {
            _authResult.update {
              AuthResult.Failure(
                result.code ?: AuthErrorCodeResponse.UnknownError.code,
                result.message.orEmpty()
              )
            }
            registerState.update {
              RegisterState.Error(result.message.orEmpty())
            }
          }

          is RegisterResult.UnknownServerConfiguration -> {
            registerState.update {
              RegisterState.UnknownServer
            }
          }
        }
      }
    }
  }

  private fun handleLinkPhoneAccount(intent: AuthIntent.LinkPhoneAccount) {
    viewModelScope.launch {
      val authParams = MbAuthParams.buildLinkPhoneAccount(
        phone = intent.phoneNumber,
        password = intent.password
      )
      _uiState.update {
        it.copy(isLoading = true)
      }
      MbAuth.linkPhoneAccount(authParams) { result ->
        _uiState.update { it.copy(isLoading = false) }
        when (result) {
          is LoginResult.Success -> {
            linkPhoneAccountState.update {
              RegisterState.Success(result.data)
            }
            _authResult.update {
              AuthResult.LinkAccount(result.data)
            }
          }

          is LoginResult.Error -> {
            linkPhoneAccountState.update {
              RegisterState.Error(result.message.orEmpty())
            }
            _authResult.update {
              AuthResult.Failure(
                status = result.status ?: AuthErrorCodeResponse.UnknownError.code,
                result.message.orEmpty()
              )
            }
          }

          is LoginResult.UnknownServerConfiguration -> {
            loginState.update {
              LoginState.UnknownServer
            }
          }
        }
      }
    }
  }

  private fun handleResetPassword(intent: AuthIntent.ResetPassword) {
    viewModelScope.launch {
      val authParams = MbAuthParams.buildResetPassword(
        phone = intent.phoneNumber,
        password = intent.password
      )
      _uiState.update {
        it.copy(isLoading = true)
      }
      MbAuth.resetPassword(authParams) { result ->
        _uiState.update {
          it.copy(isLoading = false)
        }
        when (result) {
          is ResetPasswordResult.Error -> {
            resetPasswordState.update {
              RegisterState.Error(
                result.message.orEmpty()
              )
            }
            _authResult.update {
              AuthResult.Failure(
                result.code,
                result.message.orEmpty()
              )
            }
          }

          is ResetPasswordResult.Success -> {
            _authResult.update {
              AuthResult.ResetPassword(status = 1, message = "Reset Password successful")
            }
          }
        }
      }
    }
  }

  override fun onCleared() {
    super.onCleared()
    _authResult.value = null
    _uiState.value = UiState(gameId = gameId)
  }
}