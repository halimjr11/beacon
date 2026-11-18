package com.halimjr11.beacon.domain.model

/** Where a list of articles comes from. */
sealed interface NewsFeed {
    /** Top headlines for the Home highlights. */
    data object Headlines : NewsFeed

    /** Top headlines of one genre. */
    data class Category(val category: NewsCategory) : NewsFeed

    /** Top headlines of one publisher. */
    data class Source(val id: String) : NewsFeed
}
