package com.mzantsi.table.data

/**
 * Prototype-only in-memory data.
 * Swap this for [com.mzantsi.table.data.api.MzantsiApiService] calls once a
 * REST API backend is built and deployed.
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
        ),

        Recipe(
            recipeId = 6,
            title = "Isigwaqane",
            culture = "Zulu",
            description = "A delicious, flavourful dish which consists of boiled beans being mixed to create" +
                    " a thick, crumbly porridge.",
            prepTimeMinutes = 30,
            servings = 6,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.1f,
            ingredients = listOf("2 cups of water", "a pinch of salt", "375 g of maize", "1 x 400 g of beans", "1 tbspn of butter"),
            method = listOf("Add water and salt to a pot and leave on stove until the water boils.", "Gradually add maize meal into the" +
                    " boiling water and until well mixed.", "Add beans into the pot. Stir until mashed into the maize meal.", "Cover" +
                    " the pot and let it cook for 30 minutes, while stirring occasionally. Add the butter and stir.")
        ),

        Recipe(
            recipeId = 7,
            title = "Isijingi",
            culture = "Zulu",
            description = "Tasty porridge made with pumpkin or butternut and maize meal",
            prepTimeMinutes = 30,
            servings = 3,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.3f,
            ingredients = listOf("500 g pumpkin, peeled and cubed", "1 litre of water", "150 g maize meal", "80 g sugar", "30 g butter"),
            method = listOf("Boil pumpkin in the pot in 2 cups of water. Once boiling, lower the heat and cook until soft.", "When" +
                    " the pumpkin is soft, mash and return it to the pot.", "Add the remaining water and maize meal into the pot. " +
                    "Mix with the pumpkin until everything is thick and smooth.", "Let it cook for 15-20 minutes, stir occasionally.", "Add" +
                    " butter and sugar and cook for another 5 minutes.")
        ),

        Recipe(
            recipeId = 8,
            title = "Koeksister",
            culture = "Desserts",
            description = "A sweet South African treat made from dough and syrup.",
            prepTimeMinutes = 80,
            servings = 14,
            difficulty = Difficulty.MEDIUM,
            ratingAvg = 4.7f,
            ingredients = listOf("1 cup of water", "2 cups of granulated sugar", "1 tbspn of fresh ginger", "1 cinnamon stick", "½ lemon", "1 ¼ teaspoon " +
                    "of cream tartar", "½ teaspoon of salt", "1 ½ flour", "1 cup of corn flour", "2 ½ teaspoons of baking powder", "¾ teaspoons of salt", "2 " +
                    "tbspn sugar,", "2 tbspn softened butter", "1 egg", "½ cup milk", "cooking oil"),
            method = listOf("In a saucepan, add hot water, cream tartar, salt, sugar, lemon, ginger, and cinnamon stick and let it all simmer for 10 minutes. Stir" +
                    " occasionally.", "Let syrup cool and refrigerate until ready to use.", "In a large bowl, add flour, corn flour, baking powder, salt, and sugar.", "Mix" +
                    " in the softened butter with your hands, add egg and milk.", "Knead until ingredients are combined. Let the dough rest for 30 minutes.", "Add flour to " +
                    "work surface and roll the dough until it is 3cm thick. Cut into three sections with sharp knife, and further cut those sections into thin strips.", "Take " +
                    "three strands and stretch them carefully until they are thick evenly. Pinch the ends together at one end.", "Braid it all the way down.", "Heat oil into a pot" +
                    " until it reaches 180℃.", "Gently place koeksisters in the pan and fry until the bottom is golden brown. Turn it over and fry the other side until it is golden" +
                    " brown.", "Remove from the oil with a slotted spoon and let the excess oil drain out. Place koeksisters directly into th syrup.", "Soak in the syrup for a while.", "Transfer " +
                    "to the cooling rack before serving.")
        ),

        Recipe(
            recipeId = 9,
            title = "Ulusu",
            culture = "Xhosa",
            description = "One of the most famous delicious South African meals made from animal tripe such as cows, sheep, or goat.",
            prepTimeMinutes = 210,
            servings = 6,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.8f,
            ingredients = listOf("1 kg ulusu", "1 ½ onions", "Water", "Salt"),
            method = listOf("Rinse ulusu to clean impurities and cut into pieces.", "Place ulusu into pot and add water until ulusu is completely submerged.", "Cook on high heat until ulusu" +
                    " is soft and tender. Continue to add water so that ulusu is submerged.", "Dice onions and add to ulusu. Cook on low heat until onions soften.", "Add salt. Continue to cook for " +
                    "20 minutes.", "Once fully cooked, serve while hot.")
        ),

        Recipe(
            recipeId = 10,
            title = "Samoosa",
            culture = "Cape Malay",
            description = "A triangular fried crunchy or bready pastry with savory filling.",
            prepTimeMinutes = 80,
            servings = 8,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.7f,
            ingredients = listOf("1 ½ salt", "1 onion (chopped)", "500 g minced beef", "2 teaspoons grated garlic", "1 teaspoon grated ginger", "1 teaspoon salt", "½ teaspoon salt", "½ teaspoon tumeric " +
                    "powder", "1 teaspoon chilli powder", "1 teaspoon coriander", "1 teaspoon cumin powder", "1 red chilli (sliced)", "fresh coriander leaves", "½ cups flour", "30 ml water", "8 samoosa pastry sheets"),
            method = listOf("Place all apricot chilli sauce ingredients into a saucepan and heat on medium/low temperature until combined, set aside to cool.", "Heat oil and sauté onion in a pan until " +
                    "translucent.", "Add garlic, ginger, chilli and ground spices and cook 60 seconds or until fragrant (around 60 seconds).", "Add the beef and cook for 10 minutes, stirring frequently to break down the " +
                    "mince.", "Set aside to cool before adding coriander, 1 tbsp of apricot chilli sauce and lemon Seasoning with salt and pepper to taste.", "ake one strip of filo or spring roll pastry and brush one long " +
                    "edge with egg wash.", "Add a tbsp of bobotie mix to pastry and fold into a triangle, repeating until you reach the end of the strip.", "Deep fry in medium oil until golden, about 3 to 4 minutes.", "Serve " +
                    "with remaining apricot chilli sauce.")
        )




    )

    val currentUser = User(
        userId = "1",
        name = "",
        email = "",
        recipesAdded = 0
    )
}
