import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0-SNAPSHOT"
description = "syfoveileder"

val springBootVersion = "2.1.8.RELEASE"
val oidcSupportVersion = "0.2.18"
val cxfVersion = "3.3.7"
val logstashEncoderVersion = "5.1"
val logbackVersion = "1.2.3"

tasks.withType<Jar> {
    manifest.attributes["Main-Class"] = "no.nav.syfo.ApplicationKt"
}

plugins {
    kotlin("jvm") version "1.4.21"
    id("org.jetbrains.kotlin.plugin.allopen") version "1.4.21"
    id("com.diffplug.gradle.spotless") version "3.18.0"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

buildscript {
    dependencies {
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
        classpath("org.jetbrains.kotlin:kotlin-allopen:1.3.60")
    }
}


allOpen {
    annotation("org.springframework.context.annotation.Configuration")
    annotation("org.springframework.stereotype.Service")
    annotation("org.springframework.stereotype.Component")
    annotation("org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc")
}

repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://dl.bintray.com/spekframework/spek-dev")
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
}

dependencies {
    implementation(kotlin("stdlib"))
    implementation(kotlin("reflect"))

    implementation("org.apache.cxf:cxf-rt-features-logging:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-security:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-ws-policy:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-transports-http:$cxfVersion")
    implementation("org.apache.cxf:cxf-rt-frontend-jaxws:$cxfVersion")

    implementation("ch.qos.logback:logback-classic:$logbackVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:$logstashEncoderVersion")

    implementation("no.nav.security:oidc-spring-support:$oidcSupportVersion")
    implementation("com.microsoft.azure:adal4j:1.6.4")
    implementation("com.nimbusds:oauth2-oidc-sdk:7.0.3")
    implementation("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")

    implementation("javax.inject:javax.inject:1")
    implementation("org.springframework.boot:spring-boot-starter-jersey:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-web:$springBootVersion")
    implementation("org.springframework.boot:spring-boot-starter-actuator:$springBootVersion")
    implementation("org.bitbucket.b_c:jose4j:0.5.0")
    implementation("io.micrometer:micrometer-registry-prometheus:1.0.7")
    implementation("org.slf4j:slf4j-api:1.7.25")
    implementation("org.springframework.boot:spring-boot-starter-logging:$springBootVersion")
    implementation("net.logstash.logback:logstash-logback-encoder:4.10")
    implementation("org.springframework.boot:spring-boot-starter-jta-atomikos:$springBootVersion")
    testImplementation("no.nav.security:oidc-test-support:$oidcSupportVersion")
    testImplementation("org.springframework.boot:spring-boot-starter-test:$springBootVersion")
}

tasks {

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
        transform(ServiceFileTransformer::class.java) {
            setPath("META-INF/cxf")
            include("bus-extensions.txt")
        }
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

