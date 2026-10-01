using MzantsiTableApi.Models;
using MzantsiTableApi.Repositories;

namespace MzantsiTableApi.Services;

public interface IUserService
{
    Task<User?> GetProfileAsync(string userId);
    Task<User> CreateProfileAsync(User user);
    Task<User> UpdateSettingsAsync(string userId, string dietaryPrefs, string languagePref);
}

public class UserService : IUserService
{
    private readonly IUserRepository _repository;

    public UserService(IUserRepository repository)
    {
        _repository = repository;
    }

    public Task<User?> GetProfileAsync(string userId)
    {
        return _repository.GetByIdAsync(userId);
    }

    public Task<User> CreateProfileAsync(User user)
    {
        return _repository.InsertAsync(user);
    }


    public async Task<User> UpdateSettingsAsync(string userId, string dietaryPrefs, string languagePref)
    {
        var user = await _repository.GetByIdAsync(userId)
            ?? throw new KeyNotFoundException("User not found.");
        user.DietaryPrefs = dietaryPrefs;
        user.LanguagePref = languagePref;
        return await _repository.UpdateAsync(user);
    }

}
