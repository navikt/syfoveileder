import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0-SNAPSHOT"
description = "syfoveileder"

val adal4jVersion = "1.6.4"
val javaxActivationVersion = "1.2.0"
val jaxRiVersion = "2.3.3"
val kotlinJacksonVersion = "2.11.3"
val logstashEncoderVersion = "6.3"
val logbackVersion = "1.2.3"
val nimbusSDKVersion = "7.0.3"
val oidcSupportVersion = "0.2.18"
val prometheusVersion = "1.5.5"
val slf4jVersion = "1.7.30"

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.21"
    id("org.springframework.boot") version "2.3.11.RELEASE"
    id("io.spring.dependency-management") version "1.0.10.RELEASE"
    id("com.diffplug.gradle.spotless") version "3.18.0"
    id("com.github.johnrengelman.shadow") version "6.1.0"
}

allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
    annotation("org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc")
}

repositories {
    mavenCentral()
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("com.sun.xml.ws:jaxws-ri:$jaxRiVersion")
    implementation("com.sun.activation:javax.activation:$javaxActivationVersion")

    implementation("org.springframework.boot:spring-boot-starter-actuator")
    implementation("org.springframework.boot:spring-boot-starter-jersey")
    implementation("org.springframework.boot:spring-boot-starter-jta-atomikos")
    implementation("org.springframework.boot:spring-boot-starter-logging")
    implementation("org.springframework.boot:spring-boot-starter-web")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("no.nav.security:oidc-spring-support:$oidcSupportVersion")
    implementation("com.microsoft.azure:adal4j:$adal4jVersion")
    implementation("com.nimbusds:oauth2-oidc-sdk:$nimbusSDKVersion")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:$kotlinJacksonVersion")

    implementation("io.micrometer:micrometer-registry-prometheus:$prometheusVersion")
    implementation("org.slf4j:slf4j-api:$slf4jVersion")

    testImplementation("no.nav.security:oidc-test-support:$oidcSupportVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test")
}

tasks {
    withType<Jar> {
        manifest.attributes["Main-Class"] = "no.nav.syfo.ApplicationKt"
    }

    create("printVersion") {
        doLast {
            println(project.version)
        }
    }

    test {
        doFirst {
            systemProperty("spring.profiles.active", "test")
        }
    }

    withType<ShadowJar> {
        transform(PropertiesFileTransformer::class.java) {
            paths = listOf("META-INF/spring.factories")
            mergeStrategy = "append"
        }
        mergeServiceFiles()
    }

    named<KotlinCompile>("compileKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }

    named<KotlinCompile>("compileTestKotlin") {
        kotlinOptions.jvmTarget = "1.8"
    }
}

