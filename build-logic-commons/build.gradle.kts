tasks.register("clean") {
    dependsOn(subprojects.map { "${it.name}:clean" })
}
tasks.register("check") {
    dependsOn(subprojects.map { "${it.name}:check" })
}
