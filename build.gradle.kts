plugins {
    id("java")
    id("com.gradleup.shadow") version "9.4.1"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:5.10.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
    implementation("org.yaml:snakeyaml:2.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.shadowJar {
    archiveClassifier.set("all")

    duplicatesStrategy = DuplicatesStrategy.EXCLUDE

    mergeServiceFiles()

    isPreserveFileTimestamps = false
    isReproducibleFileOrder = true

    manifest {
        attributes["Main-Class"] = "com.aquip.tetris.Main"
    }
}

tasks.jar {
    manifest {
        attributes["Main-Class"] = "com.aquip.tetris.Main"
    }
}

tasks.register<JavaExec>("runEval") {
    group = "verification"
    description = "Runs the AI evaluation suite with a live preview window."
    classpath = sourceSets["test"].runtimeClasspath
    mainClass.set("com.aquip.tetris.eval.EvalMain")
}