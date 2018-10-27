import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import com.bmuschko.gradle.docker.tasks.image.DockerBuildImage

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
    java
    kotlin("jvm") version "1.2.71"
    id("io.vertx.vertx-plugin") version "0.1.0"
    id("com.bmuschko.docker-remote-api") version "3.6.0"
}

version = "unspecified"

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.eclipse.milo:sdk-client:0.2.4")
    compile("io.vertx:vertx-core:3.5.4")
    compile("io.vertx:vertx-web:3.0.0")
    compile("ch.qos.logback:logback-classic:1.1.7")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    "runShadow"{
        dependsOn("assemble")
    }

    "assemble" {
        dependsOn("copyClientToServer")
    }

    register<Copy>("copyClientToServer") {
        dependsOn(":client:build")

        from("../client/build")
        into("build/resources/main/assets")
    }

    register<DockerBuildImage>("dockerBuildImage") {
        dependsOn("assemble")

        inputDir = project.projectDir
        tag = "jtlwheeler/data-server"
    }
}

vertx {
    mainVerticle = "com.jwheeler.data.server.DataVerticle"
}
