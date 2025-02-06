package me.viralshah.app.configuration

import org.slf4j.LoggerFactory
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding

@ConstructorBinding
@ConfigurationProperties(prefix = "app")
data class AppConfig(
    val environment: Environment,
    val featureName: String,
) {

    companion object {
        private val logger = LoggerFactory.getLogger(AppConfig::class.java)
    }

    init {
        logger.info(this.toString())
    }

    enum class Environment {
        PROD,
        QA,
        BURNIN,
        LOCAL
    }
}
