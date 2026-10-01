using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using MzantsiTableApi.Models;
using MzantsiTableApi.Services;
using System.Security.Claims;

namespace MzantsiTableApi.Controllers;

[ApiController]
[Route("api/[controller]")]
public class RecipesController : ControllerBase
{
    private readonly IRecipeService _recipeService;

    public RecipesController(IRecipeService recipeService)
    {
        _recipeService = recipeService;
    }

    // GET /api/recipes?cuisine=Zulu&language=en   (public — anyone can browse)
    [HttpGet]
    public async Task<ActionResult<List<Recipe>>> GetRecipes(
        [FromQuery] string? cuisine,
        [FromQuery] string? language)
    {
        var recipes = await _recipeService.GetRecipesAsync(cuisine, language);
        return Ok(recipes);
    }

    // GET /api/recipes/{id}   (public)
    [HttpGet("{id:int}")]
    public async Task<ActionResult<Recipe>> GetRecipe(int id)
    {
        var recipe = await _recipeService.GetRecipeAsync(id);
        return recipe is null ? NotFound() : Ok(recipe);
    }

    // GET /api/recipes
    [HttpGet("sync")]
    public async Task<ActionResult<List<Recipe>>> Sync([FromQuery] DateTime since)
    {
        var recipes = await _recipeService.SyncRecipesAsync(since);
        return Ok(recipes);
    }

    // POST /api/recipes   (requires a Firebase ID token)
    [Authorize]
    [HttpPost]
    public async Task<ActionResult<Recipe>> CreateRecipe([FromBody] Recipe recipe)
    {
        var uid = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrWhiteSpace(uid))
        {
            return Unauthorized();
        }

        recipe.AuthorUid = uid;

        var created = await _recipeService.CreateRecipeAsync(recipe);
        return CreatedAtAction(nameof(GetRecipe), new { id = created.RecipeId }, created);
    }

    // POST /api/recipes/{id}/reviews   (requires a Firebase ID token)
    [Authorize]
    [HttpPost("{id:int}/reviews")]
    public async Task<ActionResult<Review>> SubmitReview(int id, [FromBody] Review review)
    {
        var uid = User.FindFirstValue(ClaimTypes.NameIdentifier);
        if (string.IsNullOrWhiteSpace(uid))
        {
            return Unauthorized();
        }

        review.UserId = uid;

        var created = await _recipeService.SubmitReviewAsync(id, review);
        return Ok(created);
    }
}