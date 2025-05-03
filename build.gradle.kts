@file:Suppress("SpellCheckingInspection", "UnstableApiUsage")

plugins {
	idea
	id("fabric-loom") version "1.11.0-alpha.19"
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}
fun e(key: String) = extra[key].toString()
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.parchmentmc.org") // Parchment mappings
	maven("https://mvn.devos.one/releases") // Porting Lib releases
	maven("https://mvn.devos.one/snapshots") // Create and several dependencies
	maven("https://maven.tterrag.com") // Flywheel
	maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
	maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven") // Forge Config API Port
	maven("https://jitpack.io") // Fabric ASM for Porting Lib
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.terraformersmc.com/releases") // Mod Menu
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	minecraft("com.mojang:minecraft:${e("minecraft_version")}")
	mappings(loom.layered {
		officialMojangMappings { nameSyntheticMembers = false }
		parchment("org.parchmentmc.data:parchment-${e("minecraft_version")}:${e("parchment_version")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${e("fabric_loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${e("fabric_api_version")}")
	modImplementation("com.simibubi.create:create-${e("loader")}-${e("minecraft_version")}:${e("create_version")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${e("loader")}:${e("cloth_config_version")}")
	modImplementation("com.terraformersmc:modmenu:${e("modmenu_version")}")
	modImplementation("mezz.jei:jei-${e("minecraft_version")}-${e("loader")}:${e("jei_version")}")
}
tasks.processResources {
	val replace = properties.mapValues { it.value.toString() }
	inputs.properties(replace)
	from("src/main/resources") {
		include("*.json")
		expand(replace)
	}
	into("build/resources/main")
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
base.archivesName.set(e("mod_id"))
group = e("mod_group_id")
version = "${e("minecraft_version")}-${e("mod_version")}+${e("upper_loader")}"
java.withSourcesJar()
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.jar { from("LICENSE") }
configurations.configureEach { resolutionStrategy { force("net.fabricmc:fabric-loader:${e("fabric_loader_version")}") } }
idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}
tasks.ideaSyncTask {
	if(file(".idea").exists() && !file(".idea/icon.png").exists()) copy {
		from("src/main/resources/icon.png")
		into(".idea")
	}
}
loom { accessWidenerPath.set(file("src/main/resources/${e("mod_id")}.accessWidener")) }
publishMods {
	file.set(file("build/libs/${e("mod_id")}-${project.version}.jar"))
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${e("upper_loader")}] ${e("mod_name")} ${e("mod_version")}+${e("minecraft_version")}")
	modLoaders.addAll(e("upper_loader"), "Quilt")
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(e("minecraft_version"))
		requires("create", "cloth-config")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(e("minecraft_version"))
		requires("create", "cloth-config")
	}
}
