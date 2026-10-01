# The Mzantsi Table

> **Where Mzantsi eats together** — a living library of South African recipes.

The Mzantsi Table is a mobile application and backend API dedicated to preserving
and celebrating South African culinary heritage. It exists because existing recipe
apps serve global cuisines well, but none focus on South African food traditions or
support local languages such as isiXhosa, isiZulu, Sesotho, and Afrikaans.

This repository contains two projects:

| Folder | What it is |
|---|---|
| **`MzantsiTable-Android/`** | Kotlin + Jetpack Compose Android app (prototype) |
| **`MzantsiTableApi/`** | ASP.NET Core Web API backend, backed by Firebase |

Together they form a digital living library where users can discover, share, and
preserve traditional South African recipes.

---

## Table of contents

- [Repository structure](#repository-structure)
- [Quick start](#quick-start)
- [Tech stack](#tech-stack)
- [Features](#features)
- [API reference](#api-reference)
- [Security model](#security-model)
- [Project documentation](#project-documentation)
- [Collaborators](#collaborators)
- [Branch structure](#branch-structure)
- [Academic context](#academic-context)

---

## Repository structure
Mzantsi_Eats_APP/
├── MzantsiTable-Android/ # Android app (Kotlin, Jetpack Compose)
├── MzantsiTableApi/ # Backend API (.NET 8, ASP.NET Core)
├── .idea/ # Android Studio project metadata
└── README.md # This file

Each subfolder has its own README with detailed setup instructions.

---

## Quick start

### Prerequisites

- **Android Studio** (Koala or newer) for the Android app
- **Visual Studio 2022** with the **.NET 8 SDK** for the API
- A **Firebase project** with Authentication and Firestore enabled
  (see `MzantsiTableApi/FIREBASE_SETUP.md`)

### Clone the repository

```bash
git clone https://github.com/Mzantsi-eats/Mzantsi_Eats_APP.git
cd Mzantsi_Eats_APP

##Run the API
1. Open MzantsiTableApi/MzantsiTableApi.sln in Visual Studio.

2. Add your firebase-service-account.json to MzantsiTableApi/
(see MzantsiTableApi/FIREBASE_SETUP.md for how to obtain it).

3. Press F5 to start the API.

4. Swagger UI opens at **`https://localhost:5081/swagger/index.html`** — you can
   test every endpoint from the browser without the Android app.

Run the Android app
Open the MzantsiTable-Android/ folder in Android Studio.

Let Gradle sync (File → Sync Project with Gradle Files).

Start an emulator or connect a physical device.

Click the green Run button.

Connect the app to the API
In MzantsiTable-Android/app/src/main/java/com/mzantsi/table/data/api/RetrofitClient.kt,
the base URL is http://10.0.2.2:5081/ — this is how the Android emulator reaches
the host computer's localhost. For a physical device, replace with your computer's
LAN IP. For production, replace with the deployed API URL.

Tech stack
Android app
Layer	Technology
Language	Kotlin
UI	Jetpack Compose, Material 3
Navigation	Navigation-Compose
State	ViewModel + mutableStateOf
Networking	Retrofit + Gson
Auth	Firebase Authentication (email/password + Google SSO)
Local cache	Firebase Firestore offline persistence
Backend API
Layer	Technology
Framework	ASP.NET Core Web API (.NET 8)
Language	C# 12
Auth	Firebase Admin SDK (ID token verification)
Database	Cloud Firestore (NoSQL)
Documentation	Swagger / OpenAPI
Architecture	Controllers → Services → Repositories
Features
Implemented
Authentication — email/password registration with strong password rules
(min 8 chars, uppercase, lowercase, digit, special character), plus Google
Sign-In via Firebase Authentication.

Multilingual interface — English, isiZulu, and Sesotho, switchable without restart.

Recipe management — browse, search, and view recipes; add your own with photos
and cultural categorisation (Xhosa, Zulu, Cape Malay, Sesotho, Braai, Coloured,
Desserts, Street Food).

Cultural recipe discovery — explore recipes by culture and province.

Saved recipes — bookmark recipes for later.

Reviews — rate and comment on recipes.

Celebration pop-up — a confetti animation when a user marks a recipe as cooked.

User settings — profile name, dietary preferences, notification toggle,
language preference.

Offline support — recipes and user data remain accessible without an internet
connection via Firestore's offline cache.

REST API — full CRUD for recipes, users, and reviews, documented via Swagger.

Planned / future work
Firebase Storage for recipe images (currently local file paths).

Push notifications via FCM for community interactions.

Recipe substitution suggestions for common South African ingredients.

Community hub with follow / share features.

Afrikaans language support.

API reference
The API is self-documenting. When it runs locally, Swagger UI is available at:

> **<https://localhost:5081/swagger/index.html>**

Every endpoint listed below can be tested there — including authenticated ones,
after pasting a Firebase ID token into the **Authorize** dialog.

Recipes
Method	Endpoint	Auth	Purpose
GET	/api/Recipes	Public	List all recipes (optional ?cuisine= filter)
GET	/api/Recipes/{id}	Public	Get one recipe
GET	/api/Recipes/sync?since=	Public	Recipes changed since a timestamp
POST	/api/Recipes	🔒	Create a recipe (author from ID token)
POST	/api/Recipes/{id}/reviews	🔒	Submit a review for a recipe
Users
Method	Endpoint	Auth	Purpose
GET	/api/Users/me	🔒	Current user's profile
POST	/api/Users/me	🔒	Create a profile on first sign-in
PUT	/api/Users/me/settings	🔒	Update dietary prefs and language
🔒 = requires a valid Firebase ID token in the Authorization: Bearer <token> header.

Security model
User authentication — handled by Firebase Authentication. The API does not
store passwords.

Request authentication — every protected endpoint verifies the Firebase ID
token via the Firebase Admin SDK, before the request reaches the controller.

Client-supplied identity is never trusted — the API reads the user's UID
from the verified ID token, not from the request body.

Firestore access — the Android app writes to Firestore through Firebase's
security rules; the API writes through the Admin SDK, which bypasses rules
and is used only from trusted server-side code.

Service credentials — the Firebase service-account key is stored in
firebase-service-account.json, is listed in .gitignore, and is never
committed to source control.

Project documentation
Firebase setup — MzantsiTableApi/FIREBASE_SETUP.md

Google SSO setup — MzantsiTable-Android/SETUP_GOOGLE_SSO.md

API setup — MzantsiTableApi/README.md

Android app setup — see the "Quick start" section above

Collaborators
Name	Primary contributions
Ondela Skwatsha	Base Android code structure (screens, navigation, theme, mock data); initial API design
Avuyile Dumezweni	Retrofit client + RecipeRepository; AppViewModel API integration; settings menu; reviews section; HTTP cleartext for local API
Bukhulu Mjana	Authentication with validation and error handling; Google SSO; all eight screens with navigation; mock recipe data; Retrofit scaffolding; multilingual toggle
Olilitha Adam	Database creation and integration; API modifications; Firestore repository
Group 4 (shared)	Firestore migration, Firebase Auth middleware, Swagger documentation, repository reorganization
Branch structure
Each collaborator works on their own feature branch. The current branches are:

ondela-original — the baseline Android prototype

avuyile — Avuyile's working branch

avuyile-api — Avuyile's API experiments (BCrypt auth)

bukhulu — Bukhulu's working branch

olilitha — Olilitha's working branch

firebase-api — the Firestore-backed API (current production candidate)

Contributors should open a pull request to merge changes into the shared
baseline rather than committing directly to ondela-original.

Academic context
This project was developed as part of the Open Source 6312 Portfolio of
Evidence at Rosebank International University College (2026).

Video demonstration: Watch on YouTube

License
This project is developed for academic purposes. See the assignment brief
for usage terms.
