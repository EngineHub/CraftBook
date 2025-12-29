plugins {
    id("java-library")
    id("buildlogic.core-and-platform")
}

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
    constraints {
        "implementation"(libs.snakeyaml) {
            because("Bukkit provides SnakeYaml")
        }
    }

    "api"(project(":craftbook-libs:core"))

    "api"(libs.worldedit.core)
    "api"(libs.worldguard.core)
    "api"(libs.adventure)
    "api"(libs.adventureMinimessage)
    "api"(libs.adventureGson)
    "api"(libs.adventurePlain)
    "implementation"(libs.snakeyaml)
    "implementation"(libs.guava)
    "implementation"(libs.jspecify)
    "implementation"(libs.gson)
    "implementation"(libs.log4j.api)
    "implementation"(libs.fastutil)
    "languageFiles"("${project.group}:craftbook-lang:5.0.0:${libs.versions.lang.version.get()}@zip")

    "compileOnly"(libs.worldedit.libs.ap)
    "annotationProcessor"(libs.worldedit.libs.ap)
    // ensure this is on the classpath for the AP
    "annotationProcessor"(libs.guava)

    "testImplementation"(libs.hamcrest.library)
}

tasks.compileJava {
    dependsOn(":craftbook-libs:build")
    options.compilerArgs.add("-Aarg.name.key.prefix=")
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
        artifactId = the<BasePluginExtension>().archivesName.get()
        from(components["java"])
    }
}
