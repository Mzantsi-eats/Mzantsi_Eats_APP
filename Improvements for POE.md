# The Mzantsi Table — Implementation Improvements

This document outlines the improvements and additions made to The Mzantsi Table after the initial prototype and planning phase.

## 1. User Authentication
The original design proposed Firebase Authentication for secure login and registration. During implementation, this was successfully integrated, allowing users to:
- Register with an existing email and password.
- Log in to existing accounts.
- Securely authenticate using Firebase Authentication.
- Maintain authenticated sessions.
- **Password Rules:** The app now checks the password as you type and shows a short message under the field (e.g., "Add an uppercase letter and a number"). The rules require 8+ characters with uppercase, lowercase, a number, and a symbol.
- **Other Checks:** Added a Confirm Password box, pre-send email/password validation on login, and a show/hide button for every password box.
- **Post-Registration:** Users are not signed in automatically. The screen switches to Login with their email filled in and the message "Account created. Please sign in."

## 2. Cloud Database Integration
Instead of storing information locally, the application now stores recipe information inside **Google Firebase Firestore**.
- Real-time recipe retrieval.
- Cloud-based storage and automatic synchronization.
- Scalable NoSQL storage.
- Recipes remain available even when multiple users access the application simultaneously.

## 3. ASP.NET Core Web API
The application now communicates through a dedicated REST API built with ASP.NET Core (.NET 8).
- **Endpoints:** User management, Recipe CRUD operations, and secure communication between Android and Firestore.
- **Security:** Guarded by Firebase Admin SDK middleware to verify ID tokens.
- **Documentation:** Fully documented via Swagger/OpenAPI.

## 4. UI & Feature Updates

### Explore Screen
The green blocks that appeared because the culture image folder was empty have been fixed. The cards now use photos of that culture's recipes instead. As in the prototype, there is a dark green "Explore Cultures" header and full-photo cards that fade into each culture's color. Each card has a flag stripe, the culture name, and "N dishes". The "By Culture" cards on the Home screen now look the same.

### Community Hub
- **Home:** Added a new "Community" section with cook cards and "See all", as in the prototype.
- **Community Hub:** Contains three tabs:
  - **Feed:** Shows people's cooking attempts, which you can rate (1–5 stars), cheer, or reply to with an encouraging message.
  - **Communities:** One group per culture (e.g., "Xhosa Kitchen") to join or leave.
  - **Cooks:** Allows you to follow the people who post recipes.
- **Sharing:** "Share your attempt" lets you pick a recipe, a community, a photo, and a caption. After "Mark as Completed", the pop-up now has a "Share with the community" button.
- **Sample Content:** Added sample cooks, communities, and posts so the hub isn't empty. Member and follower counts are those sample numbers plus your own joins and follows.

### Offline Downloads
- Users can download a single recipe from its page, or every recipe in a culture with "Download all offline".
- The Saved screen now has a **"Saved"** tab and an **"Offline"** tab, where you can remove downloads.
- Downloaded recipes, including their reviews, open without internet.
- A grey "You're offline" banner appears when there is no connection, and tapping it opens your downloads.

### Celebratory Pop-up Notifications
- When a user completes a recipe, they must press "Mark as Done". This prompts the application to show a celebratory feature (confetti and a "Congratulations!" message).
- This also appears when they engage within the community.

### Recipe Upload and Sharing
- Recipe share lets you upload any South African recipe with a title. You can add as many recipes as you want, and these recipes are shared within the community.
- The Add Recipe form includes fields for title, category, servings, prep time, cook time, description, ingredients, and method steps.

## 5. Design Considerations
- Utilization of GitHub for version control.
- Separation of the Backend API into its own dedicated repository for better deployment and security management.


