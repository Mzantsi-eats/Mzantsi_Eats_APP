The Mzantsi Table - Android Prototype
Slogan - Where Mzantsi eats together

This is a Kotlin and Jetpack composed prototype of The Mzantsi Table 

Introduction 
Existing recipe applications offer robust features, but none cater specifically to South African food preservation 
and the celebration of South African cuisines. The Mzantsi Table bridges this gap by including support for 
South African languages such as isiXhosa, isiZulu, Sesotho, and Afrikaans.
This app aims to serve as a digital living library, enabling users to discover, share, and preserve traditional 
recipes while leveraging technology for features such as offline access, real-time notifications, and a community hub.
The app was developed as part of the Open Source 6312 Portfolio of Evidence at Rosebank International University College.

Collaborators include:
Ondela Skwatsha - she built the base code structure (screens, navigation, theme, mock data)
                  API on visual studio code
Avuyile Dumezweni - Wire Retrofit client to RecipeRepository with MockData fallback
                    Update AppViewModel with API calls, logging, and sync logic
                    Add editable settings menu (dietary prefs, language, notifications)
                    Add reviews section to RecipeDetailScreen 
                    Enable HTTP cleartext traffic for local API
Bukhulu Mjana - Added Authentication with real validation and error handling for login, register,                   gradle.
                Created the google SSO 
                All 8 screens with navigation
                Mock recipe data
                Retrofit interface scaffolding
                Working multi-language toggle (EN/ZU/ST)
Olilitha Adam - Database creation, functionality and integration, API modifications

YouTube link - https://youtu.be/DLh84YgZRdU?si=xfbpVTyqtsRZce3i 

Tech Stack
Mobile App - Kotlin, Jetpack Compose, Material 3
Navigation - Jetpack Compose Navigation
State Management - ViewModel + `mutableStateOf`
Networking - Retrofit + Gson
Backend API ASP.NET Core Web API (.NET 8)
API Docs - Swagger / OpenAPI
Planned Auth - Firebase Authentication
Planned Database - Firebase Firestore
Password Hashing - BCrypt work in progress

Running the Android App

1. Open the `MzantsiTable-Android/` folder in Android Studio
2. Let Gradle sync (File - Sync Project with Gradle Files)
3. Start an Android emulator or connect a physical device
4. Click the green Run button

Running the Backend API

1. Open `MzantsiTableApi/MzantsiTableApi.sln` in Visual Studio
2. Press F5 to run
3. Swagger UI opens automatically at `http://localhost:5080/swagger`
4. Test any endpoint with Try it out - Execute

Connecting the App to the API

In `data/api/RetrofitClient.kt`, the base URL is set to `http://10.0.2.2:5080/`
— this is how the Android emulator reaches your computer's `localhost`. 
For a physical device, replace with your computer's LAN IP. For production, replace with your deployed API URL.

Features Implemented

Mandatory Requirements

1. Register / Login
Real form validation with inline error messages
Password strength enforcement (min 8 chars, letter + number + special)
Email format validation
Google Sign-In SSO (placeholder — full integration planned via Firebase Auth)
Multi-language toggle (English / isiZulu / Sesotho)

2. User Settings
Editable profile name with Save/Cancel
Dietary preference chips (Vegetarian, Vegan, Halal, Kosher, Gluten-Free, Dairy-Free, Nut-Free)
Language selection (RadioButtons for EN / ZU / ST)
Notification toggle (Material 3 Switch)
Settings sync to API via `PUT /api/users/{id}/settings`

3. REST API Integration
Custom ASP.NET Core Web API
Endpoints for recipes, users, reviews, and settings
Retrofit client on the Android side
Repository pattern with MockData fallback when offline

Additional Features

4. Recipe Detail Screen
Hero section with culture chip and title
Prep time, servings, and rating display
Numbered ingredient list
Numbered method steps
Save / unsave heart toggle
Reviews section with "Write a Review" dialog

5. Celebration Pop-up
Appears when a user taps "Mark as Completed"
Confetti animation (`🎉 🎊 🎉`)
Congratulations message in the selected language

6. Explore Cultures
grid of South African cultures (Xhosa, Zulu, Cape Malay, Sesotho, Braai, Coloured, Desserts, Street Food)
Each tile shows the culture name and dish count

7. Add Recipe Screen
Form for submitting new recipes
Fields: title, description, culture category, servings, prep time, ingredients, instructions

8. Saved Recipes & Profile
Saved recipes accessible via bottom navigation
Profile shows user details and dietary preferences
Sign out button

Swagger Documentation
Swagger UI is available at /swagger in development mode. Every endpoint can be tested there without the Android app.

Database
The API is designed to connect to Firebase Firestore as the NoSQL database for storing:
Users, Recipes, Reviews, User preferences, Firebase Authentication will manage user registration and login and
Firebase Storage will store recipe images.

Current Implementation
During development, the API uses an in-memory repository so it runs immediately with no cloud setup. 
The repository interfaces (IRecipeRepository, IUserRepository) allow swapping to Firestore with no changes to controllers or services.

Security
Passwords are hashed with BCrypt before storage. Each hash includes a random salt, so identical passwords produce different hashes.

Branch structure
Each team member contributed on their own branch to avoid conflicts:
Ondela
Avuyile 
Bukhulu 
Olilitha

