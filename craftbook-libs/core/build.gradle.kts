applyLibrariesConfiguration()

dependencies {
    "shade"("com.sk89q:squirrelid:${Versions.SQUIRRELID}")
    "shade"("org.enginehub.jinglenote:jinglenote-core:${Versions.JINGLENOTE}") { isTransitive = false }
}