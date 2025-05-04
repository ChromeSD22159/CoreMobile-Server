package de.coreMobile.server.libs

import io.ktor.util.logging.KtorSimpleLogger

class Logging(private val name: String) {
    fun error(msg: String) {
        KtorSimpleLogger(name).error(msg)
    }
    fun info(msg: String) {
        KtorSimpleLogger(name).info(msg)
    }
    fun debug(msg: String) {
        KtorSimpleLogger(name).debug(msg)
    }
}