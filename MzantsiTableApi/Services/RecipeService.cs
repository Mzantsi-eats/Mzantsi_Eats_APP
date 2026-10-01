using MzantsiTableApi.Models;
using MzantsiTableApi.Repositories;

namespace MzantsiTableApi.Services;

public interface IRecipeService
{
    Task<List<Recipe>> GetRecipesAsync(string? cuisine, string? language);
    Task<Recipe?> GetRecipeAsync(int id);
    Task<Recipe> CreateRecipeAsync(Recipe recipe);
    Task<List<Recipe>> SyncRecipesAsync(DateTime sinceTimestamp);
    Task<Review> SubmitReviewAsync(int recipeId, Review review);
}

// Corresponds to the «controller» RecipeHandler class in the POE's UML diagram
// (createRecipe / getRecipes / submitReview / syncRecipes), implemented here as
// the service layer that RecipesController delegates to.
public class RecipeService : IRecipeService
{
    private readonly IRecipeRepository _repository;

    public RecipeService(IRecipeRepository repository)
    {
        _repository = repository;
    }

    public Task<List<Recipe>> GetRecipesAsync(string? cuisine, string? language) =>
        _repository.GetAllAsync(cuisine, language);

    public Task<Recipe?> GetRecipeAsync(int id) => _repository.GetByIdAsync(id);

    public Task<Recipe> CreateRecipeAsync(Recipe recipe) => _repository.InsertAsync(recipe);

    public Task<List<Recipe>> SyncRecipesAsync(DateTime sinceTimestamp) => _repository.GetSinceAsync(sinceTimestamp);

    public async Task<Review> SubmitReviewAsync(int recipeId, Review review)
    {
        review.RecipeId = recipeId;
        return await _repository.InsertReviewAsync(review);
    }
}
