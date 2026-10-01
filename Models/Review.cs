using Google.Cloud.Firestore;

namespace MzantsiTableApi.Models;

// Corresponds to the entity Review class 
[FirestoreData]
public class Review
{
    [FirestoreProperty("reviewId")] public int ReviewId { get; set; }
    [FirestoreProperty("recipeId")] public int RecipeId { get; set; }
    [FirestoreProperty("userId")] public string UserId { get; set; } = "";
    [FirestoreProperty("rating")] public int Rating { get; set; } // 1-5
    [FirestoreProperty("comment")] public string Comment { get; set; } = string.Empty;
    [FirestoreProperty("dateSubmitted")] public DateTime DateSubmitted { get; set; } = DateTime.UtcNow;
}