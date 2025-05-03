@file:Suppress("SpellCheckingInspection")

plugins {
	idea
	id("net.neoforged.moddev") version "2.0.82"
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}
fun e(key: String) = extra[key].toString()
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.createmod.net") // Create, Ponder, Flywheel
	maven("https://mvn.devos.one/snapshots") // Registrate
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	implementation("com.simibubi.create:create-${e("minecraft_version")}:${e("create_version")}:slim") {isTransitive = false}
	implementation("net.createmod.ponder:Ponder-${e("upper_loader")}-${e("minecraft_version")}:${e("ponder_version")}") {isTransitive = false}
	implementation("dev.engine-room.flywheel:flywheel-${e("loader")}-${e("minecraft_version")}:${e("flywheel_version")}")
	implementation("com.tterrag.registrate:Registrate:${e("registrate_version")}")
	implementation("me.shedaniel.cloth:cloth-config-${e("loader")}:${e("cloth_config_version")}")
	implementation("mezz.jei:jei-${e("minecraft_version")}-${e("loader")}:${e("jei_version")}")
}
val generateModMetadata = tasks.register<ProcessResources>("generateModMetadata") {
	val replace = properties.mapValues { it.value.toString() }
	inputs.properties(replace); expand(replace); from("src/main/templates"); into("build/generated/sources/modMetadata")
}
val copyIcon = tasks.register("copyIcon") {
	if(file(".idea").exists() && !file(".idea/icon.png").exists()) copy { from("src/main/resources/icon.png"); into(".idea") }
}
base.archivesName.set(e("mod_id"))
group = e("mod_group_id")
version = "${e("minecraft_version")}-${e("mod_version")}+${e("upper_loader")}"
java.withSourcesJar()
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
tasks.jar { from("LICENSE") }
idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}
sourceSets { named("main") { resources.srcDir(generateModMetadata) } }
neoForge {
	version = e("neo_version")
	parchment {
		mappingsVersion.set(e("parchment_version"))
		minecraftVersion.set(e("minecraft_version"))
	}
	runs {
		create("client") { client() }
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
		}
	}
	mods { create(e("mod_id")) { sourceSet(sourceSets["main"]) } }
	ideSyncTasks.addAll(generateModMetadata, copyIcon)
}
publishMods {
	file.set(tasks.jar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${e("upper_loader")}] ${e("mod_name")} ${e("mod_version")}+${e("minecraft_version")}")
	modLoaders.addAll(e("upper_loader"))
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
