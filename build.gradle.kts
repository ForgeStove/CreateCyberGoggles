plugins {
	id("net.neoforged.moddev") version "2.0.148" // https://plugins.gradle.org/m2/net/neoforged/moddev/net.neoforged.moddev.gradle.plugin/maven-metadata.xml
	id("me.modmuss50.mod-publish-plugin") version "2.2.1" // https://plugins.gradle.org/m2/me/modmuss50/mod-publish-plugin/me.modmuss50.mod-publish-plugin.gradle.plugin/maven-metadata.xml
}
base.archivesName.set(p("modId"))
group = p("modGroupId")
version = "${p("loader")}-${p("mcVersion")}-${p("modVersion")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
java.withSourcesJar()
tasks.jar { from("LICENSE") }
val generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	description = "Generate this project metadata from templates."
	val values = project.extra.properties.mapValues { it.value.toString() }
	inputs.properties(values)
	expand(values)
	from(layout.projectDirectory.dir("src/main/templates"))
	into(layout.buildDirectory.dir("generated/sources/modMetadata"))
}
sourceSets.main.get().resources.srcDir(generateMetadata)
val mixinAgentNotation = "dev.vfyjxf:mixin-hotswap-agent:${p("mixinAgentVersion")}"
val mixinAgent = configurations.create("mixinAgent").defaultDependencies { add(dependencyFactory.create(mixinAgentNotation)) }
neoForge {
	version = p("loaderVersion")
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("mcVersion"))
	}
	runs {
		create("client").client()
		create("server") {
			server()
			gameDirectory.set(file("run/server"))
			programArgument("--nogui")
		}
		configureEach {
			systemProperty("terminal.jline", "true")
			jvmArgument("-XX:+IgnoreUnrecognizedVMOptions")
			jvmArgument("-XX:+AllowEnhancedClassRedefinition")
			jvmArgument("-javaagent:${mixinAgent.files.first().toPath()}")
			// jvmArgument("-XX:-OmitStackTraceInFastThrow") // uncomment when you get exceptions with null messages etc
			// jvmArgument("-XX:+UnlockCommercialFeatures") // uncomment for profiling
		}
	}
	mods.create(p("modId")).sourceSet(sourceSets.main.get())
}
repositories {
	mavenLocal()
	mavenCentral()
	ivy {
		url = uri("https://github.com/ForgeStove/FlexConfig/releases/download")
		patternLayout { artifact("[revision]/[artifact]-[revision](-[classifier]).jar") }
		metadataSources { artifact() }
	}
	maven("https://maven.createmod.net") // Create, Ponder, Flywheel
	maven("https://mvn.devos.one/snapshots") // Registrate
	maven("https://maven.ryanhcode.dev/releases") // Aeronautics
	maven("https://maven.blamejared.com") // JEI, Veil, Ars Nouveau
	maven("https://maven.terraformersmc.com") // EMI
	maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
	maven("https://dl.zznty.ru/maven") // Create Factory Abstractions
}
dependencies {
	implementation("io.github.forgestove:flexconfig:${p("loader")}-${p("mcVersion")}-${p("flexConfigVersion")}")?.let { jarJar(it) }
	//region Create
	implementation("com.simibubi.create:create-${p("mcVersion")}:${p("createVersion")}") { isTransitive = false }
	compileOnly("dev.engine-room.flywheel:flywheel-${p("loader")}-${p("mcVersion")}:${p("flywheelVersion")}")
	compileOnly("net.createmod.ponder:ponder-${p("loader")}:${p("ponderVersion")}+mc${p("mcVersion")}")
	compileOnly("com.tterrag.registrate:Registrate:${p("registrateVersion")}")
	//endregion
	//region Create Aeronautics
	implementation("dev.simulated_team.simulated:simulated-${p("loader")}-${p("mcVersion")}:${p("areoVersion")}") { isTransitive = false }
	implementation("dev.ryanhcode.offroad:offroad-${p("loader")}-${p("mcVersion")}:${p("areoVersion")}") { isTransitive = false }
	implementation("dev.eriksonn.aeronautics:aeronautics-${p("loader")}-${p("mcVersion")}:${p("areoVersion")}") { isTransitive = false }
	implementation("dev.ryanhcode.sable:sable-${p("loader")}-${p("mcVersion")}:${p("sableVersion")}") { isTransitive = false }
	compileOnly("dev.ryanhcode.sable-companion:sable-companion-common-${p("mcVersion")}:${p("sableCompanionVersion")}")
	//endregion
	compileOnly("maven.modrinth:create-enchantment-industry:${p("ceiVersion")}")
	compileOnly("maven.modrinth:create-dragons-plus:${p("dragonPlusVersion")}")
	compileOnly("maven.modrinth:createfluidlogistic:${p("fluidlogisticVersion")}-mc${p("mcVersion")}")
	compileOnly("maven.modrinth:createphantom:${p("phantomVersion")}")
	compileOnly("maven.modrinth:create-mobile-packages:${p("mcVersion")}-${p("mobilePackagesVersion")}")
	compileOnly("ru.zznty:create_factory_abstractions-${p("mcVersion")}:${p("factoryAbstractionsVersion")}") { isTransitive = false }
	implementation("mezz.jei:jei-${p("mcVersion")}-${p("loader")}:${p("jeiVersion")}")
	compileOnly("dev.emi:emi-${p("loader")}:${p("emiVersion")}+${p("mcVersion")}")
	compileOnly("maven.modrinth:sophisticated-core:${p("mcVersion")}-${p("sophisticatedCoreVersion")}")
	compileOnly("org.appliedenergistics:appliedenergistics2:${p("appliedenergisticsVersion")}")
	compileOnly("com.hollingsworth.ars_nouveau:ars_nouveau-${p("mcVersion")}:${p("arsNouveauVersion")}") { isTransitive = false }
	compileOnly("maven.modrinth:thirst-was-reclaimed:${p("mcVersion")}-${p("thirstVersion")}")
	runtimeOnly("maven.modrinth:jade:${p("jadeVersion")}+${p("loader")}")
	compileOnly("com.demonwav.mcdev:annotations:2.1.0")
	add("additionalRuntimeClasspath", mixinAgentNotation)
}
publishMods {
	file.set(tasks.jar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(BETA)
	version.set(project.version.toString())
	displayName.set(version.get() + " for create ${p("createMinVersion")}")
	modLoaders.addAll(p("loader"))
	modrinth {
		additionalFile(tasks.named<Jar>("sourcesJar")) { type.set(SOURCES_JAR) }
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		environment.set(CLIENT_ONLY_SERVER_OPTIONAL)
		requires("create")
		optional("create-aeronautics")
	}
	curseforge {
		additionalFiles.from(tasks.named<Jar>("sourcesJar"))
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		client.set(true)
		requires("create")
		optional("create-aeronautics")
	}
}
fun p(key: String) = extra[key].toString()
println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
