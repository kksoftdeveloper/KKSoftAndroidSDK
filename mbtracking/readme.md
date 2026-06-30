# MB Tracking SDK — Integration Guide

The MB Tracking SDK provides a unified interface for tracking events across **Firebase Analytics** and **Adjust**. Events are dispatched to both providers simultaneously via a single API call.

## Table of Contents

- [Prerequisites](#prerequisites)
- [Installation](#installation)
- [Initialization](#initialization)
- [Auth Tracking (Automatic)](#auth-tracking-automatic)
- [In-Game Tracking Functions](#in-game-tracking-functions)
  - [logPlayGame](#1-logplaygame)
  - [logTutorialCompletedS1](#2-logtutorialcompleteds1)
  - [logLevelUp](#3-loglevelup)
  - [logVIPLevel](#4-logviplevel)
  - [logOnlineTime](#5-logonlinetime)
- [Model Classes](#model-classes)
- [Event Name Reference](#event-name-reference)
- [Best Practices](#best-practices)
- [Troubleshooting](#troubleshooting)

---

## Prerequisites

- Minimum SDK: API 23 (Android 6.0)
- Target SDK: API 34 (Android 14)
- `MbSdk` (mbcore module) must be initialized before the Tracking SDK

### Required Permissions

```xml
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />

<!-- For mobile carrier detection included in in-game events -->
<uses-permission android:name="android.permission.READ_PHONE_STATE" />
```

> If `READ_PHONE_STATE` is not granted, `af_mobile_carrier` / `adj_mobile_carrier` will be `"unknown"`. All other tracking continues to work normally.

---

## Installation

```gradle
dependencies {
    implementation project(':mbcore')
    implementation project(':mbtracking')
}
```

The module bundles:
- Firebase Analytics (via Firebase BOM)
- Adjust SDK + install referrer adapters
- `com.google.android.gms:play-services-ads-identifier`

---

## Initialization

Initialize the Tracking SDK in `Application.onCreate()`, **after** `MbSdk.init()`:

**Java:**
```java
public class MyApp extends Application {
    @Override
    public void onCreate() {
        super.onCreate();

        // 1. Core SDK must be initialized first
        MbSdkConfig config = new MbSdkConfig.Builder()
                .setServerClientId(null)
                .setAppId(BuildConfig.APPLICATION_ID)
                .setAppVersionName(BuildConfig.VERSION_NAME)
                .build();
        MbSdk.INSTANCE.init(this, () -> config);

        // 2. Initialize Tracking SDK — pass your Adjust App Token
        TrackingLoader.INSTANCE.loadOnce("YOUR_ADJUST_APP_TOKEN");

        // 3. Auth SDK
        MbAuth.INSTANCE.init();
    }
}
```

**Kotlin:**
```kotlin
class MyApp : Application() {
    override fun onCreate() {
        super.onCreate()
        val config = MbSdkConfig.Builder()
            .setServerClientId(null)
            .setAppId(BuildConfig.APPLICATION_ID)
            .setAppVersionName(BuildConfig.VERSION_NAME)
            .build()
        MbSdk.init(this) { config }
        TrackingLoader.loadOnce("YOUR_ADJUST_APP_TOKEN")
        MbAuth.init()
    }
}
```

`TrackingLoader.loadOnce()` is idempotent — calling it multiple times is safe.

### What happens on initialization

1. **Firebase Analytics** is started via `FirebaseAnalytics.getInstance()`.
2. **Adjust SDK** is started with your app token in `ENVIRONMENT_SANDBOX` (set `ENVIRONMENT_PRODUCTION` for release builds in `AdjustTrackingProvider`).
3. The Adjust advertiser ID (`adid`) is fetched asynchronously and saved to internal DataStore — it is then automatically included in all subsequent login and register API requests.

---

## Auth Tracking (Automatic)

The following events are tracked **automatically** by the SDK when authentication actions occur. You do not need to call these manually.

| Trigger | Firebase Event | Adjust Event |
|---|---|---|
| Login form opened | `fb_open_login_form` | `adj_open_login_form` |
| Login success | `fb_login` | `adj_login` |
| Login failure | `fb_login_fail` | `adj_login_fail` |
| Registration success | `fb_registration` | `adj_registration` |
| Day-1 retention (login on D+1) | `fb_retention_d1` | `adj_retention_d1` |

### Auth Event Parameters

**`fb_login` / `adj_login`**

| Key (Firebase) | Key (Adjust) | Value |
|---|---|---|
| `user_id` | `adj_uid` | `gameUuid` |
| `method` | `adj_login_method` | Login method (e.g. `"phone"`, `"google"`, `"facebook"`, `"guest"`) |
| — | `adj_mobile_carrier` | Mobile carrier name |

**`fb_registration` / `adj_registration`**

| Key (Firebase) | Key (Adjust) | Value |
|---|---|---|
| `user_id` | `adj_uid` | `gameUuid` |
| `method` | `adj_signup_method` | Registration method |
| — | `adj_mobile_carrier` | Mobile carrier name |

**`fb_login_fail` / `adj_login_fail`**

| Key (Firebase) | Key (Adjust) | Value |
|---|---|---|
| `method` | `adj_login_method` | Login method attempted |
| — | `adj_login_fail_reason` | Error message from API |

**`adj_retention_d1`** (Adjust only)

| Key | Value |
|---|---|
| `adj_uid` | `gameUuid` |
| `adj_retention_days` | `1` |
| `adj_mobile_carrier` | Mobile carrier name |

> **Retention D1 logic:** The SDK records a 2 AM timestamp on the first login day. On any subsequent login that falls within the 24–48 hour window after that 2 AM mark, `adj_retention_d1` is fired once per user.

---

## In-Game Tracking Functions

These functions must be called manually from your game code at the appropriate moments. All functions require `gameUuid`, `characterId`, `serverId`, and `serverName` — obtain these from `MbAuth.updateServerClientId()` after authentication.

All in-game events automatically include `af_mobile_carrier` / `adj_mobile_carrier`.

---

### 1. logPlayGame

Tracks when the player enters the game.

```kotlin
GameTracking.logPlayGame(
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
)
```

**Java:**
```java
GameTracking.INSTANCE.logPlayGame(
    gameUuid, characterId, characterName, serverId, serverName
);
```

**When:** When the player enters the game world after login and server selection.

---

### 2. logTutorialCompletedS1

Tracks completion of the first tutorial (S1).

```kotlin
GameTracking.logTutorialCompletedS1(
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
)
```

**When:** Immediately after S1 tutorial completion. Call only **once per character**.

---

### 3. logLevelUp

Tracks reaching a milestone character level (multiples of 10, up to 100).

```kotlin
GameTracking.logLevelUp(
    level: Level,
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
)
```

**Java:**
```java
import com.appmb.sdk.mbtracking.model.Level;

GameTracking.INSTANCE.logLevelUp(Level.Level50, gameUuid, characterId, characterName, serverId, serverName);
```

**When:** When the character's level reaches 10, 20, 30, 40, 50, 60, 70, 80, 90, or 100. Call only **once per milestone**.

---

### 4. logVIPLevel

Tracks reaching a VIP level milestone (1–10).

```kotlin
GameTracking.logVIPLevel(
    level: VIPLevel,
    gameUUID: String,
    characterId: String,
    characterName: String,
    serverId: String,
    serverName: String
)
```

**Java:**
```java
import com.appmb.sdk.mbtracking.model.VIPLevel;

GameTracking.INSTANCE.logVIPLevel(VIPLevel.Level5, gameUuid, characterId, characterName, serverId, serverName);
```

**When:** When the player reaches VIP level 1 through 10. Call only **once per VIP milestone**.

---

### 5. logOnlineTime

Tracks cumulative online time milestones.

```kotlin
GameTracking.logOnlineTime(
    time: OnlineTime,
    gameUUID: String,
    characterId: String,
    characterName: String,
    level: Level,       // current character level at the time of the event
    serverId: String,
    serverName: String
)
```

**Java:**
```java
import com.appmb.sdk.mbtracking.model.OnlineTime;
import com.appmb.sdk.mbtracking.model.Level;

GameTracking.INSTANCE.logOnlineTime(
    OnlineTime.OL30minutes,
    gameUuid, characterId, characterName,
    Level.Level50,   // current level
    serverId, serverName
);
```

**When:** When the player reaches 5, 10, 30, or 60 minutes of cumulative online time. Call only **once per time milestone**.

---

## Model Classes

### Level

```java
// Values: Level10, Level20, Level30, Level40, Level50,
//         Level60, Level70, Level80, Level90, Level100
Level level = Level.Level50;
int value = level.getValue();       // 50
String name = level.toString();     // "level_50"
```

### VIPLevel

```java
// Values: Level1 through Level10
VIPLevel vip = VIPLevel.Level5;
int value = vip.getValue();         // 5
String name = vip.toString();       // "vip_level_5"
```

### OnlineTime

```java
// Values: OL5minutes, OL10minutes, OL30minutes, OL60minutes
OnlineTime time = OnlineTime.OL30minutes;
int minutes = time.getMinutes();    // 30
```

---

## Event Name Reference

### Auth Events (automatic)

| Trigger | Firebase | Adjust |
|---|---|---|
| Login form opened | `fb_open_login_form` | `adj_open_login_form` |
| Login success | `fb_login` | `adj_login` |
| Login failure | `fb_login_fail` | `adj_login_fail` |
| Registration success | `fb_registration` | `adj_registration` |
| Day-1 retention | `fb_retention_d1` | `adj_retention_d1` |

### In-Game Events (manual)

| Method | Firebase | Adjust |
|---|---|---|
| `logPlayGame` | `fb_play_game` | `adj_play_game` |
| `logTutorialCompletedS1` | `fb_tutorial_completed_s1` | `adj_tutorial_completed_s1` |
| `logLevelUp(Level.Level50)` | `fb_lev_50` | `adj_lev_50` |
| `logVIPLevel(VIPLevel.Level5)` | `fb_vip_level_5` | `adj_vip_level_5` |
| `logOnlineTime(OL30minutes, ...)` | `fb_online_30mins` | `adj_online_30mins` |

### In-Game Event Parameters

| Parameter | Firebase Key | Adjust Key | Present In |
|---|---|---|---|
| Game UUID | `user_id` | `adj_uid` | All in-game events |
| Character ID | `character` | `adj_character_id` | All in-game events |
| Character Name | — | `adj_character_name` | All in-game events |
| Server ID | — | `adj_server_id` | All in-game events |
| Server Name | — | `adj_server_name` | All in-game events |
| Mobile Carrier | — | `adj_mobile_carrier` | All in-game events |
| Level (int) | `level` | `adj_level` | `logVIPLevel`, `logOnlineTime` |

> **Firebase note:** Firebase Analytics has a 40-character limit on event names and a 100-event parameter limit per event. Event names use the `fb_` prefix to stay within the reserved namespace.

> **Adjust note:** Event tokens in Adjust are configured as the full event name string (e.g. `adj_play_game`). In production, map these to your Adjust dashboard event tokens.

---

## Best Practices

1. **Initialization order is mandatory:**
   `MbSdk.init()` → `TrackingLoader.loadOnce()` → `MbAuth.init()`

2. **Obtain `gameUuid`, `characterId`, `serverId` from `updateServerClientId`** before calling any `GameTracking` method. Store and reuse these values for the entire session.

3. **Track each milestone only once.** Use a persisted flag to prevent duplicate events:
   ```java
   SharedPreferences prefs = getSharedPreferences("tracking", MODE_PRIVATE);
   if (!prefs.getBoolean("tutorial_done", false)) {
       GameTracking.INSTANCE.logTutorialCompletedS1(...);
       prefs.edit().putBoolean("tutorial_done", true).apply();
   }
   ```

4. **Request `READ_PHONE_STATE` early** so carrier data is available when the first event fires.

5. **Switch Adjust to production** before releasing:
   In `AdjustTrackingProvider.kt`, change `AdjustConfig.ENVIRONMENT_SANDBOX` to `AdjustConfig.ENVIRONMENT_PRODUCTION`.

---

## Troubleshooting

### Events not appearing in Firebase / Adjust dashboards

- Verify initialization order: `MbSdk.init()` → `TrackingLoader.loadOnce()` → `MbAuth.init()`
- Filter Logcat by `TrackingSDK-Firebase` or `TrackingSDK-Adjust` for initialization errors
- Firebase and Adjust dashboards have a propagation delay; wait a few minutes

### Logcat tags

| Tag | What it shows |
|---|---|
| `GameTracking` | Each `GameTracking.*` call with event name and parameters |
| `TrackingSDK-Firebase` | Firebase initialization and event dispatch |
| `TrackingSDK-Adjust` | Adjust initialization, Adjust ID, and event dispatch |
| `TrackingSdk` | General SDK-level logs and Adjust/GAID IDs |

**Example output:**
```
I/TrackingSDK-Firebase: Initialized FirebaseAnalytics
I/TrackingSDK-Adjust:   Initialized with appToken: ****x6gw
D/TrackingSdk:          [TESTING] Adjust ID: xxxx-xxxx-xxxx-xxxx
D/GameTracking:         [TRACK] fb_play_game | gameUUID: abc | characterId: 001 | serverId: IOS1 | carrier: Viettel
```

### Adjust ID not available

- The Adjust ID (`adid`) is fetched asynchronously with a 2-second delay after initialization
- It requires a network connection on first launch
- Look for `[TESTING] Adjust ID:` in Logcat (tag: `TrackingSdk`) after 2–3 seconds

### `af_mobile_carrier` / `adj_mobile_carrier` is "unknown"

- Declare `READ_PHONE_STATE` in the manifest and request it at runtime
- Test on a physical device with an active SIM card — emulators do not provide carrier information
