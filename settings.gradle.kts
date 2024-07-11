pluginManagement {
    repositories {
        gradlePluginPortal()
        maven {
            name = "EngineHub"
            url = uri("https://maven.enginehub.org/repo/")
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "0.8.0"
}

rootProject.name = "craftbook"

includeBuild("build-logic")

include("craftbook-libs")

listOf("core", "bukkit").forEach {
    include("craftbook-libs:$it")
    include("craftbook-$it")
}

logger.lifecycle("""
*******************************************
 You are building CraftBook!
 If you encounter trouble:
 1) Try running 'build' in a separate Gradle run
 2) Use gradlew and not gradle
 3) If you still need help, ask on Discord! https://discord.gg/enginehub
 Output files will be in [subproject]/build/libs
*******************************************
""")


include("craftbook-bukkit:doctools")
