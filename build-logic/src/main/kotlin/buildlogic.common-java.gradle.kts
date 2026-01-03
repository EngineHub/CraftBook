import buildlogic.stringyLibs
import buildlogic.getLibrary
import net.ltgt.gradle.errorprone.errorprone

plugins {
    id("eclipse")
    id("idea")
    id("checkstyle")
    id("net.ltgt.errorprone")
    id("buildlogic.common")
}

tasks
    .withType<JavaCompile>()
    .matching { it.name == "compileJava" || it.name == "compileTestJava" }
    .configureEach {
        val disabledLint = listOf(
            "processing", "path", "fallthrough", "serial", "overloads", "this-escape",
        )
        options.release.set(21)
        options.compilerArgs.addAll(listOf("-Xlint:all") + disabledLint.map { "-Xlint:-$it" })
        options.isDeprecation = true
        options.encoding = "UTF-8"
        options.compilerArgs.add("-parameters")
        if (project.name.contains("-core")) {
            options.compilerArgs.add("-Werror")
        }
        options.errorprone {
            // We use -Werror, so we don't need errorprone to fail the build separately
            allErrorsAsWarnings = true
            // Obviously we don't need to fix generated code
            disableWarningsInGeneratedCode = true
            // We use reference equality intentionally in several places
            // Perhaps we should consider testing the performance impact of using .equals() instead?
            // Especially for the types that are only compared by reference equality, we could consider
            // removing their .equals() implementations to avoid confusion.
            disable("ReferenceEquality")
            // We're on JDK 21, so System.console() can still be null
            disable("SystemConsoleNull")
        }
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
    "compileOnly"(stringyLibs.getLibrary("errorprone-annotations"))
    "errorprone"(stringyLibs.getLibrary("errorprone-core"))
    "testImplementation"(platform(stringyLibs.getLibrary("junit-bom")))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-api"))
    "testImplementation"(stringyLibs.getLibrary("junit-jupiter-params"))
    "testImplementation"(platform(stringyLibs.getLibrary("mockito-bom")))
    "testImplementation"(stringyLibs.getLibrary("mockito-core"))
    "testImplementation"(stringyLibs.getLibrary("mockito-junit-jupiter"))
    "testRuntimeOnly"(stringyLibs.getLibrary("junit-jupiter-engine"))
    "testRuntimeOnly"(stringyLibs.getLibrary("junit-platform-launcher"))
}

// Java 8 turns on doclint which we fail
tasks.withType<Javadoc>().configureEach {
    options.encoding = "UTF-8"
    (options as StandardJavadocDocletOptions).apply {
        if (project.name.contains("-core")) {
            addBooleanOption("Werror", true)
        }
        addBooleanOption("Xdoclint:all", true)
        addBooleanOption("Xdoclint:-missing", true)
        tags(
            "apiNote:a:API Note:",
            "implSpec:a:Implementation Requirements:",
            "implNote:a:Implementation Note:"
        )
    }
}

configure<JavaPluginExtension> {
    withJavadocJar()
    withSourcesJar()
}

tasks.named("check").configure {
    dependsOn("checkstyleMain", "checkstyleTest")
}
