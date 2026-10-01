using Google.Cloud.Firestore;
using MzantsiTableApi.Models;

namespace MzantsiTableApi.Repositories;

public class FirestoreUserRepository : IUserRepository
{
    private readonly FirestoreDb _db;
    private const string CollectionName = "users";

    public FirestoreUserRepository(FirestoreDb db)
    {
        _db = db;
    }

    public async Task<User?> GetByEmailAsync(string email)
    {
        var snapshot = await _db
            .Collection(CollectionName)
            .WhereEqualTo("Email", email)
            .Limit(1)
            .GetSnapshotAsync();

        if (snapshot.Count == 0)
            return null;

        return snapshot.Documents[0].ConvertTo<User>();
    }

    public async Task<User?> GetByIdAsync(string id)
    {
        var document = await _db
            .Collection(CollectionName)
            .Document(id)
            .GetSnapshotAsync();

        return document.Exists ? document.ConvertTo<User>() : null;
    }

    public async Task<User> InsertAsync(User user)
    {
        await _db
            .Collection(CollectionName)
            .Document(user.UserId)
            .SetAsync(user);

        return user;
    }

    public async Task<User> UpdateAsync(User user)
    {
        await _db
            .Collection(CollectionName)
            .Document(user.UserId)
            .SetAsync(user, SetOptions.MergeAll);

        return user;
    }
}