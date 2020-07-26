rootProject.name = "craftbook"

include("craftbook-libs")

listOf("core", "bukkit").forEach {
    include("craftbook-libs:$it")
    include("craftbook-$it")
}

include("craftbook-bukkit:doctools")
