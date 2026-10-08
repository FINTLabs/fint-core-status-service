package no.novari.status

import java.time.Instant
import java.time.OffsetDateTime
import java.time.ZoneOffset

fun Instant.toOffset(): OffsetDateTime = atOffset(ZoneOffset.UTC)
