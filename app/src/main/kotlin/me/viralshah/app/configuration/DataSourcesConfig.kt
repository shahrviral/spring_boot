package me.viralshah.app.configuration

import io.r2dbc.spi.ConnectionFactories
import io.r2dbc.spi.ConnectionFactory
import io.r2dbc.spi.ConnectionFactoryOptions
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Qualifier
import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.boot.context.properties.ConstructorBinding
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Primary
import org.springframework.r2dbc.connection.R2dbcTransactionManager
import org.springframework.r2dbc.core.DatabaseClient
import org.springframework.stereotype.Component
import org.springframework.transaction.ReactiveTransactionManager

@ConstructorBinding
@ConfigurationProperties("datasource")
data class DataSourcesConfig(
    val default: DataSourceConfig,
    val write: DataSourceConfig,
) {
    companion object {
        private val logger = LoggerFactory.getLogger(DataSourceConfig::class.java)
    }

    init {
        logger.info("Default{}", default.toString())
        logger.info("Write{}", write.toString())
    }
}

data class DataSourceConfig(
    val url: String,
    val username: String,
    val password: String,
) {
    override fun toString() = "DataSource(url=$url, username=$username)"
}

@Component
class Launcher(val dataSourcesConfig: DataSourcesConfig) {
    @Bean
    fun writeTransactionManager(@Qualifier("writeFactory") connectionFactory: ConnectionFactory): ReactiveTransactionManager {
        return R2dbcTransactionManager(connectionFactory)
    }

    @Bean
    fun defaultFactory() = ConnectionFactories.get(
        ConnectionFactoryOptions
            .parse(dataSourcesConfig.default.url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, dataSourcesConfig.default.username)
            .option(ConnectionFactoryOptions.PASSWORD, dataSourcesConfig.default.password)
            .build()
    )

    @Bean
    fun writeFactory(): ConnectionFactory = ConnectionFactories.get(
        ConnectionFactoryOptions
            .parse(dataSourcesConfig.write.url)
            .mutate()
            .option(ConnectionFactoryOptions.USER, dataSourcesConfig.write.username)
            .option(ConnectionFactoryOptions.PASSWORD, dataSourcesConfig.write.password)
            .build()
    )

    @Bean
    @Primary
    @Qualifier("readDatabaseClient")
    fun databaseClient(@Qualifier("defaultFactory") connectionFactory: ConnectionFactory) =
        DatabaseClient.create(connectionFactory)

    @Bean
    @Qualifier("writeDatabaseClient")
    fun writeDatabaseClient(@Qualifier("writeFactory") connectionFactory: ConnectionFactory) =
        DatabaseClient.create(connectionFactory)

}