plugins {
    application
    id("buildlogic.common-java")
}

application.mainClass.set("org.enginehub.craftbook.internal.util.DocumentationPrinter")
tasks.named<JavaExec>("run") {
    workingDir = rootProject.projectDir

    // Add this env var to bypass systems that break docgen.
    environment("CRAFTBOOK_DOCGEN", "true");
}

repositories {
    maven {
        name = "paper"
        url = uri("https://repo.papermc.io/repository/maven-public/")
    }
    maven {
        name = "EngineHub"
        url = uri("https://repo.enginehub.org/repo/")
    }
}

dependencies {
    "implementation"(project(":craftbook-bukkit"))
    "implementation"(libs.paperApi);
    "implementation"("com.sk89q.worldedit:worldedit-cli:${libs.versions.worldedit.get()}")
}
