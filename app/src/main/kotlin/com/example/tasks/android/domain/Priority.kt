package com.example.tasks.android.domain

// KMP-shared candidate: pure Kotlin, moves to commonMain as-is.
enum class Priority(val displayName: String, val sortWeight: Int) {
    LOW("Low", 1),
    MEDIUM("Medium", 2),
    HIGH("High", 3),
}
