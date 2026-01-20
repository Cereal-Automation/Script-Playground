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

    apply(plugin = "com.gradleup.shadow")

    tasks.withType<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar> {
        archiveFileName.set("release.jar")

        dependencies {
            // The below dependencies are included in the Cereal client by default so they can be excluded here.
            // New (breaking) versions will have a different artifact id so they will always stay compatible.
            // Be careful when adding something here because Proguard could need the code to determine that methods
            // called by any of these libs are in still in use so that Proguard doesn't remove them. For example
            // kotlinx-coroutines-core isn't excluded for that reason.
            exclude { dependency ->
                dependency.moduleGroup == "com.cereal-automation" &&
                    (dependency.moduleName == "cereal-sdk" || dependency.moduleName == "cereal-chrome-driver")
            }

            // Kotlin is included in the Cereal client by default so leave it out to make the script binary smaller and to
            // prevent conflicts with coroutines, which is also used in the Scripts' interface.
            exclude("DebugProbesKt.bin", "META-INF/**", "*.jpg", "kotlin/**")
        }
    }

    tasks.register("scriptJar", proguard.gradle.ProGuardTask::class.java) {
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
    implementation("com.cereal-automation:cereal-sdk:1.6.1:all")
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
