package me.viralshah.app.configuration

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.core.JsonGenerator
import com.fasterxml.jackson.databind.DeserializationFeature
import com.fasterxml.jackson.databind.JsonSerializer
import com.fasterxml.jackson.databind.SerializationFeature
import com.fasterxml.jackson.databind.SerializerProvider
import com.fasterxml.jackson.databind.module.SimpleModule
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule
import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import java.math.BigDecimal
import java.text.SimpleDateFormat
import java.time.ZonedDateTime

class ZonedDateTimeSerializer : JsonSerializer<ZonedDateTime>() {
    override fun serialize(value: ZonedDateTime?, gen: JsonGenerator?, serializers: SerializerProvider?) {
        gen?.writeObject(value?.toInstant())
    }

}

@Configuration
class ObjectMapperConfig {

    @Bean
    fun objectMapper() = jacksonObjectMapper().apply {
        disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
        registerModule(JavaTimeModule())
        dateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
        val simpleModule = SimpleModule().apply {
            addSerializer(ZonedDateTime::class.java, ZonedDateTimeSerializer())
        }
        registerModule(simpleModule)
        configure(DeserializationFeature.ADJUST_DATES_TO_CONTEXT_TIME_ZONE, false)
        configure(JsonGenerator.Feature.WRITE_BIGDECIMAL_AS_PLAIN, true)
        configOverride(BigDecimal::class.java).format = JsonFormat.Value.forShape(JsonFormat.Shape.STRING)
    }
}