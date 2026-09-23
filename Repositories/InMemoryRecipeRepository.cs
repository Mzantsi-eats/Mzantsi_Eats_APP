using MzantsiTableApi.Models;

namespace MzantsiTableApi.Repositories;

public class InMemoryRecipeRepository : IRecipeRepository
{
    private readonly List<Recipe> _recipes;
    private readonly List<Review> _reviews = new();
    private int _nextRecipeId;
    private int _nextReviewId = 1;

    public InMemoryRecipeRepository()
    {
        _recipes = new List<Recipe>
        {
            new()
            {
                RecipeId = 1, Title = "Bunny Chow", Culture = "Zulu",
                Description = "Durban's iconic street food — a hollowed loaf packed with fragrant curry.",
                PrepTimeMinutes = 45, Servings = 2, RatingAvg = 4.9f, AuthorName = "Sipho M.",
                Ingredients = new() { "1 loaf white bread", "500g lamb or chicken curry", "1 onion", "4 tomatoes", "5 tsp masala spice", "6 potatoes", "Coriander" },
                Steps = new() { "Fry onions, add masala, tomatoes, meat, potatoes.", "Simmer 30 min until saucy.", "Cut loaf in half, scoop out the bread centre.", "Fill the hollowed loaf with hot curry. Serve immediately." }
            },
            new()
            {
                RecipeId = 2, Title = "Umngqusho", Culture = "Xhosa",
                Description = "Samp and beans slow-cooked with onion and spice — a Xhosa comfort classic.",
                PrepTimeMinutes = 80, Servings = 6, RatingAvg = 4.7f, AuthorName = "Zanele D.",
                Ingredients = new() { "2 cups samp", "1 cup sugar beans", "1 onion, chopped", "2 tbsp oil", "Salt to taste" },
                Steps = new() { "Soak samp and beans overnight.", "Boil together until soft, about 1 hour.", "Fry onion and stir through with seasoning." }
            },
            new()
            {
                RecipeId = 3, Title = "Cape Malay Chicken Biryani", Culture = "Cape Malay",
                Description = "Fragrant layered rice and chicken, spiced with a Cape Malay masala blend.",
                PrepTimeMinutes = 70, Servings = 5, Difficulty = Difficulty.Hard, RatingAvg = 4.8f, AuthorName = "Fatima K.",
                Ingredients = new() { "1kg chicken", "3 cups basmati rice", "2 onions", "Biryani masala", "Saffron milk", "Potatoes" },
                Steps = new() { "Marinate chicken in yoghurt and spices.", "Par-boil rice with whole spices.", "Layer chicken and rice, then steam ('dum') for 20 minutes." }
            }
        };
        _nextRecipeId = _recipes.Max(r => r.RecipeId) + 1;
    }

    public Task<List<Recipe>> GetAllAsync(string? cuisine = null, string? language = null)
    {
        var query = _recipes.AsEnumerable();
        if (!string.IsNullOrWhiteSpace(cuisine))
            query = query.Where(r => r.Culture.Equals(cuisine, StringComparison.OrdinalIgnoreCase));
        if (!string.IsNullOrWhiteSpace(language))
            query = query.Where(r => r.Language.Equals(language, StringComparison.OrdinalIgnoreCase));
        return Task.FromResult(query.ToList());
    }

    public Task<Recipe?> GetByIdAsync(int id) =>
        Task.FromResult(_recipes.FirstOrDefault(r => r.RecipeId == id));

    public Task<Recipe> InsertAsync(Recipe recipe)
    {
        recipe.RecipeId = _nextRecipeId++;
        recipe.CreatedAt = DateTime.UtcNow;
        _recipes.Add(recipe);
        return Task.FromResult(recipe);
    }

    public Task<List<Recipe>> GetSinceAsync(DateTime sinceTimestamp) =>
        Task.FromResult(_recipes.Where(r => r.CreatedAt >= sinceTimestamp).ToList());

    public Task<Review> InsertReviewAsync(Review review)
    {
        review.ReviewId = _nextReviewId++;
        review.DateSubmitted = DateTime.UtcNow;
        _reviews.Add(review);

        var recipe = _recipes.FirstOrDefault(r => r.RecipeId == review.RecipeId);
        if (recipe != null)
        {
            var ratingsForRecipe = _reviews.Where(r => r.RecipeId == recipe.RecipeId).Select(r => r.Rating).ToList();
            recipe.RatingAvg = ratingsForRecipe.Count > 0 ? (float)ratingsForRecipe.Average() : 0f;
        }
        return Task.FromResult(review);
    }

    public Task<List<Review>> GetReviewsForRecipeAsync(int recipeId) =>
        Task.FromResult(_reviews.Where(r => r.RecipeId == recipeId).ToList());
}
