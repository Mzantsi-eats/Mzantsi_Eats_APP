using Microsoft.AspNetCore.Mvc;
using MzantsiTableApi.Models;
using MzantsiTableApi.Services;

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

    // GET /api/recipes?cuisine=Zulu&language=en
    [HttpGet]
    public async Task<ActionResult<List<Recipe>>> GetRecipes([FromQuery] string? cuisine, [FromQuery] string? language)
    {
        var recipes = await _recipeService.GetRecipesAsync(cuisine, language);
        return Ok(recipes);
    }

    // GET /api/recipes/{id}
    [HttpGet("{id:int}")]
    public async Task<ActionResult<Recipe>> GetRecipe(int id)
    {
        var recipe = await _recipeService.GetRecipeAsync(id);
        return recipe is null ? NotFound() : Ok(recipe);
    }

    // POST /api/recipes
    [HttpPost]
    public async Task<ActionResult<Recipe>> CreateRecipe([FromBody] Recipe recipe)
    {
        var created = await _recipeService.CreateRecipeAsync(recipe);
        return CreatedAtAction(nameof(GetRecipe), new { id = created.RecipeId }, created);
    }

    // GET /api/recipes/sync?since=2026-08-01T00:00:00Z
    [HttpGet("sync")]
    public async Task<ActionResult<List<Recipe>>> Sync([FromQuery] DateTime since)
    {
        var recipes = await _recipeService.SyncRecipesAsync(since);
        return Ok(recipes);
    }

    // POST /api/recipes/{id}/reviews
    [HttpPost("{id:int}/reviews")]
    public async Task<ActionResult<Review>> SubmitReview(int id, [FromBody] Review review)
    {
        var created = await _recipeService.SubmitReviewAsync(id, review);
        return Ok(created);
    }
}
