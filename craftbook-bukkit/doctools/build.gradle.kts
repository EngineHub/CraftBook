plugins {
    application
    id("buildlogic.common-java")
}

application.mainClass.set("org.enginehub.craftbook.internal.util.DocumentationPrinter")
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir
}

repositories {
    maven {
        name = "paper"
        url = uri("https://papermc.io/repo/repository/maven-public/")
    }
    maven {
        name = "EngineHub"
        url = uri("https://maven.enginehub.org/repo/")
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
    maven {
        name = "sonatype-oss-snapshots"
        url = uri("https://s01.oss.sonatype.org/content/repositories/snapshots/")
    }
}

dependencies {
    "implementation"(project(":craftbook-bukkit"))
    "implementation"(libs.paperApi) {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    "implementation"("com.sk89q.worldedit:worldedit-cli:${libs.versions.worldedit.get()}")
}
