# The Mzantsi Table - POE

Welcome to **The Mzantsi Table** - a digital living library dedicated to preserving and celebrating South African culinary heritage. This repository contains the projects developed for the **Open Source 6312 Portfolio of Evidence (POE)** at Rosebank International University College.

The project is split into two main components, matching the "Planning and design" section of the POE (UI Design, API Design, UML):

---
## 1. `MzantsiTable-Android/` - open in **Android Studio**

A Kotlin and Jetpack Compose application implementing the mocked-up screens as a working, navigable prototype.

### Features Implemented:
- **Launch Screen:** Includes an EN / ZU / ST language toggle.
- **Authentication:** Register / Login screens (email or "Continue with Google" SSO).
- **Home Feed:** Search bar, "Popular Right Now", and "By Culture" sections.
- **Explore:** A grid of South African cultures with dish counts.
- **Recipe Management:** Add Recipe form, Recipe detail (ingredients, method, save/heart toggle).
- **Interactive Elements:** A "Mark as Completed" celebration pop-up with confetti.
- **User Profile:** Displays saved recipes and user details.
- **Offline Support:** Integrated offline storage capabilities.

### 🎥 Video Demonstration
Watch the live demonstration of the Android application here: 
👉 **[YouTube Demo](https://youtu.be/xhbJAvW5luY)**

---

## 2. `MzantsiTableApi/` — open in **Visual Studio**

An ASP.NET Core Web API (.NET 8) implementing the layered architecture described in the POE's API Design section: **Controllers → Services → Repositories**.

This API is backed by **Firebase Authentication** and **Cloud Firestore**, serving as a secure, centralized data layer for the Android application.

> **Note:** The API has been separated into its own dedicated repository for better version control and deployment. 
> 👉 **[Link to API Repository](https://github.com/Mzantsi-eats/api-repository-)**

---

## 👥 Collaborators

This project was built collaboratively by Group 4:
- **Ondela Skwatsha**
- **Avuyile Dumezweni**
- **Bukhulu Mjana**
- **Olilitha Adam**



---
*Developed for the Open Source 6312 Portfolio of Evidence at Rosebank International University College (2026).*
