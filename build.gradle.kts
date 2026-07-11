plugins {
	id("net.neoforged.moddev") version "+"
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
configurations.create("mixinAgent") {
	isCanBeConsumed = false
	isCanBeResolved = true
	defaultDependencies { add(dependencyFactory.create("dev.vfyjxf:mixin-hotswap-agent:${p("mixinAgentVersion")}").setTransitive(false)) }
}
neoForge {
	version = p("loaderVersion")
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("mcVersion"))
	}
	runs {
		create("client").client()
		create("server").server()
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
			val files = configurations["mixinAgent"].files
			if(files.isNotEmpty()) jvmArgument("-javaagent:${files.first().toPath()}")
		}
	}
	mods.create(p("modId")).sourceSet(sourceSets.main.get())
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.createmod.net") // Create, Ponder, Flywheel
	maven("https://mvn.devos.one/snapshots") // Registrate
	maven("https://maven.ryanhcode.dev/releases") // Aeronautics
	maven("https://maven.blamejared.com") // JEI, Veil, Ars Nouveau
	maven("https://maven.terraformersmc.com") // EMI
	maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
}
dependencies {
	//region Create
	implementation("com.simibubi.create:create-${p("mcVersion")}:${p("createVersion")}:slim") { isTransitive = false }
	implementation("dev.engine-room.flywheel:flywheel-${p("loader")}-${p("mcVersion")}:${p("flywheelVersion")}")
	implementation("net.createmod.ponder:ponder-${p("loader")}:${p("ponderVersion")}+mc${p("mcVersion")}") { isTransitive = false }
	implementation("com.tterrag.registrate:Registrate:${p("registrateVersion")}")
	//endregion
	//region Aeronautics
	implementation("dev.simulated_team.simulated:simulated-${p("loader")}-${p("mcVersion")}:${p("areoVersion")}") { isTransitive = false }
	implementation("dev.ryanhcode.offroad:offroad-${p("loader")}-${p("mcVersion")}:${p("areoVersion")}") { isTransitive = false }
	implementation("dev.eriksonn.aeronautics:aeronautics-${p("loader")}-${p("mcVersion")}:${p("areoVersion")}") { isTransitive = false }
	implementation("dev.ryanhcode.sable:sable-${p("loader")}-${p("mcVersion")}:${p("sableVersion")}") { isTransitive = false }
	implementation("dev.ryanhcode.sable-companion:sable-companion-common-${p("mcVersion")}:${p("sableCompanionVersion")}") { isTransitive = false }
	implementation("foundry.veil:veil-${p("loader")}-${p("mcVersion")}:${p("veilVersion")}")
	//endregion
	//region Enchantment Industry
	compileOnly("maven.modrinth:create-enchantment-industry:${p("ceiVersion")}")
	compileOnly("maven.modrinth:create-dragons-plus:${p("dragonPlusVersion")}")
	//endregion
	implementation("mezz.jei:jei-${p("mcVersion")}-${p("loader")}:${p("jeiVersion")}")
	compileOnly("dev.emi:emi-${p("loader")}:${p("emiVersion")}+${p("mcVersion")}")
	compileOnly("maven.modrinth:sophisticated-core:${p("mcVersion")}-${p("sophisticatedCoreVersion")}")
	compileOnly("com.hollingsworth.ars_nouveau:ars_nouveau-${p("mcVersion")}:${p("arsNouveauVersion")}") { isTransitive = false }
	compileOnly("org.appliedenergistics:appliedenergistics2:${p("appliedenergisticsVersion")}")
	runtimeOnly("maven.modrinth:jade:${p("jadeVersion")}+${p("loader")}")
	add("additionalRuntimeClasspath", "dev.vfyjxf:mixin-hotswap-agent:${p("mixinAgentVersion")}")
}
publishMods {
	file.set(tasks.jar.get().archiveFile)
	additionalFiles.from(tasks.named<Jar>("sourcesJar").flatMap { it.archiveFile })
	changelog.set(file("CHANGELOG.md").readText())
	type.set(BETA)
	version.set(project.version.toString())
	displayName.set("[${p("loaderCap")}] ${p("modVersion")} for Create ${p("mcVersion")}-${p("createMinVersion")}")
	modLoaders.addAll(p("loaderCap"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		environment.set(CLIENT_ONLY_SERVER_OPTIONAL)
		requires("create")
		optional("create-aeronautics")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		client.set(true)
		requires("create")
		optional("create-aeronautics")
	}
}
fun p(key: String) = property(key).toString()
