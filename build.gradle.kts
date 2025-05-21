@file:Suppress("SpellCheckingInspection")

plugins {
	idea
	id("net.neoforged.moddev") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
	id("dev.vfyjxf.modaccessor") version "+"
}
base.archivesName.set(e("mod_id"))
group = e("mod_group_id")
version = "${e("minecraft_version")}-${e("mod_version")}+${e("upper_loader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(e("javaVersion")))
idea.module {
	isDownloadSources = true
	isDownloadJavadoc = true
}
tasks.jar { from("LICENSE") }
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
	accessTransformerFiles = project.files("src/main/resources/META-INF/modaccessor.cfg")
}
neoForge {
	version = e("loader_version")
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
	maven("https://mvn.devos.one/snapshots") // Registrate
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	add("accessCompileOnly", "com.simibubi.create:create-${e("minecraft_version")}:${e("create_version")}:slim") { isTransitive = false }
	runtimeOnly("com.simibubi.create:create-${e("minecraft_version")}:${e("create_version")}:slim") { isTransitive = false }
	implementation("net.createmod.ponder:Ponder-${e("upper_loader")}-${e("minecraft_version")}:${e("ponder_version")}") {
		isTransitive = false
	}
	implementation("dev.engine-room.flywheel:flywheel-${e("loader")}-${e("minecraft_version")}:${e("flywheel_version")}")
	implementation("com.tterrag.registrate:Registrate:${e("registrate_version")}")
	implementation("me.shedaniel.cloth:cloth-config-${e("loader")}:${e("cloth_config_version")}")
	implementation("mezz.jei:jei-${e("minecraft_version")}-${e("loader")}:${e("jei_version")}")
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
fun e(key: String) = extra[key].toString()
