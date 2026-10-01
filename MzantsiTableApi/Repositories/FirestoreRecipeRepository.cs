using Google.Cloud.Firestore;
using MzantsiTableApi.Models;

namespace MzantsiTableApi.Repositories;

public class FirestoreRecipeRepository : IRecipeRepository
{
    private readonly FirestoreDb _db;
    private const string RecipesCollection = "recipes";
    private const string ReviewsSubcollection = "reviews";

    public FirestoreRecipeRepository(FirestoreDb db)
    {
        _db = db;
    }

    public async Task<List<Recipe>> GetAllAsync(string? cuisine = null, string? language = null)
    {
       
        Query query = _db.Collection(RecipesCollection);

        if (!string.IsNullOrWhiteSpace(cuisine))
        {
            query = query.WhereEqualTo("culture", cuisine);
        }

        var snapshot = await query.GetSnapshotAsync();

        return snapshot.Documents
            .Select(d => d.ConvertTo<Recipe>())
            .OrderBy(r => r.RecipeId)
            .ToList();
    }

    public async Task<Recipe?> GetByIdAsync(int id)
    {
        var doc = await _db
            .Collection(RecipesCollection)
            .Document(id.ToString())
            .GetSnapshotAsync();

        return doc.Exists ? doc.ConvertTo<Recipe>() : null;
    }

    public async Task<Recipe> InsertAsync(Recipe recipe)
    {
     
        if (recipe.RecipeId <= 0)
        {
            recipe.RecipeId = Random.Shared.Next(100_000, int.MaxValue);
        }

        recipe.CreatedAtMillis = DateTimeOffset.UtcNow.ToUnixTimeMilliseconds();

        await _db
            .Collection(RecipesCollection)
            .Document(recipe.RecipeId.ToString())
            .SetAsync(recipe);

        return recipe;
    }

    public async Task<List<Recipe>> GetSinceAsync(DateTime sinceTimestamp)
    {
        // Convert the .NET DateTime to epoch millis so it matches the stored field type.
        var sinceMillis = new DateTimeOffset(sinceTimestamp.ToUniversalTime())
            .ToUnixTimeMilliseconds();

        var snapshot = await _db
            .Collection(RecipesCollection)
            .WhereGreaterThanOrEqualTo("createdAtMillis", sinceMillis)
            .GetSnapshotAsync();

        return snapshot.Documents
            .Select(d => d.ConvertTo<Recipe>())
            .OrderBy(r => r.CreatedAtMillis)
            .ToList();
    }

    public async Task<Review> InsertReviewAsync(Review review)
    {
        var reviewsRef = _db
            .Collection(RecipesCollection)
            .Document(review.RecipeId.ToString())
            .Collection(ReviewsSubcollection);

        // Let Firestore generate a document id, then derive a numeric id for the model.
        var doc = reviewsRef.Document();
        review.ReviewId = doc.Id.GetHashCode(); // stable for the lifetime of the process
        review.DateSubmitted = DateTime.UtcNow;

        await doc.SetAsync(review);

        // Keep the recipe's ratingAvg in sync equivalent to calculateAverageRating()
        await RecalculateAverageRatingAsync(review.RecipeId);

        return review;
    }

    public async Task<List<Review>> GetReviewsForRecipeAsync(int recipeId)
    {
        var snapshot = await _db
            .Collection(RecipesCollection)
            .Document(recipeId.ToString())
            .Collection(ReviewsSubcollection)
            .GetSnapshotAsync();

        return snapshot.Documents
            .Select(d => d.ConvertTo<Review>())
            .ToList();
    }

    private async Task RecalculateAverageRatingAsync(int recipeId)
    {
        var reviews = await GetReviewsForRecipeAsync(recipeId);
        var avg = reviews.Count == 0 ? 0d : reviews.Average(r => r.Rating);

        await _db
            .Collection(RecipesCollection)
            .Document(recipeId.ToString())
            .UpdateAsync("ratingAvg", avg);
    }
}