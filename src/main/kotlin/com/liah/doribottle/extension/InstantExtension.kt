package com.liah.doribottle.extension

import java.time.Instant
import java.time.temporal.ChronoUnit.DAYS
import java.time.temporal.ChronoUnit.HOURS

fun Instant.truncateToKstDay(): Instant {
    return this.truncatedTo(DAYS).minus(9, HOURS)
}

fun Instant.diffDaysTo(to: Instant): Long {
    return DAYS.between(this, to)
}