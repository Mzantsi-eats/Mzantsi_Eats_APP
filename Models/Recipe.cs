namespace MzantsiTableApi.Models;

public enum Difficulty
{
    Easy,
    Medium,
    Hard
}

// Corresponds to the «entity» Recipe class in the POE's UML diagram.
public class Recipe
{
    public int RecipeId { get; set; }
    public string Title { get; set; } = string.Empty;
    public string Culture { get; set; } = string.Empty; // Xhosa, Zulu, Cape Malay, Sesotho, Braai, Coloured...
    public string Province { get; set; } = string.Empty;
    public string Language { get; set; } = "en";
    public string Description { get; set; } = string.Empty;
    public string ImageUrl { get; set; } = string.Empty;
    public int PrepTimeMinutes { get; set; }
    public int Servings { get; set; }
    public Difficulty Difficulty { get; set; } = Difficulty.Medium;
    public float RatingAvg { get; set; }
    public List<string> Ingredients { get; set; } = new();
    public List<string> Steps { get; set; } = new();
    public string AuthorName { get; set; } = string.Empty;
    public int AuthorUserId { get; set; }
    public DateTime CreatedAt { get; set; } = DateTime.UtcNow;

    // getRecipeDetails() / findRecipesByCuisine() / calculateAverageRating() from the
    // UML are implemented as service-layer behaviour rather than entity methods —
    // see Services/RecipeService.cs.
}
