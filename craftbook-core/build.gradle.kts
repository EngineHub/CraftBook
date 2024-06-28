plugins {
    id("java-library")
}

applyPlatformAndCoreConfiguration()

repositories {
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
    }
}

configurations {
    register("languageFiles")
}

dependencies {
    "api"(project(":craftbook-libs:core"))
    "api"("com.sk89q.worldedit:worldedit-core:${Versions.WORLDEDIT}")
    "api"("com.sk89q.worldguard:worldguard-core:${Versions.WORLDGUARD}")
    "implementation"("org.yaml:snakeyaml:2.0")
    "implementation"("com.google.guava:guava")
    "implementation"("org.jspecify:jspecify:0.3.0")
    "implementation"("com.google.code.gson:gson")
    "implementation"("it.unimi.dsi:fastutil")
    "languageFiles"("${project.group}:craftbook-lang:5.0.0:1429@zip")

    "implementation"("org.apache.logging.log4j:log4j-api:${Versions.LOG4J}")

    "testImplementation"("org.hamcrest:hamcrest-library:1.2.1")
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(":craftbook-libs:build")
    options.compilerArgs.add("-Aarg.name.key.prefix=")
}

configure<org.cadixdev.gradle.licenser.LicenseExtension> {
    exclude {
        it.file.startsWith(project.buildDir)
    }
}

tasks.named<Copy>("processResources") {
    from(configurations.named("languageFiles")) {
        rename {
            "i18n.zip"
        }
        into("lang")
    }
}

configure<PublishingExtension> {
    publications.named<MavenPublication>("maven") {
        artifactId = the<BasePluginConvention>().archivesBaseName
        from(components["java"])
    }
}
