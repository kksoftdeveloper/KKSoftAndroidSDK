package com.appmb.sdk.mbauth


import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.view.WindowCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.flowWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.appmb.sdk.mbauth.event.LoginAnalytics
import com.appmb.sdk.mbauth.model.GuardianRegistrationInfo
import com.appmb.sdk.mbauth.model.MbAuthParams
import com.appmb.sdk.mbauth.model.RegistrationConsent
import com.appmb.sdk.mbauth.model.RegistrationInfo
import com.appmb.sdk.mbauth.model.RegistrationProfile
import com.appmb.sdk.mbauth.model.SdkParams
import com.appmb.sdk.mbauth.ui.deactivate.DeactivateAccountView
import com.appmb.sdk.mbauth.ui.components.AgeConfirmationTermsText
import com.appmb.sdk.mbauth.ui.forceupdate.RemindUpdateView
import com.appmb.sdk.mbauth.ui.linkaccount.LinkAccountScreen
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbauth.ui.login.AuthViewModel
import com.appmb.sdk.mbauth.ui.login.LoginScreen
import com.appmb.sdk.mbauth.ui.login.config.AuthConfigHolder
import com.appmb.sdk.mbauth.ui.login.config.LocalAuthUiConfig
import com.appmb.sdk.mbauth.ui.logout.LogoutView
import com.appmb.sdk.mbauth.ui.otp.OtpInputScreen
import com.appmb.sdk.mbauth.ui.passwordinput.LinkPhoneAccountPasswordInputScreen
import com.appmb.sdk.mbauth.ui.passwordinput.ResetPasswordInputView
import com.appmb.sdk.mbauth.ui.passwordinput.SignUpPasswordInputScreen
import com.appmb.sdk.mbauth.ui.phoneinput.PhoneInputView
import com.appmb.sdk.mbauth.ui.registration.GuardianInfoScreen
import com.appmb.sdk.mbauth.ui.registration.PersonalInfoScreen
import com.appmb.sdk.mbauth.ui.registration.ProfileCompletionStep
import com.appmb.sdk.mbauth.ui.registration.RegistrationFlow
import com.appmb.sdk.mbauth.ui.registration.RegistrationStep
import com.appmb.sdk.mbauth.ui.server.ServerListScreen
import com.appmb.sdk.mbauth.ui.server.components.MaintenancePopupView
import com.appmb.sdk.mbauth.ui.tokenexpiration.TokenExpirationScreen
import com.appmb.sdk.mbauth.ui.userblocked.UserBlockedScreen
import com.appmb.sdk.mbauth.worker.WorkerManager
import com.appmb.sdk.mbcore.MbSdk
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.data.datasource.MbCoreCommonDataSource
import com.appmb.sdk.mbcore.di.IsolatedKoinContext
import com.appmb.sdk.mbcore.domain.MbInitialize
import com.appmb.sdk.mbcore.event.AnalyticsProvider
import kotlinx.coroutines.launch
import com.appmb.sdk.mbcore.utils.ConnectivityViewModel
import com.appmb.sdk.mbcoreui.R
import com.appmb.sdk.mbcoreui.common.CustomCheckbox
import com.appmb.sdk.mbcoreui.theme.AppMbSampleTheme
import com.appmb.sdk.mbcoreui.utils.OfflineBanner
import com.facebook.FacebookSdk
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInAccount
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.android.gms.tasks.Task
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import org.koin.core.parameter.parametersOf
import java.security.MessageDigest
import java.util.Calendar

class AuthActivity : ComponentActivity() {

  val mbCoreCommonDataSource: MbCoreCommonDataSource by lazy {
    IsolatedKoinContext.koin.get<MbCoreCommonDataSource>()
  }

  val mixpanel: AnalyticsProvider by lazy {
    IsolatedKoinContext.koin.get()
  }

  private val mbSdkConfig: MbSdkConfig by lazy {
    MbSdk.getKoin().get<MbSdkConfig>()
  }
  // Google Sign In
  private lateinit var googleSignInClient: GoogleSignInClient
  private lateinit var googleSignInOptions: GoogleSignInOptions
  private var callBackAfterGoogleSignIn: ((GoogleSignInAccount) -> Unit)? = null

  private val googleSignInIntentLauncher = registerForActivityResult(
    ActivityResultContracts.StartActivityForResult()
  ) { result ->
    Log.d(TAG, "Google Sign In result: ${result.resultCode}")
    if (result.resultCode == Activity.RESULT_OK) {
      val data: Intent? = result.data

      val task = GoogleSignIn.getSignedInAccountFromIntent(data)
      handleGoogleSignInResult(task)
    } else {
      // Result is not OK (e.g. 0 / RESULT_CANCELED)
      val data: Intent? = result.data
      if (data != null) {
          val task = GoogleSignIn.getSignedInAccountFromIntent(data)
          try {
              task.getResult(ApiException::class.java)
          } catch (e: ApiException) {
              Log.e(TAG, "Google Sign In Failed with non-OK result. Code: ${e.statusCode}, Message: ${e.message}")
          }
      } else {
          Log.e(TAG, "Google Sign In cancelled or failed with null data. Result code: ${result.resultCode}")
      }

      mixpanel.trackMap(
        eventName = LoginAnalytics.googleLogin,
        properties = mapOf(
          "failure" to "User cancels google sign in or config error"
        )
      )
    }
  }

  private lateinit var tokenExpiratoryReceiver: BroadcastReceiver
  private lateinit var serverMaintenanceReceiver: BroadcastReceiver
  private lateinit var userBlockedReceiver: BroadcastReceiver

  override fun onCreate(savedInstanceState: Bundle?) {
    WindowCompat.setDecorFitsSystemWindows(window, false)
    lifecycleScope.launch {
      WorkerManager.cancelTimer()
    }
    super.onCreate(savedInstanceState)
//    network = NetworkUtils(applicationContext)
//    lifecycleScope.launch {
//      repeatOnLifecycle(Lifecycle.State.STARTED) {
//        network.isConnected.collect { online ->
//          if (online) {
//
//          } else {
//
//          }
//        }
//      }
//    }

    lifecycleScope.launch {
      Log.d(TAG, "Starting SDK initialization...")
      MbInitialize.initSdk()
      Log.d(TAG, "initSdk completed.")

      Log.d(TAG, "Fetching remote configurations...")
      val remoteGoogleClientId = async { mbCoreCommonDataSource.getGoogleClientId() }.await()
      val remoteFacebookAppId = async { mbCoreCommonDataSource.getFacebookAppId() }.await()
      val remoteFacebookClientToken =
        async { mbCoreCommonDataSource.getFacebookClientToken() }.await()
      val remoteGameId = async { mbCoreCommonDataSource.getGameId() }.await()
      Log.d(TAG, "Remote config fetched. gameId: $remoteGameId")

      val trackingConfig = mbSdkConfig.getTrackingConfig()
      
      val googleClientId = mbSdkConfig.getGoogleClientId()?.takeIf { it.isNotBlank() }
        ?: remoteGoogleClientId?.takeIf { it.isNotBlank() }
        ?: trackingConfig?.gidClientID?.takeIf { it.isNotBlank() }
        
      val trackingFacebookAppId =
        trackingConfig?.facebookAppID?.takeIf { trackingConfig.enableMeTa && it.isNotBlank() }
      val trackingFacebookClientToken =
        trackingConfig?.facebookClientToken?.takeIf { trackingConfig.enableMeTa && it.isNotBlank() }

      val facebookAppId = mbSdkConfig.getFacebookAppId()?.takeIf { it.isNotBlank() }
        ?: remoteFacebookAppId?.takeIf { it.isNotBlank() }
        ?: trackingFacebookAppId
        
      val facebookClientToken = mbSdkConfig.getFacebookClientToken()?.takeIf { it.isNotBlank() }
        ?: remoteFacebookClientToken?.takeIf { it.isNotBlank() }
        ?: trackingFacebookClientToken

      val gameId = mbSdkConfig.getGameId()?.takeIf { it.isNotBlank() && it != "0" }
        ?: remoteGameId?.takeIf { it.isNotBlank() && it != "0" }
        ?: "1" // Default to 1 if not set, to avoid blocking login screen

      Log.d(TAG, "Final Configs:")
      Log.d(TAG, "Google Client ID: $googleClientId")
      Log.d(TAG, "Facebook App ID: $facebookAppId")
      Log.d(TAG, "Facebook Client Token: $facebookClientToken")
      Log.d(TAG, "GameId: $gameId")

      // Initialize Google Sign In only if ID is available
      if (!googleClientId.isNullOrEmpty()) {
        if (!::googleSignInOptions.isInitialized) {
          googleSignInOptions = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(googleClientId)
            .requestEmail()
            .build()
        }

        if (!::googleSignInClient.isInitialized) {
          googleSignInClient = GoogleSignIn.getClient(this@AuthActivity, googleSignInOptions)
        }
      } else {
        Log.w(TAG, "Google Client ID is missing. Google Sign In will be disabled.")
      }

      // Initialize Facebook SDK only if ID and Token are available
      if (!facebookAppId.isNullOrEmpty() && !facebookClientToken.isNullOrEmpty()) {
        val previousFacebookAppId = FacebookSdk.getApplicationId()
        if (!previousFacebookAppId.isNullOrBlank() && previousFacebookAppId != facebookAppId) {
          Log.w(
            TAG,
            "Switching Facebook SDK App ID from $previousFacebookAppId to auth App ID $facebookAppId"
          )
        }
        FacebookSdk.setApplicationId(facebookAppId)
        FacebookSdk.setClientToken(facebookClientToken)
        FacebookSdk.sdkInitialize(applicationContext)
        FacebookSdk.fullyInitialize()
        logFacebookNativePlatformConfig(this@AuthActivity, facebookAppId)
      } else {
        Log.w(TAG, "Facebook App ID or Client Token is missing. Facebook Sign In will be disabled.")
      }
    }


    tokenExpiratoryReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (MbAuth.ACTION_TOKEN_EXPIRATION == intent?.action) {
          this@AuthActivity.finish()
        }
      }
    }

    userBlockedReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (MbAuth.ACTION_USER_BLOCKED == intent?.action) {
          this@AuthActivity.finish()
        }
      }
    }

    serverMaintenanceReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        if (MbAuth.ACTION_SERVER_MAINTENANCE == intent?.action) {
          this@AuthActivity.finish()
        }
      }
    }

    val tokenExpiratoryFilter = IntentFilter(MbAuth.ACTION_TOKEN_EXPIRATION)
    LocalBroadcastManager.getInstance(this)
      .registerReceiver(tokenExpiratoryReceiver, tokenExpiratoryFilter)

    val userBlockFilter = IntentFilter(MbAuth.ACTION_USER_BLOCKED)
    LocalBroadcastManager.getInstance(this).registerReceiver(userBlockedReceiver, userBlockFilter)

    val serverMaintenanceBlockFilter = IntentFilter(MbAuth.ACTION_SERVER_MAINTENANCE)
    LocalBroadcastManager.getInstance(this).registerReceiver(serverMaintenanceReceiver, serverMaintenanceBlockFilter)

    setContent {
      val cfg = AuthConfigHolder.uiConfig
      val params = intent.getParcelableExtra<SdkParams>(EXTRA_DATA)
      var gameId by remember { mutableStateOf<String>("") }

      val connectivityVm: ConnectivityViewModel = org.koin.androidx.compose.koinViewModel()
      val lifecycle = LocalLifecycleOwner.current.lifecycle
      val online by remember(connectivityVm, lifecycle) {
        connectivityVm.isOnline.flowWithLifecycle(lifecycle, Lifecycle.State.STARTED)
      }.collectAsState(initial = true)

      LaunchedEffect(Unit) {
        gameId = mbCoreCommonDataSource.getGameId()
      }
      CompositionLocalProvider(LocalAuthUiConfig provides cfg) {
        androidx.compose.material3.MaterialTheme {
          Box(Modifier.fillMaxSize()) {
            AppContent(
              params = params,
              gameId = if (gameId.isBlank() || gameId == "0") 1 else gameId.toInt(),
              handleGoogleSignIn = {
                if (::googleSignInClient.isInitialized) {
                  callBackAfterGoogleSignIn = it
                  startGoogleSignIn()
                } else {
                  Toast.makeText(this@AuthActivity, "Google Sign In chưa được cấu hình.", Toast.LENGTH_SHORT).show()
                }
              },
              handleGoogleSignOut = {
                if (::googleSignInClient.isInitialized) {
                  googleSignInClient.signOut()
                }
              }
            )
            OfflineBanner(
              visible = !online,
              modifier = Modifier
                .align(Alignment.TopCenter)
                .statusBarsPadding()
            )
          }
        }
      }
    }
  }

  override fun onResume() {
    super.onResume()
//    if ((mbSdkConfig.getServerClientId() == null || mbSdkConfig.getServerClientId().isNullOrEmpty()) && !maintenanceNotified) {
//      maintenanceNotified = true
//      LocalBroadcastManager.getInstance(this).sendBroadcast(Intent(MbAuth.ACTION_SERVER_MAINTENANCE))
//    }
  }

  private fun startGoogleSignIn() {
    googleSignInIntentLauncher.launch(googleSignInClient.signInIntent)
  }

  private fun handleGoogleSignInResult(task: Task<GoogleSignInAccount>) {
    try {
      val account = task.getResult(ApiException::class.java)
      Log.d(TAG, "Google Sign In Success: ${account.email}, Token: ${account.idToken?.take(10)}...")
      if (account.idToken.isNullOrBlank()) {
        Log.e(TAG, "Google ID Token is null or blank!")
      }
      callBackAfterGoogleSignIn?.invoke(account)
      callBackAfterGoogleSignIn = null
    } catch (ex: ApiException) {
      Log.e(TAG, "Google Sign In Failed. Code: ${ex.statusCode}, Message: ${ex.message}")
      callBackAfterGoogleSignIn = null
      ex.printStackTrace()
    }
  }

  override fun onDestroy() {
    super.onDestroy()
    LocalBroadcastManager.getInstance(this).unregisterReceiver(tokenExpiratoryReceiver)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(userBlockedReceiver)
    LocalBroadcastManager.getInstance(this).unregisterReceiver(serverMaintenanceReceiver)
  }

  companion object {
    const val TAG = "AuthActivity"
    const val EXTRA_DATA = "EXTRA_DATA"
    @JvmStatic
    private var maintenanceNotified = false
  }
}

private fun logFacebookNativePlatformConfig(context: Context, facebookAppId: String) {
  val packageName = context.packageName
  val keyHashes = runCatching {
    val packageInfo = context.packageManager.getPackageInfo(
      packageName,
      PackageManager.GET_SIGNATURES
    )
    packageInfo.signatures
      ?.map { signature ->
        val digest = MessageDigest.getInstance("SHA")
        digest.update(signature.toByteArray())
        Base64.encodeToString(digest.digest(), Base64.NO_WRAP)
      }
      .orEmpty()
  }.getOrElse { error ->
    Log.w(AuthActivity.TAG, "Unable to read Facebook key hash: ${error.message}")
    emptyList()
  }

  Log.i(
    AuthActivity.TAG,
    "Facebook native platform config. appId=$facebookAppId, packageName=$packageName, keyHashes=$keyHashes"
  )
}

@Composable
fun AppContent(
  params: SdkParams?,
  gameId: Int,
  handleGoogleSignIn: ((GoogleSignInAccount) -> Unit) -> Unit,
  handleGoogleSignOut: () -> Unit,
) {
  val navigationController = rememberNavController()
  AppMbSampleTheme {
    AppNavigator(
      navController = navigationController,
      params = params,
      gameId = gameId,
      handleGoogleSignIn = handleGoogleSignIn,
      handleGoogleSignOut = handleGoogleSignOut
    )
  }
}

@Composable
fun AppNavigator(
  navController: NavHostController,
  params: SdkParams?,
  gameId: Int,
  handleGoogleSignIn: ((GoogleSignInAccount) -> Unit) -> Unit,
  handleGoogleSignOut: () -> Unit,
) {
  var pendingAuthSuccess by remember { mutableStateOf<AuthResult.AuthSuccess?>(null) }
  var showMissingBirthDateDialog by remember { mutableStateOf(false) }
  var registrationPlayerOtpToken by remember { mutableStateOf<String?>(null) }
  var registrationPersonalInfo by remember { mutableStateOf<RegistrationInfo?>(null) }
  var registrationGuardianInfo by remember { mutableStateOf<GuardianRegistrationInfo?>(null) }
  var registrationGuardianOtpToken by remember { mutableStateOf<String?>(null) }

  fun clearRegistrationProfileState() {
    registrationPlayerOtpToken = null
    registrationPersonalInfo = null
    registrationGuardianInfo = null
    registrationGuardianOtpToken = null
  }

  fun completePendingLogin() {
    val result = pendingAuthSuccess ?: return
    val intent = Intent().apply {
      putExtra("authResult", result)
    }
    (navController.context as? ComponentActivity)?.setResult(Activity.RESULT_OK, intent)
    (navController.context as? ComponentActivity)?.finish()
  }

  fun cancelPendingProfileCompletion() {
    pendingAuthSuccess = null
    showMissingBirthDateDialog = false
    navController.popBackStack(route = Authenticate.name, inclusive = false)
  }

  fun openProfileCompletionFlow(requirement: LoginProfileRequirement) {
    when (requirement) {
      LoginProfileRequirement.Complete -> completePendingLogin()
      LoginProfileRequirement.MissingPhone -> {
        navController.navigate(route = CompleteProfilePhoneWithAgeConfirmation.name)
      }

      LoginProfileRequirement.MissingBirthDate -> showMissingBirthDateDialog = true
      LoginProfileRequirement.Adult -> {
        navController.navigate(route = "CompleteProfilePersonalInfo/false/false")
      }

      LoginProfileRequirement.Under16 -> {
        navController.navigate(route = "CompleteProfilePersonalInfo/true/false")
      }
    }
  }

  fun resolveLoginProfileRequirement(result: AuthResult.AuthSuccess): LoginProfileRequirement {
    return if (result.user.identityVerificationRequired == true) {
      if (result.isSocialLogin && result.user.phone.isNullOrBlank()) {
        LoginProfileRequirement.MissingPhone
      } else {
        LoginProfileRequirement.MissingBirthDate
      }
    } else {
      LoginProfileRequirement.Complete
    }
  }

  NavHost(
    navController, startDestination =
      when (params) {
        is SdkParams.Authenticate -> Authenticate.name
        is SdkParams.Logout -> Logout.name
        is SdkParams.LinkAccount -> LinkAccount.name
        is SdkParams.UpdateApp -> ReminderUpdate.name
        is SdkParams.ChooseServer -> ChooseServer.name
        is SdkParams.DeactivateAccount -> DeactivateAccount.name
        is SdkParams.TokenExpiration -> TokenExpiration.name
        is SdkParams.UserBlocked -> UserBlocked.name
        is SdkParams.ServerMaintenance -> ServerMaintenance.name
        else -> Authenticate.name
      }
  ) {
//    composable(route = Authentication.name) {
//      LoginScreen(
//        navigateToChooseServer = {
//          navController.navigateWithoutBackStack(ChooseServer)
//        },
//        navigateToRequestOtp = { otpType ->
//          val route = RequestOTP(otpType).getNavPath()
//          navController.navigate(route)
//        },
//        handleGoogleSignIn = handleGoogleSignIn
//      )
//    }
    composable(route = Authenticate.name) {
      LoginScreen(
        gameId = gameId,
        navigateToChooseServer = {
          navController.navigateWithoutBackStack(ChooseServer)
        },
        navigateToRequestOtp = { otpType ->
          val route = RequestOTP(otpType).getNavPath()
          navController.navigate(route)
        },
        onAuthSuccess = { result ->
          pendingAuthSuccess = result
          openProfileCompletionFlow(resolveLoginProfileRequirement(result))
        },
        handleGoogleSignIn = handleGoogleSignIn
      )
    }
    composable(
      route = RequestOTP.NAME,
      arguments = listOf(
        navArgument("otpType") { type = NavType.StringType }
      )
    ) {
      val otpType = it.arguments?.getString("otpType").orEmpty()
      LaunchedEffect(otpType) {
        if (otpType == MbAuthParams.OTP_TYPE_PARAM_REGISTRATION) {
          clearRegistrationProfileState()
        }
      }
      PhoneInputView(
        otpType = otpType,
        stepLabel = if (otpType == MbAuthParams.OTP_TYPE_PARAM_REGISTRATION) {
          RegistrationFlow.label(RegistrationStep.Phone, isUnder16 = false)
        } else {
          null
        },
        navigateToVerifyOtp = { phone, timeToRetry, isUnder16 ->
          navController.navigate(route = "VerifyOTP/$phone/$timeToRetry/$otpType/$isUnder16")
        },
        onClose = {
          if (otpType == MbAuthParams.OTP_TYPE_PARAM_REGISTRATION) {
            navController.closeRegistrationFlow()
          } else {
            navController.popBackStack()
          }
        }
      )
    }
    composable(
      route = VerifyOTP.NAME,
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType },
        navArgument("timeToRetry") { type = NavType.IntType },
        navArgument("otpType") { type = NavType.StringType },
        navArgument("isUnder16") { type = NavType.BoolType }
      )
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      val timeToRetry = it.arguments?.getInt("timeToRetry") ?: 0
      val otpType = it.arguments?.getString("otpType").orEmpty()
      val isUnder16 = it.arguments?.getBoolean("isUnder16") ?: false
      OtpInputScreen(
        otpType = otpType,
        phoneNumber = phoneNumber,
        timeToRetry = timeToRetry,
        stepLabel = if (otpType == MbAuthParams.OTP_TYPE_PARAM_REGISTRATION) {
          RegistrationFlow.label(RegistrationStep.UserOtp, isUnder16)
        } else {
          null
        },
        onOtpVerifiedSuccess = { verifiedOtpData ->
          when (otpType) {
            MbAuthParams.OTP_TYPE_PARAM_REGISTRATION -> {
              registrationPlayerOtpToken = verifiedOtpData.otpVerifiedToken
              navController.navigate(route = "RegisterPersonalInfo/$phoneNumber/$isUnder16")
            }

            MbAuthParams.OTP_TYPE_PARAM_LINK_PHONE_ACCOUNT -> {
              navController.navigate(route = "SetLinkPhoneAccountPassword/$phoneNumber")
            }

            MbAuthParams.OTP_TYPE_PARAM_FORGOT_PASSWORD -> {
              navController.navigate(route = "ResetPassword/$phoneNumber")
            }

            else -> {

            }
          }
        },
        onClose = {
          if (otpType == MbAuthParams.OTP_TYPE_PARAM_REGISTRATION) {
            navController.closeRegistrationFlow()
          } else {
            navController.popBackStack()
          }
        }
      )
    }

    composable(
      route = RegisterPersonalInfo.NAME,
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType },
        navArgument("isUnder16") { type = NavType.BoolType }
      )
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      val isUnder16 = it.arguments?.getBoolean("isUnder16") ?: false
      PersonalInfoScreen(
        stepLabel = RegistrationFlow.label(RegistrationStep.PersonalInfo, isUnder16),
        onContinue = { personalInfo ->
          registrationPersonalInfo = personalInfo
          val resolvedIsUnder16 =
            if (personalInfo.dateOfBirth.isUnder16BirthDate() == true) true else isUnder16
          if (resolvedIsUnder16) {
            navController.navigate(route = "RegisterGuardianInfo/$phoneNumber")
          } else {
            navController.navigate(route = "Register/$phoneNumber/$resolvedIsUnder16")
          }
        },
        onClose = {
          navController.closeRegistrationFlow()
        }
      )
    }

    composable(
      route = RegisterGuardianInfo.NAME,
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType }
      )
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      GuardianInfoScreen(
        stepLabel = RegistrationFlow.label(RegistrationStep.GuardianInfo, isUnder16 = true),
        onContinue = { guardianInfo, timeToRetry ->
          registrationGuardianInfo = guardianInfo
          navController.navigate(route = "RegisterGuardianOTP/$phoneNumber/${guardianInfo.phone}/$timeToRetry")
        },
        onClose = {
          navController.closeRegistrationFlow()
        }
      )
    }

    composable(
      route = RegisterGuardianOTP.NAME,
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType },
        navArgument("guardianPhone") { type = NavType.StringType },
        navArgument("timeToRetry") { type = NavType.IntType }
      )
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      val guardianPhone = it.arguments?.getString("guardianPhone").orEmpty()
      val timeToRetry = it.arguments?.getInt("timeToRetry") ?: 0
      OtpInputScreen(
        otpType = MbAuthParams.OTP_TYPE_PARAM_REGISTRATION,
        phoneNumber = guardianPhone,
        timeToRetry = timeToRetry,
        stepLabel = RegistrationFlow.label(RegistrationStep.GuardianOtp, isUnder16 = true),
        onOtpVerifiedSuccess = { verifiedOtpData ->
          registrationGuardianOtpToken = verifiedOtpData.otpVerifiedToken
          navController.navigate(route = "Register/$phoneNumber/true")
        },
        onClose = {
          navController.closeRegistrationFlow()
        }
      )
    }

    composable(
      route = Register.NAME,
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType },
        navArgument("isUnder16") { type = NavType.BoolType }
      )
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      val isUnder16 = it.arguments?.getBoolean("isUnder16") ?: false
      val registrationProfile = registrationPersonalInfo?.let { personalInfo ->
        RegistrationProfile(
          playerOtpVerifiedToken = registrationPlayerOtpToken,
          personalInfo = personalInfo,
          consent = RegistrationConsent(
            legalAccepted = true,
            selfRegistrationAgeConfirmed = !isUnder16,
          ),
          guardian = if (isUnder16) {
            registrationGuardianInfo?.copy(otpVerifiedToken = registrationGuardianOtpToken)
          } else {
            null
          }
        )
      }
      SignUpPasswordInputScreen(
        activity = navController.context as ComponentActivity,
        authViewModel = koinViewModel<AuthViewModel>(parameters = { parametersOf(gameId) }),
        phoneNumber = phoneNumber,
        registrationProfile = registrationProfile,
        stepLabel = RegistrationFlow.label(RegistrationStep.Password, isUnder16),
        navigateToChooseServer = {
          navController.navigateWithoutBackStack(ChooseServer)
        },
        onClose = {
          navController.closeRegistrationFlow()
        }
      ).Content()
    }

    composable(
      route = CompleteProfilePhone.NAME,
      arguments = listOf(
        navArgument("isUnder16") { type = NavType.BoolType }
      )
    ) {
      val isUnder16 = it.arguments?.getBoolean("isUnder16") ?: false
      PhoneInputView(
        otpType = MbAuthParams.OTP_TYPE_PARAM_REGISTRATION,
        stepLabel = RegistrationFlow.profileCompletionLabel(
          ProfileCompletionStep.Phone,
          isUnder16 = isUnder16,
          requiresUserPhoneVerification = true
        ),
        showAgeConfirmation = false,
        forcedIsUnder16 = isUnder16,
        navigateToVerifyOtp = { phone, timeToRetry, _ ->
          navController.navigate(route = "CompleteProfileVerifyOTP/$phone/$timeToRetry/$isUnder16")
        },
        onClose = {
          cancelPendingProfileCompletion()
        }
      )
    }

    composable(route = CompleteProfilePhoneWithAgeConfirmation.name) {
      PhoneInputView(
        otpType = MbAuthParams.OTP_TYPE_PARAM_REGISTRATION,
        stepLabel = RegistrationFlow.profileCompletionLabel(
          ProfileCompletionStep.Phone,
          isUnder16 = false,
          requiresUserPhoneVerification = true
        ),
        showAgeConfirmation = true,
        forcedIsUnder16 = null,
        navigateToVerifyOtp = { phone, timeToRetry, isUnder16 ->
          navController.navigate(route = "CompleteProfileVerifyOTP/$phone/$timeToRetry/$isUnder16")
        },
        onClose = {
          cancelPendingProfileCompletion()
        }
      )
    }

    composable(
      route = CompleteProfileVerifyOTP.NAME,
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType },
        navArgument("timeToRetry") { type = NavType.IntType },
        navArgument("isUnder16") { type = NavType.BoolType }
      )
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      val timeToRetry = it.arguments?.getInt("timeToRetry") ?: 0
      val isUnder16 = it.arguments?.getBoolean("isUnder16") ?: false
      OtpInputScreen(
        otpType = MbAuthParams.OTP_TYPE_PARAM_REGISTRATION,
        phoneNumber = phoneNumber,
        timeToRetry = timeToRetry,
        stepLabel = RegistrationFlow.profileCompletionLabel(
          ProfileCompletionStep.UserOtp,
          isUnder16 = isUnder16,
          requiresUserPhoneVerification = true
        ),
        onOtpVerifiedSuccess = { _ ->
          navController.navigate(route = "CompleteProfilePersonalInfo/$isUnder16/true")
        },
        onClose = {
          cancelPendingProfileCompletion()
        }
      )
    }

    composable(
      route = CompleteProfilePersonalInfo.NAME,
      arguments = listOf(
        navArgument("isUnder16") { type = NavType.BoolType },
        navArgument("requiresUserPhoneVerification") { type = NavType.BoolType }
      )
    ) {
      val isUnder16 = it.arguments?.getBoolean("isUnder16") ?: false
      val requiresUserPhoneVerification =
        it.arguments?.getBoolean("requiresUserPhoneVerification") ?: false
      PersonalInfoScreen(
        stepLabel = RegistrationFlow.profileCompletionLabel(
          ProfileCompletionStep.PersonalInfo,
          isUnder16 = isUnder16,
          requiresUserPhoneVerification = requiresUserPhoneVerification
        ),
        onContinue = { personalInfo ->
          val resolvedIsUnder16 =
            if (personalInfo.dateOfBirth.isUnder16BirthDate() == true) true else isUnder16
          if (resolvedIsUnder16) {
            navController.navigate(route = "CompleteProfileGuardianInfo/$requiresUserPhoneVerification")
          } else {
            completePendingLogin()
          }
        },
        onClose = {
          cancelPendingProfileCompletion()
        }
      )
    }

    composable(
      route = CompleteProfileGuardianInfo.NAME,
      arguments = listOf(
        navArgument("requiresUserPhoneVerification") { type = NavType.BoolType }
      )
    ) {
      val requiresUserPhoneVerification =
        it.arguments?.getBoolean("requiresUserPhoneVerification") ?: false
      GuardianInfoScreen(
        stepLabel = RegistrationFlow.profileCompletionLabel(
          ProfileCompletionStep.GuardianInfo,
          isUnder16 = true,
          requiresUserPhoneVerification = requiresUserPhoneVerification
        ),
        onContinue = { guardianInfo, timeToRetry ->
          navController.navigate(
            route = "CompleteProfileGuardianOTP/${guardianInfo.phone}/$timeToRetry/$requiresUserPhoneVerification"
          )
        },
        onClose = {
          cancelPendingProfileCompletion()
        }
      )
    }

    composable(
      route = CompleteProfileGuardianOTP.NAME,
      arguments = listOf(
        navArgument("guardianPhone") { type = NavType.StringType },
        navArgument("timeToRetry") { type = NavType.IntType },
        navArgument("requiresUserPhoneVerification") { type = NavType.BoolType }
      )
    ) {
      val guardianPhone = it.arguments?.getString("guardianPhone").orEmpty()
      val timeToRetry = it.arguments?.getInt("timeToRetry") ?: 0
      val requiresUserPhoneVerification =
        it.arguments?.getBoolean("requiresUserPhoneVerification") ?: false
      OtpInputScreen(
        otpType = MbAuthParams.OTP_TYPE_PARAM_REGISTRATION,
        phoneNumber = guardianPhone,
        timeToRetry = timeToRetry,
        stepLabel = RegistrationFlow.profileCompletionLabel(
          ProfileCompletionStep.GuardianOtp,
          isUnder16 = true,
          requiresUserPhoneVerification = requiresUserPhoneVerification
        ),
        onOtpVerifiedSuccess = { _ ->
          completePendingLogin()
        },
        onClose = {
          cancelPendingProfileCompletion()
        }
      )
    }
//    composable(
//      route = LinkAccount.name
//    ) {
//      LinkAccountScreen(
//        activity = navController.context as ComponentActivity,
//        repeatableReminder = {
//          countDownForGuestAsync { }
//          navController.popBackStack()
//        },
//        navigateToRequestOtp = { otpType ->
//          val route = RequestOTP(otpType).getNavPath()
//          navController.navigate(route)
//        },
//        handleGoogleSignIn = handleGoogleSignIn
//      )
//    }
    // Set Password for Link Account Phone number
    composable(
      route = "SetLinkPhoneAccountPassword/{phone}",
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType }
      )
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      LinkPhoneAccountPasswordInputScreen(
        activity = navController.context as ComponentActivity,
        authViewModel = koinViewModel<AuthViewModel>(parameters = { parametersOf(gameId) }),
        phoneNumber = phoneNumber,
        onClose = {
          navController.popBackStack()
        }
      ).Content()
    }
    // Reset Password Screen
    composable(
      route = "ResetPassword/{phone}",
      arguments = listOf(
        navArgument("phone") { type = NavType.StringType }
      ),
    ) {
      val phoneNumber = it.arguments?.getString("phone").orEmpty()
      ResetPasswordInputView(
        activity = navController.context as ComponentActivity,
        authViewModel = koinViewModel<AuthViewModel>(parameters = { parametersOf(gameId) }),
        phoneNumber = phoneNumber,
        navigateToLogin = {
          navController.navigateWithoutBackStack(Authenticate)
        },
        onClose = {
          navController.popBackStack()
        }
      ).Content()
    }
    // Force update screen
    composable(route = ReminderUpdate.name) {
      RemindUpdateView(
        onClose = {
//          navController.popBackStack()
        }
      )
    }
    // Logout View
    composable(route = Logout.name) {
      LogoutView(
        onClose = {
          (navController.context as? ComponentActivity)?.finish()
        },
        handleGoogleSignOut = {
          handleGoogleSignOut.invoke()
        }
      )
    }
    // Link Account View
    composable(
      route = LinkAccount.name
    ) {
      LinkAccountScreen(
        activity = navController.context as ComponentActivity,
        repeatableReminder = {
          val activity = (navController.context as ComponentActivity)
          val intent = Intent().apply {
            putExtra("authResult", AuthResult.RepeatableRemindLinkAccount(isRepeated = true))
          }
          activity.setResult(Activity.RESULT_OK, intent)
          activity.finish()
        },
        navigateToRequestOtp = { otpType ->
          val route = RequestOTP(otpType).getNavPath()
          navController.navigate(route)
        },
        handleGoogleSignIn = handleGoogleSignIn
      )
    }
    // Choose Server View
    composable(
      route = ChooseServer.name
    ) {
      val param = params as? SdkParams.ChooseServer
      val isEnableClose: Boolean = param?.isEnableClose ?: false
      ServerListScreen(
        isEnableClose = isEnableClose,
        onClose = {
          (navController.context as ComponentActivity).setResult(Activity.RESULT_CANCELED, null)
          (navController.context as ComponentActivity).finish()
        },
        onComplete = {
          val intent = Intent().apply {
            putExtra("authResult", AuthResult.AuthSuccess(it))
          }
          (navController.context as? ComponentActivity)?.setResult(Activity.RESULT_OK, intent)
          (navController.context as? ComponentActivity)?.finish()
        }
      )
    }
    // Deactivate Confirmation View
    composable(route = DeactivateAccount.name) {
      DeactivateAccountView(
        handleGoogleSignOut = handleGoogleSignOut
      )
    }
    composable(route = TokenExpiration.name) {
      TokenExpirationScreen(
        goToLoginScreen = {
          navController.navigateWithoutBackStack(route = Authenticate)
        }
      )
    }
    composable(route = UserBlocked.name) {
      UserBlockedScreen(
        onClose = {
          (navController.context as? ComponentActivity)?.finish()
        }
      )
    }
    composable(route = ServerMaintenance.name) {
      MaintenancePopupView(
        onClose = {
//          (navController.context as? ComponentActivity)?.finish()
        }
      )
    }
  }

  if (showMissingBirthDateDialog) {
    MissingRequiredProfileDialog(
      onContinue = { confirmedAge16OrOlder ->
        showMissingBirthDateDialog = false
        val isUnder16 = !confirmedAge16OrOlder
        navController.navigate(route = "CompleteProfilePhone/$isUnder16")
      }
    )
  }
}

@Composable
private fun MissingRequiredProfileDialog(
  onContinue: (confirmedAge16OrOlder: Boolean) -> Unit,
) {
  var confirmedAge16OrOlder by remember { mutableStateOf(true) }

  AlertDialog(
    onDismissRequest = {},
    title = {
      Text(text = stringResource(R.string.required_profile_update_title))
    },
    text = {
      Row(
        verticalAlignment = Alignment.Top,
        modifier = Modifier.padding(top = 4.dp)
      ) {
        CustomCheckbox(
          checked = confirmedAge16OrOlder,
          onCheckedChange = { confirmedAge16OrOlder = it },
          modifier = Modifier.size(16.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        AgeConfirmationTermsText(modifier = Modifier.weight(1f))
      }
    },
    confirmButton = {
      Button(
        onClick = {
          onContinue(confirmedAge16OrOlder)
        }
      ) {
        Text(text = stringResource(R.string.common_continue))
      }
    },
  )
}

private fun String.isUnder16BirthDate(): Boolean? {
  val parts = split("-")
  if (parts.size != 3) return null
  val year = parts[0].toIntOrNull() ?: return null
  val month = parts[1].toIntOrNull() ?: return null
  val day = parts[2].toIntOrNull() ?: return null
  if (month !in 1..12 || day !in 1..31) return null

  val sixteenthBirthday = Calendar.getInstance().apply {
    isLenient = false
    set(year, month - 1, day)
    add(Calendar.YEAR, 16)
  }

  return sixteenthBirthday.after(Calendar.getInstance())
}

private enum class LoginProfileRequirement {
  Complete,
  MissingPhone,
  MissingBirthDate,
  Adult,
  Under16,
}

internal fun <T : AuthScreen> NavHostController.navigateWithoutBackStack(route: T) {
  this.navigate(route = route.name) {
    popUpTo(0) { inclusive = true }
    launchSingleTop = true
  }
}

private fun NavHostController.closeRegistrationFlow() {
  if (!popBackStack(route = RequestOTP.NAME, inclusive = true)) {
    popBackStack()
  }
}
