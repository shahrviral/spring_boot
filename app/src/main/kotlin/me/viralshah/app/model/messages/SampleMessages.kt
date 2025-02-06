package me.viralshah.app.model.messages

import java.time.Instant


data class InternalMessage(
    val id: Int,
    val name: String,
    val timestamp: Instant = Instant.now(),
    val type: String = "internalMessage",
    val version: Int = 1,
)

sealed interface SampleMessage

data class SampleMessageA(
    val id: Int,
    val name: String,
    val timestamp: Instant = Instant.now(),
    val type: String = "sampleMessageA",
) : SampleMessage

data class SampleMessageB(
    val id: Int,
    val name: String,
    val timestamp: Instant = Instant.now(),
    val type: String = "sampleMessageB",
) : SampleMessage


data class SampleMessageC(
    val id: Int,
    val name: String,
    val timestamp: Instant = Instant.now(),
    val type: String = "sampleMessageC",
) : SampleMessage

