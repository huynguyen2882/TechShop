package com.example.techshop.Model

// Enum cho các tùy chọn sắp xếp
enum class SortOption {
    NONE, ASCENDING, DESCENDING
}

// Data class cho filter
data class FilterOptions(
    val priceRange: Pair<Double, Double> = Pair(0.0, Double.MAX_VALUE),
    val brands: Set<String> = emptySet(),
    val ramOptions: Set<String> = emptySet(),
    val storageOptions: Set<String> = emptySet(),
    val sortByPrice: SortOption = SortOption.NONE
)