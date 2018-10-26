import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    java
    kotlin("jvm") version "1.2.71"
    id("io.vertx.vertx-plugin") version "0.1.0"
}

version = "unspecified"

repositories {
    mavenCentral()
    jcenter()
}

dependencies {
    compile(kotlin("stdlib-jdk8"))
    compile("org.eclipse.milo:sdk-client:0.2.4")
    compile("io.vertx:vertx-core:3.5.4")
    compile("io.vertx:vertx-web:3.0.0")

    testCompile("junit", "junit", "4.12")
}

configure<JavaPluginConvention> {
    sourceCompatibility = JavaVersion.VERSION_1_8
}
tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "1.8"
}

tasks {
    "build" {
        dependsOn("copyClientToServer")
    }

    "jar"(Jar::class) {
        baseName = "data-server"
        classifier = "fat"
        version = project.version.toString()
        manifest {
            attributes(Pair("Manifest-Version", version))
            attributes(Pair("Main-Class", "io.vertx.core.Launcher"))
            attributes(Pair("Main-Verticle", "com.jwheeler.data.server.DataVerticle"))
            attributes(Pair("Class-Path", "."))
        }
        exclude("META-INF/*.RSA", "META-INF/*.SF", "META-INF/*.DSA")
        include("META-INF/services/io.vertx.core.spi.VerticleFactory")
    }

    register<Copy>("copyClientToServer") {
        dependsOn(":client:build")

        from("../client/build")
        into("src/main/resources/assets/")
    }
}


vertx {
    mainVerticle = "com.jwheeler.data.server.DataVerticle"
}