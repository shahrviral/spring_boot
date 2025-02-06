package me.viralshah.app.datasource.postgres

import io.r2dbc.spi.Row
import kotlinx.coroutines.flow.toList
import kotlinx.coroutines.withTimeout
import me.viralshah.app.model.entities.SampleTask
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.r2dbc.core.awaitOneOrNull
import org.springframework.r2dbc.core.flow
import org.springframework.stereotype.Component
import java.time.ZonedDateTime
import java.util.*
import kotlin.time.Duration.Companion.minutes

@Component
class TaskRepository(
    @Qualifier("readDatabaseClient") private val readDatabaseClient: DatabaseClient,
    @Qualifier("writeDatabaseClient") private val writeDatabaseClient: DatabaseClient,
) {


    companion object {
        private val logger = LoggerFactory.getLogger(TaskRepository::class.java)
        private val queryTimeout = 5.minutes

        private const val getAllTasks = """
SELECT * FROM public.tasks;
"""

        private const val getSingleTasks = """
SELECT * FROM public.tasks where id = :id;
"""
    }


    suspend fun getTasks(batchId: UUID? = null): List<SampleTask> =
        try {

            withTimeout(queryTimeout) {
                readDatabaseClient.sql(getAllTasks).map(sampleDateRowMapper)
                    .flow().toList()


            }

        } catch (exception: Exception) {
            logger.error("Failed to retrieve tasks | batchId={} | error={}", batchId, exception.message)
            emptyList()
        }

    suspend fun getTask(taskId: Int, batchId: UUID?): SampleTask? =
        try {
            withTimeout(queryTimeout) {
                readDatabaseClient.sql(getSingleTasks)
                    .bind("id", id)
                    .map(sampleDateRowMapper)
                    .awaitOneOrNull()
            }
        } catch (exception: Exception) {
            logger.error("Failed to retrieve task | batchId={} | error={}", batchId, exception.message)
            null
        }


    val sampleDateRowMapper = fun(row: Row) = SampleTask(
        id = row.get("id", Int::class.java)!!,
        name = row.get("name", String::class.java)!!,
        description = row.get("description", String::class.java)!!,
        create_time = row.get("created", ZonedDateTime::class.java)!!,
        update_time = row.get("updated", ZonedDateTime::class.java)!!,
    )


}