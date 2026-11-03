package com.example.tasks.android.ui.stats

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.example.tasks.android.R
import com.example.tasks.android.ui.components.CompletionRing
import com.example.tasks.android.ui.components.EmptyState
import com.example.tasks.android.ui.components.EmptyStateKind
import com.example.tasks.android.ui.components.HeroCard
import com.example.tasks.android.ui.components.PriorityBar
import com.example.tasks.android.ui.components.SecondaryButton
import com.example.tasks.android.ui.components.SkeletonBlock
import com.example.tasks.android.ui.components.StatTile
import com.example.tasks.android.ui.components.StatTileVariant
import com.example.tasks.android.ui.components.TasksIcons
import com.example.tasks.android.ui.components.animatedInt
import com.example.tasks.android.ui.components.priorityBar
import com.example.tasks.android.ui.components.rememberTargetAnimation
import com.example.tasks.android.ui.theme.Motion
import com.example.tasks.android.ui.theme.TasksShapes
import com.example.tasks.android.ui.theme.TasksTheme
import com.example.tasks.android.ui.theme.TasksType
import com.example.tasks.shared.domain.Priority

/** Stateless Stats tab (§3.4): layout only. */
@Composable
fun StatsScreen(model: StatsUiModel, onRetry: () -> Unit) {
    val colors = TasksTheme.colors
    Column(Modifier.fillMaxSize().background(colors.bg).statusBarsPadding()) {
        Text(
            stringResource(R.string.stats_title),
            style = TasksType.largeTitle,
            color = colors.textPrimary,
            modifier = Modifier
                .padding(start = 16.dp, end = 16.dp, top = 2.dp, bottom = 12.dp)
                .semantics { heading() },
        )
        when (model) {
            StatsUiModel.Loading -> StatsSkeleton()
            StatsUiModel.Error -> EmptyState(
                title = stringResource(R.string.error_title),
                body = stringResource(R.string.error_body),
                kind = EmptyStateKind.Error,
                action = { SecondaryButton(stringResource(R.string.retry), onRetry, icon = TasksIcons.Refresh) },
            )
            is StatsUiModel.Loaded -> StatsContent(model)
        }
    }
}

@Composable
private fun StatsSkeleton() {
    Column(Modifier.padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        SkeletonBlock(Modifier.fillMaxWidth().height(152.dp), TasksShapes.xl)
        repeat(2) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                SkeletonBlock(Modifier.weight(1f).height(96.dp), TasksShapes.l)
                SkeletonBlock(Modifier.weight(1f).height(96.dp), TasksShapes.l)
            }
        }
    }
}

@Composable
private fun StatsContent(model: StatsUiModel.Loaded) {
    val colors = TasksTheme.colors
    Column(
        Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(start = 16.dp, end = 16.dp, bottom = 16.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        CompletionHero(model)

        // S3: tile numbers count 0 -> value in 600 ms.
        val total = tileNumber(model.total)
        val done = tileNumber(model.done)
        val active = tileNumber(model.active)
        val overdue = tileNumber(model.overdue)
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(stringResource(R.string.total), total, StatTileVariant.Default, Modifier.weight(1f).fillMaxSize())
            StatTile(stringResource(R.string.done), done, StatTileVariant.Done, Modifier.weight(1f).fillMaxSize())
        }
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatTile(stringResource(R.string.active), active, StatTileVariant.Default, Modifier.weight(1f).fillMaxSize())
            StatTile(stringResource(R.string.overdue), overdue, StatTileVariant.Overdue, Modifier.weight(1f).fillMaxSize())
        }

        Column(
            Modifier.fillMaxWidth().clip(TasksShapes.l).background(colors.surface).padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp),
        ) {
            Text(stringResource(R.string.by_priority).uppercase(), style = TasksType.overline, color = colors.textSecondary)
            model.priorityRows.forEachIndexed { index, row ->
                // S4: fills grow 0 -> fraction, 600 ms, delayed 150 ms + index x 80 ms (High, Medium, Low).
                val fraction = rememberTargetAnimation(row.fraction, Motion.long, Motion.stagger * index + 150)
                PriorityBar(
                    label = row.priority.displayName,
                    countText = pluralStringResource(R.plurals.n_tasks, row.count, row.count),
                    fraction = fraction,
                    fill = colors.priorityBar(row.priority),
                )
            }
        }
    }
}

@Composable
private fun tileNumber(value: Int): Int {
    val animated = rememberTargetAnimation(value.toFloat(), Motion.long)
    return animatedInt(animated, value.toFloat(), value)
}

/**
 * Completion HeroCard. The ring (S1) and the percent text (S2) are driven by ONE animated value, so the arc and
 * the number can never drift apart; the last frame shows `completionPercent` exactly. Accessibility reads the
 * final value only, as one element.
 */
@Composable
private fun CompletionHero(model: StatsUiModel.Loaded) {
    val colors = TasksTheme.colors
    val target = model.completionPercent / 100f
    val progress = rememberTargetAnimation(target, Motion.extraLong)
    val percent = animatedInt(progress, target, model.completionPercent, scale = 100f)
    val label = stringResource(R.string.completion)
    val finalValue = stringResource(R.string.a11y_completion_value, model.completionPercent, model.done, model.total)

    HeroCard(padding = 24.dp) {
        Row(
            Modifier
                .fillMaxWidth()
                .clearAndSetSemantics {
                    contentDescription = label
                    stateDescription = finalValue
                },
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(label.uppercase(), style = TasksType.overline, color = colors.heroTextSecondary)
                Text("$percent%", style = TasksType.display, color = colors.accent)
                Text(
                    stringResource(R.string.n_of_m_done, model.done, model.total),
                    style = TasksType.subhead15,
                    color = colors.heroTextSecondary,
                )
            }
            CompletionRing(progress)
        }
    }
}

// region Previews

private val previewLoaded = StatsUiModel.Loaded(
    completionPercent = 33, total = 6, done = 2, active = 4, overdue = 1,
    priorityRows = listOf(
        PriorityRowUi(Priority.HIGH, 2, 2f / 6), PriorityRowUi(Priority.MEDIUM, 2, 2f / 6), PriorityRowUi(Priority.LOW, 2, 2f / 6),
    ),
)

@Composable
private fun PreviewScreen(model: StatsUiModel, dark: Boolean) {
    TasksTheme(darkTheme = dark) { StatsScreen(model, onRetry = {}) }
}

@Preview(name = "Stats light", showBackground = true, heightDp = 800)
@Composable
private fun StatsLightPreview() = PreviewScreen(previewLoaded, dark = false)

@Preview(name = "Stats dark", showBackground = true, heightDp = 800)
@Composable
private fun StatsDarkPreview() = PreviewScreen(previewLoaded, dark = true)

@Preview(name = "Stats zero light", showBackground = true, heightDp = 800)
@Composable
private fun StatsZeroLightPreview() = PreviewScreen(
    StatsUiModel.Loaded(0, 0, 0, 0, 0, Priority.entries.reversed().map { PriorityRowUi(it, 0, 0f) }), dark = false,
)

@Preview(name = "Stats loading light", showBackground = true, heightDp = 800)
@Composable
private fun StatsLoadingLightPreview() = PreviewScreen(StatsUiModel.Loading, dark = false)

@Preview(name = "Stats error dark", showBackground = true, heightDp = 800)
@Composable
private fun StatsErrorDarkPreview() = PreviewScreen(StatsUiModel.Error, dark = true)

// endregion
