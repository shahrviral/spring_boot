package me.viralshah.app.service;

import me.viralshah.app.configuration.AppConfig
import me.viralshah.app.datasource.postgres.TaskRepository
import me.viralshah.app.model.entities.SampleTask
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component;

@Component
class SampleService(
    private val appConfig: AppConfig,
    private val taskRepository: TaskRepository,
    private val sampleService: SampleService,

    ) {

    companion object {
        private val logger = LoggerFactory.getLogger(SampleService::class.java)
    }

    suspend fun runTask(task: SampleTask) {
        logger.info("Running task | taskId=${task.id}")

    }


}
