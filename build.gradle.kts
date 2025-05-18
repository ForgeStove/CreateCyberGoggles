@file:Suppress("SpellCheckingInspection")

plugins {
	idea
	id("net.neoforged.moddev.legacyforge") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
	id("dev.vfyjxf.modaccessor") version "+"
}
base.archivesName.set(e("mod_id"))
group = e("mod_group_id")
version = "${e("minecraft_version")}-${e("mod_version")}+${e("upper_loader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
idea.module {
	isDownloadSources = true
	isDownloadJavadoc = true
}
tasks.jar {
	from("LICENSE")
	manifest { attributes(mapOf("MixinConfigs" to "${e("mod_id")}.mixins.json")) }
}
tasks.processResources {
	val replace = properties.mapValues { it.value.toString() }
	inputs.properties(replace)
	from("src/main/resources") {
		include("**/*.toml")
		expand(replace)
	}
	into("build/resources/main")
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
modAccessor {
	createTransformConfiguration(configurations.compileOnly.get())
	accessTransformerFiles = legacyForge.accessTransformers.files
}
mixin {
	add(sourceSets.main.get(), "${e("mod_id")}.refmap.json")
	config("${e("mod_id")}.mixins.json")
}
legacyForge {
	version = "${e("minecraft_version")}-${e("loader_version")}"
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
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.createmod.net") // Create, Ponder, Flywheel
	maven("https://maven.tterrag.com") // Registrate
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	add("accessCompileOnly", "com.simibubi.create:create-${e("minecraft_version")}:${e("create_version")}:slim")
	modImplementation("com.simibubi.create:create-${e("minecraft_version")}:${e("create_version")}:slim")
	modImplementation("net.createmod.ponder:Ponder-${e("upper_loader")}-${e("minecraft_version")}:${e("ponder_version")}")
	modImplementation("dev.engine-room.flywheel:flywheel-${e("loader")}-${e("minecraft_version")}:${e("flywheel_version")}")
	modImplementation("com.tterrag.registrate:Registrate:${e("registrate_version")}")
	compileOnly("io.github.llamalad7:mixinextras-common:${e("mixin_extras_version")}")
	implementation("io.github.llamalad7:mixinextras-${e("loader")}:${e("mixin_extras_version")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${e("loader")}:${e("cloth_config_version")}")
	modImplementation("mezz.jei:jei-${e("minecraft_version")}-${e("loader")}:${e("jei_version")}")
	annotationProcessor("org.spongepowered:mixin:${e("mixin_version")}:processor")
	compileOnly("org.jetbrains:annotations:${e("annotations_version")}")
}
publishMods {
	file.set(tasks.named("reobfJar").get().outputs.files.singleFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${e("upper_loader")}] ${e("mod_name")} ${e("mod_version")}+${e("minecraft_version")}")
	modLoaders.addAll(e("upper_loader"), e("other_loader"))
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
fun e(key: String) = extra[key].toString()
