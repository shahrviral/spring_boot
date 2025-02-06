import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.springframework.boot.gradle.tasks.bundling.BootJar

plugins {
    kotlin("jvm") version "1.7.22"
    kotlin("plugin.spring") version "1.7.22"

    java

    id("com.gorylenko.gradle-git-properties") version "2.4.1"

    id("org.springframework.boot") version "2.7.6"

    id("io.spring.dependency-management") version "1.1.0"

    id("com.github.ben-manes.versions") version "0.44.0"

}

group = "me.viralshah"
java.sourceCompatibility = JavaVersion.VERSION_11

repositories {
    maven { url = uri("https://repo.spring.io/milestone") }
    mavenCentral()
}

val kotlinCoroutinesVersion = "1.6.4"
val mockkVersion = "1.13.3"
val assertJVersion = "3.23.1"
val junitBomVersion = "5.9.1"
val awsBomVersion = "2.18.26"


dependencies {
    implementation(kotlin("stdlib-jdk8"))
    implementation(kotlin("reflect"))
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core-jvm:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-jdk8:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-slf4j:$kotlinCoroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor")
    implementation("io.projectreactor.kotlin:reactor-kotlin-extensions")

    implementation("org.springframework.boot:spring-boot-starter-cache")
    implementation("com.github.ben-manes.caffeine:caffeine:3.1.8")

    implementation("org.springframework.boot:spring-boot-starter-webflux")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("io.micrometer:micrometer-registry-prometheus")

    developmentOnly("org.springframework.boot:spring-boot-devtools")

    implementation("org.springframework.boot:spring-boot-starter-data-r2dbc")
    implementation("io.r2dbc:r2dbc-pool")

    implementation("org.postgresql:r2dbc-postgresql")
    implementation("org.postgresql:postgresql")

    implementation("ch.qos.logback:logback-classic")


    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

    implementation(platform("software.amazon.awssdk:bom:$awsBomVersion"))

    implementation("software.amazon.awssdk:sqs")

    implementation("org.springdoc:springdoc-openapi-ui:1.8.0")
    implementation("org.springdoc:springdoc-openapi-webflux-ui:1.8.0")
    implementation("org.springdoc:springdoc-openapi-webmvc-core:1.8.0")
    implementation("org.springdoc:springdoc-openapi-kotlin:1.8.0")

    implementation("org.springframework.boot:spring-boot-starter-validation")


    annotationProcessor("org.springframework.boot:spring-boot-configuration-processor")



    testImplementation("io.projectreactor:reactor-test")

    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:$kotlinCoroutinesVersion")
    testImplementation(platform("org.junit:junit-bom:$junitBomVersion"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testImplementation("io.mockk:mockk:$mockkVersion")
    testImplementation("org.assertj:assertj-core:$assertJVersion")
}


springBoot {
    buildInfo {
        properties {
            name = "app"
        }
    }
    mainClass.set("me.viralshah.app.ApplicationKt")
}

gitProperties {
    dateFormat = "EEEE, MMMM dd, YYYY 'at' h:mm:ss a z"
    dateFormatTimeZone = "GMT-07:00"
    keys = listOf(
        "git.branch",
        "git.commit.id",
        "git.commit.message.full",
        "git.commit.time"
    )
}




tasks {
    withType<KotlinCompile> {
        kotlinOptions {
            freeCompilerArgs = listOf("-Xjsr305=strict")
            jvmTarget = "11"
        }
    }

    register<Zip>("fatZip") {
        from("src/main/resources", "build/libs")
        include("*")
        exclude("application*local.yml", "application-pass.yml")
        into("/")
        archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
        dependsOn("jar")
        dependsOn("bootJar")
    }

    "assemble" {
        dependsOn("fatZip")
    }

    getByName<BootJar>("bootJar") {
        launchScript()
        archiveFileName.set("${archiveBaseName.get()}.${archiveExtension.get()}")
    }

    test {
        useJUnitPlatform()
        testLogging {
            events("passed", "skipped", "failed")
        }
    }

}
