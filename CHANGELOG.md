## 📦 Changelog

### [1.1.0] - 2026-06-26
#### Changed
- Get Game packages by curent Server

### [1.1.0] - 2026-05-29
#### Added
- Handle staging or production by production=false in local.properties

- Migrated from multiple standalone SDK modules to a single unified SDK entry point following the current project style.
  - Keeps Auth, Payment, Tracking, and shared Core responsibilities organized behind one SDK integration surface.
  - Simplifies host app integration while preserving the existing feature behavior and module ownership.
  The files changes: SDK module structure, public SDK entry point, Unity Android example integration

### [1.1.0] - 2026-02-03
#### Added
- IAP Dialog Close Callback - Added `PurchaseResult.ClosedProductList` and `ProductListEvent.ClosedProductList` to handle when user closes the IAP dialog without making a purchase
  - When user clicks the close button, a broadcast is sent with `ClosedProductList` result
  - Allows developers to detect when users dismiss the payment screen
  The classes changes: `PurchaseResult`, `ProductListEvent`, `ProductListScreen`

#### Changed
- Updated and simplified `google-services.json` files after GCP project migration to KKSOFT owner
  The files changes: `launcher/google-services.json`, `unityLibrary/google-services.json`

- Updated default server configuration to iOS1 in demo/launcher applications
  The files changes: `MyApp.java`, `Main2Activity.java`

### [1.0.0] - 2026-01-22
#### Added
- New API `MbAuth.updateServerClientId(serverName: String?, onResult: (UpdateServerIdResult) -> Unit)` for updating server ID by server name
  - Fetches server list and matches server name to find corresponding server ID
  - Updates session with latest gameUuid and characterId when authenticated
  - Saves server ID and name locally even when user is not authenticated
  The classes changes: `MbAuth`, `MbServerManager`, `MbServerManagerImpl`

#### Enhanced
- Allow guest users to perform In-App Purchase (IAP)
  - Removed authentication requirement for payment flow
  - Guest users can now access payment screen and complete purchases
  The classes changes: `MbPayment`, Payment flow components

#### Changed
- Updated game logo assets
  The classes changes: UI resource files, `mbcoreui` module

#### Fixed
- Fixed null `sdkVersion` issue in `fetchGameInfo()` when called before `MbAuth.init()`
  The classes changes: `MbGameRepositoryImpl`

### [1.0.0] - 2025-10-17
#### Added
- Additorium API `mbSdkConfig.serverId("IOS1")`
The classs changes: `MbSdkConfig`

- Additorium Notification Server maintenance when the given server-id is null or empty

### [1.0.0] - 2025-11-21
#### Changed
- Change from `mbSdkConfig.serverId("IOS1")` tp `mbSdkConfig.setServerClientId("IOS1")`
The classs changes: `MbSdkConfig`
