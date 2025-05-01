import net.minecrell.pluginyml.bukkit.BukkitPluginDescription
import net.minecrell.pluginyml.paper.PaperPluginDescription

plugins {
    java
    alias(libs.plugins.pluginYmlPaper)
}

group = "uk.co.notnull"
version = "1.1-SNAPSHOT"
description = "LightBlocks"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(21)
    }
}

repositories {
	mavenLocal()
	mavenCentral()
	maven {
		url = uri("https://repo.papermc.io/repository/maven-public/")
	}
    maven {
        url = uri("https://maven.enginehub.org/repo/")
    }
}

dependencies {
	compileOnly(libs.paperApi)
	compileOnly(libs.griefPrevention)
	compileOnly(libs.worldguard)
}

paper {
    main = "uk.co.notnull.lightblocks.LightBlocks"
    apiVersion = libs.versions.paperApi.get().replace(Regex("\\-R\\d.\\d-SNAPSHOT"), "")
    authors = listOf("Jim (AnEnragedPigeon)")
    description = "QoL features for Light block use"

    permissions {
        register("light.give") {
            description = "Allows players to give themselves a light block"
            default = BukkitPluginDescription.Permission.Default.OP
        }
    }

    serverDependencies {
        register("WorldGuard") {
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
            required = false
        }
        register("GriefPrevention") {
            load = PaperPluginDescription.RelativeLoadOrder.AFTER
            required = false
        }
    }
}

tasks {
    compileJava {
        options.compilerArgs.addAll(listOf("-Xlint:all", "-Xlint:-processing"))
        options.encoding = "UTF-8"
    }
}
