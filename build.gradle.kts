plugins {
	id("fabric-loom") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("mod_id"))
group = p("mod_group_id")
version = "${p("minecraft_version")}-${p("mod_version")}+${p("upper_loader")}"
//java.toolchain.l anguageVersion.set(JavaLanguageVersion.of(17))
tasks.jar { from("LICENSE") }
var generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	expand(properties.mapValues { it.value.toString() })
	from("src/main/templates")
	into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateMetadata)
configurations.configureEach { resolutionStrategy.force("net.fabricmc:fabric-loader:${p("fabric_loader_version")}") }
loom.accessWidenerPath.set(file("src/main/resources/${p("mod_id")}.accesswidener"))
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.parchmentmc.org") // Parchment mappings
	maven("https://mvn.devos.one/releases") // Porting Lib releases
	maven("https://mvn.devos.one/snapshots") // Create and several dependencies
	maven("https://modmaven.dev") // Flywheel
	maven("https://maven.jamieswhiteshirt.com/libs-release") // Reach Entity Attributes
	maven("https://raw.githubusercontent.com/Fuzss/modresources/main/maven") // Forge Config API Port
	maven("https://jitpack.io") // Fabric ASM for Porting Lib
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.terraformersmc.com/releases") // Mod Menu
	maven("https://maven.blamejared.com") // JEI
}
dependencies {
	minecraft("com.mojang:minecraft:${p("minecraft_version")}")
	@Suppress("UnstableApiUsage") mappings(loom.layered {
		officialMojangMappings { nameSyntheticMembers = false }
		parchment("org.parchmentmc.data:parchment-${p("minecraft_version")}:${p("parchment_version")}@zip")
	})
	modImplementation("net.fabricmc:fabric-loader:${p("fabric_loader_version")}")
	modImplementation("net.fabricmc.fabric-api:fabric-api:${p("fabric_api_version")}")
	modImplementation("com.simibubi.create:create-${p("loader")}-${p("minecraft_version")}:${p("create_version")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("cloth_config_version")}")
	modImplementation("com.terraformersmc:modmenu:${p("modmenu_version")}")
	modImplementation("mezz.jei:jei-${p("minecraft_version")}-${p("loader")}:${p("jei_version")}")
}
publishMods {
	file.set(tasks.remapJar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("upper_loader")}] ${p("mod_name")} ${p("mod_version")}+${p("minecraft_version")}")
	modLoaders.addAll(p("upper_loader"), p("other_loader"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("minecraft_version"))
		requires("create-fabric", "cloth-config")
		optional("modmenu")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("minecraft_version"))
		requires("create-fabric", "cloth-config")
		optional("modmenu")
	}
}
fun p(key: String) = property(key).toString()
