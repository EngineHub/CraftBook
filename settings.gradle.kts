rootProject.name = "craftbook"

include("craftbook-libs")

listOf("core").forEach {
    include("craftbook-libs:$it")
    include("craftbook-$it")
}
