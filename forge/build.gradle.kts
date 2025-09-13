//@file:Suppress("PropertyName", "UnstableApiUsage")

plugins {
    alias(libs.plugins.shadow)
}

architectury {
    platformSetupLoomIde()
    forge()
}

val modId: String = rootProject.property("mod_id").toString()
loom {
    accessWidenerPath.set(project(":common").loom.accessWidenerPath)

    forge {
        convertAccessWideners.set(true)
        extraAccessWideners.add(loom.accessWidenerPath.get().asFile.name)
    }
}

sourceSets {
    main {
        resources {
            srcDir("src/generated/resources")
        }
    }
}

/**
 * @see: https://docs.gradle.org/current/userguide/migrating_from_groovy_to_kotlin_dsl.html
 * */
val common: Configuration by configurations.creating
val shadowCommon: Configuration by configurations.creating // Don't use shadow from the shadow plugin because we don't want IDEA to index this.

configurations {
    compileClasspath {
        extendsFrom(common)
    }
    runtimeClasspath {
        extendsFrom(common)
    }
    named("developmentForge") {
        extendsFrom(common)
    }
    forgeRuntimeLibrary {
        extendsFrom(shadow.get())
    }
}

repositories {
    maven {
        name = "Kotlin for Forge"
        url = uri("https://thedarkcolour.github.io/KotlinForForge/")
    }
}

dependencies {
    forge(libs.forge)
//    modImplementation(libs.architectury.forge)
    common(project(":common", configuration = "namedElements")) { isTransitive = false }
    shadowCommon(project(":common", configuration = "transformProductionForge")) { isTransitive = false }
    implementation(libs.kotlinforforge)

    shadow(libs.jel)
    shadow(libs.bundles.phcss)
    shadow(libs.slf4j)

//        shadow(libs.luna)

//        shadow(group = "none", name = "OC-LuaJ", version = "20220907.1", ext = "jar")
    shadow(group = "none", name = "OC-JNLua", version = "20230530.0", ext = "jar")
//        shadow(group = "none", name = "OC-JNLua-Natives", version = "20220928.1", ext = "jar")

    shadow(libs.bundles.luaj)

    shadow(libs.bundles.serialization) {
        isTransitive = false
    }
    forgeRuntimeLibrary(libs.bundles.serialization) { // should be provided by kff, but oh well
//        isTransitive = false
    }
    forgeRuntimeLibrary(libs.kotlin.reflect)
    shadow(libs.bundles.serialization.xml) {
        isTransitive = false
    }

    /*modApi(libs.bundles.ftb.forge) {
        isTransitive = false
    }*/

//    implementation(group = "none", name = "OC-LuaJ", version = "20220907.1", ext = "jar")
//    implementation(group = "none", name = "OC-JNLua", version = "20230530.0", ext = "jar")
//    jarJar(group = "none", name = "OC-JNLua-Natives", version = "20220928.1", ext = "jar")
    /*minecraftEmbed(group = "none", name = "OC-JNLua-Natives", version = "20220928.1"*//*, ext = "jar"*//*) {
        jarJar.ranged(this, "[20220928.1,20240101.0)")
    }*/
//    modApi("dev.ftb.mods:ftb-library-forge:${property("ftb_library_version")}") { isTransitive = false }
//    modApi("dev.ftb.mods:ftb-teams-fabric:${property("ftb_teams_version")}") { isTransitive = false }

    /*minecraftEmbed("com.github.wagyourtail.luaj:luaj-jse:05e2b7d76a") {
        jarJar.ranged(this, "05e2b7d76a")
    }

    // TODO : take out what we don't actually need
    minecraftEmbed("be.bluexin.jel:gnu-jel:2.1.3") {
        jarJar.ranged(this, "[2.1,3)")
    }
    minecraftEmbed("com.helger:ph-css:6.5.0") {
        exclude(group = "com.google.code.findbugs")
        jarJar.ranged(this, "[6.5,7)")
    }
    minecraftEmbed("com.helger.commons:ph-commons:10.1.6") {
        exclude(group = "com.google.code.findbugs")
        jarJar.ranged(this, "[10.1,11)")
    }
    minecraftEmbed("org.slf4j:slf4j-api:1.7.36") { // is this one really necessary ?
        jarJar.ranged(this, "[1.7,2)")
    }
    val serialization_xml_version: String by project
    // TODO : maybe the next two can be changed to range
    minecraftEmbed("io.github.pdvrieze.xmlutil:core-jvm:$serialization_xml_version") {
        jarJar.pin(this, serialization_xml_version)
    }
    minecraftEmbed("io.github.pdvrieze.xmlutil:core-jvm:$serialization_xml_version") {
        jarJar.pin(this, serialization_xml_version)
    }*/
}

components.named<AdhocComponentWithVariants>("java") {
    withVariantsFromConfiguration(configurations["shadowRuntimeElements"]) {
        skip()
    }
}

tasks {
    runClient {
        classpath += project.files(File(projectDir, "build/classes/kotlin/main"))
    }
    runServer {
        classpath += project.files(File(projectDir, "build/classes/kotlin/main"))
    }

    processResources {
        inputs.property("version", project.version)
        duplicatesStrategy = DuplicatesStrategy.INCLUDE

        filesMatching("META-INF/mods.toml") {
            expand("version" to project.version)
        }
    }

    shadowJar {
        exclude("fabric.mod.json")
        exclude("architectury.common.json")
        configurations = listOf(project.configurations["shadowCommon"])
        archiveClassifier.set("dev-shadow")
    }

    remapJar {
        inputFile.set(shadowJar.flatMap { it.archiveFile })
        dependsOn(shadowJar)
        archiveClassifier.set("forge")
    }

    jar {
        archiveClassifier.set("dev")
    }

    sourcesJar {
        val commonSources = project(":common").tasks.getByName<Jar>("sourcesJar")
        dependsOn(commonSources)
        from(commonSources.archiveFile.map { zipTree(it) })
        duplicatesStrategy = DuplicatesStrategy.INCLUDE
    }

    publishing {
        publications {
            create<MavenPublication>("mavenForge") {
                artifactId = "${rootProject.property("group")}-${project.name}"
//                from(javaComponent)
            }
        }

        // See https://docs.gradle.org/current/userguide/publishing_maven.html for information on how to set up publishing.
        repositories {
            // Add repositories to publish to here.
        }
    }
}
