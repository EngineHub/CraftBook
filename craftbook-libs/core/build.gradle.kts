plugins {
    id("buildlogic.libs")
}

dependencies {
    "shade"(libs.squirrelid)
//    "shade"("org.enginehub.jinglenote:jinglenote-core:${Versions.JINGLENOTE}") { isTransitive = false }
}
