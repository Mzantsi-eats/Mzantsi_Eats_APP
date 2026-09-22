# The Mzantsi Table — Prototype

`MzantsiTable-Android/` — open in **Android Studio**

Kotlin + Jetpack Compose app implementing the mocked-up screens as a working,
navigable prototype:

- Launch screen with EN / ZU / ST language toggle
- Register / Login (email or "Continue with Google" placeholder)
- Home feed (search bar, Popular Right Now, By Culture)
- Explore Cultures grid
- Add Recipe form
- Recipe detail (ingredients, method, save/heart, "Mark as Completed" celebration pop-up)
- Saved recipes
- Profile

**Data:** runs entirely on in-memory mock data (`data/MockData.kt`) so it launches
with no backend required. `data/api/MzantsiApiService.kt` is a Retrofit interface
already shaped for a future REST API — point its base URL at your API once one
exists and swap the `MockData` calls for it when you're ready to connect the two.

**To run:** open the folder in Android Studio (Koala+), let Gradle sync, run on
an emulator or device (minSdk 26).

## What's intentionally left for the team to finish

- There's no backend yet — `MzantsiApiService.kt` defines the shape of the calls
  the app expects, but nothing is implemented or deployed behind it. Building
  and connecting a REST API is still open.
- Password handling, real authentication, and persistence beyond in-memory mock
  data aren't wired up.
- The recipe-substitution engine, community hub, and multi-language UI beyond the
  launch/home strings aren't built yet — everything else in "Additional features"
  is still open.
