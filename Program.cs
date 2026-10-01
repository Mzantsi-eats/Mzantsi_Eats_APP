using FirebaseAdmin;
using Google.Apis.Auth.OAuth2;
using Google.Cloud.Firestore;
using MzantsiTableApi.Authentication;
using MzantsiTableApi.Repositories;
using MzantsiTableApi.Services;

var builder = WebApplication.CreateBuilder(args);


// Firebase Admin SDK
var serviceAccountPath = builder.Configuration["Firebase:ServiceAccountPath"]
    ?? "firebase-service-account.json";

var fullServiceAccountPath = Path.Combine(
    builder.Environment.ContentRootPath,
    serviceAccountPath);

if (!File.Exists(fullServiceAccountPath))
{
    throw new FileNotFoundException(
        $"Firebase service account not found at '{fullServiceAccountPath}'. " +
        "Download it from Firebase Console > Project settings > Service accounts, " +
        "rename it to firebase-service-account.json, and place it in the project root.");
}

FirebaseApp.Create(new AppOptions
{
    Credential = GoogleCredential.FromFile(fullServiceAccountPath)
});

// Cloud Firestore

var projectId = builder.Configuration["Firebase:ProjectId"]
    ?? throw new InvalidOperationException("Firebase:ProjectId is missing from appsettings.json.");

builder.Services.AddSingleton(
    new FirestoreDbBuilder
    {
        ProjectId = projectId,
        Credential = GoogleCredential.FromFile(fullServiceAccountPath)
    }.Build()
);

// MVC + Authentication
builder.Services.AddControllers();
builder.Services.AddAuthorization();

builder.Services.AddAuthentication("Firebase")
    .AddScheme<Microsoft.AspNetCore.Authentication.AuthenticationSchemeOptions, FirebaseAuthenticationHandler>(
        "Firebase", options => { });

builder.Services.AddEndpointsApiExplorer();
builder.Services.AddSwaggerGen(options =>
{
    options.SwaggerDoc("v1", new Microsoft.OpenApi.Models.OpenApiInfo
    {
        Title = "The Mzantsi Table API",
        Version = "v1",
        Description = "REST API for The Mzantsi Table (Open Source 6312 POE). Firestore-backed."
    });

    options.AddSecurityDefinition("Bearer", new Microsoft.OpenApi.Models.OpenApiSecurityScheme
    {
        Name = "Authorization",
        Type = Microsoft.OpenApi.Models.SecuritySchemeType.Http,
        Scheme = "bearer",
        BearerFormat = "JWT",
        In = Microsoft.OpenApi.Models.ParameterLocation.Header,
        Description = "Enter your Firebase ID token (no 'Bearer' prefix — Swagger adds it)."
    });

    options.AddSecurityRequirement(new Microsoft.OpenApi.Models.OpenApiSecurityRequirement
    {
        {
            new Microsoft.OpenApi.Models.OpenApiSecurityScheme
            {
                Reference = new Microsoft.OpenApi.Models.OpenApiReference
                {
                    Type = Microsoft.OpenApi.Models.ReferenceType.SecurityScheme,
                    Id = "Bearer"
                }
            },
            Array.Empty<string>()
        }
    });
});

builder.Services.AddSingleton<IRecipeRepository, FirestoreRecipeRepository>();
builder.Services.AddSingleton<IUserRepository, FirestoreUserRepository>();
builder.Services.AddScoped<IRecipeService, RecipeService>();
builder.Services.AddScoped<IUserService, UserService>();

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

// Order matters: the middleware verifies the Firebase ID token BEFORE authorization runs.
app.UseMiddleware<FirebaseAuthenticationMiddleware>();
app.UseAuthorization();

app.MapControllers();

app.Run();