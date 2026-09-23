using MzantsiTableApi.Models;
using MzantsiTableApi.Repositories;

namespace MzantsiTableApi.Services;

public interface IUserService
{
    Task<User> RegisterAsync(string name, string email, string password);
    Task<User?> AuthenticateAsync(string email, string password);
    Task<User?> GetProfileAsync(int userId);
    Task<User> UpdateSettingsAsync(int userId, string dietaryPrefs, string languagePref);
}

public class UserService : IUserService
{
    private readonly IUserRepository _repository;

    public UserService(IUserRepository repository)
    {
        _repository = repository;
    }

    public async Task<User> RegisterAsync(string name, string email, string password)
    {
        var existing = await _repository.GetByEmailAsync(email);
        if (existing != null) throw new InvalidOperationException("An account with this email already exists.");

        var user = new User
        {
            Name = name,
            Email = email,
            // Prototype only — swap for Firebase Authentication (per the POE's
            // "Single sign-on (SSO)" and "User registration and login" requirements)
            // rather than hashing/storing passwords in this API directly.
            PasswordHash = BCryptLikeHash(password)
        };
        return await _repository.InsertAsync(user);
    }

    public async Task<User?> AuthenticateAsync(string email, string password)
    {
        var user = await _repository.GetByEmailAsync(email);
        if (user == null || user.PasswordHash != BCryptLikeHash(password)) return null;
        return user;
    }

    public Task<User?> GetProfileAsync(int userId) => _repository.GetByIdAsync(userId);

    public async Task<User> UpdateSettingsAsync(int userId, string dietaryPrefs, string languagePref)
    {
        var user = await _repository.GetByIdAsync(userId)
            ?? throw new KeyNotFoundException("User not found.");
        user.DietaryPrefs = dietaryPrefs;
        user.LanguagePref = languagePref;
        return await _repository.UpdateAsync(user);
    }

    // Placeholder hash — replace with a real algorithm (e.g. BCrypt.Net) or,
    // better, delegate authentication to Firebase entirely.
    private static string BCryptLikeHash(string input) =>
        Convert.ToBase64String(System.Security.Cryptography.SHA256.HashData(System.Text.Encoding.UTF8.GetBytes(input)));
}
