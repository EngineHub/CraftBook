import com.mendhak.gradlecrowdin.DownloadTranslationsTask
import com.mendhak.gradlecrowdin.UploadSourceFileTask

plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
    id("com.mendhak.gradlecrowdin")
}

applyPlatformAndCoreConfiguration()

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

val crowdinApiKey = "crowdin_apikey"

if (project.hasProperty(crowdinApiKey) && !gradle.startParameter.isOffline) {
    tasks.named<UploadSourceFileTask>("crowdinUpload") {
        apiKey = "${project.property(crowdinApiKey)}"
        projectId = "craftbook"
        files = arrayOf(
            object {
                var name = "strings.json"
                var source = "${file("src/main/resources/lang/strings.json")}"
            }
        )
    }

    val dlTranslationsTask = tasks.named<DownloadTranslationsTask>("crowdinDownload") {
        apiKey = "${project.property(crowdinApiKey)}"
        destination = "${buildDir.resolve("crowdin-i18n")}"
        projectId = "craftbook"
    }

    tasks.named<Copy>("processResources") {
        dependsOn(dlTranslationsTask)
        from(dlTranslationsTask.get().destination) {
            into("lang")
        }
    }

    tasks.named("classes").configure {
        dependsOn("crowdinDownload")
    }
}
