package me.viralshah.app.service.messaging

import com.fasterxml.jackson.databind.ObjectMapper
import me.viralshah.app.configuration.AmazonConfig
import me.viralshah.app.model.messages.SampleMessage
import me.viralshah.app.model.messages.SampleMessageA
import me.viralshah.app.model.messages.SampleMessageB
import me.viralshah.app.model.messages.SampleMessageC

import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import software.amazon.awssdk.regions.Region
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import java.util.*

@Component
class SampleMessagePublisherImpl(
    private val amazonConfig: AmazonConfig,
    private val objectMapper: ObjectMapper,
) : SampleMessagePublisher {

    companion object {
        private val logger = LoggerFactory.getLogger(SampleMessagePublisherImpl::class.java)
    }

    val sqsClient: SqsClient = SqsClient.builder()
        .endpointOverride(amazonConfig.endpoint)
        .region(Region.US_WEST_2)
        .build()


    override fun sendMessage(sampleMessage: SampleMessage, transactionId: UUID, batchId: UUID?) {
        when (sampleMessage) {
            is SampleMessageA -> send(
                sampleMessage.type, "sample_message_a",
                sampleMessage, amazonConfig.sample_queue, transactionId, batchId
            )

            is SampleMessageB -> TODO()
            is SampleMessageC -> TODO()
        }
    }


    internal fun send(
        type: String,
        groupId: String,
        payload: Any,
        queueUrl: String,
        transactionId: UUID,
        batchId: UUID?,
    ) {

        val attributes = mapOf(
            "transactionId" to MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(transactionId.toString())
                .build(),
            "batchId" to MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(batchId.toString())
                .build(),
            "type" to MessageAttributeValue.builder()
                .dataType("String")
                .stringValue(type)
                .build()
        )

        val message = objectMapper.writeValueAsString(payload)

        val sendMessageRequest = SendMessageRequest.builder()
            .queueUrl(queueUrl)
            .messageBody(message)
            .messageGroupId(groupId)
            .messageDeduplicationId(message.hashCode().toString())
            .messageAttributes(attributes)
            .build()

        try {

            sqsClient.sendMessage(sendMessageRequest)

            logger.debug(
                "Successfully sent {} message to SQS Queue | message={} | queue={} | transactionId={} | batchId={}",
                type, message, queueUrl, transactionId, batchId
            )
        } catch (e: Exception) {
            logger.error(
                "Failed to send message to SQS queue | message={} | queue={} | error={} | transactionId={} | batchId={}",
                message, queueUrl, e.message, transactionId, batchId
            )
        }
    }
}
