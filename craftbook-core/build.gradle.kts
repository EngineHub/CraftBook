plugins {
    id("java-library")
    id("net.ltgt.apt-eclipse")
    id("net.ltgt.apt-idea")
}

applyPlatformAndCoreConfiguration()

dependencies {
    "compile"(project(":craftbook-libs:core"))
    "compile"("com.sk89q.worldedit:worldedit-core:${Versions.WORLDEDIT}")
    "compile"("com.sk89q.worldguard:worldguard-core:${Versions.WORLDGUARD}")

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
