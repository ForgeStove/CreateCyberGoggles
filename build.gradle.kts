@file:Suppress("SpellCheckingInspection")

plugins {
	idea
	id("net.neoforged.moddev.legacyforge") version "2.0.82"
	id("me.modmuss50.mod-publish-plugin") version "0.8.4"
}
fun e(key: String) = extra[key].toString()
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.createmod.net") // Create, Ponder, Flywheel
	maven("https://maven.tterrag.com") // Registrate
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))
	annotationProcessor("org.spongepowered:mixin:${e("mixin_version")}:processor")
	modImplementation("io.github.llamalad7:mixinextras-${e("loader")}:${e("mixin_extras_version")}")
	modImplementation("com.simibubi.create:create-${e("minecraft_version")}:${e("create_version")}:slim")
	modImplementation("net.createmod.ponder:Ponder-${e("upper_loader")}-${e("minecraft_version")}:${e("ponder_version")}")
	modImplementation("dev.engine-room.flywheel:flywheel-${e("loader")}-${e("minecraft_version")}:${e("flywheel_version")}")
	modImplementation("com.tterrag.registrate:Registrate:${e("registrate_version")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${e("loader")}:${e("cloth_config_version")}")
	modImplementation("mezz.jei:jei-${e("minecraft_version")}-${e("loader")}:${e("jei_version")}")
	compileOnly("org.jetbrains:annotations:${e("annotations_version")}")
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
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.jar {
	from("LICENSE")
	manifest { attributes(mapOf("MixinConfigs" to "${e("mod_id")}.mixins.json")) }
}
mixin {
	add(sourceSets.main.get(), "${e("mod_id")}.refmap.json")
	config("${e("mod_id")}.mixins.json")
}
idea {
	module {
		isDownloadSources = true
		isDownloadJavadoc = true
	}
}
sourceSets { named("main") { resources.srcDir(generateModMetadata) } }
legacyForge {
	version = "${e("minecraft_version")}-${e("forge_version")}"
//	validateAccessTransformers.set(true)
	parchment { mappingsVersion.set(e("parchment_version"));minecraftVersion.set(e("minecraft_version")) }
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
	file.set(file("build/libs/${e("mod_id")}-${project.version}.jar"))
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${e("upper_loader")}] ${e("mod_name")} ${e("mod_version")}+${e("minecraft_version")}")
	modLoaders.addAll(e("loader"), "NeoForge")
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
