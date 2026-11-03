package com.example.tasks.android.ui.components

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource

/**
 * One entry of a segmented control / menu, already resolved for the UI: the label to show and whether it
 * is the current choice. [value] is handed back to the event lambda unchanged, so the screen never has to
 * compare domain values itself. The label is either app copy ([labelRes]) or text from shared ([label]).
 */
data class SelectableOption<T>(
    val value: T,
    val isSelected: Boolean,
    @StringRes val labelRes: Int? = null,
    val label: String = "",
)

@Composable
fun <T> SelectableOption<T>.text(): String = labelRes?.let { stringResource(it) } ?: label
