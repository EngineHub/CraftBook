import buildlogic.stringyLibs
import buildlogic.getLibrary

plugins {
    id("eclipse")
    id("idea")
    id("checkstyle")
    id("org.enginehub.crankcase.java")
    id("buildlogic.common")
}

crankcaseJava {
    javaRelease = 25
    disabledLints = listOf("processing", "path", "fallthrough", "serial", "overloads", "this-escape")
    disabledErrorprone = listOf(
        // We use reference equality intentionally in several places
        "ReferenceEquality",
        // We're on JDK 21, so System.console() can still be null
        "SystemConsoleNull",
    )
    failOnWarnings = project.name.contains("-core")
}

configure<CheckstyleExtension> {
    configFile = rootProject.file("config/checkstyle/checkstyle.xml")
    toolVersion = "12.3.1"
}

tasks.withType<Test>().configureEach {
    useJUnitPlatform {
        includeEngines("junit-jupiter")
    }
}

dependencies {
    "compileOnly"(stringyLibs.getLibrary("jspecify"))
    "testImplementation"(platform(stringyLibs.getLibrary("mockito-bom")))
    "testImplementation"(stringyLibs.getLibrary("mockito-core"))
    "testImplementation"(stringyLibs.getLibrary("mockito-junit-jupiter"))
}

tasks.named("check").configure {
    dependsOn("checkstyleMain", "checkstyleTest")
}
