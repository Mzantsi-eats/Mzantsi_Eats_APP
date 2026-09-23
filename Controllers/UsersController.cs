using Microsoft.AspNetCore.Mvc;
using MzantsiTableApi.Models;
using MzantsiTableApi.Services;

namespace MzantsiTableApi.Controllers;

public record RegisterRequest(string Name, string Email, string Password);
public record LoginRequest(string Email, string Password);
public record UpdateSettingsRequest(string DietaryPrefs, string LanguagePref);

[ApiController]
[Route("api/[controller]")]
public class UsersController : ControllerBase
{
    private readonly IUserService _userService;

    public UsersController(IUserService userService)
    {
        _userService = userService;
    }

    // POST /api/users/register
    [HttpPost("register")]
    public async Task<ActionResult<User>> Register([FromBody] RegisterRequest request)
    {
        try
        {
            var user = await _userService.RegisterAsync(request.Name, request.Email, request.Password);
            return CreatedAtAction(nameof(GetUser), new { id = user.UserId }, Sanitize(user));
        }
        catch (InvalidOperationException ex)
        {
            return Conflict(new { message = ex.Message });
        }
    }

    // POST /api/users/login
    [HttpPost("login")]
    public async Task<ActionResult<User>> Login([FromBody] LoginRequest request)
    {
        var user = await _userService.AuthenticateAsync(request.Email, request.Password);
        return user is null ? Unauthorized() : Ok(Sanitize(user));
    }

    // GET /api/users/{id}
    [HttpGet("{id:int}")]
    public async Task<ActionResult<User>> GetUser(int id)
    {
        var user = await _userService.GetProfileAsync(id);
        return user is null ? NotFound() : Ok(Sanitize(user));
    }

    // PUT /api/users/{id}/settings
    [HttpPut("{id:int}/settings")]
    public async Task<ActionResult<User>> UpdateSettings(int id, [FromBody] UpdateSettingsRequest request)
    {
        var user = await _userService.UpdateSettingsAsync(id, request.DietaryPrefs, request.LanguagePref);
        return Ok(Sanitize(user));
    }

    private static User Sanitize(User user)
    {
        user.PasswordHash = string.Empty; // never return the hash to the client
        return user;
    }
}
