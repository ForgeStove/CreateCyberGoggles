plugins {
	id("net.neoforged.moddev.legacyforge") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
}
base.archivesName.set(p("modId"))
group = p("modGroupId")
version = "${p("mcVersion")}-${p("modVersion")}+${p("loaderCap")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.jar {
	from("LICENSE")
	manifest.attributes("MixinConfigs" to "${p("modId")}.mixins.json")
}
var generateMetadata = tasks.register<ProcessResources>("generateMetadata") {
	expand(properties.mapValues { it.value.toString() })
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
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
		}
	}
	mods.create(p("modId")).sourceSet(sourceSets.main.get())
}
repositories {
	mavenLocal()
	mavenCentral()
	maven("https://modmaven.dev") // Create, Flywheel
	maven("https://maven.tterrag.com") // Registrate
	maven("https://maven.shedaniel.me") // Cloth Config API
	maven("https://maven.blamejared.com") // JEI
	maven("https://api.modrinth.com/maven") // Modrinth
}
dependencies {
	modImplementation("com.simibubi.create:create-${p("mcVersion")}:${p("createVersion")}:slim")
	modImplementation("com.jozufozu.flywheel:flywheel-${p("loader")}-${p("mcVersion")}:${p("flywheelVersion")}")
	modImplementation("com.tterrag.registrate:Registrate:${p("registrateVersion")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("clothConfigVersion")}")
	modImplementation("mezz.jei:jei-${p("mcVersion")}-${p("loader")}:${p("jeiVersion")}")
	modImplementation("maven.modrinth:jade:${p("jadeVersion")}")
	annotationProcessor("org.spongepowered:mixin:${p("mixinVersion")}:processor")
	annotationProcessor("io.github.llamalad7:mixinextras-common:${p("mixinExtrasVersion")}")
	compileOnly("io.github.llamalad7:mixinextras-common:${p("mixinExtrasVersion")}")
	runtimeOnly("io.github.llamalad7:mixinextras-${p("loader")}:${p("mixinExtrasVersion")}")
	compileOnly("org.jetbrains:annotations:${p("annotationsVersion")}")
}
publishMods {
	file.set(tasks.named("reobfJar").get().outputs.files.singleFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("loaderCap")}] ${p("modName")} ${p("modVersion")}+${p("mcVersion")}")
	modLoaders.addAll(p("loaderCap"), p("loaderOtherCap"))
	modrinth {
		accessToken.set(providers.environmentVariable("MODRINTH_TOKEN"))
		projectId.set("TlQAWQCY")
		minecraftVersions.add(p("mcVersion"))
		requires {
			id.set("LNytGWDc")
			version.set("6R069CcK")
		}
		requires("cloth-config")
	}
	curseforge {
		accessToken.set(providers.environmentVariable("CURSEFORGE_TOKEN"))
		projectId.set("1233804")
		minecraftVersions.add(p("mcVersion"))
		requires("create", "cloth-config")
	}
}
fun p(key: String) = property(key).toString()
