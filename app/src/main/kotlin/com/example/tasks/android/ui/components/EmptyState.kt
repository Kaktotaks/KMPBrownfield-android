package com.example.tasks.android.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType

enum class EmptyStateKind { Normal, Error }

/**
 * EmptyState (§2): 96 dp illustration (outlined card + lime disc + check, drawn with tokens),
 * `title`, `body`, optional action button. [EmptyStateKind.Error] swaps the lime check for a danger disc.
 */
@Composable
fun EmptyState(
    title: String,
    body: String,
    modifier: Modifier = Modifier,
    kind: EmptyStateKind = EmptyStateKind.Normal,
    action: (@Composable () -> Unit)? = null,
) {
    val colors = TasksTheme.colors
    Column(
        modifier = modifier.fillMaxSize().padding(horizontal = 32.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp, Alignment.CenterVertically),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        EmptyIllustration(kind)
        Text(title, style = TasksType.title, color = colors.textPrimary, textAlign = TextAlign.Center, modifier = Modifier.semantics { heading() })
        Text(body, style = TasksType.body, color = colors.textSecondary, textAlign = TextAlign.Center)
        if (action != null) {
            Column(Modifier.padding(top = 12.dp)) { action() }
        }
    }
}

/** Same motif as the app icon: outlined card, a disc at its top-right, a check (or "!") in the disc. */
@Composable
private fun EmptyIllustration(kind: EmptyStateKind) {
    val colors = TasksTheme.colors
    Canvas(Modifier.size(96.dp)) {
        val u = size.width / 96f
        val strokeWidth = 4f * u
        drawRoundRect(
            color = colors.textPrimary,
            topLeft = Offset(10f * u, 26f * u),
            size = Size(58f * u, 58f * u),
            cornerRadius = CornerRadius(16f * u),
            style = Stroke(strokeWidth),
        )
        val discCenter = Offset(64f * u, 34f * u)
        val discColor = if (kind == EmptyStateKind.Error) colors.dangerContainer else colors.accent
        drawCircle(discColor, radius = 26f * u, center = discCenter)
        val glyph = if (kind == EmptyStateKind.Error) colors.danger else colors.onAccent
        val path = Path().apply {
            if (kind == EmptyStateKind.Error) {
                moveTo(discCenter.x, discCenter.y - 11f * u); lineTo(discCenter.x, discCenter.y + 2f * u)
                moveTo(discCenter.x, discCenter.y + 10f * u); lineTo(discCenter.x + 0.01f, discCenter.y + 10f * u)
            } else {
                moveTo(discCenter.x - 11f * u, discCenter.y + 1f * u)
                lineTo(discCenter.x - 3f * u, discCenter.y + 9f * u)
                lineTo(discCenter.x + 11f * u, discCenter.y - 7f * u)
            }
        }
        drawPath(path, glyph, style = Stroke(5f * u, cap = StrokeCap.Round, join = StrokeJoin.Round))
    }
}
