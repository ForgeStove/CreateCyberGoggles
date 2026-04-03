plugins {
	id("fabric-loom") version "1.10.5"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("modName"))
group = p("modGroupId")
version = "${p("mcVersion")}-${p("modVersion")}-${p("loaderCap")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
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
	@Suppress("UnstableApiUsage") mixin.defaultRefmapName.set("${p("modId")}.refmap.json")
	runs {
		configureEach { vmArgs("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition") }
		remove(getByName("server"))
	}
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.parchmentmc.org") // Parchment mappings
	maven("https://mvn.devos.one/releases") // Porting Lib releases
	maven("https://maven.createmod.net") // Porting Lib releases
	maven("https://mvn.devos.one/snapshots") // Create and several dependencies
	maven("https://modmaven.dev") // Flywheel
	maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
	maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven") // Forge Config API Port
	maven("https://jitpack.io") // Fabric ASM for Porting Lib
	maven("https://maven.terraformersmc.com/releases") // Mod Menu
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	minecraft("com.mojang:minecraft:${p("mcVersion")}")
	@Suppress("UnstableApiUsage") mappings(loom.layered {
		officialMojangMappings { nameSyntheticMembers = false }
		parchment("org.parchmentmc.data:parchment-${p("mcVersion")}:${p("parchmentVersion")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${p("fabricLoaderVersion")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${p("fabricApiVersion")}+${p("mcVersion")}")
	modImplementation("com.simibubi.create:create-${p("loader")}:${p("createVersion")}-mc${p("mcVersion")}")
	modImplementation("com.terraformersmc:modmenu:${p("modmenuVersion")}")
	modImplementation("mezz.jei:jei-${p("mcVersion")}-${p("loader")}:${p("jeiVersion")}")
}
publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("loaderCap")}] ${p("modVersion")} for Create ${p("mcVersion")}-${p("createMinVersion")}")
	modLoaders.addAll(p("loaderCap"), p("loaderOtherCap"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		requires("create-fabric")
		optional("modmenu")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		requires("create-fabric")
		optional("modmenu")
	}
}
fun p(key: String) = property(key).toString()
