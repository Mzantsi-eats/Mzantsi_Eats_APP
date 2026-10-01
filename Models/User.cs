using Google.Cloud.Firestore;

namespace MzantsiTableApi.Models;

// Corresponds to the entity User class 
//
// The feild names match the Firestore documents the Android app writes

[FirestoreData]
public class User
{
    [FirestoreProperty("UserId")] public string UserId { get; set; } = "";
    [FirestoreProperty("Name")] public string Name { get; set; } = string.Empty;
    [FirestoreProperty("Email")] public string Email { get; set; } = string.Empty;
    [FirestoreProperty("DietaryPrefs")] public string DietaryPrefs { get; set; } = string.Empty;
    [FirestoreProperty("LanguagePref")] public string LanguagePref { get; set; } = "en";
    [FirestoreProperty("RecipesAdded")] public int RecipesAdded { get; set; }
    [FirestoreProperty("AuthProvider")] public string AuthProvider { get; set; } = "EMAIL";
    [FirestoreProperty("PhotoUrl")] public string PhotoUrl { get; set; } = string.Empty;
    [FirestoreProperty("NotificationsEnabled")] public bool NotificationsEnabled { get; set; } = true;
    [FirestoreProperty("SavedRecipeIds")] public List<int> SavedRecipeIds { get; set; } = new();
}