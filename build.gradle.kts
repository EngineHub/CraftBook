import org.ajoberstar.grgit.Grgit

if (!project.hasProperty("gitCommitHash")) {
    apply(plugin = "org.ajoberstar.grgit")
    ext["gitCommitHash"] = try {
        extensions.getByName<Grgit>("grgit").head()?.abbreviatedId
    } catch (e: Exception) {
        logger.warn("Error getting commit hash", e)

        "no.git.id"
    }
}

// Work around https://github.com/gradle/gradle/issues/4823
subprojects {
    if (buildscript.sourceFile?.extension?.toLowerCase() == "kts"
        && parent != rootProject) {
        generateSequence(parent) { project -> project.parent.takeIf { it != rootProject } }
            .forEach { evaluationDependsOn(it.path) }
    }
}

logger.lifecycle("""
*******************************************
 You are building CraftBook!
 If you encounter trouble:
 1) Try running 'build' in a separate Gradle run
 2) Use gradlew and not gradle
 3) If you still need help, ask on Discord! https://discord.gg/enginehub
 Output files will be in [subproject]/build/libs
*******************************************
""")

applyCommonConfiguration()
applyRootArtifactoryConfig()
