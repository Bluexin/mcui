@file:Suppress("LocalVariableName")

import net.fabricmc.loom.api.LoomGradleExtensionAPI
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.KotlinPluginWrapper
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile
import org.jetbrains.kotlinx.serialization.gradle.SerializationGradleSubplugin

plugins {
    `java-library`
    `maven-publish`
    alias(libs.plugins.kotlin)
    alias(libs.plugins.kotlin.serialization) apply false
    alias(libs.plugins.architectury)
    alias(libs.plugins.architectury.loom) apply false
    alias(libs.plugins.ksp)// apply false
}

kotlin {
    jvmToolchain(17)
    explicitApiWarning()
}

ksp {
    arg("KOIN_CONFIG_CHECK", "true")
}

repositories(RepositoryHandler::mavenCentral)

val mcVersion = libs.versions.minecraft.get()

architectury {
    minecraft = mcVersion
}

subprojects {
    apply<JavaPlugin>()
    apply<MavenPublishPlugin>()
    apply<KotlinPluginWrapper>()
    apply<SerializationGradleSubplugin>()
    apply(plugin = "architectury-plugin")
    apply(plugin = "maven-publish")
    apply(plugin = "org.jetbrains.kotlin.jvm")
    apply(plugin = "dev.architectury.loom")
    apply(plugin = "com.google.devtools.ksp")

    val loom = extensions.getByName<LoomGradleExtensionAPI>("loom")
    loom.silentMojangMappingsLicense()

    dependencies {
        "minecraft"(rootProject.libs.minecraft)
        "mappings"(
            loom.layered {
                officialMojangMappings()
                parchment("org.parchmentmc.data:parchment-$mcVersion:${rootProject.libs.versions.parchment.get()}")
            }
        )

        val libs = rootProject.libs
        implementation(libs.kotlin.reflect)
        implementation(libs.bundles.coroutines) {
            exclude(group = "org.jetbrains.kotlin")
        }
        implementation(libs.bundles.serialization)
        implementation(libs.bundles.serialization.xml) {
            exclude(group = "org.jetbrains.kotlin")
            exclude(group = "org.jetbrains.kotlinx")
        }

        implementation(libs.jel)
        implementation(libs.bundles.phcss)
        implementation(libs.slf4j)

//        implementation(libs.luna)

//        implementation(group = "none", name = "OC-LuaJ", version = "20220907.1", ext = "jar")
//        implementation(group = "none", name = "OC-JNLua", version = "20230530.0", ext = "jar")
//        implementation(group = "none", name = "OC-JNLua-Natives", version = "20220928.1", ext = "jar")

        implementation(libs.bundles.luaj)
        implementation(libs.luajksp.annotations)
        ksp(libs.luajksp.processor)

        implementation(libs.bundles.koin)
        ksp(libs.koin.processor)
    }



    java {
        toolchain.languageVersion.set(JavaLanguageVersion.of(17))
        withSourcesJar()
    }

    kotlin {
        jvmToolchain(17)
//        explicitApiWarning() TODO :enable this later or for an API module

        sourceSets.main {
            resources.srcDir("build/generated/ksp/main/resources")
        }
        sourceSets.test {
            resources.srcDir("build/generated/ksp/test/resources")
        }
    }

    tasks.withType<ProcessResources> {
        duplicatesStrategy = DuplicatesStrategy.WARN
    }

    base.archivesName.set(property("mod_id").toString())
    version = property("version").toString()
    group = property("group").toString()

    repositories {
        mavenCentral()

        maven {
            name = "Sponge / Mixin"
            url = uri("https://repo.spongepowered.org/repository/maven-public/")
        }

        maven {
            name = "BlameJared Maven (CrT / Bookshelf)"
            url = uri("https://maven.blamejared.com")
        }
        /*maven {
            name = "Tencao Maven"
            url = uri("https://maven.tencao.com/repository/releases/")
        }*/
        maven {
            name = "Bluexin"
            url = uri("https://maven.bluexin.be/repository/releases/")
        }
        maven {
            name = "Sonatype OSSRH (Snapshots)"
            url = uri("https://oss.sonatype.org/content/repositories/snapshots/")
        }
        ivy {
            // Pulled from https://github.com/MightyPirates/OpenComputers/blob/1c0dc67182292895495cb0d421ec0f529d243d74/build.gradle
            name = "asie dependency mirror"
            artifactPattern("https://asie.pl/javadeps/[module]-[revision](-[classifier]).[ext]")
            metadataSources.artifact()
        }
        maven {
            name = "JitPack"
            url = uri("https://jitpack.io")
        }
        maven {
            name = "https://github.com/pdvrieze/xmlutil"
            url = uri("https://s01.oss.sonatype.org/content/repositories/releases/")
        }
        maven {
            url = uri("https://maven.saps.dev/releases")
            content {
                includeGroup("dev.latvian.mods")
                includeGroup("dev.ftb.mods")
            }
        }
        maven {
            name = "Modrinth"
            url = uri("https://api.modrinth.com/maven")
            content {
                includeGroup("maven.modrinth")
            }
        }
        maven {
            name = "Bluexin GH Packages"
            url = uri("https://maven.pkg.github.com/bluexin/luaj-ksp")
            credentials {
                username = project.findProperty("gpr.user") as String? ?: System.getenv("USERNAME")
                password = project.findProperty("gpr.key") as String? ?: System.getenv("TOKEN")
            }
        }
        mavenLocal()
    }

    tasks {
        val mod_name: String by project


        /*named<Jar>("sourcesJar") {
            from(rootProject.file("LICENSE")) {
                rename { "${it}_${mod_name}" }
            }
        }*/
        withType<KotlinCompile>().all {
            compilerOptions {
                javaParameters.set(true)
                jvmTarget.set(JvmTarget.JVM_17)
                apiVersion.set(KotlinVersion.KOTLIN_1_9)
                languageVersion.set(KotlinVersion.KOTLIN_1_9)
            }
        }
        /*withType<GenerateModuleMetadata>().all {
            enabled = false
        }*/
        withType<JavaCompile>().all {
            options.encoding = "UTF-8"
            options.release.set(17)
        }
    }

    /*publishing {
        publications {
            register<MavenPublication>("mavenJava") {
                groupId = group.toString()
                artifactId = archivesName.get()
                version = version as String
                artifact(tasks.named("jar"))
                from(components["java"])
            }
        }

        repositories {
            maven {
                url = if (hasProperty("local_maven")) uri("file://${property("local_maven")}")
                else uri("file://$buildDir/.m2")
            }
        }
    }*/
}
