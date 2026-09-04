plugins {
	id("fabric-loom") version "1.17.7"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("modName"))
group = p("modGroupId")
version = "${p("mcVersion")}-${p("modVersion")}-${p("loaderCap")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
java.withSourcesJar()
tasks.jar { from("LICENSE") }
val generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	description = "Generate this project metadata from templates."
	val values = project.extra.properties.mapValues { it.value.toString() }
	inputs.properties(values)
	expand(values)
	from("src/main/templates")
	into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateMetadata)
loom {
	enableTransitiveAccessWideners = true
	runs.configureEach { jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition") }
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.parchmentmc.org") // Parchment mappings
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
	modImplementation("com.terraformersmc:modmenu:${p("modmenuVersion")}")
	modImplementation("com.electronwill.night-config:core:${p("nightConfigVersion")}")
	modImplementation("com.electronwill.night-config:toml:${p("nightConfigVersion")}")
	include("com.electronwill.night-config:core:${p("nightConfigVersion")}")
	include("com.electronwill.night-config:toml:${p("nightConfigVersion")}")
}
publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("loaderCap")}] ${p("modVersion")} for Create ${p("mcVersion")}-${p("createMinVersion")}")
	modLoaders.addAll(p("loaderCap"), p("loaderOtherCap"))
	modrinth {
		additionalFile(tasks.named<Jar>("sourcesJar")) { type.set(SOURCES_JAR) }
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		environment.set(CLIENT_ONLY_SERVER_OPTIONAL)
		requires("create-fly")
		optional("modmenu")
	}
	curseforge {
		additionalFiles.from(tasks.named<Jar>("sourcesJar"))
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		client.set(true)
		requires("create-fly")
		optional("modmenu")
	}
}
fun p(key: String) = property(key).toString()
println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
