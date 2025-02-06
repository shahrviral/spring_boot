package me.viralshah.app.coroutines

import java.util.*
import kotlin.coroutines.CoroutineContext

data class MetadataContext(val transactionId: UUID, val batchId: UUID?, val forceRun: Boolean = false) :
    CoroutineContext.Element {
    override val key: CoroutineContext.Key<*> = Key

    companion object Key : CoroutineContext.Key<MetadataContext>
}