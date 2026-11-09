package com.example.tasks.android

import com.example.tasks.shared.domain.DateProvider
import com.example.tasks.shared.domain.SystemDateProvider
import com.example.tasks.shared.domain.SystemTimeProvider
import com.example.tasks.shared.domain.TimeProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime

/**
 * The one clock of the app: the repository, validators and the Map view model all read "now" from here.
 * It follows the system clock; only the debug source set ever sets [frozen] (screenshot hook, see `MapDebug`).
 */
class AppClock : TimeProvider, DateProvider {
    @Volatile
    var frozen: LocalDateTime? = null

    override fun now(): LocalDateTime = frozen ?: SystemTimeProvider.now()

    override fun today(): LocalDate = frozen?.date ?: SystemDateProvider.today()
}
