import org.gradle.kotlin.dsl.invoke

plugins {
    `java-library`
    id("com.gradleup.shadow") version "9.0.0-beta11"
    id("re.alwyn974.groupez.repository") version "1.0.0"
}

group = "fr.maxlego08.shop"
version = "3.3.1"

extra.set("targetFolder", file("target/"))
extra.set("apiFolder", file("target-api/"))
extra.set("classifier", System.getProperty("archive.classifier"))
extra.set("sha", System.getProperty("github.sha"))

allprojects {
    apply(plugin = "java-library")
    apply(plugin = "com.gradleup.shadow")
    apply(plugin = "re.alwyn974.groupez.repository")

    group = "fr.maxlego08.shop"
    version = rootProject.version

    repositories {
        mavenLocal()
        mavenCentral()

        maven(url = "https://jitpack.io")
        maven(url = "https://hub.spigotmc.org/nexus/content/repositories/snapshots/")
        maven(url = "https://repo.extendedclip.com/content/repositories/placeholderapi/")
        maven(url = "https://libraries.minecraft.net/")
        maven(url = "https://repo.groupez.dev/releases")
    }

    java {
        withSourcesJar()
        withJavadocJar()
    }

    tasks.shadowJar {

        archiveBaseName.set("zShop")
        archiveAppendix.set(if (project.path == ":") "" else project.name)
        archiveClassifier.set("")
    }

    tasks.compileJava {
        options.encoding = "UTF-8"
        options.release = 21
    }

    tasks.javadoc {
        options.encoding = "UTF-8"
        if (JavaVersion.current().isJava9Compatible)
            (options as StandardJavadocDocletOptions).addBooleanOption("html5", true)
    }

    dependencies {
        compileOnly("org.spigotmc:spigot-api:1.21.5-R0.1-SNAPSHOT")
        compileOnly("me.clip:placeholderapi:2.11.6")
        compileOnly("fr.maxlego08.menu:zmenu-api:1.1.0.8")

    }
}

repositories {
    maven(url = "https://repo.codemc.io/repository/maven-public/")
}

dependencies {
    api(projects.api)
    // api(projects.hooks)

}

tasks {
    shadowJar {

        rootProject.extra.properties["sha"]?.let { sha ->
            archiveClassifier.set("${rootProject.extra.properties["classifier"]}-${sha}")
        } ?: run {
            archiveClassifier.set(rootProject.extra.properties["classifier"] as String?)
        }
        destinationDirectory.set(rootProject.extra["targetFolder"] as File)
    }

    build {
        dependsOn(shadowJar)
    }

    processResources {
        from("resources")
        filesMatching("plugin.yml") {
            expand("version" to project.version)
        }
    }
}
