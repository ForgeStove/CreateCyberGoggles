plugins {
	id("net.neoforged.moddev") version "+"
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
val mixinAgent = "mixinAgent"
configurations {
	create(mixinAgent) {
		isCanBeConsumed = false
		isCanBeResolved = true
		defaultDependencies { add(dependencyFactory.create("dev.vfyjxf:mixin-hotswap-agent:1.1").setTransitive(false)) }
	}
}
neoForge {
	version = p("loaderVersion")
	parchment {
		mappingsVersion.set(p("parchmentVersion"))
		minecraftVersion.set(p("mcVersion"))
	}
	runs {
		create("client").client()
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
			val files = configurations[mixinAgent].files
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
	maven("https://maven.blamejared.com") // JEI
	maven("https://jitpack.io") // Jitpack
	maven("https://api.modrinth.com/maven") { content { includeGroup("maven.modrinth") } } // Modrinth
}
dependencies {
	implementation("com.simibubi.create:create-${p("mcVersion")}:${p("createVersion")}:slim") { isTransitive = false }
	implementation("dev.engine-room.flywheel:flywheel-${p("loader")}-${p("mcVersion")}:${p("flywheelVersion")}")
	implementation("net.createmod.ponder:ponder-${p("loader")}:${p("ponderVersion")}+mc${p("mcVersion")}") { isTransitive = false }
	implementation("com.tterrag.registrate:Registrate:${p("registrateVersion")}")
	implementation("mezz.jei:jei-${p("mcVersion")}-${p("loader")}:${p("jeiVersion")}")
	runtimeOnly("com.github.Snownee:Jade:${p("loader")}-${p("jadeVersion")}")
	compileOnly("maven.modrinth:create-enchantment-industry:${p("ceiVersion")}")
	compileOnly("maven.modrinth:create-dragons-plus:${p("dragonPlusVersion")}")
	add("additionalRuntimeClasspath", "dev.vfyjxf:mixin-hotswap-agent:1.1")
}
publishMods {
	file.set(tasks.jar.get().archiveFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("loaderCap")}] ${p("modVersion")} for Create ${p("mcVersion")}-${p("createMinVersion")}")
	modLoaders.addAll(p("loaderCap"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		requires("create")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		requires("create")
	}
}
fun p(key: String) = property(key).toString()
