namespace MzantsiTableApi.Models;

// Corresponds to the «entity» Review class in the POE's UML diagram.
public class Review
{
    public int ReviewId { get; set; }
    public int RecipeId { get; set; }
    public int UserId { get; set; }
    public int Rating { get; set; } // 1-5
    public string Comment { get; set; } = string.Empty;
    public DateTime DateSubmitted { get; set; } = DateTime.UtcNow;
}
