import com.mendhak.gradlecrowdin.DownloadTranslationsTask
import com.mendhak.gradlecrowdin.UploadSourceFileTask

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
    id("com.mendhak.gradlecrowdin")
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
    }
}

configurations {
    all {
        resolutionStrategy {
            force("com.google.guava:guava:21.0")
        }
    }
    register("languageFiles")
}

dependencies {
    "compile"(project(":craftbook-libs:core"))
    "api"("com.sk89q.worldedit:worldedit-core:${Versions.WORLDEDIT}")
    "api"("com.sk89q.worldguard:worldguard-core:${Versions.WORLDGUARD}")
    "implementation"("org.yaml:snakeyaml:1.9")
    "implementation"("com.google.guava:guava:${Versions.GUAVA}")
    "implementation"("com.google.code.findbugs:jsr305:1.3.9")
    "implementation"("com.google.code.gson:gson:${Versions.GSON}")
    "implementation"("org.slf4j:slf4j-api:1.7.26")
    "implementation"("it.unimi.dsi:fastutil:${Versions.FAST_UTIL}")
    "languageFiles"("${project.group}:craftbook-lang:${project.version}:10@zip")

    "compileOnly"("com.google.code.findbugs:jsr305:1.3.9")
    "testImplementation"("org.hamcrest:hamcrest-library:1.2.1")
}

tasks.withType<JavaCompile>().configureEach {
    dependsOn(":craftbook-libs:build")
}

sourceSets {
    main {
        java {
            srcDir("src/main/java")
        }
        resources {
            srcDir("src/main/resources")
        }
    }
}

tasks.named<Copy>("processResources") {
    // it's in the zip too
    exclude("**/lang/strings.json")
    from(configurations.named("languageFiles")) {
        rename {
            "i18n.zip"
        }
        into("lang")
    }
}