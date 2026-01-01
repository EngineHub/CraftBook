import buildlogic.internalVersion
import com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar

plugins {
    `java-library`
    id("buildlogic.platform")
}

repositories {
    maven {
        name = "paper"
        url = uri("https://repo.papermc.io/repository/maven-public/")
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
        name = "sonatype-oss-snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

val localImplementation = configurations.dependencyScope("localImplementation") {
    description = "Dependencies used locally, but provided by the runtime Bukkit implementation"
}
configurations.named("compileOnly") {
    extendsFrom(localImplementation.get())
}
configurations.named("testImplementation") {
    extendsFrom(localImplementation.get())
}

dependencies {
    "api"(project(":craftbook-core"))
    "api"(project(":craftbook-libs:bukkit"))

    "api"(libs.worldedit.bukkit)
    "api"(libs.worldguard.bukkit) { isTransitive = false }

    "localImplementation"(libs.paperApi) {
        exclude("junit", "junit")
    }
    "implementation"(libs.vaultApi) { isTransitive = false }

    "implementation"(libs.bstats.bukkit)

    "compileOnly"(libs.worldedit.libs.ap)
    "annotationProcessor"(libs.worldedit.libs.ap)
    // ensure this is on the classpath for the AP
    "annotationProcessor"(libs.guava)
}

tasks.named<Copy>("processResources") {
    // Avoid carrying project reference into task execution
    val internalVersion = project.internalVersion
    inputs.property("internalVersion", internalVersion)
    filesMatching("plugin.yml") {
        expand(mapOf("internalVersion" to internalVersion.get()))
    }
    filesMatching("paper-plugin.yml") {
        expand(mapOf("internalVersion" to internalVersion.get()))
    }
}

tasks.named<ShadowJar>("shadowJar") {
    dependencies {
        include(dependency(":craftbook-core"))
        include(dependency("org.bstats:"))

        relocate("org.bstats", "org.enginehub.craftbook.bukkit.bstats")
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
