package me.viralshah.app.util

sealed class Outcome<out T : Any> {
    data class Error(val message: String, val cause: Exception? = null) : Outcome<Nothing>()

    data class Success<out T : Any>(val value: T) : Outcome<T>()
}

