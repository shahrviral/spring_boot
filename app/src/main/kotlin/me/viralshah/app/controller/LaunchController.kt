package me.viralshah.app.controller


import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.Parameter
import kotlinx.coroutines.*
import me.viralshah.app.configuration.AppConfig
import me.viralshah.app.datasource.postgres.TaskRepository
import me.viralshah.app.model.messages.InternalMessage
import me.viralshah.app.service.SampleService
import me.viralshah.app.service.messaging.InternalMessagePublisher
import me.viralshah.app.util.measureTimeAndReturnResult
import me.viralshah.app.util.printTimeTaken
import org.slf4j.LoggerFactory
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*
import javax.annotation.PreDestroy

@RestController
@RequestMapping("/launch")
class LaunchController(
    private val appConfig: AppConfig,
    private val sampleService: SampleService,
    private val taskRepository: TaskRepository,
    private val internalMessagePublisher: InternalMessagePublisher
) {

    companion object {
        private val logger = LoggerFactory.getLogger(LaunchController::class.java)
        private val launchControllerScope: CoroutineScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    @PreDestroy
    fun destroy() = launchControllerScope.cancel()

    @GetMapping("/tasks")
    @Operation(
        summary = "Launch tasks",
        description = "Note: this will trigger all tasks, use with caution"
    )
    suspend fun launchTasks(): ResponseEntity<String> {
        val batchId = UUID.randomUUID()
        logger.info("Begin Scheduled Task Run | batchId={}", batchId)

        launchControllerScope.launch {

            logger.info("Fetching Tasks | batchId={}", batchId)
            val tasks = measureTimeAndReturnResult {
                taskRepository.getTasks(batchId)

            }.let {
                logger.info(
                    "Fetched Tasks | count={} | timeTaken={} | batchId={}",
                    it.first.size, it.second.printTimeTaken(), batchId
                )
                it.first
            }

            tasks
                .forEach {
                    internalMessagePublisher.send(InternalMessage(id = it.id, name = it.name), batchId)
                }
            logger.info("Finished sending internal messages | batchId={}", batchId)

        }

        return ResponseEntity.ok("Triggered Tasks, running batch: $batchId")
    }


    @GetMapping("/tasks/{taskId}")
    @Operation(summary = "Launch Single Task")
    suspend fun launchTask(
        @Parameter(
            description = "Task Id",
            required = true
        )
        @PathVariable taskId: Int,
    ): ResponseEntity<String> {

        logger.info("Begin Single Task {}", taskId)

        measureTimeAndReturnResult {
            taskRepository.getTask(taskId, null)
        }.let {
            logger.info("Fetched Task | taskId={} | timeTaken={}", taskId, it.second.printTimeTaken())

            it.first?.let {
                sampleService.runTask(it)
                return ResponseEntity.ok("Triggered $taskId")
            } ?: return ResponseEntity.notFound().build()

        }


    }

}