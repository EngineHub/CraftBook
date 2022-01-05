import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    id("java-library")
}

applyPlatformAndCoreConfiguration()
applyShadowConfiguration()

repositories {
    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "bstats"
        url = uri("https://repo.codemc.org/repository/maven-public")
    }
    maven {
        name = "Vault"
        url = uri("https://jitpack.io")
    }
    maven {
        name = "ProtocolLib"
        url = uri("https://repo.dmulloy2.net/content/groups/public/")
    }
}

val localImplementation = configurations.create("localImplementation") {
    description = "Dependencies used locally, but provided by the runtime Bukkit implementation"
    isCanBeConsumed = false
    isCanBeResolved = false
}
configurations["compileOnly"].extendsFrom(localImplementation)
configurations["testImplementation"].extendsFrom(localImplementation)

dependencies {
    "api"(project(":craftbook-core"))
    "api"(project(":craftbook-libs:bukkit"))
    "localImplementation"("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT") {
        exclude("junit", "junit")
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    "api"("com.sk89q.worldedit:worldedit-bukkit:${Versions.WORLDEDIT}") {
        exclude(group = "org.spigotmc")
    }
    "api"("com.sk89q.worldguard:worldguard-bukkit:${Versions.WORLDGUARD}") {
        exclude(group = "org.spigotmc")
    }
    "implementation"("net.milkbowl.vault:VaultAPI:1.7") { isTransitive = false }
    "implementation"("com.comphenix.protocol:ProtocolLib:4.5.1") { isTransitive = false }
    "implementation"("org.bstats:bstats-bukkit:2.2.1")

    "localImplementation"(platform("org.apache.logging.log4j:log4j-bom:2.8.1"))
    "localImplementation"("org.apache.logging.log4j:log4j-api")

    "compileOnly"("com.sk89q.worldedit.worldedit-libs:ap:${Versions.WORLDEDIT}")
    "annotationProcessor"("com.sk89q.worldedit.worldedit-libs:ap:${Versions.WORLDEDIT}")
    "annotationProcessor"("com.google.guava:guava:${Versions.GUAVA}")
}

tasks.named<Copy>("processResources") {
    val internalVersion = project.ext["internalVersion"]
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand("internalVersion" to internalVersion)
    }
}

addJarManifest();

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency(":craftbook-core"))

        relocate("org.bstats", "org.enginehub.craftbook.bukkit.bstats") {
            include(dependency("org.bstats:"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        from(components["java"])
    }
}
