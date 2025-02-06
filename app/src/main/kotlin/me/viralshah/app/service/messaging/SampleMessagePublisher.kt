package me.viralshah.app.service.messaging

import me.viralshah.app.model.messages.SampleMessage
import java.util.*


interface SampleMessagePublisher {
    fun sendMessage(sampleMessage: SampleMessage, transactionId: UUID, batchId: UUID?)
}