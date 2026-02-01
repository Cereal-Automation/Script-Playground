import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import proguard.gradle.ProGuardTask

plugins {
    kotlin("jvm") version "2.2.0"
    id("com.gradleup.shadow") version "8.3.6"
}

allprojects {
    repositories {
        mavenCentral()
        maven {
            url = uri("https://maven.cereal-automation.com/releases")
        }
    }

    // Exclude these dependencies because they are already included in the Cereal client.
    configurations.runtimeClasspath {
        exclude(group = "org.jetbrains.kotlin", module = "kotlin-stdlib")
        exclude(group = "com.cereal-automation", module = "cereal-sdk")
    }

    apply(plugin = "com.gradleup.shadow")

    tasks.withType<ShadowJar> {
        archiveFileName.set("release.jar")
    }

    tasks.register("scriptJar", ProGuardTask::class.java) {
        description = "Build script jar with obfuscation"
        dependsOn("shadowJar")

        val artifactName = "release.jar"
        val buildDir = layout.buildDirectory.get()
        val cerealScriptFolder = "$buildDir/cereal"

        injars("$buildDir/libs/$artifactName")
        outjars("$cerealScriptFolder/$artifactName")

        // Mapping for debugging
        printseeds("$cerealScriptFolder/seeds.txt")
        printmapping("$cerealScriptFolder/mapping.txt")

        // Dependencies
        libraryjars(sourceSets.main.get().compileClasspath)

        configuration(
            files(
                "${rootDir.absolutePath}/proguard-rules/script.pro",
                "${rootDir.absolutePath}/proguard-rules/coroutines.pro",
            ),
        )
    }
}

buildscript {
    dependencies {
        classpath("com.guardsquare:proguard-gradle:7.7.0")
    }
}

dependencies {
    // These dependencies are added as compileOnly because they are available in the environment where the scripts run.
    compileOnly("com.cereal-automation:cereal-licensing:1.6.1") {
        artifact {
            classifier = "all"
        }
    }
    compileOnly("org.jetbrains.kotlin:kotlin-stdlib:2.2.0")
    implementation("com.cereal-automation:cereal-licensing:1.6.1")

    testImplementation(kotlin("test"))
    testImplementation("com.cereal-automation:cereal-test-utils:1.6.1")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
    testImplementation("io.mockk:mockk:1.14.2")
}

tasks {
    kotlin {
        jvmToolchain {
            languageVersion.set(JavaLanguageVersion.of(17))
        }
    }
}
