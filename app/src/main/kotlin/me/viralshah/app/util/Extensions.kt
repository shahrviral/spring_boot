package me.viralshah.app.util

import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import me.viralshah.app.coroutines.MetadataContext
import org.slf4j.Logger
import java.util.*
import java.util.concurrent.TimeUnit
import kotlin.coroutines.coroutineContext

/**
 * Diagnostic Extensions
 */

inline fun <R> measureTimeAndReturnResult(block: () -> R): Pair<R, Long> {
    val start = System.currentTimeMillis()
    val result = block()
    return result to System.currentTimeMillis() - start
}

fun Long.printTimeTaken() = "${TimeUnit.MILLISECONDS.toMinutes(this)} min, ${
    TimeUnit.MILLISECONDS.toSeconds(this) - TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(this))
} sec"


/**
 * Logging Extensions
 */

/**
 * Log object to a log (map) with key
 *
 * @param T object to be logged
 * @param key key in the map
 * @param log log destination (map)
 */
fun <T : Any> T.logIt(key: String, log: MutableMap<String, Any>): T {
    var newKey = key
    if (key in log) {
        newKey = key + UUID.randomUUID().hashCode()
    }

    log[newKey] = this

    return this
}


/**
 * log an element into a map as part of a list
 *
 * if the key already exists in log (map), then check whether the value is a mutable list or not, if not, override it with a mutable list
 *
 * then append the element to the mutable list above
 *
 * example usage:
 * ```
 * val log = mutableMapOf<String, Any>()
 * log.logChild("foo", "bar")
 * log.logChild("foo", "baz")
 * println(log) // {foo=[bar, baz]}
 * ```
 *
 * @param MutableMap<String, Any> logging destination
 * @param key logging key for the mutable list
 * @param childValue element to append to a list
 */
fun MutableMap<String, Any>.logChild(key: String, childValue: Any) {
    if (key !in this || this[key] !is MutableList<*>)
        this[key] = mutableListOf<Any>()

    (this[key] as MutableList<Any>) += childValue
}

/**
 * Debug with MetadataContext
 *
 * allowing appending MetadataContext (if applicable, from coroutineContext) to the end of the log message
 *
 * @param message usual log message
 */
suspend fun Logger.debugMC(message: String) {
    val metadataContext: MetadataContext? = coroutineContext[MetadataContext]
    if (metadataContext == null) {
        debug(message)
    } else {
        debug("$message | $metadataContext")
    }
}

/**
 * Info with MetadataContext
 *
 * allowing appending MetadataContext (if applicable, from coroutineContext) to the end of the log message
 *
 * @param message usual log message
 */
suspend fun Logger.infoMC(message: String) {
    val metadataContext: MetadataContext? = coroutineContext[MetadataContext]
    if (metadataContext == null) {
        info(message)
    } else {
        info("$message | $metadataContext")
    }
}

/**
 * Warn with MetadataContext
 *
 * allowing appending MetadataContext (if applicable, from coroutineContext) to the end of the log message
 *
 * @param message usual log message
 */
suspend fun Logger.warnMC(message: String) {
    val metadataContext: MetadataContext? = coroutineContext[MetadataContext]
    if (metadataContext == null) {
        warn(message)
    } else {
        warn("$message | $metadataContext")
    }
}

/**
 * Error with MetadataContext
 *
 * allowing appending MetadataContext (if applicable, from coroutineContext) to the end of the log message
 *
 * @param message usual log message
 */
suspend fun Logger.errorMC(message: String) {
    val metadataContext: MetadataContext? = coroutineContext[MetadataContext]
    if (metadataContext == null) {
        error(message)
    } else {
        error("$message | $metadataContext")
    }
}

/**
 * List Extensions
 * Returns middle value in an odd numbered list
 * or second of the two middle values in an even numbered list
 * or default if list is empty
 */
fun <T> List<T>.getMiddleValue(default: T)
        where T : Comparable<T>,
              T : Any = sortedBy { it }.getOrElse(size / 2) { default }


/**
 * Map async using coroutines
 *
 * @param T
 * @param R
 * @param concurrencyLimit
 * @param transformation
 * @receiver list of elements
 * @return list of mapped elements
 */
suspend fun <T, R> List<T>.mapAsync(
    concurrencyLimit: Int = Int.MAX_VALUE, transformation: suspend (T) -> R,
): List<R> = coroutineScope {
    val semaphore = Semaphore(concurrencyLimit)

    this@mapAsync.map {
        async {
            semaphore.withPermit {
                transformation(it)
            }
        }
    }.awaitAll()
}