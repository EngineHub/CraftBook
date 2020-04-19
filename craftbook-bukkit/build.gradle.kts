import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar
import org.gradle.api.internal.HasConvention

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
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
        url = uri("http://repo.dmulloy2.net/content/groups/public/")
    }
}

dependencies {
    "compile"(project(":craftbook-core"))
    "compile"(project(":craftbook-libs:bukkit"))
    "api"("com.destroystokyo.paper:paper-api:1.15.2-R0.1-SNAPSHOT")
    "implementation"("io.papermc:paperlib:1.0.2")
    "api"("com.sk89q.worldedit:worldedit-bukkit:${Versions.WORLDEDIT}") { isTransitive = false }
    "api"("com.sk89q.worldguard:worldguard-bukkit:${Versions.WORLDGUARD}") { isTransitive = false }
    "api"("net.milkbowl.vault:VaultAPI:1.7") { isTransitive = false }
    "api"("com.comphenix.protocol:ProtocolLib:4.5.0") { isTransitive = false }
    "implementation"("org.bstats:bstats-bukkit:1.7")
}

tasks.named<Upload>("install") {
    (repositories as HasConvention).convention.getPlugin<MavenRepositoryHandlerConvention>().mavenInstaller {
        pom.whenConfigured {
            dependencies.firstOrNull { dep ->
                dep!!.withGroovyBuilder {
                    getProperty("groupId") == "com.destroystokyo.paper" && getProperty("artifactId") == "paper-api"
                }
            }?.withGroovyBuilder {
                setProperty("groupId", "org.bukkit")
                setProperty("artifactId", "bukkit")
            }
        }
    }
}

tasks.named<Copy>("processResources") {
    filesMatching("plugin.yml") {
        expand("internalVersion" to project.ext["internalVersion"])
    }
}

addJarManifest();

tasks.named<Jar>("jar") {
    manifest {
        attributes("Implementation-Version" to project.version)
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        relocate("org.bstats", "com.sk89q.craftbook.bukkit.bstats") {
            include(dependency("org.bstats:bstats-bukkit:1.7"))
        }
        relocate ("io.papermc.lib", "com.sk89q.craftbook.bukkit.paperlib") {
            include(dependency("io.papermc:paperlib:1.0.2"))
        }
    }
}

tasks.named("assemble").configure {
    dependsOn("shadowJar")
}
