import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

buildscript {
    repositories {
        mavenCentral()
        jcenter()
    }
    dependencies {
        classpath("com.bmuschko:gradle-docker-plugin:3.3.1")
    }
}

plugins {
    id("com.github.johnrengelman.shadow") version "4.0.1"
    java
    kotlin("jvm") version "1.2.71"
    application
    id("com.bmuschko.docker-remote-api") version "3.6.0"
}

version = "1.0-SNAPSHOT"

application {
    mainClassName = "com.jwheeler.server.ApplicationKt"
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.eclipse.milo:sdk-server:0.2.4")
    compile("org.bouncycastle:bcprov-jdk15on:1.58")
    compile("org.bouncycastle:bcpkix-jdk15on:1.58")
    compile("ch.qos.logback:logback-classic:1.1.7")

    testCompile("junit", "junit", "4.12")
    testCompile("org.assertj:assertj-core:3.11.1")
}

val compileKotlin: KotlinCompile by tasks
compileKotlin.kotlinOptions {
    jvmTarget = "1.8"
}
val compileTestKotlin: KotlinCompile by tasks
compileTestKotlin.kotlinOptions {
    jvmTarget = "1.8"
}

tasks {
    register<DockerBuildImage>("dockerBuildImage") {
        dependsOn("assemble")

        inputDir = project.projectDir
        tag = "jtlwheeler/device-server"
    }
}