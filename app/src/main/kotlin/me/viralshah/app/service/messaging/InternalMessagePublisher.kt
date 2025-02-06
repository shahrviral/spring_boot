package me.viralshah.app.service.messaging


import com.fasterxml.jackson.databind.ObjectMapper
import me.viralshah.app.configuration.AmazonConfig
import me.viralshah.app.model.messages.InternalMessage
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.util.*


@Component
class InternalMessagePublisher(
    private val amazonConfig: AmazonConfig,
    private val objectMapper: ObjectMapper
) {

    private val sqsClient: SqsClient = SqsClient.builder()
        .endpointOverride(amazonConfig.endpoint)
        .region(Region.US_WEST_2)
        .build()

    companion object {
        private val logger = LoggerFactory.getLogger(InternalMessagePublisher::class.java)
    }

    fun send(internalMessage: InternalMessage, batchId: UUID) {

        val message = objectMapper.writeValueAsString(internalMessage)

        val attributes = mapOf(
            "batchId" to MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(batchId.toString())
                .build(),
            "version" to MessageAttributeValue.builder()
                .dataType("Number")
                .stringValue(internalMessage.version.toString())
                .build(),
            "type" to MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(internalMessage.type.toString())
                .build(),
        )
        val sendMessageRequest = SendMessageRequest.builder()
            .queueUrl(amazonConfig.sample_queue)
            .messageBody(message)
            .messageGroupId("internal_message_${internalMessage.id}")
            .messageDeduplicationId(UUID.randomUUID().toString())
            .messageAttributes(attributes)
            .build()
        try {
            sqsClient.sendMessage(sendMessageRequest)
            logger.trace(
                "Successfully sent message | message={} | SQSQueue={} | batchId={}",
                message, amazonConfig.sample_queue, batchId
            )
        } catch (e: Exception) {
            logger.error(
                "Failed to send message | message={} | SQSQueue={} | error={} | batchId={}",
                message, amazonConfig.sample_queue, e.message, batchId
            )
        }
    }
}
