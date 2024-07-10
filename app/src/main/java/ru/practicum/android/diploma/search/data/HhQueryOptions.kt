package ru.practicum.android.diploma.search.data

enum class HhQueryOptions(val key: String) {
    TEXT("text"),
    PAGE("page"),
    PER_PAGE("per_page"),
    SEARCH_FIELD("search_field"),
    DESCRIBE_ARGUMENTS("describe_arguments"),
    VACANCY_SEARCH_ORDER("vacancy_search_order"),
    NO_MAGIC("no_magic"),
}
