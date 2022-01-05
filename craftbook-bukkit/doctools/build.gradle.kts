plugins {
    application
}

applyCommonConfiguration()

application.mainClassName = "org.enginehub.craftbook.internal.util.DocumentationPrinter"
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
}

dependencies {
    "implementation"(project(":craftbook-bukkit"))
    "implementation"("io.papermc.paper:paper-api:1.18.1-R0.1-SNAPSHOT") {
        exclude(group = "org.slf4j", module = "slf4j-api")
    }
    "implementation"("com.sk89q.worldedit:worldedit-cli:${Versions.WORLDEDIT}")
}
