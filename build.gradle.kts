plugins {
	id("net.neoforged.moddev.legacyforge") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("modName"))
group = p("modGroupId")
version = "${p("mcVersion")}-${p("modVersion")}-${p("loaderCap")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(p("javaVersion")))
java.withSourcesJar()
tasks.jar {
	from("LICENSE")
	manifest.attributes("MixinConfigs" to "${p("modId")}.mixins.json")
}
val generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	description = "Generate this project metadata from templates."
	val values = project.extra.properties.mapValues { it.value.toString() }
	inputs.properties(values)
	expand(values)
	from("src/main/templates")
	into("build/generated/sources/modMetadata")
}
sourceSets.main.get().resources.srcDir(generateMetadata)
mixin {
	add(sourceSets.main.get(), "${p("modId")}.refmap.json")
	config("${p("modId")}.mixins.json")
}
legacyForge {
	version = "${p("mcVersion")}-${p("loaderVersion")}"
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("mcVersion"))
	}
	runs {
		create("client").client()
		create("server").server()
		configureEach {
			systemProperty("terminal.jline", "true")
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
		}
	}
	mods.create(p("modId")).sourceSet(sourceSets.main.get())
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.createmod.net") // Create, Ponder, Flywheel
	maven("https://maven.tterrag.com") // Registrate
	maven("https://maven.blamejared.com") // JEI
	maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
}
dependencies {
	modImplementation("com.simibubi.create:create-${p("mcVersion")}:${p("createVersion")}:slim") { isTransitive = false }
	modImplementation("net.createmod.ponder:Ponder-${p("loaderCap")}-${p("mcVersion")}:${p("ponderVersion")}")
	modImplementation("dev.engine-room.flywheel:flywheel-${p("loader")}-${p("mcVersion")}:${p("flywheelVersion")}")
	modImplementation("com.tterrag.registrate:Registrate:${p("registrateVersion")}")
	modImplementation("mezz.jei:jei-${p("mcVersion")}-${p("loader")}:${p("jeiVersion")}")
	modRuntimeOnly("maven.modrinth:jade:${p("jadeVersion")}+${p("loader")}")
	annotationProcessor("org.spongepowered:mixin:${p("mixinVersion")}:processor")
	annotationProcessor("io.github.llamalad7:mixinextras-common:${p("mixinExtrasVersion")}")
	compileOnly("io.github.llamalad7:mixinextras-common:${p("mixinExtrasVersion")}")
	runtimeOnly("io.github.llamalad7:mixinextras-${p("loader")}:${p("mixinExtrasVersion")}")
}
publishMods {
	file.set(tasks.named("reobfJar").get().outputs.files.singleFile)
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
		requires("create")
	}
	curseforge {
		additionalFiles.from(tasks.named<Jar>("sourcesJar"))
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		client.set(true)
		requires("create")
	}
}
fun p(key: String) = property(key).toString()
println("Java: ${System.getProperty("java.version")}, JVM: ${System.getProperty("java.vm.version")} (${System.getProperty("java.vendor")}), Arch: ${System.getProperty("os.arch")}")
