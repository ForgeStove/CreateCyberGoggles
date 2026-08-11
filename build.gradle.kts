plugins {
	id("net.fabricmc.fabric-loom") version "1.17.19"
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
	implementation("net.fabricmc:fabric-loader:${p("fabricLoaderVersion")}")
	implementation("net.fabricmc.fabric-api:fabric-api:${p("fabricApiVersion")}+${p("mcVersion")}")
	implementation("maven.modrinth:create-fly:${p("createVersion")}")
	implementation("com.terraformersmc:modmenu:${p("modmenuVersion")}")
	impleInclude("com.electronwill.night-config:core:${p("nightConfigVersion")}")
	impleInclude("com.electronwill.night-config:toml:${p("nightConfigVersion")}")
}
fun DependencyHandler.impleInclude(dependency: Any) {
	include(dependency)?.let { implementation(it) }
}
publishMods {
	file.set(tasks.jar.get().archiveFile)
	additionalFiles.from(tasks.named<Jar>("sourcesJar"))
	changelog.set(file("CHANGELOG.md").readText())
	type.set(BETA)
	version.set(project.version.toString())
	displayName.set("[${p("loaderCap")}] ${p("modVersion")} for Create ${p("mcVersion")}-${p("createMinVersion")}")
	modLoaders.addAll(p("loaderCap"), p("loaderOtherCap"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		requires("create-fly")
		optional("modmenu")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		client.set(true)
		requires("create-fly")
		optional("modmenu")
	}
}
fun p(key: String) = property(key).toString()
