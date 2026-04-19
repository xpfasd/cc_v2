# Install Referrer Attribution Design

**Date:** 2026-04-19

**Scope:** `super-video-downloader-master/super-video-downloader-master/`

## Goal

Add Google Play Install Referrer support to the downloader app and determine, during app initialization, whether the current install should be treated as an ad-attributed user.

## Product Rules

The app marks a user as ad-attributed when the Google Play install referrer string contains any of these non-empty query parameters:

- `gclid`
- `gbraid`
- `wbraid`
- `fbclid`
- `ttclid`

Persistence rules:

- Once the app records ad attribution as `true`, it never rolls back to `false`.
- If Install Referrer lookup fails, times out, or is unavailable, the app keeps the current stored value.
- If the stored value is still `false`, the app may retry on a later startup.

## Recommended Approach

Use the Play Install Referrer client directly in the app module, not in `topon-ads`.

Why:

- Attribution is app state, not ad-SDK state.
- The existing initialization entry point already lives in `DLApplication`.
- `SharedPrefHelper` is already the repo’s persistence home for app-scoped boolean flags.

## Architecture

### 1. New app dependency

Add the Play Install Referrer library to the downloader app module.

Official Android documentation currently recommends:

- `implementation("com.android.installreferrer:installreferrer:2.2")`

Source:

- [Play Install Referrer Library](https://developer.android.com/google/play/installreferrer/library)

### 2. New attribution checker

Add a small app-level component, tentatively named `InstallReferrerAttributionChecker`, with one responsibility:

- asynchronously fetch Install Referrer details
- parse the returned referrer string as query parameters
- decide whether the install is ad-attributed
- persist the result

Suggested shape:

- app-scoped Kotlin class under `app/src/main/java/com/myAllVideoBrowser/ads/` or `util/ads/`
- public entry method like `checkAndPersistIfNeeded()`

Internals:

- short-circuit immediately if `SharedPrefHelper` already says ad-attributed
- build `InstallReferrerClient`
- call `startConnection(...)`
- on `OK`, read `installReferrer`
- parse with `Uri.parse("https://referrer.local/?$rawReferrer")` or equivalent safe query parsing
- if any tracked parameter exists and is not blank, persist `true`
- always call `endConnection()`

### 3. Persistent flags

Extend `SharedPrefHelper` with two keys:

- `IS_AD_ATTRIBUTED_USER`
- `HAS_ATTEMPTED_INSTALL_REFERRER_ATTRIBUTION`

Behavior:

- `getIsAdAttributedUser(): Boolean`
- `setIsAdAttributedUser(isAttributed: Boolean)` but implementation should only ever persist `true`
- `hasAttemptedInstallReferrerAttribution(): Boolean`
- `setHasAttemptedInstallReferrerAttribution(attempted: Boolean)`

This second flag gives the app a stable notion of “we already tried once,” while still allowing retries when the stored attribution result is still `false`.

### 4. App initialization hook

Start the attribution check from `DLApplication.onCreate()`.

Current relevant file:

- [DLApplication.kt](/D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/DLApplication.kt)

Placement:

- after Dagger injection is available
- before or after `TopOnAds.initializeFromManifest(...)` is acceptable
- do not block startup on the Install Referrer request

Recommended behavior:

- launch attribution lookup on a background coroutine
- keep app startup and ad SDK initialization non-blocking

### 5. Logging

Add lightweight internal logs only:

- attribution check started
- referrer fetch success/failure code
- referrer matched tracked parameters or not
- stored value became `true`

Do not log the raw full referrer string in release-oriented code paths.

## Data Flow

1. App process starts.
2. `DLApplication.onCreate()` kicks off the attribution checker.
3. Checker reads persisted state from `SharedPrefHelper`.
4. If already ad-attributed, it exits.
5. Otherwise it requests Install Referrer from Google Play.
6. Returned referrer string is parsed.
7. If any of `gclid`, `gbraid`, `wbraid`, `fbclid`, `ttclid` is present and non-empty, `SharedPrefHelper` stores ad-attributed = `true`.
8. Future startups treat the user as ad-attributed without reevaluating back to `false`.

## Error Handling

Handle these as non-fatal:

- `FEATURE_NOT_SUPPORTED`
- `SERVICE_UNAVAILABLE`
- remote exceptions / runtime exceptions while reading referrer
- malformed referrer payload

Rules:

- never crash app startup
- never block `DLApplication.onCreate()`
- never overwrite stored `true` with `false`

## Testing Strategy

### Unit tests first

Prefer pure JVM tests for the parsing and persistence rules.

Add tests for:

1. `gclid` marks attributed
2. `gbraid` marks attributed
3. `wbraid` marks attributed
4. `fbclid` marks attributed
5. `ttclid` marks attributed
6. empty referrer does not mark attributed
7. unrelated parameters do not mark attributed
8. once stored `true`, later non-matching referrers do not revert it

### Checker boundary tests

Abstract the Install Referrer fetch behind a small interface if needed so tests can simulate:

- successful response
- unsupported feature
- unavailable service
- exception path

## File Plan

Likely touched files:

- `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/build.gradle.kts`
- `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/gradle/libs.versions.toml`
- `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/DLApplication.kt`
- `D:/code/cc_v2/cc_v2/super-video-downloader-master/super-video-downloader-master/app/src/main/java/com/myAllVideoBrowser/util/SharedPrefHelper.kt`
- one new Kotlin source file for attribution checking
- one or more new JVM tests under `app/src/test/java/com/myAllVideoBrowser/...`

## Non-Goals

- No MMP integration
- No backend attribution upload
- No change to ad scene logic yet
- No user-facing UI for attribution state in this task

## Notes

This design intentionally uses Google Play Install Referrer as the transport even for Facebook and TikTok style click IDs. The attribution decision remains simple: the app only checks whether those click identifiers appear in the Play-provided install referrer payload.
