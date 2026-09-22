package com.mzantsi.table.data

/**
 * Prototype-only in-memory data.
 * Swap this for [com.mzantsi.table.data.api.MzantsiApiService] calls once the
 * ASP.NET Core backend (see the companion MzantsiTableApi project) is deployed.
 */
object MockData {

    val cultures = listOf("Xhosa", "Zulu", "Cape Malay", "Sesotho", "Braai", "Coloured", "Desserts", "Street Food")

    val recipes = listOf(
        Recipe(
            recipeId = 1,
            title = "Bunny Chow",
            culture = "Zulu",
            description = "Durban's iconic street food — a hollowed loaf packed with fragrant curry. Messy, magnificent, unforgettable.",
            prepTimeMinutes = 45,
            servings = 2,
            difficulty = Difficulty.MEDIUM,
            ratingAvg = 4.9f,
            ingredients = listOf(
                "1 loaf white bread", "500g lamb or chicken curry", "1 onion",
                "4 tomatoes", "5 tsp masala spice", "6 potatoes", "Coriander, to garnish"
            ),
            method = listOf(
                "Fry onions, add masala, tomatoes, meat, potatoes.",
                "Simmer 30 min until saucy.",
                "Cut loaf in half, scoop out the bread centre.",
                "Fill the hollowed loaf with hot curry. Serve immediately."
            ),
            authorName = "Sipho M."
        ),
        Recipe(
            recipeId = 2,
            title = "Umngqusho",
            culture = "Xhosa",
            description = "Samp and beans slow-cooked with onion and spice — a Xhosa comfort classic, famously Nelson Mandela's favourite dish.",
            prepTimeMinutes = 80,
            servings = 6,
            difficulty = Difficulty.MEDIUM,
            ratingAvg = 4.7f,
            ingredients = listOf("2 cups samp", "1 cup sugar beans", "1 onion, chopped", "2 tbsp oil", "Salt to taste"),
            method = listOf(
                "Soak samp and beans overnight.",
                "Boil together until soft, about 1 hour.",
                "Fry onion and stir through with seasoning."
            ),
            authorName = "Zanele D."
        ),
        Recipe(
            recipeId = 3,
            title = "Cape Malay Chicken Biryani",
            culture = "Cape Malay",
            description = "Fragrant layered rice and chicken, spiced with a Cape Malay masala blend.",
            prepTimeMinutes = 70,
            servings = 5,
            difficulty = Difficulty.HARD,
            ratingAvg = 4.8f,
            ingredients = listOf("1kg chicken", "3 cups basmati rice", "2 onions", "Biryani masala", "Saffron milk", "Potatoes"),
            method = listOf(
                "Marinate chicken in yoghurt and spices.",
                "Par-boil rice with whole spices.",
                "Layer chicken and rice, then steam ('dum') for 20 minutes."
            ),
            authorName = "Fatima K."
        ),
        Recipe(
            recipeId = 4,
            title = "Braai Boerewors",
            culture = "Braai",
            description = "Coarsely minced, coiled sausage grilled low and slow over the coals.",
            prepTimeMinutes = 35,
            servings = 4,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.8f,
            ingredients = listOf("1kg boerewors", "Rolls", "Tomato and onion relish"),
            method = listOf("Coil the boerewors onto skewers.", "Grill over medium coals, turning occasionally, 20-25 min.")
        ),
        Recipe(
            recipeId = 5,
            title = "Sesotho Moroho",
            culture = "Sesotho",
            description = "A wholesome mix of leafy greens and pumpkin, gently stewed with onion.",
            prepTimeMinutes = 40,
            servings = 4,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.5f,
            ingredients = listOf("Bunch of morogo (wild spinach)", "1 onion", "1 tomato", "Pumpkin, cubed"),
            method = listOf("Fry onion and tomato.", "Add greens and pumpkin, simmer until tender.")
        )
    )

    val currentUser = User(
        userId = 1,
        name = "",
        email = "",
        recipesAdded = 0
    )
}
