plugins {
    idea
}

architectury {
    val enabled_platforms: String by rootProject
    common(enabled_platforms.split(","))
}

loom {
    accessWidenerPath.set(file("src/main/resources/${property("mod_id")}.accesswidener"))
}

dependencies {
    // We depend on fabric loader here to use the fabric @Environment annotations and get the mixin dependencies
    // Do NOT use other classes from fabric loader
    modImplementation(libs.fabric.loader)
    // Remove the next line if you don't want to depend on the API
//    modImplementation(libs.architectury)
    compileOnly(kotlin("stdlib-jdk8"))
    implementation(libs.jsr305)
//    modApi(libs.bundles.ftb.fabric)
    modApi(libs.forge.config)
    implementation(libs.bundles.nightconfig)
}

tasks.named<ProcessResources>("processResources") {
    filesMatching("pack.mcmeta") {
        expand(properties.toMap())
    }
}

// Publish TypeScript typings (.d.ts) as a separate classifier artifact for the common module
val typingsJar by tasks.registering(Jar::class) {
    archiveClassifier.set("typings")
    // Ensure generated KSP resources are produced before we package them
    dependsOn(tasks.named("kspKotlin"))
    // Declare inputs so Gradle understands the read usage
    inputs.dir(layout.buildDirectory.dir("generated/ksp/main/resources")).withPropertyName("kspGeneratedResources")

    from("src/main/resources/assets/mcui/library") { include("**/*.d.ts") }
    from(layout.buildDirectory.dir("generated/ksp/main/resources/typings")) { include("**/*.d.ts") }
    duplicatesStrategy = DuplicatesStrategy.INCLUDE
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        artifact(typingsJar.get()) {
            classifier = "typings"
        }
    }
}

artifacts {
    add("archives", typingsJar)
}

// --- NPM package assembly for TypeScript typings ---
// Produces a ready-to-publish npm package with ambient globals and generated module typings
val npmTypingsDir = layout.buildDirectory.dir("npm-typings")

// Copy .d.ts files into package layout: types/ (ambient) and types/typings (generated)
val prepareNpmTypings by tasks.registering(Copy::class) {
    dependsOn(typingsJar)
    into(npmTypingsDir)
    // Ambient globals at package root types/
    from("src/main/resources/assets/mcui/library") {
        include("**/*.d.ts")
        into("types")
    }
    // Generated module typings under types/typings/
    from(layout.buildDirectory.dir("generated/ksp/main/resources/typings")) {
        include("**/*.d.ts")
        into("types/typings")
    }
}

// Generate a minimal package.json for the typings-only npm package
val writeNpmTypingsPackageJson by tasks.registering {
    dependsOn(prepareNpmTypings)
    doLast {
        val pkgDir = npmTypingsDir.get().asFile
        pkgDir.mkdirs()
        val pkgJson = file(pkgDir.resolve("package.json"))
        val name = "mcui-typings"
        val versionStr = project.version.toString()
        val content = """
            {
              \"name\": \"$name\",
              \"version\": \"$versionStr\",
              \"private\": false,
              \"description\": \"TypeScript typings for MCUI (ambient globals and generated API)\",
              \"license\": \"MIT\",
              \"types\": \"types/index.d.ts\",
              \"files\": [\"types/**/*\"],
              \"keywords\": [\"typescript\", \"lua\", \"tstl\", \"mcui\"]
            }
        """.trimIndent()
        pkgJson.writeText(content)
    }
}

// Create an index.d.ts that references ambient globals so they become visible when users add the package in tsconfig `types`
val writeNpmTypingsIndex by tasks.registering {
    dependsOn(prepareNpmTypings)
    doLast {
        val typesDir = npmTypingsDir.get().asFile.resolve("types")
        typesDir.mkdirs()
        val indexFile = typesDir.resolve("index.d.ts")
        val content = """
            /// <reference path=\"./support.d.ts\" />
            /// <reference path=\"./settings.d.ts\" />
            /// <reference path=\"./theme.d.ts\" />
            // Generated module typings are available under the subpath \"typings/\".
            // Example: import type { Setting } from \"mcui-typings/typings/Setting\";
        """.trimIndent()
        indexFile.writeText(content)
    }
}

// Aggregate task to assemble the npm package folder
val assembleNpmTypings by tasks.registering {
    dependsOn(prepareNpmTypings, writeNpmTypingsPackageJson, writeNpmTypingsIndex)
}

// Optional: produce a .tgz using pnpm pack (or use npm pack) so it can be distributed without Gradle consumers
val pnpmPackTypings by tasks.registering(Exec::class) {
    dependsOn(assembleNpmTypings)
    workingDir = npmTypingsDir.get().asFile
    // If pnpm is not available, run with: ./gradlew :common:assembleNpmTypings and then `npm pack` inside build/npm-typings
    commandLine = listOf("pnpm", "pack")
}
