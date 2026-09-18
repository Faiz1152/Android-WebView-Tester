# WebView Tester

A minimal Android WebView browser: Back / Forward / Reload / URL box with a Go arrow, and nothing else.

## Status — please read

This project was written entirely by Claude, but **could not be compiled or run** in the
environment it was written in (no Android SDK, and network access to Google's Maven
repository / Gradle's distribution server is blocked there). So:

- ✅ All source, layout, and Gradle configuration is complete and believed correct.
- ✅ The Gradle wrapper (`gradlew`, `gradlew.bat`, `gradle-wrapper.jar`) is the real,
  official one, fetched directly from the Gradle project's GitHub repo (tag v8.7.0) —
  not hand-written.
- ❌ **Not built. Not run. Not tested on a device or emulator.** No APK is included in
  this ZIP — there is no `APK` folder here. Do not trust any claim otherwise.

## How to get the actual APK — 3 steps, no local installs

1. Create a new GitHub repository and upload every file in this ZIP to it
   (GitHub's web UI "Add file → Upload files" works fine — no git command line needed).
2. Go to the repo's **Actions** tab. A workflow (`.github/workflows/build.yml`) runs
   automatically on push, using GitHub's own build servers (which have full SDK/Maven
   access, unlike the environment this was written in).
3. When the run finishes (~2 minutes), open it and download the **WebViewTester-APK**
   artifact. Inside is `WebViewTester.apk` — the real, compiled, installable app.

## What the app does

- Toolbar: Back | Forward | Reload | URL/search box with a right-facing arrow (Go) — nothing else.
- `https://…` / `http://…` load exactly as typed (no scheme is ever rewritten).
- `youtube.com` → `https://youtube.com` (no `www.` added).
- `www.youtube.com` → `https://www.youtube.com` (`www.` preserved, never stripped or added).
- Anything not shaped like a URL/domain is sent to Google search.
- The Go arrow and the keyboard's Go/Enter key do exactly the same thing.
- Android system Back button navigates WebView history first, then closes the app.
- Starts on `https://example.com`.

## If you'd rather build locally instead of using Actions

Requires Android Studio (or a JDK 17 + Android SDK with `ANDROID_HOME` set) already installed:

```
./gradlew assembleDebug
```

Output: `app/build/outputs/apk/debug/app-debug.apk`
