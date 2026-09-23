namespace MzantsiTableApi.Models;

// Corresponds to the «entity» User class in the POE's UML diagram.
public class User
{
    public int UserId { get; set; }
    public string Name { get; set; } = string.Empty;
    public string Email { get; set; } = string.Empty;

    // Prototype only: a real deployment authenticates via Firebase Authentication
    // (per the POE's "Requirements for the app" section) and never stores or
    // returns a password hash through this API.
    public string PasswordHash { get; set; } = string.Empty;

    public string DietaryPrefs { get; set; } = string.Empty;
    public string LanguagePref { get; set; } = "en";
    public int RecipesAdded { get; set; }
}
