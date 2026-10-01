# Google Sign-In (SSO) — setup

The POE specifies: *"Google Sign-In integration using Firebase Authentication"*.
That is exactly what `auth/GoogleSignInManager.kt` does (Android Credential Manager → Google ID token → Firebase Auth).

## It works out of the box (demo mode)
With **no** `google-services.json`, tapping **Continue with Google** opens a demo account chooser
(two sample accounts + "use another account"). It runs through the *same* code path as real
Google sign-in, so you can demo/test everything — registration, auto-login, sign-out, and the
rule *"if you signed up with Google you can only log in with Google"*.

## Switch on real Google accounts (5 minutes)
1. Go to <https://console.firebase.google.com> → **Add project** (e.g. "Mzantsi Table").
2. **Build ▸ Authentication ▸ Get started ▸ Sign-in method ▸ Google ▸ Enable** → save.
3. **Project settings ▸ Your apps ▸ Add app ▸ Android**
   - Package name: `com.mzantsi.table`
   - SHA-1: run in Android Studio's terminal  
     `./gradlew signingReport` (Windows: `gradlew signingReport`) and copy the **debug** SHA-1.
4. Download **`google-services.json`** and put it in **`MzantsiTable-Android/app/`** (next to `build.gradle.kts`).
   - If Firebase asks you to add the SHA-1 *after* enabling Google sign-in, re-download the file so it contains the web client (`client_type: 3`).
5. **File ▸ Sync Project with Gradle Files**, then Run.
   The Gradle build detects the file and applies the `google-services` plugin automatically — no other edits.
6. Use an **emulator image with the Play Store icon** (or a real phone) that has a Google account added.

Each teammate must add **their own debug SHA-1** in Firebase (Project settings ▸ your Android app ▸ Add fingerprint),
otherwise Google Sign-In returns an error on their machine. They can all share the same `google-services.json`.

## How the pieces fit
| File | Role |
|------|------|
| `auth/GoogleSignInManager.kt` | Credential Manager + Firebase sign-in / sign-out |
| `auth/AuthValidation.kt` | Email format + password-strength rules from the POE |
| `data/LocalAccountStore.kt` | On-device accounts (PBKDF2-hashed passwords) + "stay signed in" |
| `viewmodel/AppViewModel.kt` | `register`, `login`, `onGoogleSignedIn`, `signOut`, session restore |
| `ui/screens/AuthScreens.kt` | Register / Login tabs, "Continue with Google", demo chooser |

## Troubleshooting
- **"No Google account found on this device"** → add a Google account on the emulator/phone.
- **Error 10 / "developer error" / sign-in sheet closes instantly** → SHA-1 missing/wrong in Firebase, or `google-services.json` was downloaded before enabling Google sign-in.
- **Still seeing "Demo mode" text** → `google-services.json` isn't in `app/`, or it has no web client; re-download and re-sync.
