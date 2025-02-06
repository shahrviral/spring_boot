package me.viralshah.app.configuration

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import java.net.URI

@ConstructorBinding
@ConfigurationProperties(prefix = "amazon")
data class AmazonConfig(


    val sample_queue: String,
    val waitTime: Int,
    val endpoint: URI,
    val pollingConcurrencyLimit: Int,
    val processingConcurrencyLimit: Int,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(me.viralshah.app.configuration.AmazonConfig::class.java)
    }

    init {
        me.viralshah.app.configuration.AmazonConfig.Companion.logger.info(this.toString())
    }
}
