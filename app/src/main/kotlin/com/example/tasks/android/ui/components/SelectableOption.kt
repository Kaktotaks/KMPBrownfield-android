package com.example.tasks.android.ui.components

/**
 * One entry of a segmented control / menu, already resolved for the UI: the label to show and whether it
 * is the current choice. [value] is handed back to the event lambda unchanged, so the screen never has to
 * compare domain values itself.
 */
data class SelectableOption<T>(
    val value: T,
    val label: String,
    val isSelected: Boolean,
)
