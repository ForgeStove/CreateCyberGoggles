plugins {
	id("fabric-loom") version "1.11.8"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("mod_id"))
group = p("mod_group_id")
version = "${p("minecraft_version")}-${p("mod_version")}+${p("upper_loader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
tasks.jar { from("LICENSE") }
var generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	expand(properties.mapValues { it.value.toString() })
	from("src/main/templates")
	into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateMetadata)
configurations.configureEach { resolutionStrategy.force("net.fabricmc:fabric-loader:${p("fabric_loader_version")}") }
loom {
	enableTransitiveAccessWideners = true
	accessWidenerPath.set(file("src/main/resources/${p("mod_id")}.accesswidener"))
	runConfigs.configureEach { ideConfigGenerated(false) }
	@Suppress("UnstableApiUsage") mixin.defaultRefmapName.set("${p("mod_id")}.refmap.json")
	runs {
		configureEach {
			vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
		}
		remove(getByName("server"))
	}
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.parchmentmc.org") // Parchment mappings
	maven("https://maven.shedaniel.me") // Cloth Config API, REI
	maven("https://jitpack.io") // Create Fly
	maven("https://maven.terraformersmc.com/releases") // Mod Menu
}
dependencies {
	minecraft("com.mojang:minecraft:${p("minecraft_version")}")
	@Suppress("UnstableApiUsage") mappings(loom.layered {
		officialMojangMappings { nameSyntheticMembers = false }
		parchment("org.parchmentmc.data:parchment-${p("minecraft_version")}:${p("parchment_version")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${p("fabric_loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${p("fabric_api_version")}")
	modImplementation("com.github.ZurrTum:Create-Fly:${p("create_version")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("cloth_config_version")}")
	modImplementation("com.terraformersmc:modmenu:${p("modmenu_version")}")
	modRuntimeOnly("me.shedaniel:RoughlyEnoughItems-${p("loader")}:${p("rei_version")}")
}
publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(BETA)
	version.set(project.version.toString())
	displayName.set("[${p("upper_loader")}] ${p("mod_name")} ${p("mod_version")}+${p("minecraft_version")}")
	modLoaders.addAll(p("upper_loader"), p("other_loader"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("minecraft_version"))
		requires("create-fly", "cloth-config")
		optional("modmenu")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("minecraft_version"))
		requires("create-fly", "cloth-config")
		optional("modmenu")
	}
}
fun p(key: String) = property(key).toString()
