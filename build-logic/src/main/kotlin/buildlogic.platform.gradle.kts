plugins {
    id("com.gradleup.shadow")
    id("buildlogic.core-and-platform")
}

shadow {
    addShadowVariantIntoJavaComponent = false
}

tasks.named<com.github.jengelman.gradle.plugins.shadow.tasks.ShadowJar>("shadowJar") {
    archiveClassifier.set("dist")
    dependencies {
        include(project(":craftbook-libs:core"))
        include(project(":craftbook-core"))

        exclude("org.jspecify:jspecify")
    }
    exclude("GradleStart**")
    exclude(".cache")
    exclude("LICENSE*")
    exclude("META-INF/maven/**")
    minimize()
}

afterEvaluate {
    tasks.named<Jar>("jar") {
        val version = project(":craftbook-core").version
        inputs.property("version", version)
        val attributes = mutableMapOf(
            "Implementation-Version" to version,
            "CraftBook-Version" to version,
        )
        manifest.attributes(attributes)
    }
}
