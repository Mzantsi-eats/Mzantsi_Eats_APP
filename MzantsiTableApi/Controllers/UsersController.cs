using Microsoft.AspNetCore.Mvc;
using MzantsiTableApi.Models;
using MzantsiTableApi.Services;
using System.Security.Claims;
using Microsoft.AspNetCore.Authorization;

namespace MzantsiTableApi.Controllers;

public record CreateProfileRequest(string Name, string Email);

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

    [Authorize]
    [HttpGet("me")]
    public async Task<ActionResult<User>> GetCurrentUser()
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

        if (string.IsNullOrWhiteSpace(userId))
        {
            return Unauthorized();
        }

        var user = await _userService.GetProfileAsync(userId);

        return user is null ? NotFound() : Ok(user);
    }

    [Authorize]
    [HttpPost("me")]
    public async Task<ActionResult<User>> CreateProfile([FromBody] CreateProfileRequest request)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

        if (string.IsNullOrWhiteSpace(userId))
        {
            return Unauthorized();
        }

        var existingUser = await _userService.GetProfileAsync(userId);

        if (existingUser is not null)
        {
            return Ok(existingUser);
        }

        var user = new User
        {
            UserId = userId,
            Name = request.Name,
            Email = request.Email,
            DietaryPrefs = "",
            LanguagePref = "en",
            RecipesAdded = 0
        };

        var createdUser = await _userService.CreateProfileAsync(user);

        return Ok(createdUser);
    }

    [Authorize]
    [HttpPut("me/settings")]
    public async Task<ActionResult<User>> UpdateSettings(
        [FromBody] UpdateSettingsRequest request)
    {
        var userId = User.FindFirstValue(ClaimTypes.NameIdentifier);

        if (string.IsNullOrWhiteSpace(userId))
        {
            return Unauthorized();
        }

        var user = await _userService.UpdateSettingsAsync(userId, request.DietaryPrefs, request.LanguagePref);

        return Ok(user);
    }

}
