plugins {
	id("net.neoforged.moddev") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
	id("io.github.forgestove.modaccessor") version "+"
}
base.archivesName.set(p("mod_id"))
group = p("mod_group_id")
version = "${p("minecraft_version")}-${p("mod_version")}+${p("upper_loader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(21))
tasks.jar { from("LICENSE") }
tasks.processResources {
	from(sourceSets.main.get().resources) {
		include("**/*mods.toml")
		expand(properties)
		outputs.upToDateWhen { false }
		duplicatesStrategy = DuplicatesStrategy.INCLUDE
	}
}
neoForge {
	version = p("loader_version")
	parchment {
		mappingsVersion.set(p("parchment_version"))
		minecraftVersion.set(p("minecraft_version"))
	}
	runs {
		create("client").client()
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
		}
	}
	mods.create(p("mod_id")).sourceSet(sourceSets.main.get())
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://maven.createmod.net") // Create, Ponder, Flywheel
	maven("https://mvn.devos.one/snapshots") // Registrate
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
	maven("https://api.modrinth.com/maven") // Modrinth
}
dependencies {
	accessCompileOnly("com.simibubi.create:create-${p("minecraft_version")}:${p("create_version")}:slim")
	runtimeOnly("com.simibubi.create:create-${p("minecraft_version")}:${p("create_version")}:slim") { isTransitive = false }
	implementation("dev.engine-room.flywheel:flywheel-${p("loader")}-${p("minecraft_version")}:${p("flywheel_version")}") {
		isTransitive = false
	}
	implementation("net.createmod.ponder:Ponder-${p("upper_loader")}-${p("minecraft_version")}:${p("ponder_version")}") {
		isTransitive = false
	}
	implementation("com.tterrag.registrate:Registrate:${p("registrate_version")}")
	implementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("cloth_config_version")}")
	implementation("mezz.jei:jei-${p("minecraft_version")}-${p("loader")}:${p("jei_version")}")
	implementation("maven.modrinth:jade:${p("jade_version")}")
}
publishMods {
	file.set(tasks.jar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("upper_loader")}] ${p("mod_name")} ${p("mod_version")}+${p("minecraft_version")}")
	modLoaders.addAll(p("upper_loader"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("minecraft_version"))
		requires("create", "cloth-config")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("minecraft_version"))
		requires("create", "cloth-config")
	}
}
fun p(key: String) = property(key).toString()
