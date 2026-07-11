pluginManagement {
    // pluginManagement repositories resolve plugins before the repo-reconfiguration plugin can
    // apply, so they must point at EngineHub mirrors directly rather than upstream URLs.
    repositories {
        maven {
            name = "EngineHub"
            url = uri("https://repo.enginehub.org/libs-release/")
            mavenContent {
                releasesOnly()
                includeGroupAndSubgroups("com.sk89q")
                includeGroupAndSubgroups("org.enginehub")
            }
        }
        maven {
            name = "EngineHub Maven Central Mirror"
            url = uri("https://repo.enginehub.org/internal/maven-central-proxy/")
            mavenContent {
                releasesOnly()
            }
        }
        maven {
            name = "EngineHub Gradle Plugin Portal Mirror"
            url = uri("https://repo.enginehub.org/internal/plugin-portal-proxy/")
            mavenContent {
                releasesOnly()
            }
        }
    }
}
plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    id("org.enginehub.crankcase.repo-reconfiguration") version "0.1.2"
}
dependencyResolutionManagement {
    repositories {
        maven {
            name = "EngineHub"
            url = uri("https://repo.enginehub.org/repo/")
        }
        ivy {
            url = uri("https://repo.enginehub.org/language-files/")
            name = "EngineHub Language Files"
            patternLayout {
                artifact("[organisation]/[module]/[revision]/[artifact]-[revision](+[classifier])(.[ext])")
                setM2compatible(true)
            }
            metadataSources {
                artifact()
            }
            content {
                includeModuleByRegex(".*", "craftbook-lang")
            }
        }
    }
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

rootProject.name = "craftbook"

includeBuild("build-logic")

include("craftbook-libs")

listOf("core", "bukkit").forEach {
    include("craftbook-libs:$it")
    include("craftbook-$it")
}

include("craftbook-bukkit:doctools")
