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
            "yarn.lock",
            "tsconfig.json"
    )

    register<YarnTask>("build") {
        dependsOn("yarn")

        inputs.files(javascriptRuntime)
        inputs.dir("src")

        args = listOf("run", "build")
    }
}