using MzantsiTableApi.Models;

namespace MzantsiTableApi.Repositories;

// Corresponds to dataAccess RecipeDBReader 
public interface IRecipeRepository
{
    Task<List<Recipe>> GetAllAsync(string? cuisine = null, string? language = null);
    Task<Recipe?> GetByIdAsync(int id);
    Task<Recipe> InsertAsync(Recipe recipe);
    Task<List<Recipe>> GetSinceAsync(DateTime sinceTimestamp);
    Task<Review> InsertReviewAsync(Review review);
    Task<List<Review>> GetReviewsForRecipeAsync(int recipeId);
}

// Corresponds to dataAccess UserDBReader 
public interface IUserRepository
{
    Task<User?> GetByEmailAsync(string email);
    Task<User?> GetByIdAsync(string id);
    Task<User> InsertAsync(User user);
    Task<User> UpdateAsync(User user);
}
