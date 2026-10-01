# Connecting The Mzantsi Table to Firebase

The app now uses **Firebase Authentication** (email/password + Google) and **Cloud Firestore**
for all user data. Everything a signed-in user changes is written to Firestore; the app listens
to Firestore live, so what you see on screen is what is in the database.

## One-time setup (Firebase console)
1. **Authentication > Sign-in method**: enable **Email/Password** and **Google**.
2. **Firestore Database**: make sure it exists (you already created it).
3. **Firestore > Rules**: paste the contents of `firestore.rules` and **Publish**.
4. **Project settings > Your apps > Android** (`com.mzantsi.table`): add each teammate's debug
   **SHA-1** (`./gradlew signingReport`), then **download `google-services.json`** *after*
   enabling Google sign-in and put it in `app/` (next to `app/build.gradle.kts`).
5. Android Studio: **Sync Project with Gradle Files**, then Run.

## What is stored where
Field names match your existing `users` documents (`Name`, `Email`, `LanguagePref`, `DietaryPrefs`,
`RecipesAdded`, `UserId`); the app adds `AuthProvider`, `PhotoUrl`, `NotificationsEnabled`, `SavedRecipeIds`.
`DietaryPrefs` stays a comma-separated string (e.g. `Vegan,Halal`).

| Firestore path | Written when |
|---|---|
| `users/{uid}` | register / Google first sign-in; language, dietary chips, notification switch, name edit, heart (save) toggle, adding a recipe |
| `users/{uid}/completed/{recipeId}` | "Mark as Completed" |
| `recipes/{recipeId}` (all recipe fields + authorUid, createdAtMillis) | Add Recipe |

Collection names live in `data/firebase/FirestorePaths.kt` — change them there if your existing
database uses different names.

## Test checklist (watch the Firebase console while you do each step)
1. Register with email → user appears under **Authentication > Users** and in **Firestore > users**.
2. Profile: toggle a dietary chip / the notification switch / change language / edit name → the
   `users/{uid}` fields change.
3. Open a recipe and tap the heart → its id appears in `savedRecipeIds`.
4. Add a recipe → new doc in `recipes`, and `recipesAdded` goes up by 1.
5. Mark as Completed → doc in `users/{uid}/completed`.
6. Sign out, sign in again (or reinstall) → everything is still there.
7. Turn on airplane mode, make changes, turn it off → they sync to the console.

If a write is rejected (usually the security rules), the app shows a snackbar and logs
`Firestore write failed` in Logcat (filter on `AppViewModel`).

## Known limits
- Recipe **photos** are still saved on the phone only (`imageRes` = local file path), so other
  devices see the placeholder. Syncing photos needs Firebase Storage + an image loader (and Storage
  may require the Blaze plan on new projects).
- The ASP.NET Core API / Retrofit code is no longer used by the app (kept in the project, untouched).
- `LocalAccountStore.kt` is no longer used and can be deleted.
