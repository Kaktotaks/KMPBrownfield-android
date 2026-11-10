package com.example.tasks.android.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.tasks.android.R
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

/** PillSearchField (§2): custom `BasicTextField`, h 44, r `pill`, `surface2`; clear button when not empty. */
@Composable
fun PillSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    val colors = TasksTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .heightIn(min = 44.dp)
            .clip(TasksShapes.pill)
            .background(colors.surface2)
            .padding(horizontal = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(TasksIcons.Search, contentDescription = null, tint = colors.textSecondary, modifier = Modifier.size(18.dp))
        Box(Modifier.weight(1f)) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                enabled = enabled,
                singleLine = true,
                textStyle = TasksType.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.textPrimary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { inner ->
                    if (value.isEmpty()) {
                        Text(stringResource(R.string.search_placeholder), style = TasksType.body, color = colors.textSecondary)
                    }
                    inner()
                },
            )
        }
        if (value.isNotEmpty()) {
            Icon(
                TasksIcons.Close,
                contentDescription = stringResource(R.string.search_clear),
                tint = colors.textSecondary,
                modifier = Modifier
                    .size(20.dp)
                    .clickable(role = Role.Button, onClick = { onValueChange("") }),
            )
        }
    }
}

/** Border of a field (§2): 1.5 `border`, focused 2 `textPrimary`, invalid 2 `danger`. */
@Composable
private fun Modifier.fieldChrome(focused: Boolean, invalid: Boolean, minHeight: androidx.compose.ui.unit.Dp): Modifier {
    val colors = TasksTheme.colors
    val (width, color) = when {
        invalid -> 2.dp to colors.danger
        focused -> 2.dp to colors.textPrimary
        else -> 1.5.dp to colors.border
    }
    return this
        .fillMaxWidth()
        .heightIn(min = minHeight)
        .clip(TasksShapes.s)
        .background(colors.surface)
        .border(width, color, TasksShapes.s)
}

/** Label above a field: `overline`, `textSecondary`, gap 6. */
@Composable
private fun FieldLabel(label: String) {
    Text(label.uppercase(), style = TasksType.overline, color = TasksTheme.colors.textSecondary)
}

/** Message under a field; M6 fade + height expand. */
@Composable
private fun FieldError(message: String?) {
    AnimatedVisibility(visible = message != null, enter = TasksVisibility.enter, exit = TasksVisibility.exit) {
        // Keep the last message while the exit animation runs.
        val shown = remember(message) { message }
        if (shown != null) {
            Text(shown, style = TasksType.caption, color = TasksTheme.colors.danger, modifier = Modifier.padding(top = 6.dp))
        }
    }
}

/** TasksTextField (§2): not M3 `OutlinedTextField`. [minLines] > 1 makes it the notes field. */
@Composable
fun TasksTextField(
    label: String,
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
    error: String? = null,
    minLines: Int = 1,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
) {
    val colors = TasksTheme.colors
    val source = remember { MutableInteractionSource() }
    val focused by source.collectIsFocusedAsState()
    val multiline = minLines > 1
    Column(modifier) {
        FieldLabel(label)
        Box(
            Modifier
                .padding(top = 6.dp)
                .fieldChrome(focused, error != null, if (multiline) 96.dp else 48.dp)
                .padding(horizontal = 14.dp, vertical = if (multiline) 12.dp else 0.dp),
            contentAlignment = if (multiline) Alignment.TopStart else Alignment.CenterStart,
        ) {
            BasicTextField(
                value = value,
                onValueChange = onValueChange,
                singleLine = !multiline,
                minLines = minLines,
                interactionSource = source,
                keyboardOptions = keyboardOptions,
                textStyle = TasksType.body.copy(color = colors.textPrimary),
                cursorBrush = SolidColor(colors.textPrimary),
                modifier = Modifier
                    .fillMaxWidth()
                    .semantics {
                        contentDescription = label
                        if (error != null) error(error)
                    },
                decorationBox = { inner ->
                    if (value.isEmpty() && placeholder != null) {
                        Text(placeholder, style = TasksType.body, color = colors.textSecondary)
                    }
                    inner()
                },
            )
        }
        FieldError(error)
    }
}

/** DateField (§2): looks like [TasksTextField]; tapping opens the system picker (hosted by the screen). */
@Composable
fun DateField(
    label: String,
    valueText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    error: String? = null,
) {
    val colors = TasksTheme.colors
    val description = stringResource(R.string.a11y_date_field, label, valueText)
    Column(modifier) {
        FieldLabel(label)
        Row(
            Modifier
                .padding(top = 6.dp)
                .fieldChrome(focused = false, invalid = error != null, minHeight = 48.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics(mergeDescendants = true) { contentDescription = description }
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(valueText, style = TasksType.body, color = colors.textPrimary)
            Icon(TasksIcons.Calendar, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
        }
        FieldError(error)
    }
}

/**
 * TimeField: looks like [DateField] (clock icon); tapping opens the M3 time picker (hosted by the screen).
 * [valueText] is an AnnotatedString so a caller can bold the time ("Today, Wed 4 Nov · **16:30**"); [placeholder] = "No time" state.
 */
@Composable
fun TimeField(
    label: String,
    valueText: androidx.compose.ui.text.AnnotatedString,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaceholder: Boolean = false,
) {
    val colors = TasksTheme.colors
    val description = stringResource(R.string.a11y_date_field, label, valueText.text)
    Column(modifier) {
        FieldLabel(label)
        Row(
            Modifier
                .padding(top = 6.dp)
                .fieldChrome(focused = false, invalid = false, minHeight = 48.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics(mergeDescendants = true) { contentDescription = description }
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(valueText, style = TasksType.body, color = if (isPlaceholder) colors.textSecondary else colors.textPrimary)
            Icon(TasksIcons.Clock, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

/** PlaceField: looks like [DateField] (pin icon); tapping opens the address search (hosted by the sheet). */
@Composable
fun PlaceField(
    label: String,
    valueText: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    isPlaceholder: Boolean = false,
) {
    val colors = TasksTheme.colors
    val description = stringResource(R.string.a11y_date_field, label, valueText)
    Column(modifier) {
        FieldLabel(label)
        Row(
            Modifier
                .padding(top = 6.dp)
                .fieldChrome(focused = false, invalid = false, minHeight = 48.dp)
                .clickable(role = Role.Button, onClick = onClick)
                .semantics(mergeDescendants = true) { contentDescription = description }
                .padding(horizontal = 14.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                valueText,
                style = TasksType.body,
                color = if (isPlaceholder) colors.textSecondary else colors.textPrimary,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = androidx.compose.ui.text.style.TextOverflow.Ellipsis,
            )
            Icon(com.example.tasks.android.ui.map.MapIcons.Pin, contentDescription = null, tint = colors.textPrimary, modifier = Modifier.size(20.dp))
        }
    }
}

/** ErrorBanner (§2): `dangerContainer`, r `m`, alert icon + text; the caller animates visibility. */
@Composable
fun ErrorBanner(text: String, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(TasksShapes.m)
            .background(colors.dangerContainer)
            .padding(horizontal = 14.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.spacedBy(10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(TasksIcons.Alert, contentDescription = null, tint = colors.onDangerContainer, modifier = Modifier.size(20.dp))
        Text(text, style = TasksType.subhead, color = colors.onDangerContainer)
    }
}
