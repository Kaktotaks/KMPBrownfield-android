package com.example.tasks.android.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

/** Shared look of the three 52 dp buttons (§2): h 52, r `m`, `button` type, optional 20 dp leading icon. */
@Composable
private fun TasksButton(
    text: String,
    onClick: () -> Unit,
    container: Color,
    content: Color,
    modifier: Modifier = Modifier,
    icon: ImageVector? = null,
    enabled: Boolean = true,
) {
    val colors = TasksTheme.colors
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressScale(source)
            .heightIn(min = 52.dp)
            .clip(TasksShapes.m)
            .background(if (enabled) container else colors.surface2)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        val tint = if (enabled) content else colors.textTertiary
        if (icon != null) Icon(icon, contentDescription = null, tint = tint, modifier = Modifier.size(20.dp))
        Text(text, style = TasksType.button, color = tint)
    }
}

@Composable
fun PrimaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    val c = TasksTheme.colors
    TasksButton(text, onClick, c.accent, c.onAccent, modifier, icon, enabled)
}

@Composable
fun SecondaryButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    val c = TasksTheme.colors
    TasksButton(text, onClick, c.inverse, c.onInverse, modifier, icon, enabled)
}

@Composable
fun DestructiveButton(text: String, onClick: () -> Unit, modifier: Modifier = Modifier, icon: ImageVector? = null, enabled: Boolean = true) {
    val c = TasksTheme.colors
    TasksButton(text, onClick, c.dangerContainer, c.onDangerContainer, modifier, icon, enabled)
}

/** SavePill (§2): h 36, pill, 0 x 18 padding; disabled = `surface2` / `textTertiary`. Success haptic on tap. */
@Composable
fun SavePill(text: String, onClick: () -> Unit, enabled: Boolean, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val haptic = LocalHapticFeedback.current
    val source = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .pressScale(source)
            .heightIn(min = 36.dp)
            .clip(TasksShapes.pill)
            .background(if (enabled) colors.accent else colors.surface2)
            .clickable(interactionSource = source, indication = null, enabled = enabled, role = Role.Button) {
                haptic.performHapticFeedback(HapticFeedbackType.Confirm)
                onClick()
            }
            .padding(horizontal = 18.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(text, style = TasksType.savePill, color = if (enabled) colors.onAccent else colors.textTertiary)
    }
}

/** FloatingAddButton (§2): 56 x 56, r `m`, `accent`; the only add affordance. */
@Composable
fun FloatingAddButton(onClick: () -> Unit, contentDescription: String, modifier: Modifier = Modifier) {
    val colors = TasksTheme.colors
    val source = remember { MutableInteractionSource() }
    FloatingActionButton(
        onClick = onClick,
        modifier = modifier.size(56.dp).pressScale(source),
        shape = TasksShapes.m,
        containerColor = colors.accent,
        contentColor = colors.onAccent,
        elevation = FloatingActionButtonDefaults.elevation(6.dp),
        interactionSource = source,
    ) {
        Icon(TasksIcons.PlusBold, contentDescription = contentDescription, modifier = Modifier.size(26.dp))
    }
}
