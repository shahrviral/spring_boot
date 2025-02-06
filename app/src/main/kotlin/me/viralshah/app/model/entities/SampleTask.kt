package me.viralshah.app.model.entities

import java.time.ZonedDateTime

data class SampleTask(
    val id: Int,
    val name: String,
    val description: String,
    val create_time: ZonedDateTime,
    val update_time: ZonedDateTime
)
