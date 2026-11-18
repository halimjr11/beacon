package com.halimjr11.beacon.domain.model

enum class NewsCategory(val apiValue: String, val displayName: String) {
    Business("business", "Business"),
    Entertainment("entertainment", "Entertainment"),
    General("general", "General"),
    Health("health", "Health"),
    Science("science", "Science"),
    Sports("sports", "Sports"),
    Technology("technology", "Technology"),
}
