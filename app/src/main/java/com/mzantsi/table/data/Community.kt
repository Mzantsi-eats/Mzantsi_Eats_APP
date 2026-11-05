package com.mzantsi.table.data

// The Community Hub lets users:
//  - join communities (one per culture, for example "Xhosa Kitchen"),
//  - follow the cooks who share recipes,
//  - share their own attempt at a recipe ("I cooked this!"),
//  - rate other people's attempts (1 to 5 stars), cheer them on and leave a short encouraging comment.

// A community that users can join. Each one is about one culture's food.
data class CommunityGroup(
    val id: String,
    val name: String,
    val culture: String,
    val description: String,
    val baseMembers: Int // members that are already in the community (sample data)
)

// A cook that users can follow. Cooks are the people who shared recipes.
data class Cook(
    val name: String,
    val culture: String,
    val bio: String,
    val baseFollowers: Int
)

// A short encouraging message on an attempt, for example "Looks delicious, well done!".
data class PostComment(
    val authorName: String,
    val text: String,
    val createdAtMillis: Long
)

// Someone's attempt at cooking a recipe, shared with a community.
data class CommunityPost(
    val postId: String,
    val authorKey: String,     // the Firebase uid, or the email in offline mode
    val authorName: String,
    val recipeId: Int,
    val recipeTitle: String,
    val communityId: String,
    val caption: String,
    val photoPath: String = "", // "file:..." for the user's own photo. Empty means the recipe's photo.
    val createdAtMillis: Long,
    val ratings: Map<String, Int> = emptyMap(), // who rated -> stars (1 to 5)
    val cheers: Set<String> = emptySet(),       // who cheered
    val comments: List<PostComment> = emptyList(),
    val isSample: Boolean = false               // sample posts that come with the app
) {
    val averageRating: Float get() = if (ratings.isEmpty()) 0f else ratings.values.average().toFloat()
}

// The sample communities, cooks and attempts that come with the app.
object CommunityData {

    private val descriptions = mapOf(
        "Xhosa" to "Umngqusho, umphokoqo and more. Share your Xhosa kitchen attempts.",
        "Zulu" to "From bunny chow to amasi - Zulu home cooking, together.",
        "Cape Malay" to "Biryani, bobotie and koesisters. Spice lovers welcome.",
        "Sesotho" to "Moroho, mogodu and hearty Sesotho classics.",
        "Braai" to "Coals, boerewors and roosterkoek. Braai masters unite.",
        "Coloured" to "Cape Flats favourites, koesisters and Sunday lunches.",
        "Desserts" to "Malva, milk tart and peppermint crisp - sweet tooth corner.",
        "Street Food" to "Samoosas, vetkoek and kota. Quick bites, big flavour."
    )

    val groups: List<CommunityGroup> = MockData.cultures.mapIndexed { i, culture ->
        CommunityGroup(
            id = groupIdFor(culture),
            name = "$culture Kitchen",
            culture = culture,
            description = descriptions[culture] ?: "Home cooks sharing $culture dishes and their attempts.",
            baseMembers = listOf(128, 96, 143, 64, 210, 57, 88, 119)[i % 8]
        )
    }

    val cooks: List<Cook> = listOf(
        Cook("Zanele D.", "Xhosa", "Cooking my gogo's recipes from the Eastern Cape.", 342),
        Cook("Sipho M.", "Zulu", "Durban born. Curry, bunny chow and Sunday kos.", 287),
        Cook("Fatima K.", "Cape Malay", "Bo-Kaap spice blends and family biryani.", 451),
        Cook("Thabo M.", "Sesotho", "Free State farm cooking - moroho and mogodu.", 168),
        Cook("Johan V.", "Braai", "If it can be braaied, I have braaied it.", 233),
        Cook("Nadia A.", "Coloured", "Koesisters every Sunday morning.", 129),
        Cook("Lerato K.", "Desserts", "Baker. Peppermint crisp tart is my love language.", 204),
        Cook("Ayesha P.", "Street Food", "Samoosas folded the old way.", 176)
    )

    fun groupIdFor(culture: String): String =
        culture.lowercase().replace(Regex("[^a-z0-9]+"), "_").trim('_')

    private const val HOUR = 60L * 60L * 1000L

    // Sample attempts, so the feed is not empty the first time the app opens.
    fun samplePosts(now: Long = System.currentTimeMillis()): List<CommunityPost> = listOf(
        CommunityPost(
            postId = "sample-1", authorKey = "sample:lerato", authorName = "Lerato K.",
            recipeId = 2, recipeTitle = "Umngqusho", communityId = "xhosa",
            caption = "My first umngqusho! Soaked the samp overnight like Zanele said. So creamy.",
            createdAtMillis = now - 3 * HOUR,
            ratings = mapOf("sample:zanele" to 5, "sample:thabo" to 4),
            cheers = setOf("sample:zanele", "sample:sipho", "sample:fatima"),
            comments = listOf(PostComment("Zanele D.", "Beautiful! Gogo would be proud.", now - 2 * HOUR)),
            isSample = true
        ),
        CommunityPost(
            postId = "sample-2", authorKey = "sample:johan", authorName = "Johan V.",
            recipeId = 3, recipeTitle = "Cape Malay Chicken Biryani", communityId = "cape_malay",
            caption = "Tried Fatima's biryani for the family. The dum step makes all the difference.",
            createdAtMillis = now - 20 * HOUR,
            ratings = mapOf("sample:fatima" to 5, "sample:nadia" to 5, "sample:ayesha" to 4),
            cheers = setOf("sample:fatima", "sample:nadia"),
            comments = listOf(PostComment("Fatima K.", "Looks perfect, the rice is so fluffy!", now - 18 * HOUR)),
            isSample = true
        ),
        CommunityPost(
            postId = "sample-3", authorKey = "sample:ayesha", authorName = "Ayesha P.",
            recipeId = 4, recipeTitle = "Braai Boerewors", communityId = "braai",
            caption = "Weekend braai with the cousins. Low and slow over the coals.",
            createdAtMillis = now - 30 * HOUR,
            ratings = mapOf("sample:johan" to 4),
            cheers = setOf("sample:johan", "sample:sipho"),
            isSample = true
        ),
        CommunityPost(
            postId = "sample-4", authorKey = "sample:thabo", authorName = "Thabo M.",
            recipeId = 1, recipeTitle = "Bunny Chow", communityId = "zulu",
            caption = "Quarter mutton bunny. Not as good as Durban yet but getting there!",
            createdAtMillis = now - 52 * HOUR,
            ratings = mapOf("sample:sipho" to 4, "sample:lerato" to 3),
            cheers = setOf("sample:sipho"),
            comments = listOf(PostComment("Sipho M.", "Keep going, more masala next time!", now - 50 * HOUR)),
            isSample = true
        )
    )

    // Quick encouraging messages users can send with one tap.
    val encouragements = listOf(
        "Looks delicious, well done!",
        "Keep cooking - you've got this!",
        "Proud of you for trying this one!",
        "That's how gogo makes it!"
    )
}
