package com.example.tasks.android.ui.map

import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.example.tasks.android.R
import com.example.tasks.shared.domain.Priority
import com.example.tasks.shared.presentation.DayLabel
import kotlinx.datetime.LocalDate
import kotlinx.datetime.toJavaLocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale

// Localised words for the Map screens (the shared module only exposes numbers, enums and times).

@Composable
@ReadOnlyComposable
internal fun currentLocale(): Locale = LocalConfiguration.current.locales[0]

@Composable
internal fun priorityLabel(priority: Priority): String = stringResource(
    when (priority) {
        Priority.HIGH -> R.string.map_priority_high
        Priority.MEDIUM -> R.string.map_priority_medium
        Priority.LOW -> R.string.map_priority_low
    },
)

/** "Wed 4 Nov" / "ср, 4 лист." */
@Composable
internal fun dateText(day: LocalDate): String {
    val locale = currentLocale()
    val pattern = stringResource(R.string.map_date_pattern)
    return DateTimeFormatter.ofPattern(pattern, locale).format(day.toJavaLocalDate())
}

/** "14 min" / "1 h 02 min". */
@Composable
internal fun durationText(minutes: Int): String {
    val (h, m) = MapFormat.hoursAndMinutes(minutes)
    return if (h > 0) stringResource(R.string.map_duration_hm, h, m) else stringResource(R.string.map_duration_m, m)
}

@Composable
internal fun distanceText(meters: Double): String =
    stringResource(R.string.map_distance_km, MapFormat.kilometers(meters, currentLocale()))

@Composable
internal fun stopsText(count: Int): String = pluralStringResource(R.plurals.map_stops, count, count)

@Composable
internal fun summaryText(model: MapScreenModel): String {
    val stops = stopsText(model.stopsCount)
    return when (model.summary) {
        SummaryKind.Ready ->
            if (model.durationMinutes != null && model.distanceMeters != null) {
                stringResource(R.string.map_summary_ready, stops, durationText(model.durationMinutes), distanceText(model.distanceMeters))
            } else {
                stops
            }
        SummaryKind.Loading -> stringResource(R.string.map_summary_loading, stops)
        SummaryKind.Fallback -> stringResource(R.string.map_summary_fallback, stops)
        SummaryKind.Idle -> stops
        SummaryKind.Empty -> stringResource(R.string.map_summary_empty)
    }
}

/** Route sheet heading: "Today's route" / "Tomorrow" / "Yesterday" / "Mon 9 Nov". */
@Composable
internal fun sheetTitle(label: DayLabel, day: LocalDate): String = when (label) {
    DayLabel.TODAY -> stringResource(R.string.map_sheet_title_today)
    DayLabel.TOMORROW -> stringResource(R.string.map_day_tomorrow)
    DayLabel.YESTERDAY -> stringResource(R.string.map_day_yesterday)
    DayLabel.NONE -> dateText(day)
}

@Composable
internal fun legText(leg: LegModel): String = stringResource(R.string.map_leg, durationText(leg.minutes), distanceText(leg.meters))

@Composable
internal fun statusFlags(done: Boolean, overdue: Boolean, past: Boolean): String = buildString {
    if (done) append(stringResource(R.string.map_a11y_flag_done))
    if (overdue) append(stringResource(R.string.map_a11y_flag_overdue))
    if (past && !done && !overdue) append(stringResource(R.string.map_a11y_flag_past))
}

/** TalkBack text of a pin / row: "09:30, Pick up dry cleaning, Low priority, done, stop 1". */
@Composable
internal fun pinDescription(pin: PinModel): String {
    val flags = statusFlags(pin.done, pin.overdue, pin.past)
    val priority = priorityLabel(pin.priority)
    return if (pin.order != null && pin.timeText != null) {
        stringResource(R.string.map_a11y_pin, pin.timeText, pin.title, priority, flags, pin.order)
    } else {
        stringResource(R.string.map_a11y_pin_untimed, pin.title, priority, flags)
    }
}
