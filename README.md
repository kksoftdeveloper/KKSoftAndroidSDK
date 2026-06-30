# KKSoft Android SDK - Unity Usage

## 1. Add SDK To Unity

1. Add JitPack to `settings.gradle`:

   ```gradle
   dependencyResolutionManagement {
       repositoriesMode.set(RepositoriesMode.FAIL_ON_PROJECT_REPOS)
       repositories {
           google()
           mavenCentral()
           maven { url "https://jitpack.io" }
       }
   }
   ```

2. Add the KKSoft SDK dependency to the Android library/app module:

   ```gradle
   implementation("com.github.kksoftdeveloper:KKSoftAndroidSDK:352227d")
   ```

   Use `api(...)` instead of `implementation(...)` when another module, such as
   `:launcher`, needs to compile directly against SDK classes exposed through a
   library module.

Add to `gradleTemplate.properties`:

```properties
android.useAndroidX=true
android.enableJetifier=true
```

## 2. Environment Configuration

Host apps do not need to add `BuildConfig.ENVIRONMENT`, `BuildConfig.IS_STAGING`,
or SDK base URL fields. These values are generated inside the KKSoft SDK.

By default, the SDK uses production:

```text
BuildConfig.ENVIRONMENT = ""
BuildConfig.IS_STAGING = false
BuildConfig.IS_PRODUCTION = true
```

For SDK development only, set the environment before building the SDK:

```properties
# local.properties
environment=staging
```

or:

```bash
./gradlew assembleDebug -PENVIRONMENT=staging
```

If `environment` / `ENVIRONMENT` is missing, empty, or not `staging`, the SDK falls
back to production. The old `staging=true` local property is no longer used.

## 3. Create Android Bridge

Create a Kotlin or Java bridge in the Unity Android plugin source. Unity should call this bridge instead of calling every SDK class directly.

`baseUrl` is optional. Host apps should leave it empty unless they intentionally
need to override the SDK endpoint at runtime. Unity only provides third-party
tracking IDs and tokens through `TrackingConfig`.

```kotlin
package com.company.game

import android.app.Activity
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.appmb.sdk.mbauth.ui.login.AuthResult
import com.appmb.sdk.mbcore.config.MbSdkConfig
import com.appmb.sdk.mbcore.config.TrackingConfig
import com.appmb.sdk.mbpayment.MbPayment
import com.appmb.sdk.mbpayment.model.PurchaseResult
import com.appmb.sdk.mbtracking.model.Level
import com.appmb.sdk.mbtracking.model.OnlineTime
import com.appmb.sdk.mbtracking.model.VIPLevel
import com.kksoft.sdk.KKSoftAndroidSdk
import com.unity3d.player.UnityPlayer

object KKSoftUnityBridge {
  private const val AUTH_REQUEST = 9101
  private const val FORCE_UPDATE_REQUEST = 9102
  private const val LOGOUT_REQUEST = 9103
  private const val LINK_ACCOUNT_REQUEST = 9104
  private const val CHANGE_SERVER_REQUEST = 9105
  private const val DEACTIVATE_REQUEST = 9106

  private const val UNITY_OBJECT = "KKSoftAndroid"

  private var paymentReceiver: BroadcastReceiver? = null

  @JvmStatic
  fun initialize(
    activity: Activity,
    gameId: String,
    serverClientId: String?,
    googleClientId: String?,
    facebookAppId: String?,
    facebookClientToken: String?,
    firebaseAppId: String?,
    adjustId: String?,
    adjustToken: String?,
    tiktokAppId: String?,
    tiktokAccessToken: String?,
    facebookDisplayName: String?,
    baseUrl: String?
  ) {
    val trackingConfig = TrackingConfig.Builder()
      .enableFirebase(!firebaseAppId.isNullOrBlank(), firebaseAppId)
      .enableAdjust(!adjustToken.isNullOrBlank(), adjustId, adjustToken)
      .enableTikTok(!tiktokAppId.isNullOrBlank(), tiktokAppId, tiktokAccessToken)
      .enableMeta(!facebookAppId.isNullOrBlank(), facebookAppId, facebookClientToken, facebookDisplayName)
      .enableGid(googleClientId)
      .build()

    val builder = MbSdkConfig.Builder()
      .setAppId(activity.packageName)
      .setGameId(gameId)
      .setAppVersionName(getVersionName(activity))
      .setServerClientId(serverClientId)
      .setGoogleClientId(googleClientId)
      .setFacebookAppId(facebookAppId)
      .setFacebookClientToken(facebookClientToken)
      .setTrackingConfig(trackingConfig)

    if (!baseUrl.isNullOrBlank()) {
      builder.setBaseUrl(baseUrl)
    }

    KKSoftAndroidSdk.init(activity, builder.build())
    registerPaymentResult(activity)
  }

  @JvmStatic
  fun startAuthentication(activity: Activity) {
    KKSoftAndroidSdk.startAuthentication(activity, AUTH_REQUEST)
  }

  @JvmStatic
  fun checkForceUpdate(activity: Activity) {
    KKSoftAndroidSdk.startCheckingForceUpdate(activity, FORCE_UPDATE_REQUEST)
  }

  @JvmStatic
  fun startLogout(activity: Activity) {
    KKSoftAndroidSdk.startLogout(activity, LOGOUT_REQUEST)
  }

  @JvmStatic
  fun startLinkAccount(activity: Activity) {
    KKSoftAndroidSdk.startLinkAccount(activity, LINK_ACCOUNT_REQUEST)
  }

  @JvmStatic
  fun startChangeServer(activity: Activity) {
    KKSoftAndroidSdk.startChangeGameServer(activity, CHANGE_SERVER_REQUEST)
  }

  @JvmStatic
  fun startDeactivateAccount(activity: Activity) {
    KKSoftAndroidSdk.startDeactivateAccount(activity, DEACTIVATE_REQUEST)
  }

  @JvmStatic
  fun startPayment(activity: Activity) {
    KKSoftAndroidSdk.startPayment(activity)
  }

  @JvmStatic
  fun logPlayGame(gameUuid: String, characterId: String, characterName: String, serverId: String, serverName: String) {
    KKSoftAndroidSdk.logPlayGame(gameUuid, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logTutorialCompletedS1(gameUuid: String, characterId: String, characterName: String, serverId: String, serverName: String) {
    KKSoftAndroidSdk.logTutorialCompletedS1(gameUuid, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logLevelUp(levelValue: Int, gameUuid: String, characterId: String, characterName: String, serverId: String, serverName: String) {
    KKSoftAndroidSdk.logLevelUp(levelFrom(levelValue), gameUuid, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logVipLevel(vipValue: Int, gameUuid: String, characterId: String, characterName: String, serverId: String, serverName: String) {
    KKSoftAndroidSdk.logVIPLevel(vipLevelFrom(vipValue), gameUuid, characterId, characterName, serverId, serverName)
  }

  @JvmStatic
  fun logOnlineTime(minutes: Int, gameUuid: String, characterId: String, characterName: String, levelValue: Int, serverId: String, serverName: String) {
    KKSoftAndroidSdk.logOnlineTime(onlineTimeFrom(minutes), gameUuid, characterId, characterName, levelFrom(levelValue), serverId, serverName)
  }

  @JvmStatic
  fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    if (requestCode !in AUTH_REQUEST..DEACTIVATE_REQUEST) return

    val result = data?.getParcelableExtra<AuthResult>("authResult")
    val payload = when (result) {
      is AuthResult.AuthSuccess -> "auth_success"
      is AuthResult.RegisterSuccess -> "register_success"
      is AuthResult.LinkAccount -> "link_success"
      is AuthResult.Logout -> if (result.isLogoutSuccess) "logout_success" else "logout_failed"
      is AuthResult.DeactivateAccount -> if (result.isSuccess) "deactivate_success" else "deactivate_failed"
      is AuthResult.SelectedServerGame -> "server_selected:${result.serverId ?: ""}"
      is AuthResult.ResetPassword -> "reset_password:${result.status}:${result.message}"
      is AuthResult.Failure -> "failure:${result.status}:${result.msg}"
      is AuthResult.RepeatableRemindLinkAccount -> "repeat_link:${result.isRepeated}"
      null -> if (resultCode == Activity.RESULT_CANCELED) "cancelled" else "unknown"
    }

    UnityPlayer.UnitySendMessage(UNITY_OBJECT, "OnAuthResult", payload)
  }

  @JvmStatic
  fun release(activity: Activity) {
    paymentReceiver?.let {
      LocalBroadcastManager.getInstance(activity).unregisterReceiver(it)
      paymentReceiver = null
    }
  }

  private fun registerPaymentResult(activity: Activity) {
    if (paymentReceiver != null) return

    paymentReceiver = object : BroadcastReceiver() {
      override fun onReceive(context: Context?, intent: Intent?) {
        val result = intent?.getParcelableExtra<PurchaseResult>(MbPayment.EXTRA_PURCHASE_RESULT)
        UnityPlayer.UnitySendMessage(UNITY_OBJECT, "OnPaymentResult", paymentPayload(result))
      }
    }

    LocalBroadcastManager.getInstance(activity).registerReceiver(
      paymentReceiver!!,
      IntentFilter(MbPayment.ACTION_PURCHASE_DONE)
    )
  }

  private fun paymentPayload(result: PurchaseResult?): String = when (result) {
    is PurchaseResult.PurchasedSuccess -> "success:${result.productName}"
    PurchaseResult.PurchasedFailure -> "failure"
    PurchaseResult.PurchasedUserCancel -> "cancelled"
    PurchaseResult.ClosedProductList -> "closed"
    PurchaseResult.PurchasedError -> "error"
    PurchaseResult.PurchasedUnavailableInSelectedServer -> "unavailable_in_server"
    PurchaseResult.PurchasedUserNotAuthenticated -> "not_authenticated"
    null -> "unknown"
  }

  private fun getVersionName(context: Context): String =
    context.packageManager.getPackageInfo(context.packageName, 0).versionName ?: "1.0.0"

  private fun levelFrom(value: Int): Level =
    Level.values().firstOrNull { it.value == value } ?: Level.Level10

  private fun vipLevelFrom(value: Int): VIPLevel =
    VIPLevel.values().firstOrNull { it.value == value } ?: VIPLevel.Level1

  private fun onlineTimeFrom(minutes: Int): OnlineTime =
    OnlineTime.values().firstOrNull { it.minutes == minutes } ?: OnlineTime.OL5minutes
}
```

If your Unity project uses a custom `UnityPlayerActivity`, forward `onActivityResult`:

```kotlin
class MainActivity : com.unity3d.player.UnityPlayerActivity() {
  override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
    super.onActivityResult(requestCode, resultCode, data)
    KKSoftUnityBridge.onActivityResult(requestCode, resultCode, data)
  }

  override fun onDestroy() {
    KKSoftUnityBridge.release(this)
    super.onDestroy()
  }
}
```

## 4. Call From Unity C#

Attach this script to a GameObject named `KKSoftAndroid`, because the Android bridge sends callbacks to that object.

```csharp
using UnityEngine;

public class KKSoftAndroid : MonoBehaviour
{
    static AndroidJavaObject CurrentActivity
    {
        get
        {
            using var unityPlayer = new AndroidJavaClass("com.unity3d.player.UnityPlayer");
            return unityPlayer.GetStatic<AndroidJavaObject>("currentActivity");
        }
    }

    static AndroidJavaClass Bridge => new AndroidJavaClass("com.company.game.KKSoftUnityBridge");

    public void Initialize()
    {
        Bridge.CallStatic(
            "initialize",
            CurrentActivity,
            "1",
            "server-client-id",
            "google-client-id",
            "facebook-app-id",
            "facebook-client-token",
            "firebase-app-id",
            "adjust-id",
            "adjust-token",
            "tiktok-app-id",
            "tiktok-access-token",
            "facebook-display-name",
            null
        );
    }

    public void StartAuth()
    {
        Bridge.CallStatic("startAuthentication", CurrentActivity);
    }

    public void CheckForceUpdate()
    {
        Bridge.CallStatic("checkForceUpdate", CurrentActivity);
    }

    public void Logout()
    {
        Bridge.CallStatic("startLogout", CurrentActivity);
    }

    public void LinkAccount()
    {
        Bridge.CallStatic("startLinkAccount", CurrentActivity);
    }

    public void ChangeServer()
    {
        Bridge.CallStatic("startChangeServer", CurrentActivity);
    }

    public void DeactivateAccount()
    {
        Bridge.CallStatic("startDeactivateAccount", CurrentActivity);
    }

    public void StartPayment()
    {
        Bridge.CallStatic("startPayment", CurrentActivity);
    }

    public void TrackPlayGame(string gameUuid, string characterId, string characterName, string serverId, string serverName)
    {
        Bridge.CallStatic("logPlayGame", gameUuid, characterId, characterName, serverId, serverName);
    }

    public void TrackTutorialCompleted(string gameUuid, string characterId, string characterName, string serverId, string serverName)
    {
        Bridge.CallStatic("logTutorialCompletedS1", gameUuid, characterId, characterName, serverId, serverName);
    }

    public void TrackLevelUp(int level, string gameUuid, string characterId, string characterName, string serverId, string serverName)
    {
        Bridge.CallStatic("logLevelUp", level, gameUuid, characterId, characterName, serverId, serverName);
    }

    public void TrackVipLevel(int vipLevel, string gameUuid, string characterId, string characterName, string serverId, string serverName)
    {
        Bridge.CallStatic("logVipLevel", vipLevel, gameUuid, characterId, characterName, serverId, serverName);
    }

    public void TrackOnlineTime(int minutes, string gameUuid, string characterId, string characterName, int level, string serverId, string serverName)
    {
        Bridge.CallStatic("logOnlineTime", minutes, gameUuid, characterId, characterName, level, serverId, serverName);
    }

    public void OnAuthResult(string payload)
    {
        Debug.Log("KKSoft auth result: " + payload);
    }

    public void OnPaymentResult(string payload)
    {
        Debug.Log("KKSoft payment result: " + payload);
    }
}
```

## 5. Auth Flow

Use this order:

1. Call `Initialize()` once when the game starts.
2. Call `CheckForceUpdate()` before login if the game wants SDK force-update UI.
3. Call `StartAuth()` to open login/register/server selection UI.
4. Wait for `OnAuthResult(payload)`.
5. If needed, call `LinkAccount()`, `ChangeServer()`, `Logout()`, or `DeactivateAccount()`.

Common auth payloads:

```text
auth_success
register_success
link_success
logout_success
deactivate_success
server_selected:<serverId>
failure:<status>:<message>
cancelled
```

## 6. Payment Flow

Use this order:

1. Make sure auth has returned `auth_success` or `register_success`.
2. Call `StartPayment()`.
3. The SDK opens the product list UI.
4. Wait for `OnPaymentResult(payload)`.

Common payment payloads:

```text
success:<productName>
failure
cancelled
closed
error
unavailable_in_server
not_authenticated
```

## 7. Tracking Flow

Tracking is initialized by `Initialize()` through `TrackingConfig`.

Host only passes IDs/tokens for the third-party providers it wants to enable:

```text
firebaseAppId
adjustId
adjustToken
tiktokAppId
tiktokAccessToken
facebookAppId
facebookClientToken
facebookDisplayName
googleClientId
```

Available tracking calls:

```text
TrackPlayGame(...)
TrackTutorialCompleted(...)
TrackLevelUp(level, ...)
TrackVipLevel(vipLevel, ...)
TrackOnlineTime(minutes, ..., level, ...)
```

Supported values:

```text
level: 10, 20, 30, 40, 50, 60, 70, 80, 90, 100
vipLevel: 1, 2, 3, 4, 5, 6, 7, 8, 9, 10
minutes: 5, 10, 30, 60
```
