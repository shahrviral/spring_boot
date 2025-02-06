package me.viralshah.app.service.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import kotlinx.coroutines.*
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.future.await
import me.viralshah.app.configuration.AmazonConfig
import me.viralshah.app.model.messages.InternalMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.model.Message
import software.amazon.awssdk.services.sqs.model.ReceiveMessageRequest
import java.util.*
import javax.annotation.PostConstruct
import javax.annotation.PreDestroy

@Component
class InternalMessageSubscriber(
    private val amazonConfig: AmazonConfig,
    private val objectMapper: ObjectMapper,

    ) {
    companion object {
        private val logger = LoggerFactory.getLogger(InternalMessageSubscriber::class.java)
        private val internalMessageSubscriberScope = CoroutineScope(Dispatchers.Default + SupervisorJob())
    }

    private val sqsClient = SqsAsyncClient.builder()
        .endpointOverride(amazonConfig.endpoint)
        .region(Region.US_WEST_2)
        .build()


    @PostConstruct
    fun start(): Job {
        logger.info("Starting Internal Message Subscriber")
        return internalMessageSubscriberScope.launch {
            supervisorScope {
                val channel: Channel<Message> = Channel()

                repeat(amazonConfig.pollingConcurrencyLimit) {
                    launch {
                        while (true) {
                            try {
                                coroutineScope {//For explicit error handling
                                    val messages: List<Message> = receiveMessages()
                                    messages.forEach { channel.send(it) }
                                }
                            } catch (cancellationException: CancellationException) {
                                logger.info("Coroutine for receiving sqs messages has been cancelled")
                                throw cancellationException
                            } catch (ex: Exception) {
                                logger.error("Exception occurred when receiving messages from sqs. Message: ${ex.message}; StackTrace: ${ex.stackTraceToString()}")
                                ex.printStackTrace()
                            }
                        }
                    }
                }

                repeat(amazonConfig.processingConcurrencyLimit) {
                    launch {
                        for (message in channel) {
                            try {
                                coroutineScope {//For explicit error handling
                                    launchWorker(message)
                                }
                            } catch (cancellationException: CancellationException) {
                                logger.info("Coroutine for processing messages has been cancelled")
                                throw cancellationException
                            } catch (ex: Exception) {
                                logger.error("Exception occurred when processing message ${message.body()}. Exception message: ${ex.message}; StackTrace: ${ex.stackTraceToString()}")
                                ex.printStackTrace()
                            }
                        }
                    }
                }
            }
        }
    }

    @PreDestroy
    fun stop() {
        internalMessageSubscriberScope.cancel()
    }

    private suspend fun receiveMessages(): List<Message> = withContext(Dispatchers.IO) {
        val receiveRequest = ReceiveMessageRequest.builder()
            .queueUrl(amazonConfig.sample_queue)
            .messageAttributeNames("batchId", "version", "type")
            .maxNumberOfMessages(10)
            .waitTimeSeconds(amazonConfig.waitTime)
            .build()
        sqsClient.receiveMessage(receiveRequest).await().messages()
    }

    private suspend fun launchWorker(message: Message) =
        try {

            val (internalMessage, batchId) = parseMessage(message)
            withContext(Dispatchers.IO) {
                deleteMessage(message)
            }
            // do something with the message
        } catch (cancellationException: CancellationException) {
            logger.info("Coroutine for worker process has been cancelled")
            throw cancellationException
        } catch (ex: Exception) {
            logger.error(
                "Exception trying to process message | message={} | error={} | stackTrace={}",
                message.body(), ex.message, ex.stackTraceToString()
            )
        }

    private fun parseMessage(message: Message): Pair<InternalMessage, UUID> {
        val batchId = UUID.fromString(message.messageAttributes()["batchId"]!!.stringValue())
        val internalMessage = objectMapper.readValue(message.body(), InternalMessage::class.java)
        logger.debug(
            "Received Interal Message | message={} | batchId={}",
            internalMessage,
            batchId
        )
        return Pair(internalMessage, batchId)
    }

    private suspend fun deleteMessage(message: Message) {
        sqsClient.deleteMessage { req ->
            req.queueUrl(amazonConfig.sample_queue)
            req.receiptHandle(message.receiptHandle())
        }.await()
    }
}