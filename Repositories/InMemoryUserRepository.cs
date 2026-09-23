using MzantsiTableApi.Models;

namespace MzantsiTableApi.Repositories;

public class InMemoryUserRepository : IUserRepository
{
    private readonly List<User> _users = new();
    private int _nextUserId = 1;

    public Task<User?> GetByEmailAsync(string email) =>
        Task.FromResult(_users.FirstOrDefault(u => u.Email.Equals(email, StringComparison.OrdinalIgnoreCase)));

    public Task<User?> GetByIdAsync(int id) =>
        Task.FromResult(_users.FirstOrDefault(u => u.UserId == id));

    public Task<User> InsertAsync(User user)
    {
        user.UserId = _nextUserId++;
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
