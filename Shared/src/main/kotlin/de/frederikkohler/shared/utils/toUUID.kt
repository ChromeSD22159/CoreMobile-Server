package de.frederikkohler.shared.utils

import java.util.UUID

fun String.toUUID(): UUID {
    return UUID.fromString(this)
}