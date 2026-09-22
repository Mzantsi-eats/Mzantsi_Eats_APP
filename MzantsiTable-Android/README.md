# The Mzantsi Table — Prototype

Two projects, matching the "Planning and design" section of the Open Source 6312
POE (UI Design, API Design, UML):

## 1. `MzantsiTable-Android/` — open in **Android Studio**

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
already shaped to match the C# API below — point its base URL at your deployed
API and swap `MockData` calls for it when you're ready to connect the two.

**To run:** open the folder in Android Studio (Koala+), let Gradle sync, run on
an emulator or device (minSdk 26).

## 2. `MzantsiTableApi/` — open in **Visual Studio**

ASP.NET Core Web API (.NET 8) implementing the layered architecture described
in the POE's API Design section: **Controllers → Services → Repositories**.

- `Controllers/RecipesController.cs` — `GET/POST /api/recipes`, `GET /api/recipes/{id}`,
  `POST /api/recipes/{id}/reviews`, `GET /api/recipes/sync`
- `Controllers/UsersController.cs` — register, login, profile, settings
- `Services/` — business logic (mirrors the `RecipeHandler` controller class from
  the POE's UML diagram)
- `Repositories/` — currently **in-memory** (`InMemoryRecipeRepository`,
  `InMemoryUserRepository`) seeded with the same recipes shown in the mockups, so
  the API runs immediately with no cloud setup. Swap these for a Firestore-backed
  implementation (per the POE's "Offline Mode with Sync" section) once you have a
  Firebase service-account key — the interfaces (`IRecipeRepository`,
  `IUserRepository`) are already there to implement against.
- Swagger UI is enabled in development (`/swagger`) so you can try every endpoint
  without the Android app.

**To run:** open `MzantsiTableApi.sln` in Visual Studio, hit Run (or
`dotnet run` from the folder) — it starts on `http://localhost:5080` and opens
Swagger automatically. Enable CORS is already configured so the Android emulator
(`http://10.0.2.2:5080/`) can reach it.

## What's intentionally left for the team to finish

- Password hashing is a placeholder SHA-256 — real auth should go through
  Firebase Authentication as the POE specifies, not this API.
- Firestore, Firebase Storage, and push notifications aren't wired up — the
  repository interfaces and service layer are shaped so those slot in without
  changing the controllers.
- The recipe-substitution engine, community hub, and multi-language UI beyond the
  launch/home strings aren't built yet — everything else in "Additional features"
  is still open.
- Your document has two open review notes worth resolving before submission:
  the "we need more weaknesses" note under Recipe Keeper, and "please include
  milestones" on the Gantt chart.
