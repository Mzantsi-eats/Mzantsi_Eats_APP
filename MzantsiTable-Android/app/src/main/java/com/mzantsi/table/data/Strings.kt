package com.mzantsi.table.data

/**
 * Minimal multilingual lookup for the prototype. A production build should move
 * this into Android's resource-based localization (strings.xml per locale) as
 * noted in the POE's "Multi-language" requirement.
 */
object Strings {

    private val table: Map<String, Map<AppLanguage, String>> = mapOf(
        "tagline" to mapOf(
            AppLanguage.EN to "Welcome to the Living Library of South African Recipes",
            AppLanguage.ZU to "Siyakwamukela emndenini wezindlela zokupheka zaseNingizimu Afrika",
            AppLanguage.ST to "Amohelwa Buka ya Diphetoho tsa Afrika Borwa"
        ),
        "get_started" to mapOf(
            AppLanguage.EN to "Get Started",
            AppLanguage.ZU to "Qala Manje",
            AppLanguage.ST to "Qala Joale"
        ),
        "slogan" to mapOf(
            AppLanguage.EN to "Where Mzantsi Eats Together",
            AppLanguage.ZU to "Lapho uMzantsi Adla Ndawonye",
            AppLanguage.ST to "Moo Mzantsi a Jang Hammoho"
        ),
        "home_search_hint" to mapOf(
            AppLanguage.EN to "Search South African recipes...",
            AppLanguage.ZU to "Sesha izindlela zokupheka...",
            AppLanguage.ST to "Batla diphetoho..."
        ),
        "featured_today" to mapOf(
            AppLanguage.EN to "Featured Today",
            AppLanguage.ZU to "Okukhethiwe Namuhla",
            AppLanguage.ST to "Ekhethilweng Kajeno"
        ),
        "popular_right_now" to mapOf(
            AppLanguage.EN to "Popular Right Now",
            AppLanguage.ZU to "Okuthandwa Kakhulu",
            AppLanguage.ST to "Tse Tumileng Hona Jwale"
        ),
        "discover_more" to mapOf(
            AppLanguage.EN to "Discover More",
            AppLanguage.ZU to "Thola Okuningi",
            AppLanguage.ST to "Sibolela Haholoanyane"
        ),
        "cooked_celebration" to mapOf(
            AppLanguage.EN to "Congratulations! You cooked",
            AppLanguage.ZU to "Halala! Uphekile",
            AppLanguage.ST to "Ho lokile! O phehile"
        )
    )

    fun of(key: String, lang: AppLanguage): String =
        table[key]?.get(lang) ?: table[key]?.get(AppLanguage.EN) ?: key
}
