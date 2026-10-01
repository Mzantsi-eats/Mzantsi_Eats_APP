using MzantsiTableApi.Models;

namespace MzantsiTableApi.Repositories;

public class InMemoryUserRepository : IUserRepository
{
    private readonly List<User> _users = new();

    public Task<User?> GetByEmailAsync(string email) =>
        Task.FromResult(_users.FirstOrDefault(u => u.Email.Equals(email, StringComparison.OrdinalIgnoreCase)));

    public Task<User?> GetByIdAsync(string id) =>
        Task.FromResult(_users.FirstOrDefault(u => u.UserId == id));

    public Task<User> InsertAsync(User user)
    {
        _users.Add(user);
        return Task.FromResult(user);
    }

    public Task<User> UpdateAsync(User user)
    {
        var index = _users.FindIndex(u => u.UserId == user.UserId);
        if (index >= 0) _users[index] = user;
        return Task.FromResult(user);
    }
}
