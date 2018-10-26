import com.moowork.gradle.node.yarn.YarnTask

plugins {
    id("com.moowork.node") version "1.2.0"
}

node {
    version = "11.0.0"
    npmVersion = "6.1.0"
    yarnVersion = "1.10.1"
    download = true
}

tasks {
    val javascriptRuntime = arrayOf(
            fileTree("node_modules"),
            "package.json",
            "yarn.lock")

    register<YarnTask>("e2e") {
        dependsOn("yarn", "composeUp")

        inputs.files(javascriptRuntime)
        inputs.dir("src")

        args = listOf("run", "e2e")
    }

    register<YarnTask>("integrationTest") {
        dependsOn("yarn", "::composeUp")

        val envs = mapOf("PROTRACTOR_BASE_URL" to "http://localhost:8080")
        setEnvironment(envs)

        inputs.files(javascriptRuntime)
        inputs.dir("src")

        args = listOf("run", "e2e")
    }
}