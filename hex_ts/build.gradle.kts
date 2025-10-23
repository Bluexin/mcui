plugins {
    base
    idea
}

val themeName = "hex-ts"
val themeNamespace = "mcui"
val packDescription = "TS-based theme for MCUI [BETA]"

val sourcesDir = layout.projectDirectory.dir("src")
val tsDir = sourcesDir.dir("main/ts")
val resourcesDir = sourcesDir.dir("main/resources")
val typingsDir = sourcesDir.dir("typings")

val prepareTypings = tasks.register("prepareTypings", Copy::class) {
    // Ensure the jar is built in the :common project
    dependsOn(":common:typingsJar")
    from({
        // Resolve the output jar from the :common task
        val jarTask = project.tasks.getByPath(":common:typingsJar") as Jar
        zipTree(jarTask.archiveFile.get().asFile)
    })
    exclude("META-INF/**")
    into(typingsDir)
}

val pnpmInstall = tasks.register("pnpmInstall", Exec::class) {
    workingDir = project.projectDir
    isIgnoreExitValue = false
    // Prefer frozen lockfile if present
    commandLine = if (file("pnpm-lock.yaml").exists()) listOf("pnpm", "install", "--frozen-lockfile") else listOf(
        "pnpm",
        "install"
    )
}

val buildLua = tasks.register("buildLua", Exec::class) {
    workingDir = project.projectDir
    dependsOn(prepareTypings)
    dependsOn(pnpmInstall)
    commandLine = listOf("pnpm", "run", "build")
}

val buildPackMeta = tasks.register("buildPackMeta") {
    val packMeta = layout.buildDirectory.dir("tmp").get().file("pack.mcmeta").asFile
    outputs.file(packMeta)
    doLast {
        packMeta.writeText(
            """
            {
              "pack": {
                "pack_format": 13,
                "description": "$packDescription"
              }
            }
        """.trimIndent()
        )
    }
}

tasks.assemble {
    dependsOn(buildLua)
}

tasks.build {
    dependsOn(buildLua)
}

// Package the compiled Lua files into a distributable zip (optional)
val luaDist = layout.buildDirectory.dir("dist")
val distZip = tasks.register("distZip", Zip::class) {
    dependsOn(buildLua, buildPackMeta)
    val themeRoot = "assets/$themeNamespace/themes/$themeName"
    from(resourcesDir) {
        into(themeRoot)
    }
    from("dist") {
        include("**/*.lua")
        into("$themeRoot/scripts")
    }
    from(buildPackMeta.get().outputs.files)

    destinationDirectory.set(luaDist)
    archiveBaseName.set("hex-ts")
}

val cleanGenerated = tasks.register(/* name = */ "cleanGenerated") {
    group = LifecycleBasePlugin.CLEAN_TASK_NAME
    delete(layout.projectDirectory.dir("dist"), typingsDir)
}

tasks.clean {
    dependsOn(cleanGenerated)
}

idea {
    module {
        sourceDirs.add(tsDir.asFile)
        resourceDirs.add(resourcesDir.asFile)
        generatedSourceDirs.add(typingsDir.asFile)
    }
}
