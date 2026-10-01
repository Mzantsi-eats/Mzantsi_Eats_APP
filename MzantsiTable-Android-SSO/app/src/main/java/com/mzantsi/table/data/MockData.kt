package com.mzantsi.table.data


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
            culture = "Xhosa",
            province = "Eastern Cape",
            description = "A crumbly, savoury maize-meal dish cooked with onion and tomato, eaten as a hearty side or main.",
            prepTimeMinutes = 10,
            cookTimeMinutes = 30,
            servings = 4,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.4f,
            ingredients = listOf("2 cups maize meal", "3 cups water", "1 onion, finely chopped", "2 tomatoes, grated", "2 tbsp oil", "1 tsp mild curry or mixed spice", "Salt to taste"),
            method = listOf(
                "Fry the onion in oil until soft, add tomato and spice, and simmer for 5 minutes.",
                "Add the water and salt and bring to the boil.",
                "Sprinkle in the maize meal, cover and let it steam for 10 minutes.",
                "Stir with a fork to keep it crumbly and cook on low heat for 20 more minutes."
            )
        ),
        Recipe(
            recipeId = 7,
            title = "Isijingi",
            culture = "Xhosa",
            province = "Eastern Cape",
            description = "Creamy pumpkin and maize-meal porridge, golden, soft and comforting.",
            prepTimeMinutes = 10,
            cookTimeMinutes = 30,
            servings = 4,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.6f,
            ingredients = listOf("500g pumpkin, cubed", "1 cup water", "1/2 cup maize meal", "1 tbsp butter", "1 tbsp sugar (optional)", "Pinch of salt"),
            method = listOf(
                "Boil the pumpkin in the water until very soft, then mash it in the pot.",
                "Stir in the maize meal gradually to avoid lumps.",
                "Cook on low heat for about 20 minutes, stirring often.",
                "Finish with butter, a pinch of salt and sugar if you like it sweet."
            )
        ),
        Recipe(
            recipeId = 8,
            title = "Koeksisters",
            culture = "Afrikaner",
            province = "Western Cape",
            description = "Plaited, deep-fried dough dunked in ice-cold syrup: crisp outside, sticky and sweet inside.",
            prepTimeMinutes = 40,
            cookTimeMinutes = 30,
            servings = 12,
            difficulty = Difficulty.HARD,
            ratingAvg = 4.8f,
            ingredients = listOf("4 cups flour", "4 tsp baking powder", "1/4 tsp salt", "2 tbsp butter", "1 egg", "1 cup milk", "Oil for deep frying", "Syrup: 4 cups sugar, 2 cups water, 1 tsp cream of tartar, 1 tsp lemon juice"),
            method = listOf(
                "Boil the syrup ingredients for 5 minutes, then chill until ice cold.",
                "Rub butter into the dry ingredients, add egg and milk and knead to a soft dough. Rest 1 hour.",
                "Roll out, cut into strips and plait three strips together, pinching the ends.",
                "Deep-fry on medium heat until golden brown.",
                "Dip the hot koeksisters straight into the cold syrup for about a minute, then drain."
            )
        ),
        Recipe(
            recipeId = 9,
            title = "Mapone (Mopane Worms)",
            culture = "Sesotho",
            province = "Limpopo",
            description = "Dried mopane worms stewed with onion and tomato: a protein-rich delicacy enjoyed across southern Africa.",
            prepTimeMinutes = 30,
            cookTimeMinutes = 45,
            servings = 4,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.2f,
            ingredients = listOf("500g dried mopane worms", "2 onions, chopped", "2 tomatoes, chopped", "2 tbsp oil", "1 chilli (optional)", "Salt to taste"),
            method = listOf(
                "Soak the dried worms in warm water for 2-3 hours, then rinse well.",
                "Boil in fresh salted water for 30-40 minutes until tender.",
                "Fry the onion, tomato and chilli until saucy.",
                "Add the worms and simmer for 10 minutes. Serve with pap."
            )
        ),
        Recipe(
            recipeId = 10,
            title = "Mogodu",
            culture = "Sesotho",
            province = "Free State",
            description = "Slow-cooked tripe in its own rich broth, traditionally served with pap.",
            prepTimeMinutes = 30,
            cookTimeMinutes = 180,
            servings = 6,
            difficulty = Difficulty.MEDIUM,
            ratingAvg = 4.5f,
            ingredients = listOf("1kg cleaned tripe", "2 onions, chopped", "3 garlic cloves", "2 tbsp oil", "1 tsp salt", "1 tsp mixed spice", "Pap, to serve"),
            method = listOf(
                "Wash the tripe well and cut it into bite-sized pieces.",
                "Boil in salted water for 2.5 to 3 hours until tender.",
                "Fry the onion and garlic with the spice, then add the tripe and some of its broth.",
                "Simmer for 30 minutes and serve hot with pap."
            )
        ),
        Recipe(
            recipeId = 11,
            title = "Peppermint Crisp Tart",
            culture = "Desserts",
            province = "Gauteng",
            description = "The no-bake South African classic: caramel cream layered with biscuits and grated peppermint chocolate.",
            prepTimeMinutes = 20,
            cookTimeMinutes = 0,
            servings = 8,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.9f,
            ingredients = listOf("200g Tennis biscuits", "1 tin (360g) caramel treat / dulce de leche", "500ml fresh cream, whipped", "2 Peppermint Crisp bars, grated"),
            method = listOf(
                "Whip the cream to soft peaks and fold in half of the caramel.",
                "Layer biscuits in a dish, then spread with the caramel cream and sprinkle with grated chocolate.",
                "Repeat the layers, finishing with chocolate on top.",
                "Chill overnight so the biscuits soften."
            )
        ),
        Recipe(
            recipeId = 12,
            title = "Roosterkoek",
            culture = "Braai",
            province = "Free State",
            description = "Bread rolls grilled over the braai coals: smoky, chewy and perfect with butter, cheese or jam.",
            prepTimeMinutes = 30,
            cookTimeMinutes = 20,
            servings = 8,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.7f,
            ingredients = listOf("4 cups flour", "1 tsp salt", "1 tbsp sugar", "10g instant yeast", "1 1/2 cups warm water", "2 tbsp oil"),
            method = listOf(
                "Mix all ingredients and knead for 10 minutes until smooth.",
                "Cover and let rise for 1 hour.",
                "Shape into flat rolls and rest for 20 minutes.",
                "Grill over medium coals for 15-20 minutes, turning often. Serve warm."
            )
        ),
        Recipe(
            recipeId = 13,
            title = "Samoosas",
            culture = "Street Food",
            province = "KwaZulu-Natal",
            description = "Crispy triangles of spiced mince folded in thin pastry and fried until golden.",
            prepTimeMinutes = 40,
            cookTimeMinutes = 15,
            servings = 6,
            difficulty = Difficulty.MEDIUM,
            ratingAvg = 4.8f,
            ingredients = listOf("1 pack samoosa pastry", "500g mince", "1 onion, finely chopped", "2 tsp masala", "1 tsp garlic and ginger paste", "Handful of fresh coriander", "Flour-and-water paste to seal", "Oil for frying"),
            method = listOf(
                "Fry the onion, add paste, masala and mince and cook until dry. Stir in coriander and cool.",
                "Cut pastry into strips, fold into cones and fill with the mince.",
                "Fold into triangles and seal the edge with the flour paste.",
                "Deep-fry until golden and crisp. Drain and serve hot."
            )
        ),
        Recipe(
            recipeId = 14,
            title = "Ulusu",
            culture = "Zulu",
            province = "KwaZulu-Natal",
            description = "Tender tripe gently simmered in a light, flavourful broth: a Zulu favourite for family gatherings.",
            prepTimeMinutes = 30,
            cookTimeMinutes = 180,
            servings = 6,
            difficulty = Difficulty.MEDIUM,
            ratingAvg = 4.5f,
            ingredients = listOf("1kg cleaned tripe", "2 onions, sliced", "2 garlic cloves", "2 carrots, sliced", "1 tbsp curry powder (optional)", "Salt and pepper", "Pap or bread, to serve"),
            method = listOf(
                "Clean the tripe well and cut it into pieces.",
                "Boil in salted water for 2.5 to 3 hours until soft.",
                "Add onion, garlic, carrots and seasoning and simmer for 30 minutes.",
                "Serve hot with pap or bread."
            )
        ),
        Recipe(
            recipeId = 15,
            title = "Umvubo",
            culture = "Zulu",
            province = "KwaZulu-Natal",
            description = "Crumbly maize meal mixed with cold, tangy amasi (sour milk): refreshing, quick and deeply traditional.",
            prepTimeMinutes = 5,
            cookTimeMinutes = 20,
            servings = 2,
            difficulty = Difficulty.EASY,
            ratingAvg = 4.3f,
            ingredients = listOf("2 cups crumbly pap (phutu), cooled", "1 litre amasi (sour milk)", "Pinch of salt", "Sugar, to taste (optional)"),
            method = listOf(
                "Cook the maize meal into a stiff, crumbly pap and let it cool slightly.",
                "Crumble the pap into a bowl.",
                "Pour the cold amasi over and mix. Add a pinch of salt or sugar to taste."
            )
        )
    )

    val currentUser = User(
        userId = 1,
        name = "",
        email = "",
        recipesAdded = 0
    )
}
