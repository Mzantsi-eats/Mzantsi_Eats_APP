using MzantsiTableApi.Repositories;
using MzantsiTableApi.Services;

var builder = WebApplication.CreateBuilder(args);

builder.Services.AddControllers();
builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "The Mzantsi Table API",
        Version = "v1",
        Description = "Prototype REST API for The Mzantsi Table (Open Source 6312 POE). " +
                      "Backed by an in-memory repository — swap for Firebase Firestore before production."
    });
});

// Layered structure per the POE's API Design section: controllers -> services -> repositories.
builder.Services.AddSingleton<IRecipeRepository, InMemoryRecipeRepository>();
builder.Services.AddSingleton<IUserRepository, InMemoryUserRepository>();
builder.Services.AddScoped<IRecipeService, RecipeService>();
builder.Services.AddScoped<IUserService, UserService>();

// Lets the Kotlin/Jetpack Compose app (via Retrofit) call this API from an
// emulator or device during development.
builder.Services.AddCors(options =>
{
    options.AddPolicy("AllowAndroidClient", policy =>
        policy.AllowAnyOrigin().AllowAnyMethod().AllowAnyHeader());
});

var app = builder.Build();

if (app.Environment.IsDevelopment())
{
    app.UseSwagger();
    app.UseSwaggerUI();
}

app.UseCors("AllowAndroidClient");
app.UseHttpsRedirection();
app.UseAuthorization();
app.MapControllers();

app.Run();
