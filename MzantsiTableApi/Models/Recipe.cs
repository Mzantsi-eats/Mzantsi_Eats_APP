using Google.Cloud.Firestore;

namespace MzantsiTableApi.Models;

// Kotlin enum Difficulty { EASY, MEDIUM, HARD }.
public enum Difficulty
{
    EASY,
    MEDIUM,
    HARD
}

[FirestoreData]
public class Recipe
{
    [FirestoreProperty("recipeId")] public int RecipeId { get; set; }
    [FirestoreProperty("title")] public string Title { get; set; } = string.Empty;
    [FirestoreProperty("culture")] public string Culture { get; set; } = string.Empty;
    [FirestoreProperty("province")] public string Province { get; set; } = string.Empty;
    [FirestoreProperty("description")] public string Description { get; set; } = string.Empty;
    [FirestoreProperty("imageRes")] public string ImageRes { get; set; } = string.Empty;
    [FirestoreProperty("prepTimeMinutes")] public int PrepTimeMinutes { get; set; }
    [FirestoreProperty("cookTimeMinutes")] public int CookTimeMinutes { get; set; }
    [FirestoreProperty("servings")] public int Servings { get; set; }
    [FirestoreProperty("difficulty")] public string Difficulty { get; set; } = "MEDIUM";
    [FirestoreProperty("ratingAvg")] public double RatingAvg { get; set; }
    [FirestoreProperty("ingredients")] public List<string> Ingredients { get; set; } = new();
    [FirestoreProperty("method")] public List<string> Method { get; set; } = new();
    [FirestoreProperty("authorName")] public string AuthorName { get; set; } = string.Empty;
    [FirestoreProperty("authorUid")] public string AuthorUid { get; set; } = string.Empty;
    [FirestoreProperty("createdAtMillis")] public long CreatedAtMillis { get; set; }

}