import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import com.github.jengelman.gradle.plugins.shadow.transformers.PropertiesFileTransformer
import com.github.jengelman.gradle.plugins.shadow.transformers.ServiceFileTransformer
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

group = "no.nav.syfo"
version = "1.0.0-SNAPSHOT"
description = "syfoveileder"

plugins {
    kotlin("jvm") version "1.3.50"
    id("com.diffplug.gradle.spotless") version "3.18.0"
    id("com.github.johnrengelman.shadow") version "4.0.4"
}

buildscript {
    dependencies {
        classpath("javax.xml.bind:jaxb-api:2.4.0-b180830.0359")
        classpath("org.glassfish.jaxb:jaxb-runtime:2.4.0-b180830.0438")
        classpath("com.sun.activation:javax.activation:1.2.0")
    }
}


repositories {
    mavenCentral()
    jcenter()
    maven(url = "https://dl.bintray.com/kotlin/ktor")
    maven(url = "https://repo.adeo.no/repository/maven-releases/")
    maven(url = "https://dl.bintray.com/spekframework/spek-dev")
    maven(url = "https://dl.bintray.com/kotlin/kotlinx/")
    maven(url = "http://packages.confluent.io/maven/")
}

dependencies {
    compile("org.apache.cxf:cxf-spring-boot-starter-jaxws:3.3.3")
    compile("org.apache.cxf:cxf-rt-features-logging:3.3.3")
    compile("org.apache.cxf:cxf-rt-ws-security:3.3.3")
    compile("org.apache.cxf:cxf-rt-ws-policy:3.3.3")
    compile("no.nav.security:oidc-spring-support:0.2.4")
    compile("no.nav.security:oidc-support:0.2.4")
    compile("com.microsoft.azure:adal4j:1.6.4")
    compile("com.nimbusds:oauth2-oidc-sdk:6.5")
    compile("com.fasterxml.jackson.module:jackson-module-kotlin:2.9.8")
    compile("org.jetbrains.kotlin:kotlin-stdlib-jdk8:1.2.71")
    compile("org.jetbrains.kotlin:kotlin-reflect:1.2.71")
    compile("javax.inject:javax.inject:1")
    compile("org.springframework.boot:spring-boot-starter-jersey:2.0.6.RELEASE")
    compile("org.springframework.boot:spring-boot-starter-web:2.0.6.RELEASE")
    compile("org.springframework.boot:spring-boot-starter-actuator:2.0.6.RELEASE")
    compile("org.bitbucket.b_c:jose4j:0.5.0")
    compile("io.micrometer:micrometer-registry-prometheus:1.0.7")
    compile("org.slf4j:slf4j-api:1.7.25")
    compile("org.springframework.boot:spring-boot-starter-logging:2.0.6.RELEASE")
    compile("net.logstash.logback:logstash-logback-encoder:4.10")
    compile("org.springframework.boot:spring-boot-starter-jta-atomikos:2.0.6.RELEASE")
    compile("com.oracle:ojdbc8:12.2.0.1")
    testCompile("no.nav.security:oidc-spring-test:0.2.4")
    testCompile("com.h2database:h2:1.4.196")
    testCompile("org.springframework.boot:spring-boot-starter-test:2.0.6.RELEASE")
    compile("org.flywaydb:flyway-core:5.0.7")
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

