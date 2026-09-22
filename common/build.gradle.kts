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

// --- NPM package assembly for TypeScript typings ---
// Produces a ready-to-publish npm package with ambient globals and generated module typings
val npmTypingsDir = layout.buildDirectory.dir("npm-typings")

val prepareNpmTypings by tasks.registering(Copy::class) {
    description = "Copy .d.ts files into package layout: types/ (ambient) and types/generated (generated)"
    dependsOn(tasks.named("kspKotlin"))
    into(npmTypingsDir)
    // Ambient globals at package root types/
    from("src/main/resources/assets/mcui/library") {
        include("**/*.d.ts")
//        into("types")
    }
    // Generated module typings under types/generated/
    from(layout.buildDirectory.dir("generated/ksp/main/resources/typings")) {
        include("**/*.d.ts")
        into("generated")
    }
}

// Generate a minimal package.json for the typings-only npm package
val writeNpmTypingsPackageJson by tasks.registering {
    dependsOn(prepareNpmTypings)
    doLast {
        val pkgDir = npmTypingsDir.get().asFile
        pkgDir.mkdirs()
        val pkgJson = file(pkgDir.resolve("package.json"))
        val name = "@mcui/types"
        val versionStr = project.version.toString()
        val content = """
            {
              "name": "$name",
              "version": "$versionStr",
              "private": false,
              "description": "TypeScript typings for MCUI (ambient globals and generated API)",
              "license": "MIT",
              "types": "index.d.ts",
              "files": ["**/*.d.ts"],
              "keywords": ["typescript", "lua", "tstl", "mcui"]
            }
        """.trimIndent()
        pkgJson.writeText(content)
    }
}

val writeNpmTypingsIndex by tasks.registering {
    description =
        "Create an index.d.ts that references ambient globals so they become visible when users add the package in tsconfig `types`"
    dependsOn(prepareNpmTypings)
    doLast {
        val indexFile = npmTypingsDir.get().asFile.resolve("index.d.ts")
        // TODO : this should not be hardcoded
        val content = """
            /// <reference path="./support.d.ts" />
            /// <reference path="./settings.d.ts" />
            /// <reference path="./theme.d.ts" />
            // Generated module typings are available under the subpath "generated".
            // Example: import type { Setting } from "@mcui/types/generated/Setting";
        """.trimIndent()
        indexFile.writeText(content)
    }
}

val assembleNpmTypings by tasks.registering {
    description = "Aggregate task to assemble the npm package folder"
    dependsOn(prepareNpmTypings, writeNpmTypingsPackageJson, writeNpmTypingsIndex)
}

val pnpmPackTypings by tasks.registering(Exec::class) {
    description = "Produce a .tgz using pnpm pack so it can be distributed without Gradle consumers"
    dependsOn(assembleNpmTypings)
    workingDir = npmTypingsDir.get().asFile
    // If pnpm is not available, run with: ./gradlew :common:assembleNpmTypings and then `npm pack` inside build/npm-typings
    commandLine = listOf("pnpm", "pack")
}
