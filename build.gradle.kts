plugins {
	id("fabric-loom") version "1.15.5"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("modName"))
group = p("modGroupId")
version = "${p("mcVersion")}-${p("modVersion")}-${p("loaderCap")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
tasks.jar { from("LICENSE") }
var generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	val values = properties.mapValues { it.value.toString() }
	inputs.properties(values)
	expand(values)
	from("src/main/templates")
	into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateMetadata)
configurations.configureEach { resolutionStrategy.force("net.fabricmc:fabric-loader:${p("fabricLoaderVersion")}") }
loom {
	enableTransitiveAccessWideners = true
	runConfigs.configureEach { ideConfigGenerated(false) }
	runs {
		configureEach { vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition") }
		remove(getByName("server"))
	}
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.parchmentmc.org") // Parchment mappings
	maven("https://maven.shedaniel.me") // Cloth Config API, REI
	maven("https://maven.terraformersmc.com/releases") // Mod Menu
	maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
}
dependencies {
	minecraft("com.mojang:minecraft:${p("mcVersion")}")
	@Suppress("UnstableApiUsage") mappings(loom.layered {
		officialMojangMappings { nameSyntheticMembers = false }
		parchment("org.parchmentmc.data:parchment-${p("mcVersion")}:${p("parchmentVersion")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${p("fabricLoaderVersion")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${p("fabricApiVersion")}+${p("mcVersion")}")
	modImplementation("maven.modrinth:create-fly:${p("mcVersion")}-${p("createVersion")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("clothConfigVersion")}")
	modImplementation("com.terraformersmc:modmenu:${p("modmenuVersion")}")
}
publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(BETA)
	version.set(project.version.toString())
	displayName.set("[${p("loaderCap")}] ${p("modVersion")} for Create ${p("mcVersion")}-${p("createMinVersion")}")
	modLoaders.addAll(p("loaderCap"), p("loaderOtherCap"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		requires("create-fly", "cloth-config", "modmenu")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		requires("create-fly", "cloth-config", "modmenu")
	}
}
fun p(key: String) = property(key).toString()
