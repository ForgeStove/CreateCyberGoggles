@file:Suppress("SpellCheckingInspection")

plugins {
	id("net.neoforged.moddev.legacyforge") version "+"
	id("me.modmuss50.mod-publish-plugin") version "+"
	id("io.github.forgestove.modaccessor") version "+"
}
base.archivesName.set(p("mod_id"))
group = p("mod_group_id")
version = "${p("minecraft_version")}-${p("mod_version")}+${p("upper_loader")}"
java.toolchain.languageVersion.set(JavaLanguageVersion.of(17))
tasks.jar {
	from("LICENSE")
	manifest { attributes(mapOf("MixinConfigs" to "${p("mod_id")}.mixins.json")) }
}
tasks.processResources {
	from("src/main/resources") {
		include("**/*.toml")
		expand(properties.mapValues { it.value.toString() })
	}
	duplicatesStrategy = DuplicatesStrategy.INCLUDE
}
mixin {
	add(sourceSets.main.get(), "${p("mod_id")}.refmap.json")
	config("${p("mod_id")}.mixins.json")
}
legacyForge {
	version = "${p("minecraft_version")}-${p("loader_version")}"
	parchment {
		mappingsVersion.set(p("parchment_version"))
		minecraftVersion.set(p("minecraft_version"))
	}
	runs {
		create("client") { client() }
		configureEach {
			jvmArguments.addAll("-XX:+IgnoreUnrecognizedVMOptions", "-XX:+AllowEnhancedClassRedefinition")
			systemProperty("terminal.jline", "true")
		}
	}
	mods { create(p("mod_id")) { sourceSet(sourceSets["main"]) } }
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
	accessCompileOnly("com.simibubi.create:create-${p("minecraft_version")}:${p("create_version")}:all")
	modImplementation("com.simibubi.create:create-${p("minecraft_version")}:${p("create_version")}:all")
	modImplementation("com.jozufozu.flywheel:flywheel-${p("loader")}-${p("minecraft_version")}:${p("flywheel_version")}")
	modImplementation("com.tterrag.registrate:Registrate:${p("registrate_version")}")
	modImplementation("me.shedaniel.cloth:cloth-config-${p("loader")}:${p("cloth_config_version")}")
	modImplementation("mezz.jei:jei-${p("minecraft_version")}-${p("loader")}:${p("jei_version")}")
	modImplementation("maven.modrinth:jade:${p("jade_version")}")
	annotationProcessor("org.spongepowered:mixin:${p("mixin_version")}:processor")
	compileOnly("io.github.llamalad7:mixinextras-common:${p("mixin_extras_version")}")
	implementation("io.github.llamalad7:mixinextras-${p("loader")}:${p("mixin_extras_version")}")
	compileOnly("org.jetbrains:annotations:${p("annotations_version")}")
}
publishMods {
	file.set(tasks.named("reobfJar").get().outputs.files.singleFile)
	changelog.set(file("CHANGELOG.md").readText())
	type.set(STABLE)
	version.set(project.version.toString())
	displayName.set("[${p("upper_loader")}] ${p("mod_name")} ${p("mod_version")}+${p("minecraft_version")}")
	modLoaders.addAll(p("upper_loader"), p("other_loader"))
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
